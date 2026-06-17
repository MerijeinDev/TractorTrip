package farmyard.tractortrip.lab

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.fragment.app.Fragment
import farmyard.tractortrip.lab.databinding.FragmentNoInternetBinding

class NoInternetFragment : Fragment() {

    private var _binding: FragmentNoInternetBinding? = null
    private val binding get() = _binding!!

    private var spinAnimator: ObjectAnimator? = null

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
            startSpin()
            (parentFragment as? NoInternetHost)?.onRetryConnection()
            binding.btnRetry.postDelayed({ stopSpin() }, SPIN_FEEDBACK_MS)
        }
    }

    private fun startSpin() {
        spinAnimator?.cancel()
        val target = _binding?.ivRetryIcon ?: return
        spinAnimator = ObjectAnimator.ofFloat(target, View.ROTATION, 0f, 360f).apply {
            duration = SPIN_DURATION_MS
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            start()
        }
    }

    private fun stopSpin() {
        spinAnimator?.cancel()
        spinAnimator = null
        _binding?.ivRetryIcon?.rotation = 0f
    }

    override fun onDestroyView() {
        spinAnimator?.cancel()
        spinAnimator = null
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val SPIN_DURATION_MS = 800L
        private const val SPIN_FEEDBACK_MS = 1200L
    }
}
