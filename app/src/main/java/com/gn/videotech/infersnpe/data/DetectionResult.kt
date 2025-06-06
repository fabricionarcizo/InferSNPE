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
package com.gn.videotech.infersnpe.data

import android.graphics.RectF

/**
 * Represents a single object detection result.
 *
 * Each result includes the detected object's label (e.g., "person", "car"), the confidence score
 * from the model, and the bounding box in image coordinates.
 *
 * @property label The name or class of the detected object.
 * @property confidence The model's confidence score for the detection (range: 0.0 to 1.0).
 * @property boundingBox The rectangular area where the object was detected, in image coordinates.
 */
data class DetectionResult(
    val label: String,
    val confidence: Float,
    val boundingBox: RectF
)
