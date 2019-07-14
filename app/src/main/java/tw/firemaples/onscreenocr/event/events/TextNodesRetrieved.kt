package tw.firemaples.onscreenocr.event.events

import tw.firemaples.onscreenocr.detect.TextNode
import tw.firemaples.onscreenocr.event.BaseEvent

class TextNodesRetrieved(val textNodes: List<TextNode>) : BaseEvent