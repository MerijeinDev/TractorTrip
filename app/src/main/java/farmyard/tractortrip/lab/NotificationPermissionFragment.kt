package farmyard.tractortrip.lab

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import farmyard.tractortrip.lab.databinding.FragmentNotificationPermissionBinding

class NotificationPermissionFragment : Fragment() {

    private var _binding: FragmentNotificationPermissionBinding? = null
    private val binding get() = _binding!!

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> resolve() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationPermissionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSkip.setOnClickListener {
            context?.let { SoundManager.play(it, SoundManager.Effect.CLICK) }
            resolve()
        }
        binding.btnAccept.setOnClickListener {
            context?.let { SoundManager.play(it, SoundManager.Effect.CLICK) }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                resolve()
            }
        }
    }

    private fun resolve() {
        context?.let { GameProgress.setNotificationPromptShown(it) }
        (parentFragment as? NotificationPermissionHost)?.onNotificationPromptResolved()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
