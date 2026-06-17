package farmyard.tractortrip.lab

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class OutlinedTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var strokeWidthPx: Float = 0f
    private var strokeColorTop: Int = 0
    private var strokeColorBottom: Int = 0

    init {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.OutlinedTextView)
        try {
            strokeWidthPx = ta.getDimension(R.styleable.OutlinedTextView_outlineStrokeWidth, 0f)
            strokeColorTop = ta.getColor(R.styleable.OutlinedTextView_outlineStrokeColorTop, 0)
            strokeColorBottom = ta.getColor(R.styleable.OutlinedTextView_outlineStrokeColorBottom, 0)
        } finally {
            ta.recycle()
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (strokeWidthPx > 0f) {
            val p = paint
            val savedStyle = p.style
            val savedStrokeWidth = p.strokeWidth
            val savedShader = p.shader
            val savedJoin = p.strokeJoin

            p.style = Paint.Style.STROKE
            p.strokeWidth = strokeWidthPx
            p.strokeJoin = Paint.Join.ROUND
            p.shader = LinearGradient(
                0f, 0f, width.toFloat(), 0f,
                strokeColorTop, strokeColorBottom,
                Shader.TileMode.CLAMP,
            )
            super.onDraw(canvas)

            p.style = savedStyle
            p.strokeWidth = savedStrokeWidth
            p.shader = savedShader
            p.strokeJoin = savedJoin
        }
        super.onDraw(canvas)
    }
}
