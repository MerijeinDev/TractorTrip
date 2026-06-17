package farmyard.tractortrip.lab

import android.animation.ObjectAnimator
import android.net.ConnectivityManager
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

class SplashFragment : Fragment(), NoInternetHost, NotificationPermissionHost {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    private val handler = Handler(Looper.getMainLooper())
    private var progressAnimator: ObjectAnimator? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
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
            proceedAfterConnection()
        } else {
            showNoInternetPopup()
        }
    }

    private fun proceedAfterConnection() {
        if (_binding == null || finished) return
        if (GameProgress.isNotificationPromptShown(requireContext())) {
            navigateNext()
        } else {
            showNotificationPrompt()
        }
    }

    private fun showNotificationPrompt() {
        if (childFragmentManager.findFragmentByTag(TAG_NOTIFY_PROMPT) != null) return
        binding.splashOverlay.isVisible = true
        childFragmentManager.beginTransaction()
            .add(R.id.splash_overlay, NotificationPermissionFragment(), TAG_NOTIFY_PROMPT)
            .commit()
    }

    override fun onNotificationPromptResolved() {
        childFragmentManager.findFragmentByTag(TAG_NOTIFY_PROMPT)?.let { fragment ->
            childFragmentManager.beginTransaction().remove(fragment).commit()
        }
        binding.splashOverlay.isVisible = false
        navigateNext()
    }

    private fun showNoInternetPopup() {
        if (childFragmentManager.findFragmentByTag(TAG_NO_INTERNET) != null) return
        childFragmentManager.beginTransaction()
            .add(R.id.splash_overlay, NoInternetFragment(), TAG_NO_INTERNET)
            .commit()
        startNetworkWatcher()
    }

    private fun startNetworkWatcher() {
        if (networkCallback != null) return
        val ctx = context?.applicationContext ?: return
        networkCallback = NetworkUtils.observeOnline(ctx) {
            handler.post { onRetryConnection() }
        }
    }

    private fun stopNetworkWatcher() {
        val cb = networkCallback ?: return
        context?.applicationContext?.let { NetworkUtils.stopObserving(it, cb) }
        networkCallback = null
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
        if (_binding == null || finished) return
        if (NetworkUtils.isOnline(requireContext())) {
            stopNetworkWatcher()
            childFragmentManager.findFragmentByTag(TAG_NO_INTERNET)?.let { fragment ->
                childFragmentManager.beginTransaction().remove(fragment).commit()
            }
            binding.splashOverlay.isVisible = false
            proceedAfterConnection()
        }
    }

    override fun onDestroyView() {
        stopNetworkWatcher()
        handler.removeCallbacksAndMessages(null)
        progressAnimator?.cancel()
        progressAnimator = null
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val LOADING_MS = 1800L
        private const val TAG_NO_INTERNET = "no_internet"
        private const val TAG_NOTIFY_PROMPT = "notify_prompt"
    }
}