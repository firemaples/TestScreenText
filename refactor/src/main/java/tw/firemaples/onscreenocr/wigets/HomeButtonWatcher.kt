package tw.firemaples.onscreenocr.wigets

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import tw.firemaples.onscreenocr.utils.Logger

/**
 * Reference: http://stackoverflow.com/a/31340960/2906153
 */
class HomeButtonWatcher(context: Context) {
    private val logger: Logger by lazy { Logger(this::class) }

    private val filter: IntentFilter by lazy { IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS) }

    var onHomeButtonPressed: (() -> Unit)? = null

    fun startWatch() {

    }

    fun stopWatch() {

    }
}