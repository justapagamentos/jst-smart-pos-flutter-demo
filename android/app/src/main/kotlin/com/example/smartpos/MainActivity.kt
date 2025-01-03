package com.example.smartpos

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import br.com.aditum.IAditumSdkService
import br.com.aditum.data.v2.enums.AbecsCommands
import br.com.aditum.data.v2.enums.Acquirer
import br.com.aditum.data.v2.enums.InstallmentType
import br.com.aditum.data.v2.enums.PayOperationType
import br.com.aditum.data.v2.enums.PaymentType
import br.com.aditum.data.v2.enums.PrintStatus
import br.com.aditum.data.v2.enums.TransactionStatus
import br.com.aditum.data.v2.model.Charge
import br.com.aditum.data.v2.model.PinpadMessages
import br.com.aditum.data.v2.model.init.InitRequest
import br.com.aditum.data.v2.model.init.InitResponse
import br.com.aditum.data.v2.model.init.InitResponseCallback
import br.com.aditum.data.v2.model.payment.PaymentRequest
import br.com.aditum.data.v2.model.payment.PaymentResponse
import br.com.aditum.data.v2.model.payment.PaymentResponseCallback
import br.com.aditum.data.v2.model.transactions.ConfirmTransactionCallback
import br.com.aditum.device.callbacks.IPrintStatusCallback
import com.example.smartpos.dto.SdkPaymentResult
import com.example.smartpos.dto.SdkPrintResult
import com.example.smartpos.dto.SdkTerminalData
import com.example.smartpos.dto.SdkTerminalInit
import com.example.smartpos.dto.SdkTerminalNotification
import com.google.gson.Gson
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import java.io.ByteArrayOutputStream
import java.util.UUID
import kotlin.concurrent.thread

// Activity de exemplo para integração com o SDK da Aditum utilizando Flutter.
class MainActivity : FlutterActivity() {

    // Nome do canal de comunicação entre Flutter e Android.
    private val channelName = "br.com.justa.smartpos/aditum"

    // EventSink para enviar eventos do Nativo para o Flutter.
    private var eventSink: EventChannel.EventSink? = null

    // Gson para serialização e deserialização de objetos (JSON).
    private val gson: Gson = Gson()

    // Listener para conexão com o serviço da Aditum.
    private val mServiceConnected = object : JustaApplication.OnServiceConnectionListener {
        override fun onServiceConnection(serviceConnected: Boolean) {
            Log.d(TAG, "onServiceConnection - serviceConnected: $serviceConnected")
            if (serviceConnected) {
                val app = app()
                app.communicationService?.registerPaymentCallback(mPaymentCallback)
            }
        }
    }

