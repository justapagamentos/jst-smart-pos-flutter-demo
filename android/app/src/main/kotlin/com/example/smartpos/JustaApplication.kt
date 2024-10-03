package com.example.smartpos

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import br.com.aditum.IAditumSdkService
import br.com.aditum.data.v2.IPaymentCallback
import br.com.aditum.data.v2.enums.AbecsCommands
import br.com.aditum.data.v2.enums.TransactionStatus
import br.com.aditum.data.v2.model.MerchantData
import br.com.aditum.data.v2.model.callbacks.GetClearDataFinishedCallback
import br.com.aditum.data.v2.model.callbacks.GetClearDataRequest
import br.com.aditum.data.v2.model.callbacks.GetMenuSelectionFinishedCallback
import br.com.aditum.data.v2.model.callbacks.GetMenuSelectionRequest

// Log tag.
private const val TAG = "JustaApplication"

// Aditum package e serviços.
// --------------------------
// O pacote base da Aditum é utilizado como forma de inicializar o serviço para realizar
// operações financeiras - e.g. vendas no crédito, débito, Pix, etc - com o Smart POS.
// --------------------------
private const val PACKAGE_BASE_NAME: String = "br.com.aditum"
private const val PACKAGE_NAME: String = "$PACKAGE_BASE_NAME.smartpostef"
private const val ACTION_COMMUNICATION_SERVICE: String = "$PACKAGE_BASE_NAME.AditumSdkService"

// Aplicação base que gerencia a conexão com o serviço da Aditum.
// Note que existe uma gestão de memória e que por isso precisamos usar pela gestão da Aplicação.
// --------------------------
// Para configurar essa classe, você deve abrir o arquivo "AndroidManifest.xml" e incluir na
// propriedade "android:name" da tag "application" o valor "com.example.smartpos.JustaApplication".
class JustaApplication : Application() {

    // Flag informando se o serviço da Aditum está conectado corretamente.
    @Volatile
    private var mIsServiceConnected: Boolean = false
    val isServiceConnected: Boolean
        get() = mIsServiceConnected

    // Serviço de comunicação com a Aditum. */
    @Volatile
    private var mSdkService: IAditumSdkService? = null
    val communicationService: IAditumSdkService?
        get() = mSdkService

    // Dados do estabelecimento inicializado no Smart POS.
    @Volatile
    private var mMerchantData: MerchantData? = null
    var merchantData: MerchantData?
        get() = mMerchantData
        set(data) {
            mMerchantData = data
        }

    // Listener com o serviço da Aditum informando quando estamos conectados ao serviço.
    private var mSdkConnectionListener: OnServiceConnectionListener? = null
    var serviceConnectionListener: OnServiceConnectionListener?
        get() = mSdkConnectionListener
        set(listener) {
            mSdkConnectionListener = listener
        }

    // Listener com o serviço da Aditum informando quando estamos conectados ao serviço.
    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            Log.d(TAG, "onServiceConnected / Component: $componentName")
            mSdkService = IAditumSdkService.Stub.asInterface(service)
            setServiceConnected(true)
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            Log.d(TAG, "onServiceDisconnected / Component: $componentName")
            mSdkService = null
            setServiceConnected(false)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
    }

    override fun onTerminate() {
        super.onTerminate()
        Log.d(TAG, "onTerminate")
    }

    // Método para inicializar o SDK e conectar no serviço para operações de pagamentos.
    fun initSdk() {
        Log.d(TAG, "initSdk / started")
        val intent = Intent(ACTION_COMMUNICATION_SERVICE)
        intent.setPackage(PACKAGE_NAME)

        Log.d(TAG, "initSdk / PACKAGE_NAME: $PACKAGE_NAME")
        Log.d(TAG, "initSdk / ACTION_COMMUNICATION_SERVICE: $ACTION_COMMUNICATION_SERVICE")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "initSdk / Android Oreo or higher: ${Build.VERSION.SDK_INT}");
            startForegroundService(intent);
        } else {
            Log.d(TAG, "initSdk / Android Nougat or lower: ${Build.VERSION.SDK_INT}");
            startService(intent);
        }

        val bound = bindService(intent, mServiceConnection, (Context.BIND_AUTO_CREATE))
        Log.d(TAG, "initSdk / bindService returned: $bound")
        Log.d(TAG, "initSdk / ended")
    }

    // Método para informar à aplicação sobre conexão do serviço da Aditum.
    private fun setServiceConnected(isConnected: Boolean) {
        Log.d(TAG, "setServiceConnected / isConnected: $isConnected")
        synchronized(this) {
            mIsServiceConnected = isConnected
            mSdkConnectionListener?.onServiceConnection(mIsServiceConnected)
        }
    }

    // Interface para informar quando estamos conectados ao serviço da Aditum.
    interface OnServiceConnectionListener {
        fun onServiceConnection(serviceConnected: Boolean)
    }

    // Classe para tratar as respostas relacionadas às operações de pagamentos no Smart POS.
    abstract class PaymentCallback : IPaymentCallback.Stub() {

        override fun notification(
            message: String?,
            transactionStatus: TransactionStatus?,
            command: AbecsCommands?
        ) {
            Log.d(
                TAG,
                "notification - message: $message, transactionStatus: $transactionStatus, command: $command"
            )
        }

        override fun pinNotification(message: String, length: Int) {
            Log.d(TAG, "pinNotification - message: $message, length: $length")
        }

        override fun startGetClearData(
            clearDataRequest: GetClearDataRequest?,
            finished: GetClearDataFinishedCallback?
        ) {
            Log.d(TAG, "startGetClearData - clearDataRequest: $clearDataRequest, finished: $finished")
        }

        override fun startGetMenuSelection(
            menuSelectionRequest: GetMenuSelectionRequest?,
            finished: GetMenuSelectionFinishedCallback?
        ) {
            Log.d(
                TAG,
                "startGetMenuSelection - menuSelectionRequest: $menuSelectionRequest, finished: $finished"
            )
        }

        override fun qrCodeGenerated(qrCode: String, expirationTime: Int) {
            Log.d(TAG, "qrCodeGenerated - qrCode: $qrCode, expirationTime: $expirationTime")
        }
    }
}
