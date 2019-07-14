package tw.firemaples.onscreenocr.detect

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import tw.firemaples.onscreenocr.event.EventUtil
import tw.firemaples.onscreenocr.event.events.RetrieveTextNodes
import tw.firemaples.onscreenocr.event.events.TextNodesRetrieved
import tw.firemaples.onscreenocr.utils.NotchUtil
import kotlin.system.measureTimeMillis

class TextDetectService : AccessibilityService() {
    private val logger: Logger by lazy { LoggerFactory.getLogger(TextDetectService::class.java) }

    override fun onServiceConnected() {
        super.onServiceConnected()
        logger.debug("onServiceConnected()")
        EventUtil.register(this)
    }

    override fun onInterrupt() {
        logger.debug("onInterrupt()")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        logger.debug("onAccessibilityEvent(), event: $event")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        logger.debug("onUnbind(), intent: $intent")
        EventUtil.unregister(this)
        return super.onUnbind(intent)
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    fun retrieveTextNodes(event: RetrieveTextNodes) {
        val textNodeList = retrieveTexts()
        if (textNodeList.isNotEmpty()) {
            EventUtil.post(TextNodesRetrieved(textNodeList))
        }
    }

    private fun retrieveTexts(): List<TextNode> {
        logger.debug("retrieveTexts()")

        logger.debug("rootInActiveWindow: ${rootInActiveWindow != null}, " +
                "windows: ${windows.size}, " +
                "activeWindow: ${windows.count { it.isActive }}")

        rootInActiveWindow?.also { rootNode ->
            val rootNodeBounds = Rect()
            rootNode.getBoundsInScreen(rootNodeBounds)

            val traverseNodeList = mutableListOf<AccessibilityNodeInfo>()
            val textNodeList = mutableListOf<TextNode>()

            val timeSpent = measureTimeMillis {
                traverseNodeList.add(rootNode)
                while (traverseNodeList.size > 0) {
                    val node = traverseNodeList.first()

                    if (node.childCount > 0) { //Node is a GroupView
                        for (i in 0 until node.childCount) {
                            node.getChild(i)?.also { traverseNodeList.add(it) }
                        }
                    } else {
                        val text = node.text?.toString()
                        if (!text.isNullOrBlank()) {
                            val nodeBound = Rect()
                            node.getBoundsInScreen(nodeBound)
                            if (rootNodeBounds.contains(nodeBound)) {
                                textNodeList.add(TextNode(text = text,
                                        bound = nodeBound,
                                        className = node.className.toString()))
                            }
                        }
                    }

                    traverseNodeList.remove(node)
                    node.recycle()
                }
            }

            if (NotchUtil.hasNotch) {
                val notch = NotchUtil.statusBarHeight
                textNodeList.forEach { node ->
                    node.bound.apply {
                        top -= notch
                        bottom -= notch
                    }
                }
            }

            logger.debug("${textNodeList.size} text node(s) found, spent: ${timeSpent}ms")
            logger.debug("Text nodes: ${textNodeList.joinToString()}")

            serviceInfo = serviceInfo

            return textNodeList.toList()
        }

        return emptyList()
    }
}

data class TextNode(val text: String, val bound: Rect, val className: String)