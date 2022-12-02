package com.example.contio.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.contio.viewmodels.ChannelListViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.ui.channels.ChannelsScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import kotlin.coroutines.CoroutineContext

@AndroidEntryPoint
class ChannelListActivity : ComponentActivity() {

    private val viewModel: ChannelListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscribeToEvents()

        val userId = intent.getStringExtra(KEY_USER_ID) ?: ""

        setContent {
            ChatTheme {
                var showDialog by remember { mutableStateOf(false) }

                if (showDialog) {
                    CreateChannelDialog(
                        dismiss = { channelName ->
                            viewModel.createChannel(channelName)
                            showDialog = false
                        }
                    )
                }

                ChannelsScreen(
                    filters = Filters.`in`(
                        fieldName = "type",
                        values = listOf("gaming", "messaging", "commerce", "team", "livestream")
                    ),
                    title = "Channel List",
                    isShowingSearch = true,
                    onItemClick = { channel ->
                        startActivity(MessageActivity.getIntent(this, channelId = channel.cid))
                    },
                    onBackPressed = { finish() },
                    onHeaderActionClick = {
                        showDialog = true
                    },
                    onHeaderAvatarClick = {
                        Log.d("AnhHuy", "Go to user details activity")
                        startActivity(UserDetailsActivity.getUserIdIntent(this, userId = userId))
                    }
                )
            }
        }
    }

    @Composable
    private fun CreateChannelDialog(dismiss: (String) -> Unit) {
        var channelName by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { dismiss(channelName) },
            title = {
                Text(text = "Enter channel name")
            },
            text = {
                TextField(value = channelName, onValueChange = { channelName = it })
            },
            buttons = {
                Row(
                    modifier = Modifier
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { dismiss(channelName) }
                    ) {
                        Text(text = "Create channel")
                    }
                }
            }
        )
    }

    private fun subscribeToEvents() {
        lifecycleScope.launchWhenStarted {
            viewModel.createChannelEvent.collect() { event ->
                when (event) {
                    is ChannelListViewModel.CreateChannelEvent.Success -> {
                        showMessage("Channel created successful")
                    }

                    is ChannelListViewModel.CreateChannelEvent.Error -> {
                        val error = event.error
                        showMessage("Error: $error")
                    }
                }
            }
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val KEY_USER_ID = "userId"

        fun getUserIdIntent(context: Context, userId: String?) : Intent {
            return Intent(context, ChannelListActivity::class.java).apply {
                putExtra(KEY_USER_ID, userId)
            }
        }
    }
}