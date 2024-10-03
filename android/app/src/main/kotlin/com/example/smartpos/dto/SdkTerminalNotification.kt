package com.example.smartpos.dto

data class SdkTerminalNotification(
    val operation: String = "Notification",
    val message: String,
    val status: String?,
    val abecsCommand: String?
)
