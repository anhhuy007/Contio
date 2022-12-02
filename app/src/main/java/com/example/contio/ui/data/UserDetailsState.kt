package com.example.contio.ui.data

import io.getstream.chat.android.client.models.User

data class UserDetailsState(
    val user: User = User(id = "local", name = "local"),
    val isLoading: Boolean = false,
    val error: String = ""
)