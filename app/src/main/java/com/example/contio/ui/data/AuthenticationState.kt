package com.example.contio.ui.data

import androidx.annotation.StringRes
import com.example.contio.R

data class AuthenticationState(
    val authenticationMode: AuthenticationMode = AuthenticationMode.SIGN_IN,
    val userName: String ? = null,
    val password: String ? = null,
    val passwordRequirements: List<PasswordRequirements> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    fun isFormValid() : Boolean {
        return password?.isNotEmpty() == true && userName?.isNotEmpty() == true &&
                (authenticationMode == AuthenticationMode.SIGN_IN ||
                 passwordRequirements.containsAll(PasswordRequirements.values().toList()))
    }
}

enum class PasswordRequirements(@StringRes val label: Int) {
    CAPITAL_LETTER(R.string.password_requirement_capital),
    NUMBER(R.string.password_requirement_digit),
    EIGHT_CHARACTERS(R.string.password_requirement_characters)
}

enum class AuthenticationMode {
    SIGN_IN, SIGN_UP
}