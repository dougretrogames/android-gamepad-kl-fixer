package com.dougretrogames.gamepadfixer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dougretrogames.gamepadfixer.model.GamepadDevice
import com.dougretrogames.gamepadfixer.model.KeyMapping
import com.dougretrogames.gamepadfixer.model.KlFile
import com.dougretrogames.gamepadfixer.model.RootOperationResult
import com.dougretrogames.gamepadfixer.repository.GamepadRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel for [KlFileActivity].
 *
 * Handles KL file generation, save, install, backup and restore.
 */
class KlFileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GamepadRepository(application)

    private val _klFile = MutableLiveData<KlFile?>(null)
    val klFile: LiveData<KlFile?> = _klFile

    private val _operationResult = MutableLiveData<String>()
    val operationResult: LiveData<String> = _operationResult

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val customMappings = mutableListOf<KeyMapping>()

    /**
     * Generates the KL file for [device] and stores it locally.
     */
    fun generateKlFile(device: GamepadDevice) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            val (klFile, _) = repository.generateAndSaveKlFile(device, customMappings.toList())
            _klFile.postValue(klFile)
            _operationResult.postValue("KL file generated: ${device.klFileName}")
            _isLoading.postValue(false)
        }
    }

    /**
     * Backs up the existing system .kl file, then installs the newly generated one.
     */
    fun backupAndInstall(device: GamepadDevice) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            // Step 1: backup
            val backupResult = repository.backupKlFile(device.klFileName)
            if (backupResult is RootOperationResult.NoRoot) {
                _operationResult.postValue("ERROR: Root access not available.")
                _isLoading.postValue(false)
                return@launch
            }
            // Step 2: generate & install
            val (_, localFile) = repository.generateAndSaveKlFile(device, customMappings.toList())
            val installResult = repository.installKlFile(localFile, device.klFileName)
            _operationResult.postValue(
                when (installResult) {
                    is RootOperationResult.Success -> "Installed successfully: ${device.klFileName}"
                    is RootOperationResult.Failure -> "Install failed: ${installResult.error}"
                    is RootOperationResult.NoRoot  -> "ERROR: Root access not available."
                }
            )
            _isLoading.postValue(false)
        }
    }

    /**
     * Restores the backed-up .kl file.
     */
    fun restore(device: GamepadDevice) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            val result = repository.restoreKlFile(device.klFileName)
            _operationResult.postValue(
                when (result) {
                    is RootOperationResult.Success -> "Restored successfully: ${device.klFileName}"
                    is RootOperationResult.Failure -> "Restore failed: ${result.error}"
                    is RootOperationResult.NoRoot  -> "ERROR: Root access not available."
                }
            )
            _isLoading.postValue(false)
        }
    }

    /** Adds a captured key mapping to the custom list. */
    fun addCustomMapping(mapping: KeyMapping) {
        customMappings.removeAll { it.scanCode == mapping.scanCode }
        customMappings.add(mapping)
    }

    fun clearCustomMappings() = customMappings.clear()
}
