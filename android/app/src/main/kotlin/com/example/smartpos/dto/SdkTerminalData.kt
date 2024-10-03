package com.example.smartpos.dto

// Classe que representa os dados do terminal.
data class SdkTerminalData(
    // Contactless?
    val contactlessSupported: Boolean?,
    // Nome do fabricante.
    val manufacturerName: String?,
    // Versão do fabricante.
    val manufacturerVersion: String?,
    // Modelo.
    val model: String?,
    // Versão do sistema operacional.
    val osVersion: String?,
    // Número de série.
    val serialNumber: String?,
    // Versão da especificação.
    val specVersion: String?
)
