package com.example.waterlock.presentation

import android.content.Context
import android.util.Log
import com.samsung.android.sdk.accessory.SAAgentV2
import com.samsung.android.sdk.accessory.SAPeerAgent
import com.samsung.android.sdk.accessory.SASocket

class MyProviderService(context: Context) : SAAgentV2(TAG, context, SASOCKET_CLASS) {

    private var mConnection: ServiceConnection? = null

    override fun onFindPeerAgentsResponse(peerAgents: Array<SAPeerAgent>?, result: Int) {
        // Обработка ответа на поиск агента
        Log.d(TAG, "onFindPeerAgentsResponse: result = $result")
    }

    override fun onServiceConnectionResponse(peerAgent: SAPeerAgent?, socket: SASocket?, result: Int) {
        if (result == CONNECTION_SUCCESS) {
            Log.d(TAG, "Service connection successful")
            mConnection = socket as? ServiceConnection
        } else {
            Log.e(TAG, "Service connection failed: result = $result")
        }
    }

    fun sendWaterLockCommand() {
        try {
            mConnection?.let {
                val command = byteArrayOf(/* команда для включения Water Lock */)
                it.send(0, command)
                Log.d(TAG, "Water Lock command sent")
            } ?: run {
                Log.e(TAG, "Connection is null")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending Water Lock command", e)
            e.printStackTrace()
        }
    }

    inner class ServiceConnection : SASocket(ServiceConnection::class.java.name) {
        override fun onError(channelId: Int, errorString: String?, error: Int) {
            // Обработка ошибок
            Log.e(TAG, "ServiceConnection onError: $errorString, error = $error")
        }

        override fun onReceive(channelId: Int, data: ByteArray?) {
            // Обработка полученных данных
            Log.d(TAG, "Data received: ${data?.size} bytes")
        }

        override fun onServiceConnectionLost(reason: Int) {
            // Обработка потери соединения
            Log.e(TAG, "Service connection lost: reason = $reason")
        }
    }

    companion object {
        private const val TAG = "MyProviderService"
        private val SASOCKET_CLASS = ServiceConnection::class.java
    }
}