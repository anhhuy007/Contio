package com.example.contio.viewmodels

import androidx.lifecycle.ViewModel
import com.example.contio.ui.data.UserDetailsState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class UserDetailsViewModel @Inject constructor(
    val chatClient: ChatClient
)  : ViewModel() {
    val userDetailUiState = MutableStateFlow(UserDetailsState())

    fun getCurrentUser(){
        userDetailUiState.value = userDetailUiState.value.copy(
            user = chatClient.getCurrentUser() ?: User(id = "local", name = "local")
        )
    }

}