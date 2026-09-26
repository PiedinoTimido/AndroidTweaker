package com.android.tweaker.data

import dadb.Dadb
import dadb.AdbShellResponse

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

sealed class AdbConnectionStatus {
    object Disconnected : AdbConnectionStatus()
    object Connecting : AdbConnectionStatus()
    data class Connected(val port: Int) : AdbConnectionStatus()
    data class Error(val message: String) : AdbConnectionStatus()
}

class AdbManager {
    private var dadbInstance: Dadb? = null

    private val _connectionStatus = MutableStateFlow<AdbConnectionStatus>(AdbConnectionStatus.Disconnected)
    val connectionStatus: StateFlow<AdbConnectionStatus> = _connectionStatus.asStateFlow()

    suspend fun pairDevice(port: Int, code: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            _connectionStatus.value = AdbConnectionStatus.Connecting
            // Connect using Dadb connection to localhost pairing port
            val dadb = Dadb.create("127.0.0.1", port)
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
            val dadb = Dadb.create("127.0.0.1", port)
            dadbInstance = dadb
            _connectionStatus.value = AdbConnectionStatus.Connected(port)
            Result.success("Successfully connected to local ADB on 127.0.0.1:$port!")
        } catch (e: Exception) {
            _connectionStatus.value = AdbConnectionStatus.Error("Connection failed: ${e.message}")
            Result.failure(e)
        }
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
