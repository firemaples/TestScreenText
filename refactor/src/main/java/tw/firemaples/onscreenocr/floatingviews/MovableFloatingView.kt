package tw.firemaples.onscreenocr.floatingviews

import android.content.Context
import android.view.View
import tw.firemaples.onscreenocr.utils.Logger

abstract class MovableFloatingView(context: Context) : FloatingView(context) {
    private val logger: Logger by lazy { Logger(this::class) }

    private lateinit var dragView: View

    fun setDragView(view: View){
        dragView = view
    }

//    private val fromAlpha
//
//    init {
//        fromAlpha = dragView.alpha
//
//        setDragView(View())
//    }


}
