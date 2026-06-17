package farmyard.tractortrip.lab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import farmyard.tractortrip.lab.databinding.FragmentPauseBinding

class PauseFragment : Fragment() {

    private var _binding: FragmentPauseBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPauseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val level = requireArguments().getInt(ARG_LEVEL)

        binding.btnContinue.setOnClickListener {
            context?.let { SoundManager.play(it, SoundManager.Effect.CLICK) }
            (parentFragment as? PauseHost)?.onPauseContinue()
            dismissChildOverlayBackStack()
        }
        binding.btnReplay.setOnClickListener {
            context?.let { SoundManager.play(it, SoundManager.Effect.CLICK) }
            navigateToGame(level)
        }
        binding.btnHome.setOnClickListener {
            context?.let { SoundManager.play(it, SoundManager.Effect.BUTTON_BACK) }
            navigateToMainMenuClearingBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_LEVEL = "level"

        fun newInstance(level: Int): PauseFragment = PauseFragment().apply {
            arguments = Bundle().apply { putInt(ARG_LEVEL, level) }
        }
    }
}

interface PauseHost {
    fun onPauseContinue()
}
