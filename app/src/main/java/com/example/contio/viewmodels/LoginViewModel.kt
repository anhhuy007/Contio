package com.example.contio.viewmodels

import android.provider.SyncStateContract.Constants
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

const val MIN_USERNAME_LENGTH = 3

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val client: ChatClient
) : ViewModel() {
    private val _loginEvent = MutableSharedFlow<LogInEvent>()
    val loginEvent = _loginEvent.asSharedFlow()

    private val _loadingState = MutableLiveData<UiLoadingState>()
    val loadingState : LiveData<UiLoadingState>
        get() = _loadingState

    private fun isValidUserName(username: String): Boolean {
        return username.length > MIN_USERNAME_LENGTH
    }

    fun LoginUser(username: String, token: String? = null) {
        val trimUsername = username.trim()
        viewModelScope.launch {
            if (isValidUserName(trimUsername) && token != null) {
                // login this user
                loginRegisteredUser(userName = trimUsername, token = token)
            } else {
                _loginEvent.emit(LogInEvent.ErrorInputTooShort)
            }
        }
    }

    private fun loginRegisteredUser(userName: String, token: String) {
        val user = User(id = userName, name = userName)

        _loadingState.value = UiLoadingState.Loading

        client.connectUser(
            user = user,
            token = token
        ).enqueue() {
            result -> _loadingState.value = UiLoadingState.NotLoading

            if (result.isSuccess) {
                viewModelScope.launch {
                    _loginEvent.emit(LogInEvent.Success)
                }
            } else {
                viewModelScope.launch {
                    _loginEvent.emit(LogInEvent.ErrorLogin(
                        result.error().message ?: "Unknown Error"
                    ))
                }
            }
        }
    }

    sealed class LogInEvent {
        object ErrorInputTooShort : LogInEvent()
        data class ErrorLogin(val error: String) : LogInEvent()
        object Success : LogInEvent()
    }

    sealed class UiLoadingState {
        object Loading: UiLoadingState()
        object NotLoading: UiLoadingState()
    }
}