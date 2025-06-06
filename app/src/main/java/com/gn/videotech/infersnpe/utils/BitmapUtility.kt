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
package com.gn.videotech.infersnpe.utils

import android.graphics.Bitmap
import java.nio.ByteBuffer

/**
 * This class provides utility functions for converting a Bitmap to a ByteBuffer and normalizing
 * the pixel values.
 */
class BitmapUtility {

    /**
     * This is a temporary float array used for conversion.
     */
    private var tempFloatBuffer: FloatArray = FloatArray(0)

    /**
     * This is a temporary byte buffer used for conversion.
     */
    private var tempByteBuffer: ByteBuffer? = null

    /**
     * This is a flag indicating if the byte buffer is considered black.
     */
    private var isBufferBlack: Boolean = false

    /**
     * This checks if the byte buffer is null or not correctly sized.
     *
     * @return True if the buffer is invalid, false otherwise.
     */
    private fun isBufferInvalid(byteBuffer: ByteBuffer?, expectedSize: Int): Boolean =
        byteBuffer?.capacity() != expectedSize

    /**
     * This checks if the byte buffer is black.
     *
     * @return True if the buffer is black, false otherwise.
     */
    fun isBufferBlack(): Boolean = isBufferBlack

    /**
     * Converts a Bitmap to ByteBuffer and initializes float buffer if needed.
     *
     * @param inputBitmap The Bitmap to be converted.
     */
    fun convertBitmapToBuffer(inputBitmap: Bitmap) {

        // Calculate the size of the input bitmap.
        val inputSize = inputBitmap.rowBytes * inputBitmap.height

        // Check if the byte buffer is invalid or needs to be resized.
        if (isBufferInvalid(tempByteBuffer, inputSize)) {
            tempByteBuffer = ByteBuffer.allocate(inputSize)
            tempFloatBuffer = FloatArray(inputBitmap.width * inputBitmap.height * 3)
        }

        // Copy the pixels from the input bitmap to the byte buffer.
        tempByteBuffer?.apply {
            rewind()
            inputBitmap.copyPixelsToBuffer(this)
        }
    }

    /**
     * Converts RGBA bytes into normalized RGB float array and checks for black buffer.
     *
     * @return The normalized RGB float array.
     */
    fun bufferToFloatsRGB(): FloatArray {

        // Calculate the number of pixels in the input bitmap.
        val inputArray = tempByteBuffer?.array() ?: return FloatArray(0)
        val pixelCount = tempFloatBuffer.size / 3

        // Check if the buffer is considered black.
        isBufferBlack = countBluePixel(pixelCount, inputArray) < pixelCount * 13

        // Return the normalized RGB float array.
        return tempFloatBuffer
    }

    /**
     * Counts blue pixel values and populates the float buffer with normalized RGB data.
     *
     * @param pixelCount The number of pixels in the input bitmap.
     * @param inputArray The byte array representing the input bitmap.
     *
     * @return The sum of blue pixel values.
     */
    private fun countBluePixel(pixelCount: Int, inputArray: ByteArray): Long {

        // This scale is used to normalize pixel values that originally range from 0 to 255 (as
        // 8-bit integers) to the range [0, 1] for neural network input.
        val inputScale = 1 / 255.0f
        var blueSum = 0L

        // Iterate through each pixel in the input bitmap.
        (0 until pixelCount).forEach { i ->
            val srcIndex = i * 4
            val dstIndex = i * 3

            // Extract the RGB values from the input array.
            val red = inputArray[srcIndex].toInt() and 0xFF
            val green = inputArray[srcIndex + 1].toInt() and 0xFF
            val blue = inputArray[srcIndex + 2].toInt() and 0xFF

            // Normalize and store the RGB values in the float buffer.
            tempFloatBuffer[dstIndex] = inputScale * red
            tempFloatBuffer[dstIndex + 1] = inputScale * green
            tempFloatBuffer[dstIndex + 2] = inputScale * blue

            blueSum += blue
        }

        return blueSum
    }

}
