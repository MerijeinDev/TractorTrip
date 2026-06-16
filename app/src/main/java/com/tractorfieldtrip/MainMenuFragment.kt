package com.tractorfieldtrip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tractorfieldtrip.databinding.FragmentMainMenuBinding

class MainMenuFragment : Fragment() {

    private var _binding: FragmentMainMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnPlay.setOnClickListener {
            playClick()
            navigateTo(LevelSelectFragment())
        }
        binding.btnShop.setOnClickListener {
            playClick()
            navigateTo(TractorSelectFragment())
        }
        binding.btnAchievements.setOnClickListener {
            playClick()
            navigateTo(AchievementsFragment())
        }
        binding.btnSettingsShortcut.setOnClickListener {
            playClick()
            navigateTo(SettingsFragment())
        }
    }

    private fun playClick() {
        context?.let { SoundManager.play(it, SoundManager.Effect.CLICK) }
    }

    override fun onResume() {
        super.onResume()
        binding.tvCoinCount.text = GameProgress.formatCoins(
            GameProgress.getCoins(requireContext())
        )
    }

    private fun navigateTo(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}