package com.example.chatlib

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatlib.databinding.ActivityChatBinding
import java.net.URI

class ChatActivity : AppCompatActivity(), ChatWebSocketClient.MessageListener {

    private lateinit var binding: ActivityChatBinding
    private lateinit var adapter: MessageAdapter
    private var webSocketClient: ChatWebSocketClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupWebSocket()
        setupSendButton()
    }

    private fun setupRecyclerView() {
        adapter = MessageAdapter()
        binding.messagesRecyclerView.adapter = adapter
        binding.messagesRecyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
    }

    private fun setupWebSocket() {
        try {
            val serverUri = URI("wss://ws.ifelse.io/")
            webSocketClient = ChatWebSocketClient(serverUri, this)
            webSocketClient?.connect()
        } catch (e: Exception) {
            Toast.makeText(this, "WebSocket connection error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }


    private fun setupSendButton() {
        binding.sendButton.isEnabled = false
        binding.sendButton.setOnClickListener {
            val message = binding.messageInput.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
                binding.messageInput.text.clear()
            }
        }
    }

    private fun sendMessage(message: String) {
        // Add user message to the chat
        adapter.addMessage(Message(message, true))
        scrollToBottom()

        // Send message via WebSocket only if connected
        if (webSocketClient?.isOpen == true) {
            webSocketClient?.send(message)
        } else {
            Toast.makeText(this, "Not connected to chat server", Toast.LENGTH_SHORT).show()
        }
    }

    private fun scrollToBottom() {
        binding.messagesRecyclerView.post {
            binding.messagesRecyclerView.smoothScrollToPosition(adapter.itemCount - 1)
        }
    }

    override fun onMessageReceived(message: String) {
        runOnUiThread {
            adapter.addMessage(Message(message, false))
            scrollToBottom()
        }
    }

    override fun onConnectionEstablished() {
        runOnUiThread {
            Toast.makeText(this, "Connected to chat server", Toast.LENGTH_SHORT).show()
            binding.sendButton.isEnabled = true // Enable button once connected
        }
    }

    override fun onConnectionClosed() {
        runOnUiThread {
            Toast.makeText(this, "Disconnected from chat server", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onError(error: Exception) {
        runOnUiThread {
            Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        webSocketClient?.close()
        super.onDestroy()
    }
}