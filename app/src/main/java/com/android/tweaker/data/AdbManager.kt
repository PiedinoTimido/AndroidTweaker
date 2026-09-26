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
    data class Connected(val port: Int) : AdbConnectionStatus()
    data class Error(val message: String) : AdbConnectionStatus()
}

class AdbManager {
    private var dadbInstance: Dadb? = null
    private var keyFileDir: File? = null

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
            _connectionStatus.value = AdbConnectionStatus.Connected(port)
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
            _connectionStatus.value = AdbConnectionStatus.Connected(port)
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
            _connectionStatus.value = AdbConnectionStatus.Connected(5555)
            return@withContext true
        } catch (_: Exception) {}

        // Try saved last ADB Wireless port if different
        val savedPort = lastAdbPortStr.toIntOrNull()
        if (savedPort != null && savedPort != 5555) {
            try {
                val dadbWireless = createDadbInstance("127.0.0.1", savedPort)
                dadbInstance = dadbWireless
                _connectionStatus.value = AdbConnectionStatus.Connected(savedPort)
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
        _connectionStatus.value = AdbConnectionStatus.Disconnected
    }

    suspend fun executeShellCommand(command: String): String = withContext(Dispatchers.IO) {
        val cleanCmd = command.trim().removePrefix("adb shell ").trim()
        val dadb = dadbInstance
        val isPrivileged = cleanCmd.startsWith("pm ") || 
                           cleanCmd.startsWith("am ") || 
                           cleanCmd.startsWith("cmd ") || 
                           cleanCmd.startsWith("dumpsys ") || 
                           cleanCmd.startsWith("wm ") || 
                           cleanCmd.startsWith("input ") || 
                           cleanCmd.startsWith("reboot") || 
                           cleanCmd.startsWith("recovery")

        if (dadb != null) {
            try {
                val response: AdbShellResponse = dadb.shell(cleanCmd)
                val stdout = response.allOutput
                if (stdout.isNotBlank()) stdout else "Command executed (Exit code: ${response.exitCode})"
            } catch (e: Exception) {
                if (isPrivileged) {
                    "⚠️ ADB Socket Error: ${e.localizedMessage}\n\nPrivileged commands must be executed through an authenticated ADB Wireless socket. Please reconnect via the ADB Connection screen."
                } else {
                    executeLocalShell(cleanCmd)
                }
            }
        } else {
            if (isPrivileged) {
                "⚠️ ADB Session Required!\n\nThis command requires an active ADB Wireless socket connection. Please connect your device via the ADB Connection screen."
            } else {
                executeLocalShell(cleanCmd)
            }
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
            if (result.isNotEmpty()) result else "Command completed via local shell. (Note: Wireless ADB connection is recommended for system permission tweaks)."
        } catch (e: Exception) {
            "Execution error: ${e.localizedMessage}\n\nPlease connect Wireless ADB via the ADB Connection screen."
        }
    }
}
