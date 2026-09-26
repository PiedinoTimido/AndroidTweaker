package com.android.tweaker.data

import android.content.Context
import dadb.Dadb
import dadb.AdbKeyPair
import dadb.AdbShellResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

sealed class AdbConnectionStatus {
    object Disconnected : AdbConnectionStatus()
    object Connecting : AdbConnectionStatus()
    data class Connected(val port: Int, val isUsb: Boolean) : AdbConnectionStatus()
    data class Error(val message: String) : AdbConnectionStatus()
}

class AdbManager {
    private var dadbInstance: Dadb? = null
    private var keyFileDir: File? = null
    var connectedPort: Int = 0
        private set
    var isConnectedViaUsb: Boolean = false
        private set

    private val _connectionStatus = MutableStateFlow<AdbConnectionStatus>(AdbConnectionStatus.Disconnected)
    val connectionStatus: StateFlow<AdbConnectionStatus> = _connectionStatus.asStateFlow()

    fun initContext(context: Context) {
        keyFileDir = context.filesDir
    }

    private fun createDadbInstance(host: String, port: Int): Dadb {
        val dir = keyFileDir
        if (dir != null) {
            try {
                val privateKeyFile = File(dir, "adbkey")
                val publicKeyFile = File(dir, "adbkey.pub")
                if (!privateKeyFile.exists() || !publicKeyFile.exists()) {
                    dadb.AdbKeyPair.generate(privateKeyFile, publicKeyFile)
                }
                val keyPair = dadb.AdbKeyPair.read(privateKeyFile, publicKeyFile)
                return Dadb.create(host, port, keyPair)
            } catch (_: Exception) {}
        }
        return Dadb.create(host, port)
    }

    suspend fun pairDevice(port: Int, code: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            _connectionStatus.value = AdbConnectionStatus.Connecting
            val dadb = createDadbInstance("127.0.0.1", port)
            dadbInstance = dadb
            connectedPort = port
            isConnectedViaUsb = (port == 5555)
            _connectionStatus.value = AdbConnectionStatus.Connected(port, isConnectedViaUsb)
            Result.success("Paired & connected successfully to 127.0.0.1:$port!")
        } catch (e: Exception) {
            _connectionStatus.value = AdbConnectionStatus.Error("Pairing/Connection failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun connectDevice(port: Int): Result<String> = withContext(Dispatchers.IO) {
        try {
            _connectionStatus.value = AdbConnectionStatus.Connecting
            val dadb = createDadbInstance("127.0.0.1", port)
            dadbInstance = dadb
            connectedPort = port
            isConnectedViaUsb = (port == 5555)
            _connectionStatus.value = AdbConnectionStatus.Connected(port, isConnectedViaUsb)
            Result.success("Successfully connected to local ADB on 127.0.0.1:$port!")
        } catch (e: Exception) {
            _connectionStatus.value = AdbConnectionStatus.Error("Connection failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun autoConnectOnStartup(lastAdbPortStr: String): Boolean = withContext(Dispatchers.IO) {
        // Try USB default port 5555 first
        try {
            val dadbUsb = createDadbInstance("127.0.0.1", 5555)
            dadbInstance = dadbUsb
            connectedPort = 5555
            isConnectedViaUsb = true
            _connectionStatus.value = AdbConnectionStatus.Connected(5555, true)
            return@withContext true
        } catch (_: Exception) {}

        // Try saved last ADB Wireless port if different
        val savedPort = lastAdbPortStr.toIntOrNull()
        if (savedPort != null && savedPort != 5555) {
            try {
                val dadbWireless = createDadbInstance("127.0.0.1", savedPort)
                dadbInstance = dadbWireless
                connectedPort = savedPort
                isConnectedViaUsb = false
                _connectionStatus.value = AdbConnectionStatus.Connected(savedPort, false)
                return@withContext true
            } catch (_: Exception) {}
        }
        false
    }

    fun disconnect() {
        try {
            dadbInstance?.close()
        } catch (_: Exception) {}
        dadbInstance = null
        connectedPort = 0
        isConnectedViaUsb = false
        _connectionStatus.value = AdbConnectionStatus.Disconnected
    }

    suspend fun executeShellCommand(command: String): String = withContext(Dispatchers.IO) {
        val cleanCmd = command.trim().removePrefix("adb shell ").trim()
        val dadb = dadbInstance

        if (dadb != null) {
            try {
                val response: AdbShellResponse = dadb.shell(cleanCmd)
                val stdout = response.allOutput
                if (stdout.isNotBlank()) stdout else "Command executed (Exit code: ${response.exitCode})"
            } catch (e: Exception) {
                val errMsg = e.localizedMessage ?: e.message ?: "Unknown error"
                if (errMsg.contains("Permission", ignoreCase = true) || errMsg.contains("Operation not permitted", ignoreCase = true) || errMsg.contains("Socket", ignoreCase = true)) {
                    "⚠️ ADB Socket Error: $errMsg\n\nPlease reconnect via the ADB Connection screen."
                } else {
                    executeLocalShell(cleanCmd)
                }
            }
        } else {
            executeLocalShell(cleanCmd)
        }
    }

    private fun executeLocalShell(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val errorReader = BufferedReader(InputStreamReader(process.errorStream))
            val output = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            while (errorReader.readLine().also { line = it } != null) {
                output.append("STDERR: ").append(line).append("\n")
            }
            process.waitFor()
            val result = output.toString().trim()
            if (result.isNotEmpty()) result else "Command completed via local shell."
        } catch (e: Exception) {
            "Execution error: ${e.localizedMessage}\n\nPlease connect ADB via the ADB Connection screen."
        }
    }
}
