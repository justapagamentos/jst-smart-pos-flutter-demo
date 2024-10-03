package com.example.smartpos.dto

data class SdkTerminalData(
    val contactlessSupported: Boolean?,
    val manufacturerName: String?,
    val manufacturerVersion: String?,
    val model: String?,
    val osVersion: String?,
    val serialNumber: String?,
    val specVersion: String?
)
