package com.example.smartpos.dto

// Classe que representa uma notificação do terminal.
data class SdkTerminalNotification(
    // Operação realizada.
    val operation: String = "Notification",
    // Mensagem.
    val message: String,
    // Status.
    val status: String?,
    // Comandos padrão ABECS.
    val abecsCommand: String?
)
