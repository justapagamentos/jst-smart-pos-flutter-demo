package com.example.smartpos.dto

// Classe que representa o resultado de uma transação de pagamento.
data class SdkPaymentResult(
    // Operação realizada.
    val operation: String = "Payment",
    // Aprovado?
    val isApproved: Boolean,
    // Cancelado?
    val isCanceled: Boolean,
    // AID.
    val aid: String?,
    // Valor da autorização.
    val amount: Long?,
    // Adquirente.
    val acquirer: String?,
    // Código de autorização.
    val authorizationCode: String?,
    // Código de resposta da autorização.
    val authorizationResponseCode: String?,
    // Bandeira.
    val brand: String?,
    // Data e hora do cancelamento.
    val cancelDateTime: String?,
    // Data e hora da captura.
    val captureDateTime: String?,
    // Número do cartão.
    val cardNumber: String?,
    // Nome do portador do cartão.
    val cardholderName: String?,
    // Comprovante do portador.
    val cardholderReceipt: List<String>?,
    // Código de resposta da captura.
    val chargeStatus: String?,
    // Data e hora da criação.
    val creationDateTime: String?,
    // Moeda.
    val currency: Int?,
    // Parcelas.
    val installmentNumber: Int?,
    // Tipo de parcelamento.
    val installmentType: String?,
    // Id interno da venda do lojista.
    val merchantChargeId: String?,
    // Comprovante do lojista.
    val merchantReceipt: List<String>?,
    // NSU.
    val nsu: String?,
    // Origem.
    val origin: String?,
    // Tipo de pagamento.
    val paymentType: String?,
    // Id da venda.
    val transactionId: String?,
    // Erros de validação.
    val errors: List<SdkError>? = emptyList()
)