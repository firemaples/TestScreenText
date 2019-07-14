package tw.firemaples.onscreenocr.floatingviews.screencrop

import android.content.Context
import android.graphics.Rect
import android.view.WindowManager
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.detect.TextNode
import tw.firemaples.onscreenocr.event.EventUtil
import tw.firemaples.onscreenocr.event.events.RetrieveTextNodes
import tw.firemaples.onscreenocr.event.events.TextNodesRetrieved
import tw.firemaples.onscreenocr.floatingviews.FloatingView
import tw.firemaples.onscreenocr.utils.SettingUtil
import tw.firemaples.onscreenocr.views.AreaSelectionView
import tw.firemaples.onscreenocr.views.FadeOutHelpTextView
import tw.firemaples.onscreenocr.views.OnAreaSelectionViewCallback
import tw.firemaples.onscreenocr.views.ProgressBorderView

/**
 * Created by firemaples on 21/10/2016.
 */

class DrawAreaView(context: Context) : FloatingView(context) {
    val areaSelectionView: AreaSelectionView = rootView.findViewById(R.id.view_areaSelectionView)
    private val progressBorderView: ProgressBorderView = rootView
            .findViewById(R.id.view_progressBorder)
    private val helpTextView: FadeOutHelpTextView = rootView.findViewById(R.id.view_helpText)

    init {
        areaSelectionView.helpTextView = helpTextView
    }

    private val textNodes = mutableListOf<TextNode>()
    public var callback: OnDrawAreaCallback? = null

    override fun getLayoutId(): Int = R.layout.view_draw_area

    override fun fullScreenMode(): Boolean = true

    override fun getLayoutSize(): Int = WindowManager.LayoutParams.MATCH_PARENT

    override fun attachToWindow() {
        super.attachToWindow()
        progressBorderView.start()

        areaSelectionView.callback = object : OnAreaSelectionViewCallback {
            override fun onFixedAreaTapped(rect: Rect) {
                textNodes.firstOrNull { it.bound == rect }?.also {
                    callback?.onTextNodeClicked(it)
                }
            }

            override fun onAreaSelected(rect: Rect) {
                callback?.onAreaSelected(rect)
            }

        }

        if (SettingUtil.isRememberLastSelection) {
            areaSelectionView.setSelectedBox(SettingUtil.lastSelectedArea)
        }

        EventUtil.register(this)
        EventUtil.post(RetrieveTextNodes())
    }

    override fun detachFromWindow() {
        EventUtil.unregister(this)
        areaSelectionView.callback = null
        areaSelectionView.clear()
        progressBorderView.stop()
        super.detachFromWindow()
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTextNodesRetrieved(event: TextNodesRetrieved) {
        if (isAttached) {
            textNodes.clear()
            textNodes.addAll(event.textNodes)
            areaSelectionView.setFixedBoxList(event.textNodes.map { it.bound })
        }
    }
}

interface OnDrawAreaCallback {
    fun onTextNodeClicked(node: TextNode)
    fun onAreaSelected(selectedArea: Rect)
}
