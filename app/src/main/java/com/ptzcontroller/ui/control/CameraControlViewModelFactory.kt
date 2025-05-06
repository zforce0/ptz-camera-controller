
package com.ptzcontroller.ui.control

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ptzcontroller.CameraControlRepository

class CameraControlViewModelFactory(
    private val repository: CameraControlRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CameraControlViewModel::class.java)) {
            return CameraControlViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
