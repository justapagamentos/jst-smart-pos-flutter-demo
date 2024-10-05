package com.example.smartpos.dto

// Classe que representa o resultado da impressão.
data class SdkPrintResult(
    // Operação realizada.
    val operation: String = "Print",
    // Status da impressão.
    val status: String?,
)