    // Método para obter a instância da aplicação.
    private fun app(): JustaApplication = application as JustaApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = app()
        app.serviceConnectionListener = mServiceConnected
        app.initSdk()
    }

    override fun onResume() {
        super.onResume()
        val app = app()
        app.communicationService?.let { communicationService: IAditumSdkService ->
            communicationService.registerPaymentCallback(mPaymentCallback)
        }
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        flutterEngine.dartExecutor.binaryMessenger.let {
            val channel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, channelName)
            channel.setMethodCallHandler { call, result ->
                when (call.method) {
                    "init" -> init(call, result)
                    "pay" -> pay(call, result)
                    "print" -> print(call, result)
                    else -> result.notImplemented()
                }
            }

            EventChannel(it, "$channelName/events").setStreamHandler(
                object : EventChannel.StreamHandler {
                    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
                        eventSink = events
                    }

                    override fun onCancel(arguments: Any?) {
                        eventSink = null
                    }
                }
            )
        }
    }

    // Inicializa o SDK da Aditum com os dados do estabelecimento informado.
    private fun init(call: MethodCall, result: MethodChannel.Result, retry: Boolean = false) {
        Log.d(TAG, "init / started")

        // Código de ativação do estabelecimento.
        // Caso não seja informado, usaremos o código de teste da Aditum.
        val merchantActivationCode = call.argument("merchantActivationCode") ?: "880776044"

        // Token da aplicação fornecido pela Justa.
        // Caso não seja informado, usaremos o token de teste da Justa.
        val appToken = call.argument("applicationToken") ?: "mk_Lfq9yMzRoYaHjowfxLvoyi"

        thread {
            val app = app()
            app.communicationService?.let { communicationService: IAditumSdkService ->
                // Exibe o nome da aplicação no display do Smart POS.
                val pinpadMessages = PinpadMessages()
                pinpadMessages.mainMessage = "Justa"

                val initRequest = InitRequest()
                initRequest.pinpadMessages = pinpadMessages
                initRequest.applicationToken = appToken                 // Token da aplicação.
                initRequest.applicationName = "JustaApplication"        // Nome da aplicação.
                initRequest.applicationVersion = "1.0.0"                // Versão da aplicação.
                initRequest.activationCode = merchantActivationCode     // Código de ativação.
                initRequest.useOnlySdk = false                          // Usar modo transparente?

                communicationService.init(initRequest, mInitResponseCallback)
                result.success("Inicialização iniciada de forma assíncrona.")
            } ?: run {
                if (retry) {
                    result.error("JST-001", "Comunicação com o serviço não está disponível.", null)
                    return@run
                }

                // Caso tenha falhado qualquer tipo de conexào com o serviço da Aditum, tentaremos
                // novamente inicializar o Sdk para evita qualquer problema de processamento.
                app.initSdk()

                // Note que, como esse é um cenário alternativo, você precisará implementar alguma
                // lógica para continuar o processo de onde parou. No exemplo abaixo estamos apenas
                // chamando a função init() novamente, mas isso pode ficar parado em loop infinito.
                init(call, result, true)
            }
        }
        Log.d(TAG, "init / ended")
    }

    // Realiza o pagamento com os dados informados.
    private fun pay(
        call: MethodCall,
        result: MethodChannel.Result
    ) {
        Log.d(TAG, "pay / started")

        val amount: Int = call.argument<Int>("amount") ?: 500
        Log.d(TAG, "pay / amount: $amount")
        val installmentNumber = call.argument("installmentNumber") ?: 1
        Log.d(TAG, "pay / installmentNumber: $installmentNumber")

        var paymentType = PaymentType.Credit
        if (call.hasArgument("paymentType")) {
            paymentType = parsePaymentType(call.argument("paymentType") ?: 1)
        }
        Log.d(TAG, "pay / paymentType: $paymentType")

        var installmentType = InstallmentType.None
        if (call.hasArgument("installmentType")) {
            installmentType = parseInstallmentType(call.argument("installmentType") ?: 1)
        }
        Log.d(TAG, "pay / installmentType: $installmentType")

        var operationType = PayOperationType.Authorization
        if (call.hasArgument("operationType")) {
            operationType = parseOperationType(call.argument("operationType") ?: 1)
        }
        Log.d(TAG, "pay / operationType: $operationType")

        // O pagamento é feito através de um objeto PaymentRequest, que contém todas as informações
        // necessárias para realizar a transação financeira de débito, crédito, Pix, etc.
        val payment = PaymentRequest(
            acquirer = Acquirer.PagSeguro,
            paymentType = paymentType,
            operationType = operationType,
            amount = amount.toLong(),
            installmentNumber = installmentNumber,
            installmentType = installmentType,
            // region Identificadores.
            // Identificador da venda em seu sistema.
            merchantChargeId = UUID.randomUUID().toString(),
            // endregion
            currency = 986, // BRL
            isQrCode = paymentType == PaymentType.Pix,
            allowContactless = true,
            manualEntry = false,
        )

        val app = app()
        thread {
            app.communicationService?.let { communicationService: IAditumSdkService ->
                app.merchantData = communicationService.merchantData
                thread {
                    communicationService.pay(payment, mPayResponseCallback)
                    result.success("Pagamento iniciado de forma assíncrona.")
                }
            } ?: run {
                Log.e(
                    TAG,
                    "pay / Comunicação com o serviço da Aditum não disponível. Tentando reconectar."
                )
                result.error("JST-002", "Comunicação com o serviço não está disponível.", null)
            }
        }
        Log.d(TAG, "pay / ended")
    }

    // Realiza a impressão da imagem informada.
    private fun print(
        call: MethodCall,
        result: MethodChannel.Result
    ) {
        Log.d(TAG, "print / started")
        val imageByteArray: ByteArray = call.argument("imageByteArray") ?: ByteArray(0)
        val bitmap = getBitmapFromByteArray(imageByteArray)
        val app = app()
        app.communicationService?.let {
            thread {
                it.deviceSdk.printerSdk.print(bitmap, mPrintStatusCallback)
            }
        }
        result.success("Impressão iniciada de forma assíncrona.")
        Log.d(TAG, "print / ended")
    }

    // Função para confirmar o pagamento após sua autorização.
    private fun confirmPayment(charge: Charge) {
        Log.d(TAG, "confirmPayment / started")
        val app = app()
        app.communicationService?.let {
            thread {
                it.confirmTransaction(
                    charge.nsu,
                    object : ConfirmTransactionCallback.Stub() {

                        override fun onResponse(confirmed: Boolean) {
                            Log.d(TAG, "confirmPayment / confirmed: $confirmed")

                            // Pagamento não foi confirmado junto à adquirente.
                            if (!confirmed) {
                                Log.e(
                                    TAG,
                                    "confirmPayment / Payment ${charge.merchantChargeId} not confirmed."
                                )
                                eventSink?.error(
                                    "JST-010",
                                    "Pagamento não confirmado.",
                                    charge.merchantChargeId
                                )
                                return
                            }

                            runOnUiThread {
                                // Podemos considerar que o processo completo de autorização e
                                // captura foi concluído e o pagamento foi efetuado com sucesso.
                                eventSink?.success(
                                    gson.toJson(
                                        SdkPaymentResult(
                                            isApproved = charge.isApproved,
                                            isCanceled = charge.isCanceled,
                                            aid = charge.aid,
                                            amount = charge.amount,
                                            acquirer = charge.acquirer.name,
                                            authorizationCode = charge.authorizationCode,
                                            authorizationResponseCode = charge.authorizationResponseCode,
                                            brand = charge.brand.name,
                                            cancelDateTime = charge.cancelationDateTime,
                                            captureDateTime = charge.captureDateTime,
                                            cardNumber = charge.cardNumber,
                                            cardholderName = charge.cardholderName,
                                            cardholderReceipt = charge.cardholderReceipt,
                                            chargeStatus = charge.chargeStatus?.name,
                                            creationDateTime = charge.creationDateTime,
                                            currency = charge.currency,
                                            installmentNumber = charge.installmentNumber,
                                            installmentType = charge.installmentType.name,
                                            merchantChargeId = charge.merchantChargeId,
                                            merchantReceipt = charge.merchantReceipt,
                                            nsu = charge.nsu,
                                            origin = charge.origin,
                                            paymentType = charge.paymentType.name,
                                            transactionId = charge.transactionId,
                                        )
                                    )
                                )
                            }
                        }

                    })
            }
        }
        Log.d(TAG, "confirmPayment / ended")
    }

    // Função para converter um ByteArray em um Bitmap.
    private fun getBitmapFromByteArray(imageBytes: ByteArray): Bitmap? {
        return if (imageBytes.isNotEmpty()) {
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        } else {
            getDefaultImageToPrint()
        }
    }

    // Função para obter uma imagem padrão para impressão.
    private fun getDefaultImageToPrint(): Bitmap {
        // Get image from drawable folder.
        val drawable = R.drawable.plugjusta
        // Convert drawable to bitmap.
        return BitmapFactory.decodeResource(resources, drawable)
    }

    // Função para converter um Bitmap em um ByteArray.
    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        // Compressão do bitmap para o formato desejado (PNG, JPEG, etc.)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    // Função para converter o tipo de operação informado para o tipo de operação do SDK.
    private fun parseOperationType(operationType: Int): PayOperationType {
        return when (operationType) {
            1 -> PayOperationType.Authorization
            2 -> PayOperationType.PreAuthorization
            3 -> PayOperationType.PreAuthorizationCapture
            4 -> PayOperationType.IncrementalPreAuthorization
            else -> PayOperationType.Authorization
        }
    }

    // Função para converter o tipo de parcelamento informado para o tipo de parcelamento do SDK.
    private fun parseInstallmentType(installmentType: Int): InstallmentType {
        return when (installmentType) {
            1 -> InstallmentType.None
            2 -> InstallmentType.Merchant
            3 -> InstallmentType.Issuer
            else -> InstallmentType.None
        }
    }

    // Função para converter o tipo de pagamento informado para o tipo de pagamento do SDK.
    private fun parsePaymentType(paymentType: Int): PaymentType {
        return when (paymentType) {
            1 -> PaymentType.Debit
            2 -> PaymentType.Credit
            3 -> PaymentType.Pix
            4 -> PaymentType.Voucher
            5 -> PaymentType.Wallet
            else -> PaymentType.Debit
        }
    }

    // Callback para resposta da inicialização do SDK.
    private val mInitResponseCallback = object : InitResponseCallback.Stub() {
        override fun onResponse(initResponse: InitResponse?) {
            Log.d(TAG, "onResponse - initResponse: $initResponse")
            if (initResponse?.initialized == true) {
                // Estabelecimento inicializado nas cargas de tabelas.
                Log.d(
                    TAG,
                    "InitResponseCallback.onResponse / Estabelecimento inicializado nas cargas de tabelas."
                )

                runOnUiThread {
                    eventSink?.success(
                        gson.toJson(
                            SdkTerminalInit(
                                initialized = initResponse.initialized,
                                terminalInfo = SdkTerminalData(
                                    contactlessSupported = initResponse.terminalInfo?.contactlessSupported,
                                    manufacturerName = initResponse.terminalInfo?.manufacturerName,
                                    manufacturerVersion = initResponse.terminalInfo?.manufacturerVersion,
                                    model = initResponse.terminalInfo?.model,
                                    osVersion = initResponse.terminalInfo?.osVersion,
                                    serialNumber = initResponse.terminalInfo?.serialNumber,
                                    specVersion = initResponse.terminalInfo?.specVersion
                                ),
                                availableBrands = initResponse.availableBrands?.map { it.name }
                                    ?: emptyList()
                            )
                        )
                    )
                }
            }
        }
    }

    // Callback para notificações de pagamento.
    private val mPaymentCallback = object : JustaApplication.PaymentCallback() {
        override fun notification(
            message: String?,
            transactionStatus: TransactionStatus?,
            command: AbecsCommands?
        ) {
            Log.d(
                TAG,
                "notification / message: $message, transactionStatus: $transactionStatus, command: $command"
            )

            runOnUiThread {
                eventSink?.success(
                    gson.toJson(
                        SdkTerminalNotification(
                            message = message ?: "",
                            status = transactionStatus?.name,
                            abecsCommand = command?.name
                        )
                    )
                )
            }
        }
    }

    // Callback para resposta da inicialização do SDK.
    private val mPayResponseCallback = object : PaymentResponseCallback.Stub() {
        override fun onResponse(paymentResponse: PaymentResponse?) {
            Log.d(TAG, "onResponse - paymentResponse: $paymentResponse")
            runOnUiThread {
                paymentResponse?.charge?.let {
                    confirmPayment(it)
                }
            }
        }
    }

    // Callback para resposta da impressão.
    private val mPrintStatusCallback = object : IPrintStatusCallback.Stub() {
        override fun finished(status: PrintStatus) {
            Log.d(TAG, "Print result: $status")
            when (status) {
                PrintStatus.Ok -> {
                    runOnUiThread {
                        eventSink?.success(
                            gson.toJson(
                                SdkPrintResult(
                                    status = status.name
                                )
                            )
                        )
                    }
                }

                else -> {
                    runOnUiThread {
                        eventSink?.error("JST-003", "Erro ao imprimir o comprovante.", status.name)
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }

}
