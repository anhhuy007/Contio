package com.example.contio.activities

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.contio.R
import com.example.contio.ui.data.AuthenticationEvent
import com.example.contio.ui.data.AuthenticationMode
import com.example.contio.ui.data.AuthenticationState
import com.example.contio.ui.data.PasswordRequirements
import com.example.contio.viewmodels.AuthenticationViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import kotlinx.coroutines.CoroutineScope

@AndroidEntryPoint
class AuthenticationActivity : ComponentActivity() {

    private val authenViewModel: AuthenticationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subcribeToEvents()

        setContent {
            ChatTheme {
                Authentication()
            }
        }
    }

    private fun subcribeToEvents() {
        lifecycleScope.launchWhenStarted {
            authenViewModel.authenEvent.collect {
                event -> when (event) {
                    is AuthenticationEvent.SuccessSignInUser -> {
                        showToast("Login successful")
                        startActivity(ChannelListActivity.getUserIdIntent(this@AuthenticationActivity, authenViewModel.authenUiState.value.userName))
                    }

                    is AuthenticationEvent.FailureSignInUser -> {
                        val errorMessage = event.signInError
                        showToast(errorMessage)
                    }

                    is AuthenticationEvent.SuccessSignUpUser -> {
                        showToast("Successfully sign up user")
                    }

                    is AuthenticationEvent.FailureSignUpUser -> {
                        val errorMessage = event.signUpError
                        showToast(errorMessage)
                    }
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    @Preview(showBackground = true)
    @Composable
    fun Authentication() {

        MaterialTheme {
            AuthenticationContent(
                modifier = Modifier.fillMaxSize(),
                authenticationState = authenViewModel.authenUiState.collectAsState().value,
                handleEvent = authenViewModel::handleEvent
            )
        }
    }

    @Composable
    fun AuthenticationContent(
        modifier : Modifier = Modifier,
        authenticationState: AuthenticationState,
        handleEvent: (event: AuthenticationEvent) -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.surface)
        ) {
            HeaderElement(
                authenticationMode = authenticationState.authenticationMode
            )
            UserTextField(
                userName = authenticationState.userName,
                password = authenticationState.password,
                onUserNameChanged = { name -> handleEvent(AuthenticationEvent.OnUserNameChanged(name)) },
                onPasswordChanged = { password -> handleEvent(AuthenticationEvent.OnPasswordChanged(password)) },
                onAuthenticate = { handleEvent(AuthenticationEvent.Authenticate) }
            )

            AnimatedVisibility(
                visible = authenticationState.authenticationMode == AuthenticationMode.SIGN_UP
            ) {
                ShowPasswordRequirements(satisfiedRequirements = authenticationState.passwordRequirements)
            }

            OtherLoginMethod()

            ToggleText(
                authenticationMode = authenticationState.authenticationMode,
                toggleAuthentication = { handleEvent(AuthenticationEvent.ToggleAuthenticationMode) }
            )

            LoginButton(
                authenticationMode = authenticationState.authenticationMode,
                enableAuthentication = authenticationState.isFormValid(),
                onOkAction = { if (authenticationState.authenticationMode == AuthenticationMode.SIGN_IN) authenViewModel.signInUser()
                                else authenViewModel.signUpUser()
                             },
                onCancel = { onBackPressed() },
                onAuthenticate = { handleEvent(AuthenticationEvent.Authenticate) }
            )

            Spacer(Modifier.height(10.dp))

            if (authenticationState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(50.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }

    @Composable
    private fun LoginButton(
        authenticationMode: AuthenticationMode,
        enableAuthentication: Boolean = false,
        onOkAction: () -> Unit,
        onCancel: () -> Unit,
        onAuthenticate: () -> Unit
    ) {
        val buttonLabel = if (authenticationMode == AuthenticationMode.SIGN_IN) R.string.signin
        else R.string.signup

        Spacer(Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.Center)
            ) {
                Button(onClick = { onCancel() }) {
                    Text(text = stringResource(R.string.cancel))
                }

                Spacer(Modifier.width(60.dp))

                Button(onClick = { onOkAction() } ) {
                    Text(text = stringResource(buttonLabel))
                }
            }
        }
    }

    @Composable
    fun ToggleText(
        authenticationMode: AuthenticationMode,
        toggleAuthentication: () -> Unit
    ) {
        Spacer(Modifier.height(10.dp))

        val textRes = if (authenticationMode == AuthenticationMode.SIGN_UP) R.string.action_already_have_account
        else R.string.dont_have_account

        val actionText = if (authenticationMode == AuthenticationMode.SIGN_UP) R.string.sign_in_action
        else R.string.create_here

        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clickable {
                        toggleAuthentication()
                        Log.d("AnhHuy", "Create new account!")
                    }
            ) {
                Text(
                    text = stringResource(id = textRes),
                )

                Spacer(Modifier.width(3.dp))

                Text(
                    text = stringResource(id = actionText),
                    fontWeight = FontWeight.Black,
                    textDecoration = TextDecoration.Underline,
                    color = Color.Blue
                )
            }
        }
    }

    @Composable
    private fun UserTextField(
        userName: String?,
        password: String?,
        onUserNameChanged: (name: String) -> Unit,
        onPasswordChanged: (password: String) -> Unit,
        onAuthenticate: () -> Unit
    ) {
        var hiddenPassword by remember { mutableStateOf(true) }

        val visual = if (hiddenPassword) PasswordVisualTransformation()
        else VisualTransformation.None

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(Modifier.height(30.dp))

            OutlinedTextField(
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.user),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = { Text(text = stringResource(R.string.enter_user_name)) },
                singleLine = true,
                value = userName ?: "",
                onValueChange = { name -> onUserNameChanged(name) },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next,
                    keyboardType = KeyboardType.Email
                ),
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.CenterHorizontally)
                    .widthIn(300.dp, 300.dp)
            )

            OutlinedTextField(
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.padlock),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    Icon(
                        painter = painterResource(id =
                        if (hiddenPassword) R.drawable.hide else R.drawable.show
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .clickable {
                                hiddenPassword = !hiddenPassword
                            }
                            .size(20.dp)
                    )
                },
                label = { Text(text = stringResource(R.string.enter_password)) },
                singleLine = true,
                value = password ?: "",
                onValueChange = { password -> onPasswordChanged(password) },
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.CenterHorizontally)
                    .widthIn(300.dp, 300.dp),
                visualTransformation = visual,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        onAuthenticate()
                    }
                )
            )
        }
    }

    @Composable
    private fun ShowPasswordRequirements(
        modifier : Modifier = Modifier,
        satisfiedRequirements: List<PasswordRequirements>
    ) {

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
            ) {
                PasswordRequirements.values().forEach { requirement ->
                    RequirementElement(
                        message = stringResource(id = requirement.label),
                        satisfied = satisfiedRequirements.contains(
                            requirement
                        )
                    )
                }
            }
        }
    }

    @Composable
    private fun RequirementElement(
        modifier : Modifier = Modifier,
        message: String,
        satisfied: Boolean
    ) {
        if (satisfied) return;

        val tint = Color.Red
        val requirementStatus = stringResource(id = R.string.password_requirement_satisfied, message)

        Row(
            modifier = Modifier
                .padding(6.dp)
                .semantics(mergeDescendants = true) {
                    text = AnnotatedString(requirementStatus)
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(15.dp),
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = tint
            )

            Spacer(Modifier.width(8.dp))

            Text(
                modifier = Modifier.clearAndSetSemantics {  },
                text = message,
                fontSize = 12.sp,
                color = tint
            )
        }
    }

    @Composable
    private fun HeaderElement(
        modifier : Modifier = Modifier,
        authenticationMode: AuthenticationMode
    ) {

        Column {
            Spacer(Modifier.height(20.dp))

            Text(
                text = stringResource(id =
                if (authenticationMode == AuthenticationMode.SIGN_IN) {
                    R.string.sign_in
                } else {
                    R.string.sign_up
                }
                ),
                fontSize = 35.sp,
                color = Color.Black,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.h1,
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(20.dp))

            Image(
                painter = painterResource(R.drawable.contio),
                contentDescription = null,
                modifier = Modifier
                    .size(74.dp)
                    .padding(10.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(CircleShape)
                    .border(0.5.dp, Color.Black, CircleShape),
                contentScale = ContentScale.Crop
            )

            Text(
                text = stringResource(R.string.welcome),
                fontSize = 30.sp,
                style = MaterialTheme.typography.h1,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )

            Text(
                text = stringResource(R.string.app_description),
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                textAlign = TextAlign.Center
            )
        }
    }

    @Composable
    private fun OtherLoginMethod() {

        Spacer (Modifier.height(10.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Image(
                    painter = painterResource(R.mipmap.facebook),
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .padding(10.dp),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.width(10.dp))

                Image(
                    painter = painterResource(R.mipmap.gmail),
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .padding(10.dp),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.width(10.dp))

                Image(
                    painter = painterResource(R.mipmap.github),
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .padding(10.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Text(
                text = stringResource(R.string.other_methods),
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }
    }
}