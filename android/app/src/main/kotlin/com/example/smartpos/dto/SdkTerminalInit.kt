package com.example.smartpos.dto

data class SdkTerminalInit(
    val operation: String = "Init",
    val initialized: Boolean?,
    val terminalInfo: SdkTerminalData?,
    val availableBrands: List<String>? = emptyList(),
    val errors: List<SdkError>? = emptyList()
)