package com.example.smartpos.dto

// Classe que representa os dados de inicialização do terminal.
data class SdkTerminalInit(
    // Operação realizada.
    val operation: String = "Init",
    // Inicializado?
    val initialized: Boolean?,
    // Dados do terminal.
    val terminalInfo: SdkTerminalData?,
    // Bandeiras disponíveis.
    val availableBrands: List<String>? = emptyList(),
    // Erros de validação.
    val errors: List<SdkError>? = emptyList()
)