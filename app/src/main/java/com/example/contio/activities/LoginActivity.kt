package com.example.contio.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.contio.R
import com.example.contio.ui.theme.ContioTheme
import com.example.contio.viewmodels.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : ComponentActivity() {
    private val viewModel : LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribeToEvent()

        setContent {
            ContioTheme {
                LoginScreen()
            }
        }
    }

    // catch login event returned by MutableLiveData.emit
    private fun subscribeToEvent() {
        lifecycleScope.launchWhenStarted {
            viewModel.loginEvent.collect {
                event -> when (event) {
                    is LoginViewModel.LogInEvent.ErrorInputTooShort -> {
                        showToast(getString(R.string.invalid_user_name))
                    }
                    is LoginViewModel.LogInEvent.ErrorLogin -> {
                        val errorMessage = event.error
                        showToast("Error: $errorMessage")
                    }
                    is LoginViewModel.LogInEvent.Success -> {
                        showToast("Login successful")
                        startActivity(Intent(this@LoginActivity, ChannelListActivity::class.java))
                        finish()
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
    private fun LoginScreen()  {

        var userName by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var isShowingProgress by remember { mutableStateOf(false) }

        viewModel.loadingState.observe(this, Observer { uiLoadingState ->
            isShowingProgress = when (uiLoadingState) {
                is LoginViewModel.UiLoadingState.Loading -> { true }
                is LoginViewModel.UiLoadingState.NotLoading -> { false }
            }
        })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.surface)
        ) {

            HeaderElement()
            UserTextField(
                userName = userName,
                password = password,
                onUserNameChanged = { userName = it },
                onPasswordChanged = { password = it }
            )
            OtherLoginMethod()
            LoginButton(
                userName = userName,
                onLoginUser = { viewModel.LoginUser(
                    username = userName,
                ) },
                onCancel = {  }
            )

            Spacer(Modifier.height(10.dp))

            if (isShowingProgress) {
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
        userName: String,
        onLoginUser: () -> Unit,
        onCancel: () -> Unit
    ) {
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
                Button(onClick = { onCancel }) {
                    Text(text = stringResource(R.string.cancel))
                }

                Spacer(Modifier.width(60.dp))

                Button(onClick = {
                    viewModel.LoginUser(
                    username = userName, token = getString(R.string.jwt_token))

                    Log.d("AnhHuy", "Login")

                }) {
                    Text(text = stringResource(R.string.signup))
                }
            }
        }
    }

    @Composable
    private fun UserTextField(
        userName: String,
        password: String,
        onUserNameChanged: (String) -> Unit,
        onPasswordChanged: (String) -> Unit
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(Modifier.height(30.dp))

            OutlinedTextField(
                label = { Text(text = stringResource(R.string.enter_user_name)) },
                value = userName,
                onValueChange = onUserNameChanged,
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.CenterHorizontally)
            )

            OutlinedTextField(
                label = { Text(text = stringResource(R.string.enter_password))},
                value = password,
                onValueChange = onPasswordChanged,
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.CenterHorizontally),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
        }
    }

    @Composable
    private fun HeaderElement() {

        Column {
            Spacer(modifier = Modifier.height(70.dp))

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

