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
package com.gn.videotech.infersnpe.ml

import android.app.Application
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import com.gn.videotech.infersnpe.data.DetectionResult
import com.gn.videotech.infersnpe.data.classNameMapping
import com.gn.videotech.infersnpe.utils.BitmapUtility
import com.gn.videotech.infersnpe.utils.resized
import com.qualcomm.qti.snpe.FloatTensor
import com.qualcomm.qti.snpe.NeuralNetwork
import com.qualcomm.qti.snpe.SNPE

/**
 * A helper class for managing SNPE-based object detection inference.
 *
 * This class initializes and handles a Qualcomm SNPE (Snapdragon Neural Processing Engine) network,
 * loads the model from assets, prepares input tensors, and manages bitmap preprocessing.
 *
 * @param application The application context used for accessing assets and other system resources.
 * @param isUnsignedPD Flag indicating whether the input data format is unsigned (default is true).
 */
class SNPEHelper(
    private val application: Application,
    private val isUnsignedPD: Boolean = true
) {

    /**
     * The SNPE neural network instance. Null until initialized.
     */
    private var neuralNetwork: NeuralNetwork? = null

    /**
     * The selected model type for inference.
     */
    private var selectedModel = ModelType.default

    /**
     * The dimensions (height, width, channels) of the input tensor.
     */
    private var inputTensorHWC = IntArray(0)

    /**
     * The input tensor used to store preprocessed image data.
     */
    private var inputTensor: FloatTensor? = null

    /**
     * The input map of tensor names to their corresponding FloatTensor values.
     */
    private var inputMap: Map<String, FloatTensor>? = null

    /**
     * Utility object for bitmap-to-buffer preprocessing operations.
     */
    private val bitmapUtility = BitmapUtility()

    companion object {

        /**
         * Name of the input layer expected by the model.
         */
        private const val INPUT_LAYER = "input"

        /**
         * Names of the output layers used to extract model results.
         */
        private val OUTPUT_LAYERS = arrayOf(
            arrayOf("/heads/Mul", "/heads/Sigmoid"),
            arrayOf("Transpose_output_bboxes", "Transpose_output_classes"),
        )

        /**
         * Aliases for the output tensors: bounding boxes and class scores.
         */
        private val OUTPUT_NAMES = arrayOf("output_bboxes", "output_classes")

    }

    /**
     * Loads the SNPE model from assets using the specified runtime and initializes input tensors.
     *
     * This method attempts to load the model file from assets using the desired runtime (GPU, DSP,
     * or CPU), initializes the input tensor shape and buffer, and prepares the input tensor map.
     * It also disposes of any previously loaded neural network before initializing a new one.
     *
     * @param runtimeChar A character specifying the desired runtime:
     *  - `'G'` for GPU
     *  - `'D'` for DSP
     *  - Any other character defaults to CPU
     * @param selectedModel The [ModelType] representing the model to load.
     *
     * @return `true` if the model was successfully loaded and initialized; `false` otherwise.
     */
    fun loadModel(runtimeChar: Char, selectedModel: ModelType): Boolean {
        this.selectedModel = selectedModel

        disposeNeuralNetwork()
        val runtime = when (runtimeChar) {
            'G' -> NeuralNetwork.Runtime.GPU
            'D' -> NeuralNetwork.Runtime.DSP
            else -> NeuralNetwork.Runtime.CPU
        }

        val model = loadModelFromAssets(runtime) ?: return false
        val shape = model.inputTensorsShapes[INPUT_LAYER] ?: return false

        inputTensorHWC = shape
        inputTensor = model.createFloatTensor(*shape)
        inputMap = mapOf(INPUT_LAYER to inputTensor!!)
        neuralNetwork = model
        return true
    }

    /**
     * Loads the SNPE neural network model from the application's asset folder.
     *
     * This method initializes a SNPE `NeuralNetwork` instance using the provided runtime
     * configuration. It sets the runtime check option based on the input format (unsigned or not),
     * specifies output layers, and enables CPU fallback. If an error occurs during model loading,
     * it logs the exception and returns null.
     *
     * @param runtime The desired SNPE runtime (e.g., CPU, GPU, DSP) used for inference.
     *
     * @return A configured [NeuralNetwork] instance if loading succeeds, or `null` if an error
     *         occurs.
     */
    private fun loadModelFromAssets(runtime: NeuralNetwork.Runtime): NeuralNetwork? = try {
        val filePath = selectedModel.filePath
        application.assets.open(filePath).use { stream ->
            val outputLayers = getOutputLayers()
            val model = SNPE.NeuralNetworkBuilder(application)
                .setRuntimeCheckOption(
                    if (isUnsignedPD) NeuralNetwork.RuntimeCheckOption.UNSIGNEDPD_CHECK
                    else NeuralNetwork.RuntimeCheckOption.NORMAL_CHECK
                )
                .setOutputLayers(*outputLayers)
                .setModel(stream, stream.available())
                .setPerformanceProfile(NeuralNetwork.PerformanceProfile.DEFAULT)
                .setRuntimeOrder(runtime)
                .setCpuFallbackEnabled(false)
                .build()
            model
        }
    } catch (e: Exception) {
        Log.e("SNPEHelper", "Model loading error", e)
        null
    }

    /**
     * Performs object detection on the given bitmap using the loaded SNPE model.
     *
     * This method preprocesses the input bitmap, runs inference, reads output tensors for
     * bounding boxes and class scores, and constructs detection results above the confidence
     * threshold. It also rescales the bounding boxes to match the original bitmap dimensions and
     * applies Non-Maximum Suppression (NMS) to reduce redundant detections.
     *
     * @param bitmap The input image to run inference on.
     * @param threshold The minimum confidence score required to include a detection (default is
     *                  0.5).
     *
     * @return A list of [DetectionResult] containing detected object labels, confidence scores,
     *         and bounding boxes.
     */
    fun inference(bitmap: Bitmap, threshold: Float = 0.5f): List<DetectionResult> {
        val output = runModel(bitmap) ?: return emptyList()
        val outputNames = arrayOf("output_bboxes", "output_classes")

        val boxes = output[outputNames[0]] ?: return emptyList()
        val classes = output[outputNames[1]] ?: return emptyList()

        val numDetections = boxes.shape[1]
        val numCorners = boxes.shape[2]
        val numClasses = classes.shape[2]

        val boxArray = FloatArray(numDetections * numCorners)
        val classArray = FloatArray(numDetections * numClasses)
        boxes.read(boxArray, 0, boxArray.size)
        classes.read(classArray, 0, classArray.size)

        val scaleX = bitmap.width.toFloat() / getInputWidth()
        val scaleY = bitmap.height.toFloat() / getInputHeight()
        val rectFormat = selectedModel.rectFormat
        val classNameMap = getClassNameMapping()

        val results = mutableListOf<DetectionResult>()

        for (i in 0 until numDetections) {
            val boxOffset = i * numCorners
            val classOffset = i * numClasses

            val box = createRectF(boxArray, boxOffset, scaleX, scaleY, rectFormat)
            val (bestIndex, maxScore) = getBestClass(classArray, classOffset, numClasses)

            if (maxScore >= threshold) {
                val label = classNameMap[bestIndex] ?: "Unknown"
                results.add(DetectionResult(label, maxScore, box))
            }
        }

        return applyNMS(results)
    }

    /**
     * Runs the SNPE neural network model on a preprocessed and resized bitmap.
     *
     * This method first resizes the input bitmap to match the model's input dimensions. It then
     * converts the bitmap to a float buffer in BGR format and writes it to the input tensor. If
     * the model, input tensor, or input map are uninitialized, or if the buffer is detected as
     * black, the method returns `null`. Any exceptions during execution are logged and suppressed.
     *
     * @param bitmap The original input image to be processed.
     *
     * @return A map of output tensor names to [FloatTensor] results, or `null` if inference fails.
     */
    private fun runModel(bitmap: Bitmap): Map<String, FloatTensor>? {
        val resized = bitmap.resized(getInputWidth())
        if (
            resized.width != getInputWidth() || resized.height != getInputHeight() ||
            inputTensor == null || inputMap == null || neuralNetwork == null
        ) return null

        return runCatching {
            bitmapUtility.convertBitmapToBuffer(resized)
            val floats = bitmapUtility.bufferToFloatsRGB()

            // Skip black frames.
            if (bitmapUtility.isBufferBlack()) return null

            inputTensor?.write(floats, 0, floats.size, 0, 0)
            neuralNetwork?.execute(inputMap)
        }.onFailure {
            Log.e("SNPEHelper", "Inference error", it)
        }.getOrNull()
    }

    /**
     * Creates a [RectF] representing a bounding box from a float array, applying scaling and format conversion.
     *
     * Supports two formats:
     * - `"center"`: (cx, cy, width, height) — the box is defined by center coordinates and size.
     * - `"corner"`: (left, top, right, bottom) — the box is defined directly by corner coordinates.
     *
     * The resulting rectangle is scaled using [scaleX] and [scaleY] to match image dimensions.
     *
     * @param boxArray The array containing bounding box values.
     * @param boxOffset The starting index of the box in the array.
     * @param scaleX The horizontal scaling factor.
     * @param scaleY The vertical scaling factor.
     * @param rectFormat The box format: either `"center"` (default) or `"corner"`.
     *
     * @return A scaled [RectF] representing the bounding box.
     *
     * @throws IllegalArgumentException if the format is unsupported.
     */
    private fun createRectF(
        boxArray: FloatArray,
        boxOffset: Int,
        scaleX: Float,
        scaleY: Float,
        rectFormat: String = "center" // "center" or "corner"
    ): RectF {
        return when {
            rectFormat.equals("center", ignoreCase = true) -> {
                val cx = boxArray[boxOffset]
                val cy = boxArray[boxOffset + 1]
                val w = boxArray[boxOffset + 2]
                val h = boxArray[boxOffset + 3]

                RectF(
                    (cx - w / 2f) * scaleX,
                    (cy - h / 2f) * scaleY,
                    (cx + w / 2f) * scaleX,
                    (cy + h / 2f) * scaleY
                )
            }

            rectFormat.equals("corner", ignoreCase = true) -> {
                val x1 = boxArray[boxOffset]
                val y1 = boxArray[boxOffset + 1]
                val x2 = boxArray[boxOffset + 2]
                val y2 = boxArray[boxOffset + 3]

                RectF(x1 * scaleX, y1 * scaleY, x2 * scaleX, y2 * scaleY)
            }

            else -> error("Unsupported format: '$rectFormat'. Use 'center' or 'corner'.")
        }
    }

    /**
     * Finds the class with the highest confidence score starting from the given offset.
     *
     * This method scans a segment of the class score array and identifies the index (class ID)
     * with the maximum value, which represents the most likely predicted class.
     *
     * @param data The full array of class confidence scores output by the model.
     * @param offset The starting index of the class scores for a specific detection.
     * @param numClasses The number of classes in the model's output.
     *
     * @return A [Pair] where the first element is the index of the best class, and the second is
     *         its corresponding confidence score.
     */
    private fun getBestClass(data: FloatArray, offset: Int, numClasses: Int): Pair<Int, Float> {
        var best = -1
        var max = -Float.MAX_VALUE
        for (i in 0 until numClasses) {
            val score = data[offset + i]
            if (score > max) {
                best = i
                max = score
            }
        }
        return best to max
    }

    /**
     * Applies Non-Maximum Suppression (NMS) to remove redundant overlapping detections.
     *
     * Detections are first grouped by label. Within each group, they are sorted by confidence, and
     * overlapping boxes (based on Intersection-over-Union) are removed to keep only the most
     * confident prediction per overlapping region.
     *
     * @param detections A list of [DetectionResult]s returned from raw model output.
     * @param iouThreshold The IoU threshold above which overlapping boxes are suppressed (default
     *                     is 0.2).
     *
     * @return A filtered list of [DetectionResult]s with reduced redundancy.
     */
    private fun applyNMS(detections: List<DetectionResult>,
                         iouThreshold: Float = 0.2f): List<DetectionResult> {
        return detections.groupBy { it.label }.flatMap { (_, group) ->
            val sorted = group.sortedByDescending { it.confidence }.toMutableList()
            val final = mutableListOf<DetectionResult>()
            while (sorted.isNotEmpty()) {
                val top = sorted.removeAt(0)
                final.add(top)
                sorted.removeAll { computeIoU(top.boundingBox, it.boundingBox) > iouThreshold }
            }
            final
        }
    }

    /**
     * Computes the Intersection-over-Union (IoU) between two bounding boxes.
     *
     * IoU is a metric used to evaluate the overlap between two rectangular regions. It is defined
     * as the area of their intersection divided by the area of their union. A higher IoU indicates
     * a greater overlap.
     *
     * @param a The first bounding box.
     * @param b The second bounding box.
     *
     * @return The IoU score between the two rectangles, ranging from 0.0 to 1.0.
     */
    private fun computeIoU(a: RectF, b: RectF): Float {
        val left = maxOf(a.left, b.left)
        val top = maxOf(a.top, b.top)
        val right = minOf(a.right, b.right)
        val bottom = minOf(a.bottom, b.bottom)
        val intersection = maxOf(0f, right - left) * maxOf(0f, bottom - top)
        val union = a.width() * a.height() + b.width() * b.height() - intersection
        return if (union <= 0f) 0f else intersection / union
    }

    /**
     * Retrieves the width of the model's expected input tensor.
     *
     * @return The width dimension, or `0` if the tensor shape is not properly initialized.
     */
    private fun getInputWidth() = inputTensorHWC.getOrNull(1) ?: 0

    /**
     * Retrieves the height of the model's expected input tensor.
     *
     * @return The height dimension, or `0` if the tensor shape is not properly initialized.
     */
    private fun getInputHeight() = inputTensorHWC.getOrNull(2) ?: 0

    /**
     * Returns the output layer names associated with the currently selected model.
     *
     * This uses the [selectedModel]'s [ModelType.index] property to select the correct set of
     * output layer names from the predefined [OUTPUT_LAYERS] list.
     *
     * @return An array of output layer names for the selected model.
     */
    private fun getOutputLayers() = OUTPUT_LAYERS[selectedModel.index]

    /**
     * Returns the class index-to-label mapping for the currently selected model.
     *
     * This uses the [selectedModel]'s [ModelType.index] to access the corresponding class name map
     * from [classNameMapping], enabling label decoding after inference.
     *
     * @return A map of class indices to human-readable class labels.
     */
    private fun getClassNameMapping() = classNameMapping[selectedModel.index]

    /**
     * Releases resources associated with the current SNPE neural network instance.
     *
     * This method safely releases the underlying native resources and clears all references to the
     * model, input tensor, and input map to avoid memory leaks.
     */
    private fun disposeNeuralNetwork() {
        neuralNetwork?.release()
        neuralNetwork = null
        inputTensorHWC = IntArray(0)
        inputTensor = null
        inputMap = null
    }

}
