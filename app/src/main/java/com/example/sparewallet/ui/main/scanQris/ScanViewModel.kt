package com.example.sparewallet.ui.main.scanQris

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ScanViewModel : ViewModel() {

    private val _scanResult = MutableLiveData<String>()
    val scanResult: LiveData<String> = _scanResult

    fun setScanResult(result: String) {
        _scanResult.value = result
    }
}
