/*
 * MIT License
 *
 * Copyright (c) 2025 Fabricio Batista Narcizo, Elizabete Munzlinger, Sai Narsi Reddy Donthi Reddy,
 * and Shan Ahmed Shaffi.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.gn.videotech.infersnpe.viewmodel

import androidx.camera.core.CameraSelector
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel for managing the selected camera in a CameraX-based application.
 *
 * Holds the current [CameraSelector] in a [LiveData] observable, allowing UI components to react
 * to changes in the active camera (e.g., switching between front and back cameras).
 */
class CameraViewModel : ViewModel() {

    /**
     * Backing [MutableLiveData] holding the current [CameraSelector] value.
     *
     * Initialized to [CameraSelector.DEFAULT_BACK_CAMERA]. Used internally to expose camera
     * selection changes via [cameraSelector] to observing UI components.
     */
    private val _cameraSelector = MutableLiveData(CameraSelector.DEFAULT_BACK_CAMERA)

    /**
     * LiveData holding the currently selected [CameraSelector], defaulting to the back camera.
     *
     * Observers can use this to update the preview and analysis pipelines accordingly.
     */
    val cameraSelector: LiveData<CameraSelector> get() = _cameraSelector

    /**
     * Updates the current camera selector if it differs from the existing one.
     *
     * This prevents redundant updates when the selected camera hasn't changed.
     *
     * @param newSelector The new [CameraSelector] to be applied.
     */
    fun updateCameraSelector(newSelector: CameraSelector) {
        if (_cameraSelector.value != newSelector) {
            _cameraSelector.value = newSelector
        }
    }

}
