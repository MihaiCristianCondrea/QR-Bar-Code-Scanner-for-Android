package com.d4rk.qrcodescanner.plus.ui.components.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.d4rk.qrcodescanner.plus.R

class BarcodeOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = resources.getDimension(R.dimen.barcode_overlay_stroke_width)
        color = ContextCompat.getColor(context, R.color.overlay_color)
    }

    private val path = Path()

    private var overlays: List<BarcodeShape> = emptyList()
    private var imageWidth: Int = 0
    private var imageHeight: Int = 0
    private var isImageFlipped: Boolean = false
    private var scaleFactorX: Float = 1f
    private var scaleFactorY: Float = 1f

    fun setImageSourceInfo(width: Int, height: Int, rotation: Int, flipped: Boolean) {
        if (rotation == 0 || rotation == 180) {
            imageWidth = width
            imageHeight = height
        } else {
            imageWidth = height
            imageHeight = width
        }
        isImageFlipped = flipped
        invalidate()
    }

    fun update(overlays: List<BarcodeShape>) {
        this.overlays = overlays
        invalidate()
    }

    fun clear() {
        overlays = emptyList()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (imageWidth == 0 || imageHeight == 0) {
            return
        }
        scaleFactorX = width.toFloat() / imageWidth.toFloat()
        scaleFactorY = height.toFloat() / imageHeight.toFloat()
        overlays.forEach { overlay ->
            drawOverlay(canvas, overlay)
        }
    }

    private fun drawOverlay(canvas: Canvas, overlay: BarcodeShape) {
        val points = overlay.cornerPoints
        if (points.isEmpty()) {
            drawBoundingBox(canvas, overlay.boundingBox)
            return
        }
        path.reset()
        points.forEachIndexed { index, point ->
            val translatedX = translateX(point.x)
            val translatedY = translateY(point.y)
            if (index == 0) {
                path.moveTo(translatedX, translatedY)
            } else {
                path.lineTo(translatedX, translatedY)
            }
        }
        path.close()
        canvas.drawPath(path, borderPaint)
    }

    private fun drawBoundingBox(canvas: Canvas, rect: Rect) {
        val translated = RectF(
            translateX(rect.left.toFloat()),
            translateY(rect.top.toFloat()),
            translateX(rect.right.toFloat()),
            translateY(rect.bottom.toFloat()),
        )
        canvas.drawRect(translated, borderPaint)
    }

    private fun translateX(x: Float): Float {
        val scaledX = x * scaleFactorX
        return if (isImageFlipped) {
            width - scaledX
        } else {
            scaledX
        }
    }

    private fun translateY(y: Float): Float {
        return y * scaleFactorY
    }

    data class BarcodeShape(
        val boundingBox: Rect,
        val cornerPoints: List<PointF>,
    )
}
