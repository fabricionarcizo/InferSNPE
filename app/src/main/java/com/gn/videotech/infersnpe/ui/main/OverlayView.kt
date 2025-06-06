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
package com.gn.videotech.infersnpe.ui.main

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF

import android.util.AttributeSet
import android.view.View
import com.gn.videotech.infersnpe.data.DetectionResult
import com.google.android.material.color.MaterialColors

/**
 * A custom view for rendering graphical overlays on top of a camera preview or image.
 *
 * This view is typically used to draw bounding boxes, detection results, or annotations over a live
 * or static image source. Extend this class and override the [onDraw] method to implement custom
 * rendering logic.
 *
 * @constructor Creates an instance of [OverlayView].
 *
 * @param context The context used to instantiate the view.
 * @param attrs The attribute set passed from the XML layout (optional).
 */
class OverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    /**
     * The primary color used for drawing overlays.
     */
    private val primaryColor = MaterialColors.getColor(
        context, com.google.android.material.R.attr.colorPrimary, Color.RED)

    /**
     * The color used for text labels on overlays.
     */
    private val primaryTextColor = MaterialColors.getColor(
        context, com.google.android.material.R.attr.colorOnPrimary, Color.YELLOW)

    /**
     * The width of the source image used for scaling bounding boxes.
     */
    var imageWidth: Int = 480

    /**
     * The height of the source image used for scaling bounding boxes.
     */
    var imageHeight: Int = 640

    /**
     * A flag indicating whether the camera is in front or back position.
     */
    var isFrontCamera: Boolean = false

    /**
     * A reusable [RectF] instance for mapping detection coordinates to the view space.
     */
    private val mappedBox = RectF()

    /**
     * A reusable [Rect] instance used to measure text dimensions.
     */
    private val textBounds = Rect()

    /**
     * The list of detection results to be rendered on the overlay.
     */
    private var detectionResults: List<DetectionResult> = emptyList()

    /**
     * Paint used to draw bounding boxes on the overlay.
     *
     * Configured with anti-aliasing, primary color, stroke style, and a fixed stroke width. This
     * paint is typically used in the [onDraw] method to render detection results.
     */
    private val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = primaryColor
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    /**
     * Paint used to draw detection labels (e.g., class names and scores) on the overlay.
     *
     * Configured with anti-aliasing, primary text color, and a text size of 48sp. Typically used in
     * combination with [boxPaint] to annotate detected objects.
     */
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = primaryTextColor
        textSize = 48f
        style = Paint.Style.FILL
    }

    /**
     * Paint used to draw background rectangles for label annotations.
     *
     * Configured with anti-aliasing, same color as [boxPaint], and a fill style.
     */
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = primaryColor
        style = Paint.Style.FILL
    }

    /**
     * Updates the list of detection results and triggers a redraw of the overlay.
     *
     * This method replaces the current detection list with the provided results and calls
     * [invalidate] to refresh the view, causing [onDraw] to be invoked.
     *
     * @param results A list of new [DetectionResult] objects to render on the overlay.
     */
    fun updateDetections(results: List<DetectionResult>) {
        detectionResults = results
        invalidate()
    }

    /**
     * Draws detection results on the canvas overlay.
     *
     * This method is called automatically when the view is invalidated. It maps each detection's
     * bounding box to the view's coordinate space, draws a red rectangle using [boxPaint], and
     * annotates it with a label and confidence score using [labelPaint]. Drawing is skipped if the
     * image dimensions are not properly set.
     *
     * @param canvas The canvas on which the overlay content is drawn.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (imageWidth == 0 || imageHeight == 0) return

        val padding = 12f

        for (result in detectionResults) {
            val originalBox = if (isFrontCamera) {
                flipHorizontally(result.boundingBox, imageWidth)
            } else {
                result.boundingBox
            }

            mapBox(originalBox, mappedBox)
            canvas.drawRect(mappedBox, boxPaint)

            val label = "${result.label} ${(result.confidence * 100).toInt()}%"
            val textX = mappedBox.left
            val textY = mappedBox.top - padding

            labelPaint.getTextBounds(label, 0, label.length, textBounds)

            val bgLeft = textX - padding
            val bgTop = textY + textBounds.top - padding
            val bgRight = textX + textBounds.width() + padding
            val bgBottom = textY + textBounds.bottom + padding

            canvas.drawRect(bgLeft, bgTop, bgRight, bgBottom, backgroundPaint)
            canvas.drawText(label, textX, textY, labelPaint)
        }
    }

    /**
     * Flips a [RectF] horizontally across the vertical axis of the image.
     *
     * Useful when rendering bounding boxes for front-facing camera input, which is typically
     * mirrored by default.
     *
     * @param rect The original bounding box in image coordinates.
     * @param imageWidth The width of the image used to compute the mirrored positions.
     *
     * @return A new [RectF] representing the horizontally flipped bounding box.
     */
    private fun flipHorizontally(rect: RectF, imageWidth: Int) = RectF(
        imageWidth - rect.right,
        rect.top,
        imageWidth - rect.left,
        rect.bottom
    )

    /**
     * Maps a bounding box from image coordinates to view coordinates, preserving aspect ratio.
     *
     * This method scales and offsets the input [src] rectangle so that it aligns correctly with the
     * dimensions of the view while preserving the image's aspect ratio. The mapped coordinates are
     * written to the [out] rectangle.
     *
     * @param src The bounding box in the coordinate space of the original image.
     * @param out The output rectangle to receive the transformed coordinates in view space.
     */
    private fun mapBox(src: RectF, out: RectF) {
        val imageAspect = imageWidth.toFloat() / imageHeight
        val viewAspect = width.toFloat() / height

        val scale: Float
        val dx: Float
        val dy: Float

        if (imageAspect > viewAspect) {
            scale = height.toFloat() / imageHeight
            dx = (width - imageWidth * scale) / 2f
            dy = 0f
        } else {
            scale = width.toFloat() / imageWidth
            dx = 0f
            dy = (height - imageHeight * scale) / 2f
        }

        out.set(
            src.left * scale + dx,
            src.top * scale + dy,
            src.right * scale + dx,
            src.bottom * scale + dy
        )
    }

}
