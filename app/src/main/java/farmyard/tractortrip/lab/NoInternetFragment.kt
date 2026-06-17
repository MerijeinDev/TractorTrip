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

    private var rotateAnimator: ObjectAnimator? = null

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
            startRotation()
            (parentFragment as? NoInternetHost)?.onRetryConnection()
            binding.btnRetry.postDelayed({ stopRotation() }, ROTATION_FEEDBACK_MS)
        }
    }

    private fun startRotation() {
        rotateAnimator?.cancel()
        val target = _binding?.ivRetryIcon ?: return
        rotateAnimator = ObjectAnimator.ofFloat(target, View.ROTATION, 0f, 360f).apply {
            duration = ROTATION_DURATION_MS
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            start()
        }
    }

    private fun stopRotation() {
        rotateAnimator?.cancel()
        rotateAnimator = null
        _binding?.ivRetryIcon?.rotation = 0f
    }

    override fun onDestroyView() {
        rotateAnimator?.cancel()
        rotateAnimator = null
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ROTATION_DURATION_MS = 800L
        private const val ROTATION_FEEDBACK_MS = 1200L
    }
}
