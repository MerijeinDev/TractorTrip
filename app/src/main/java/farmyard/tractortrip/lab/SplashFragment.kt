package farmyard.tractortrip.lab

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import farmyard.tractortrip.lab.databinding.FragmentSplashBinding

class SplashFragment : Fragment(), NoInternetHost {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    private val handler = Handler(Looper.getMainLooper())
    private var progressAnimator: ObjectAnimator? = null
    private var finished = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startLoading()
    }

    private fun startLoading() {
        binding.progressLoading.progress = 0
        progressAnimator = ObjectAnimator.ofInt(binding.progressLoading, "progress", 0, 100).apply {
            duration = LOADING_MS
            interpolator = DecelerateInterpolator()
            start()
        }
        handler.postDelayed({ onLoadingFinished() }, LOADING_MS)
    }

    private fun onLoadingFinished() {
        if (finished || _binding == null) return
        if (NetworkUtils.isOnline(requireContext())) {
            navigateNext()
        } else {
            showNoInternetPopup()
        }
    }

    private fun showNoInternetPopup() {
        if (childFragmentManager.findFragmentByTag(TAG_NO_INTERNET) != null) return
        childFragmentManager.beginTransaction()
            .add(R.id.splash_overlay, NoInternetFragment(), TAG_NO_INTERNET)
            .commit()
    }

    private fun navigateNext() {
        if (finished) return
        finished = true
        context?.let { SoundManager.play(it, SoundManager.Effect.LEVEL_UP, volume = 0.6f) }
        val next = if (GameProgress.isOnboardingDone(requireContext())) {
            MainMenuFragment()
        } else {
            OnboardingFragment()
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, next)
            .commit()
    }

    override fun onRetryConnection() {
        if (NetworkUtils.isOnline(requireContext())) {
            childFragmentManager.findFragmentByTag(TAG_NO_INTERNET)?.let { fragment ->
                childFragmentManager.beginTransaction().remove(fragment).commit()
            }
            binding.splashOverlay.isVisible = false
            navigateNext()
        }
    }

    override fun onContinueOffline() {
        childFragmentManager.findFragmentByTag(TAG_NO_INTERNET)?.let { fragment ->
            childFragmentManager.beginTransaction().remove(fragment).commit()
        }
        navigateNext()
    }

    override fun onDestroyView() {
        handler.removeCallbacksAndMessages(null)
        progressAnimator?.cancel()
        progressAnimator = null
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val LOADING_MS = 1800L
        private const val TAG_NO_INTERNET = "no_internet"
    }
}