package com.example.kotlinchatapp.model

import com.google.firebase.Timestamp

data class ChatMessageModel(
    var message: String = "",
    var senderId: String = "",
    var timestamp: Timestamp = Timestamp.now()
)
