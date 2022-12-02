package com.example.contio.ui.data

sealed class AuthenticationEvent {
    object ToggleAuthenticationMode: AuthenticationEvent()

    class OnUserNameChanged(val name: String): AuthenticationEvent()
    class OnPasswordChanged(val password: String): AuthenticationEvent()

    object Authenticate : AuthenticationEvent()
    object ErrorDismissed : AuthenticationEvent()

    // sign in event
    object SignInUser : AuthenticationEvent()
    object SignUpUser : AuthenticationEvent()
    object SuccessSignInUser : AuthenticationEvent()
    class FailureSignInUser(val signInError: String) : AuthenticationEvent()
    object SuccessSignUpUser : AuthenticationEvent()
    class FailureSignUpUser(val signUpError: String) : AuthenticationEvent()
}