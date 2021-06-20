package tw.firemaples.onscreenocr.floatingviews

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import tw.firemaples.onscreenocr.R

class FloatingContainer @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    init {
        View.inflate(context, R.layout.view_floating_containter, null)
    }
}
