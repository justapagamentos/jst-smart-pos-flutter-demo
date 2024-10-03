package com.example.smartpos.dto

// Classe que representa um erro retornado pela SDK.
data class SdkError(
    // Código do erro.
    val code: String?,
    // Descrição do erro.
    val description: String?,
    // Mensagem adicional.
    val message: String?
)
