package com.tractorfieldtrip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tractorfieldtrip.databinding.FragmentOnboardingBinding

class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    private val pages: List<OnboardingPage> = listOf(
        OnboardingPage(
            backgroundRes = R.drawable.onboarding_bg_movement,
            textRes = R.string.onboarding_movement
        ),
        OnboardingPage(
            backgroundRes = R.drawable.onboarding_bg_collect,
            textRes = R.string.onboarding_collect
        ),
        OnboardingPage(
            backgroundRes = R.drawable.onboarding_bg_fuel,
            textRes = R.string.onboarding_fuel
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewPagerOnboarding.isUserInputEnabled = false
        binding.viewPagerOnboarding.adapter = OnboardingAdapter(pages) { position ->
            context?.let { SoundManager.play(it, SoundManager.Effect.CLICK) }
            if (position < pages.size - 1) {
                binding.viewPagerOnboarding.currentItem = position + 1
            } else {
                finishOnboarding()
            }
        }
    }

    private fun finishOnboarding() {
        GameProgress.setOnboardingDone(requireContext())
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, MainMenuFragment())
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
