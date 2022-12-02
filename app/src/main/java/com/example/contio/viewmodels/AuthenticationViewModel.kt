package com.example.contio.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.contio.module.AppModule
import com.example.contio.ui.data.AuthenticationEvent
import com.example.contio.ui.data.AuthenticationMode
import com.example.contio.ui.data.AuthenticationState
import com.example.contio.ui.data.PasswordRequirements
import dagger.hilt.android.lifecycle.HiltViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    val chatClient: ChatClient
) : ViewModel() {
    // ui state hodling a reference to our authentication state
    val authenUiState = MutableStateFlow(AuthenticationState())

    private val _authenEvent = MutableSharedFlow<AuthenticationEvent>()
    val authenEvent = _authenEvent.asSharedFlow()

    fun signInUser() {
        viewModelScope.launch {
            if (satisfied())
            {
                // sign in user
                authenUiState.value = authenUiState.value.copy(isLoading = true)
                val user: User = User(id = authenUiState.value.userName!!, name = authenUiState.value.userName!!)

                chatClient.connectUser(
                    user = user,
                    token = AppModule.token
                ).enqueue() { result ->
                    authenUiState.value = authenUiState.value.copy(isLoading = false)
                    if (result.isSuccess) {
                        viewModelScope.launch {
                            _authenEvent.emit(AuthenticationEvent.SuccessSignInUser)
                        }
                    } else {
                        viewModelScope.launch {
                            val error = result.error().message ?: "Unknown error"
                            _authenEvent.emit(AuthenticationEvent.FailureSignInUser(signInError = error))
                        }
                    }
                }

            } else {
                _authenEvent.emit(AuthenticationEvent.FailureSignInUser(signInError = "Invalid user name or password, please try again!"))
            }
        }
    }

    fun signUpUser() {
        viewModelScope.launch {
            if (satisfied())
            {
                // sign in user
                authenUiState.value = authenUiState.value.copy(isLoading = true)
                val user = User(id = authenUiState.value.userName!!, name = authenUiState.value.userName!!)
                chatClient.connectUser(
                    user = user,
                    token = AppModule.token
                ).enqueue() { result ->
                    authenUiState.value = authenUiState.value.copy(isLoading = false)
                    if (result.isSuccess) {
                        viewModelScope.launch {
                            _authenEvent.emit(AuthenticationEvent.SuccessSignUpUser)
                        }
                    } else {
                        viewModelScope.launch {
                            val error = result.error().message ?: "Unknown error"
                            _authenEvent.emit(AuthenticationEvent.FailureSignUpUser(signUpError = error))
                        }
                    }
                }

            } else {
                _authenEvent.emit(AuthenticationEvent.FailureSignUpUser(signUpError = "Invalid user name or password, please try again!"))
            }
        }
    }

    private fun satisfied() : Boolean {
        return (authenUiState.value.passwordRequirements.containsAll(listOf(
            PasswordRequirements.NUMBER,
            PasswordRequirements.CAPITAL_LETTER,
            PasswordRequirements.EIGHT_CHARACTERS)) && authenUiState.value.userName != null)
    }

    private fun toggleAuthenticationMode() {
        val authenticationMode = authenUiState.value.authenticationMode
        val newAuthenticationMode = if (
                authenticationMode == AuthenticationMode.SIGN_IN
            ) {
                AuthenticationMode.SIGN_UP
            } else {
                AuthenticationMode.SIGN_IN
            }
        authenUiState.value = authenUiState.value.copy(
            authenticationMode = newAuthenticationMode
        )
    }

    private fun updateUserName(username: String) {
        authenUiState.value = authenUiState.value.copy(
            userName = username
        )
    }

    private fun updatePassword(password: String) {
        val requirements = mutableListOf<PasswordRequirements>()

        if (password.length > 7) {
            requirements.add(PasswordRequirements.EIGHT_CHARACTERS)
        }

        if (password.any { it.isUpperCase() }) {
            requirements.add(PasswordRequirements.CAPITAL_LETTER)
        }

        if (password.any { it.isDigit() }) {
            requirements.add(PasswordRequirements.NUMBER)
        }

        authenUiState.value = authenUiState.value.copy(
            password = password,
            passwordRequirements = requirements.toList()
        )
    }

    private fun authenticate() {
        authenUiState.value = authenUiState.value.copy(
            isLoading = true
        )

        viewModelScope.launch(Dispatchers.IO) {
            delay(2000L)

            withContext(Dispatchers.Main) {
                authenUiState.value = authenUiState.value.copy(
                    isLoading = false,
                    error = "Something went wrong!"
                )
            }
        }
    }

    private fun dismissError() {
        authenUiState.value = authenUiState.value.copy(
            error = null
        )
    }

    fun handleEvent(event: AuthenticationEvent) {
        when (event) {
            is AuthenticationEvent.ToggleAuthenticationMode -> {
                toggleAuthenticationMode()
                Log.d("AnhHuy", "Toggle event")
            }

            is AuthenticationEvent.OnUserNameChanged -> {
                updateUserName(event.name)
            }

            is AuthenticationEvent.OnPasswordChanged -> {
                updatePassword(event.password)
            }

            is AuthenticationEvent.Authenticate -> {
                authenticate()
            }

            is AuthenticationEvent.ErrorDismissed -> {
                dismissError()
            }

            is AuthenticationEvent.SignInUser -> {
                signInUser()
            }
        }
    }
}