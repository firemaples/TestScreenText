package tw.firemaples.onscreenocr.floatingviews

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import tw.firemaples.onscreenocr.utils.Logger

abstract class FloatingView(private val context: Context) {
    private val logger: Logger by lazy { Logger(this::class) }

    private val windowManager: WindowManager by lazy { context.getSystemService(Context.WINDOW_SERVICE) as WindowManager }

    open val layoutWidth: Int = WindowManager.LayoutParams.WRAP_CONTENT
    open val layoutHeight: Int = WindowManager.LayoutParams.WRAP_CONTENT
    open val layoutFocusable: Boolean = false
    open val layoutCanMoveOutsideScreen: Boolean = false
    open val fullscreenMode: Boolean = false
    open val layoutGravity: Int = Gravity.TOP or Gravity.LEFT

    private val params: WindowManager.LayoutParams by lazy {
        val type =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE

        var flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        if (!layoutFocusable)
            flags = flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        if (layoutCanMoveOutsideScreen)
            flags = flags or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        if (fullscreenMode)
            flags = flags or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN

        WindowManager.LayoutParams(layoutWidth, layoutHeight, type, flags, PixelFormat.TRANSLUCENT).apply {
            gravity = layoutGravity
        }
    }

}
