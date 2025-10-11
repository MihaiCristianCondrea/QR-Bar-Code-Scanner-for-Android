package com.d4rk.qrcodescanner.plus.ui.components.views

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import com.d4rk.qrcodescanner.plus.R
import com.google.zxing.Result
import kotlin.math.max

class ResultPointsView : View {
    private val pointsPaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.BLUE
        strokeWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            8f,
            Resources.getSystem().displayMetrics
        )
        strokeCap = Paint.Cap.ROUND
    }
    private var resultPoints = floatArrayOf()
    private var rect = RectF()

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        0
    )

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        context?.obtainStyledAttributes(attrs, R.styleable.ResultPointsView)?.apply {
            pointsPaint.color = getColor(
                R.styleable.ResultPointsView_resultPointColor,
                ContextCompat.getColor(context, R.color.colorAccent)
            )
            pointsPaint.strokeWidth = getDimension(
                R.styleable.ResultPointsView_resultPointSize, pointsPaint.strokeWidth
            )
            recycle()
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawPoints(resultPoints, pointsPaint)
    }

    fun showResult(result: Result, imageWidth: Int, imageHeight: Int, imageRotation: Int) {
        val localMatrix = createMatrix(imageWidth.toFloat(), imageHeight.toFloat(), imageRotation)
        resultPoints = result.resultPoints.flatMap { listOf(it.x, it.y) }.toFloatArray()
        localMatrix.mapPoints(resultPoints)
        postInvalidate()
    }

    private fun createMatrix(imageWidth: Float, imageHeight: Float, imageRotation: Int) =
        Matrix().apply {
            preTranslate((width - imageWidth) / 2f, (height - imageHeight) / 2f)
            preRotate(imageRotation.toFloat(), imageWidth / 2f, imageHeight / 2f)
            val wScale: Float
            val hScale: Float
            if (imageRotation % 180 == 0) {
                wScale = width.toFloat() / imageWidth
                hScale = height.toFloat() / imageHeight
            } else {
                wScale = height.toFloat() / imageWidth
                hScale = width.toFloat() / imageHeight

            }
            val scale = max(wScale, hScale)
            preScale(scale, scale, imageWidth / 2f, imageHeight / 2f)
        }
}