package tw.firemaples.onscreenocr.ui.permissions

import android.app.Activity
import android.content.Context
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.screenshot.ScreenshotManager
import tw.firemaples.onscreenocr.ui.BaseFragment
import kotlin.reflect.KClass

class PermissionCaptureScreenFragment : BaseFragment<PermissionCaptureScreenViewModel>() {

    override val layoutId: Int
        get() = R.layout.permission_capture_screen_fragment

    override val vmClass: KClass<PermissionCaptureScreenViewModel>
        get() = PermissionCaptureScreenViewModel::class

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (ScreenshotManager.isGranted) {
            startService()
        } else {
            requestMediaProject()
        }
    }

    private fun requestMediaProject() {
        val manager = requireContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        resultLauncher.launch(manager.createScreenCaptureIntent())
    }

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val intent = it.data
        if (it.resultCode == Activity.RESULT_OK && intent != null) {
            ScreenshotManager.onMediaProjectionGranted(intent)
            startService()
        }
    }

    private fun startService() {

    }
}
