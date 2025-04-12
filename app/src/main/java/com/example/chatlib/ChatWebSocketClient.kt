package com.example.chatlib

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class ChatWebSocketClient(
    serverUri: URI,
    private val messageListener: MessageListener
) : WebSocketClient(serverUri) {

    interface MessageListener {
        fun onMessageReceived(message: String)
        fun onConnectionEstablished()
        fun onConnectionClosed()
        fun onError(error: Exception)
    }

    override fun onOpen(handshakedata: ServerHandshake?) {
        messageListener.onConnectionEstablished()
    }

    override fun onMessage(message: String?) {
        message?.let {
            // Check for special message format "203 = 0xcb"
            if (it == "203 = 0xcb") {
                messageListener.onMessageReceived("This is a predefined message for special code")
            } else {
                messageListener.onMessageReceived(it)
            }
        }
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        messageListener.onConnectionClosed()
    }

    override fun onError(ex: Exception?) {
        ex?.let { messageListener.onError(it) }
    }
}