package tw.firemaples.onscreenocr.ui.permissions

import android.app.Activity
import android.content.Context
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import tw.firemaples.onscreenocr.R
import tw.firemaples.onscreenocr.floatingviews.ViewHolderService
import tw.firemaples.onscreenocr.screenshot.ScreenshotManager

class PermissionCaptureScreenFragment : Fragment(R.layout.permission_capture_screen_fragment) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setViews(view)

        if (ScreenshotManager.isGranted) {
            startService()
        }
    }

    private fun setViews(view: View) {
        view.findViewById<View>(R.id.bt_requestPermission).setOnClickListener {
            requestMediaProject()
        }
    }

    private fun requestMediaProject() {
        val manager =
            requireContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        resultLauncher.launch(manager.createScreenCaptureIntent())
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val intent = it.data
            if (it.resultCode == Activity.RESULT_OK && intent != null) {
                ScreenshotManager.onMediaProjectionGranted(intent)
                startService()
            }
        }

    private fun startService() {
        ViewHolderService.showViews(requireActivity())
        requireActivity().finishAffinity()
    }
}
