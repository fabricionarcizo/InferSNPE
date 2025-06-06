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

/**
 * Enum representing supported SNPE model variants, including their file paths and classification
 * indices.
 *
 * Each model type includes:
 * - [displayName]: A human-readable label for display in UI.
 * - [filePath]: The file name of the model in the assets directory.
 * - [index]: An integer index used to select label maps or output formats (e.g., 0 = COCO,
 *            1 = hadRID).
 *
 * This enum supports multiple precision formats (FP32, INT8, INT8+HTP) for both YOLO-NAS and
 * YOLO-hagRID models.
 *
 * @property displayName The name shown to users (e.g., in dropdowns or settings).
 * @property filePath The relative file path to the model within the assets directory.
 * @property rectFormat The format used to represent bounding boxes (e.g., "center" or "corner").
 * @property index The model group index used to look up class mappings or rect formats.
 */
enum class ModelType(
    val displayName: String,
    val filePath: String,
    val rectFormat: String,
    val index: Int
) {
    /**
     * YOLO-NAS S model with full precision (FP32).
     */
    YOLO_NAS_FP32("YOLO-NAS S (FP32)", "yolo_nas_s_fp32.dlc", "corner", 0),

    /**
     * YOLO-NAS S model with 8-bit integer quantization (INT8).
     */
    YOLO_NAS_INT8("YOLO-NAS S (INT8)", "yolo_nas_s_int8.dlc", "corner", 0),

    /**
     * YOLO-NAS S model optimized for HTP with INT8 precision.
     */
    YOLO_NAS_INT8_HTP("YOLO-NAS S (INT8+HTP)", "yolo_nas_s_int8_htp_sm7325.dlc", "corner", 0),

    /**
     * YOLO-hagRID model with full precision (FP32).
     */
    YOLO_HAGRID_FP32("YOLO hagRID (FP32)", "yolo_hagRID_fp32.dlc", "center", 1),

    /**
     * YOLO-hagRID model with 8-bit integer quantization (INT8).
     */
    YOLO_HAGRID_INT8("YOLO hagRID (INT8)", "yolo_hagRID_int8.dlc", "center", 1),

    /**
     * YOLO-hagRID model optimized for HTP with INT8 precision.
     */
    YOLO_HAGRID_INT8_HTP("YOLO hagRID (INT8+HTP)", "yolo_hagRID_int8_htp_sm7325.dlc", "center", 1);

    companion object {

        /**
         * The default model to be used when no specific selection is made.
         */
        val default = YOLO_NAS_INT8_HTP

        /**
         * Returns a [ModelType] based on a case-insensitive display name match.
         *
         * @param name The display name to search for.
         *
         * @return A matching [ModelType], or `null` if not found.
         */
        fun fromDisplayName(name: String): ModelType? =
            entries.find { it.displayName.equals(name, ignoreCase = true) }

    }

}
