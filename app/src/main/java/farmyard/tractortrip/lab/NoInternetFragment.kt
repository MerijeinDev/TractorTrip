package farmyard.tractortrip.lab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import farmyard.tractortrip.lab.databinding.FragmentNoInternetBinding

class NoInternetFragment : Fragment() {

    private var _binding: FragmentNoInternetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoInternetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnRetry.setOnClickListener {
            context?.let { SoundManager.play(it, SoundManager.Effect.CLICK) }
            (parentFragment as? NoInternetHost)?.onRetryConnection()
        }
        binding.btnOffline.setOnClickListener {
            context?.let { SoundManager.play(it, SoundManager.Effect.CLICK) }
            (parentFragment as? NoInternetHost)?.onContinueOffline()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
