package tw.firemaples.onscreenocr.views

import android.content.Context
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatImageView
import android.util.AttributeSet
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.log.FirebaseEvent

/**
 * Created by Firemaples on 2016/3/1.
 */

class AreaSelectionView(context: Context, attrs: AttributeSet) : AppCompatImageView(context, attrs) {
    @Suppress("unused")
    private val logger: Logger by lazy { LoggerFactory.getLogger(AreaSelectionView::class.java) }

    private var drawingStartPoint: Point? = null
    private var drawingEndPoint: Point? = null

    var selectBox: Rect? = null
    val fixedBoxList = mutableListOf<Rect>()
    var resizeBase: Rect = Rect()

    private var drawingLinePaint = Paint().apply {
        isAntiAlias = true
        color = ContextCompat.getColor(context,
                R.color.captureAreaSelectionViewPaint_drawingLinePaint)
        strokeWidth = 10f
    }
    private var boxPaint = Paint().apply {
        isAntiAlias = true
        color = ContextCompat.getColor(context, R.color.captureAreaSelectionViewPaint_boxPaint)
        strokeWidth = 6f
        style = Paint.Style.STROKE
    }
    private var fixedBoxPaint = Paint().apply {
        isAntiAlias = true
        color = ContextCompat.getColor(context, R.color.captureAreaSelectionViewPaint_fixedBoxPaint)
        strokeWidth = 6f
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(10f, 20f), 0f)
    }

    var helpTextView: FadeOutHelpTextView? = null

    var callback: OnAreaSelectionViewCallback? = null

    private val onGesture = object : OnGesture {
        override fun onOneFingerTap(point: Point) {
            fixedBoxList.firstOrNull { it.contains(point.x, point.y) }?.also {
                callback?.onFixedAreaTapped(it)
            }
        }

        override fun onAreaCreationStart(startPoint: Point) {
            FirebaseEvent.logDragSelectionArea()

            selectBox = null
            helpTextView?.hasBox = false

            drawingStartPoint = startPoint
        }

        override fun onAreaCreationDragging(endPoint: Point) {
            drawingEndPoint = endPoint
            invalidate()
            helpTextView?.isDrawing = true
        }

        override fun onAreaCreationFinish(startPoint: Point, endPoint: Point) {
            val box = createNewBox(startPoint, endPoint)
            this@AreaSelectionView.selectBox = box
            helpTextView?.hasBox = true
            drawingStartPoint = null
            drawingEndPoint = null

            invalidate()

            helpTextView?.isDrawing = false
            helpTextView?.startAnim()
            callback?.onAreaSelected(box)
        }

        override fun onAreaResizeStart() {
            FirebaseEvent.logResizeSelectionArea()

            selectBox?.also {
                resizeBase = Rect(it)
            }
        }

        override fun onAreaResizing(leftDiff: Int, rightDiff: Int, topDiff: Int, bottomDiff: Int) {
            selectBox?.apply {
                left = resizeBase.left + leftDiff
                right = Math.max(left + 1, resizeBase.right + rightDiff)
                top = resizeBase.top + topDiff
                bottom = Math.max(top + 1, resizeBase.bottom + bottomDiff)

                invalidate()
                helpTextView?.isDrawing = true
            }
        }

        override fun onAreaResizeFinish() {
            helpTextView?.isDrawing = false
            helpTextView?.startAnim()
            selectBox?.also {
                callback?.onAreaSelected(it)
            }
        }
    }

    init {
        if (!isInEditMode) {
            SelectionGestureAdapter(this, onGesture)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        helpTextView?.stopAnim()
        helpTextView = null
    }

    fun clear() {
        drawingEndPoint = null
        drawingStartPoint = drawingEndPoint
        selectBox = null
        fixedBoxList.clear()
        invalidate()
    }

    fun setSelectedBox(box: Rect?) {
        if (box == null) {
            selectBox = null
            helpTextView?.hasBox = false
        } else {
            selectBox = box
            helpTextView?.hasBox = true
            helpTextView?.startAnim()
            callback?.onAreaSelected(box)
        }

        invalidate()
    }

    fun setFixedBoxList(boxList: List<Rect>) {
        fixedBoxList.clear()
        fixedBoxList.addAll(boxList)

        invalidate()
    }

    private fun createNewBox(startPoint: Point, endPoint: Point): Rect {
        val x1 = startPoint.x
        val x2 = endPoint.x
        val y1 = startPoint.y
        val y2 = endPoint.y

        val left = Math.min(x1, x2)
        val right = Math.max(x1, x2)
        val top = Math.min(y1, y2)
        val bottom = Math.max(y1, y2)

        return Rect(left, top, right, bottom)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isInEditMode) {
            canvas.save()

            fixedBoxList.forEach {
                canvas.drawRect(it, fixedBoxPaint)
            }

            val drawingStartPoint = this.drawingStartPoint
            val drawingEndPoint = this.drawingEndPoint
            if (drawingStartPoint != null && drawingEndPoint != null) {
                canvas.drawRect(createNewBox(drawingStartPoint, drawingEndPoint), drawingLinePaint)
            }

            selectBox?.also {
                canvas.drawRect(it, boxPaint)
            }

            canvas.restore()
        }
    }
}

interface OnAreaSelectionViewCallback {
    fun onFixedAreaTapped(rect: Rect)
    fun onAreaSelected(rect: Rect)
}
