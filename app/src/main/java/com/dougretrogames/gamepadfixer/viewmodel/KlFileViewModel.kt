package com.dougretrogames.gamepadfixer.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dougretrogames.gamepadfixer.generator.KlFileGenerator
import com.dougretrogames.gamepadfixer.model.InputDeviceInfo
import com.dougretrogames.gamepadfixer.model.KlFile
import com.dougretrogames.gamepadfixer.model.RootResult
import com.dougretrogames.gamepadfixer.root.RootManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class KlFileViewModel(
    private val rootManager: RootManager = RootManager(),
    private val generator: KlFileGenerator = KlFileGenerator()
) : ViewModel() {

    private val _klContent = MutableLiveData<String>("")
    val klContent: LiveData<String> = _klContent

    private val _statusMessage = MutableLiveData<String>("")
    val statusMessage: LiveData<String> = _statusMessage

    private val _isRooted = MutableLiveData<Boolean?>(null)
    val isRooted: LiveData<Boolean?> = _isRooted

    private var currentKlFile: KlFile? = null

    fun checkRoot() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = rootManager.checkRoot()
            _isRooted.postValue(result)
        }
    }

    fun generateKlFromDevice(device: InputDeviceInfo) {
        val klFile = generator.generateFromDevice(device)
        currentKlFile = klFile
        _klContent.postValue(klFile.toFileContent())
        _statusMessage.postValue("Generated: ${klFile.fileName}")
    }

    fun generateKlFromEvents(
        device: InputDeviceInfo,
        keyCodes: Set<Int>,
        axes: Map<Int, Float>
    ) {
        val klFile = generator.generateFromCapturedEvents(device, keyCodes, axes)
        currentKlFile = klFile
        _klContent.postValue(klFile.toFileContent())
        _statusMessage.postValue("Generated: ${klFile.fileName} (from captured events)")
    }

    fun saveToPrivateStorage(context: Context): File? {
        val klFile = currentKlFile ?: return null
        return try {
            val dir = File(context.filesDir, "kl_files")
            dir.mkdirs()
            val file = File(dir, klFile.fileName)
            file.writeText(klFile.toFileContent())
            _statusMessage.postValue("Saved to: ${file.absolutePath}")
            file
        } catch (e: Exception) {
            _statusMessage.postValue("Save failed: ${e.message}")
            null
        }
    }

    fun installToSystem(context: Context) {
        val klFile = currentKlFile ?: run {
            _statusMessage.postValue("No KL file generated yet")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val dir = File(context.filesDir, "kl_files")
            dir.mkdirs()
            val tmpFile = File(dir, klFile.fileName)
            tmpFile.writeText(klFile.toFileContent())

            // Backup existing
            rootManager.backupKlFile(klFile.fileName)

            val result = rootManager.installKlFile(tmpFile.absolutePath, klFile.fileName)
            val msg = when (result) {
                is RootResult.Success -> "Installed ${klFile.fileName} successfully!"
                is RootResult.Error -> "Install failed: ${result.message}"
                RootResult.NoRoot -> "No root access available"
            }
            _statusMessage.postValue(msg)
        }
    }

    fun restoreBackup() {
        val klFile = currentKlFile ?: run {
            _statusMessage.postValue("No KL file selected")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val result = rootManager.restoreBackup(klFile.fileName)
            val msg = when (result) {
                is RootResult.Success -> "Restored backup for ${klFile.fileName}"
                is RootResult.Error -> "Restore failed: ${result.message}"
                RootResult.NoRoot -> "No root access available"
            }
            _statusMessage.postValue(msg)
        }
    }

    fun updateContent(content: String) {
        _klContent.value = content
        currentKlFile?.let {
            currentKlFile = it.copy(
                mappings = it.mappings // Content edited manually, track for save
            )
        }
    }
}
