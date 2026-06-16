package com.tractorfieldtrip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tractorfieldtrip.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()

        applyMusicState(GameProgress.isMusicEnabled(context))
        applySoundState(GameProgress.isSoundEnabled(context))

        binding.btnBack.setOnClickListener {
            SoundManager.play(context, SoundManager.Effect.BUTTON_BACK)
            parentFragmentManager.popBackStack()
        }

        binding.btnMusic.setOnClickListener {
            val newState = !GameProgress.isMusicEnabled(context)
            GameProgress.setMusicEnabled(context, newState)
            SoundManager.play(context, SoundManager.Effect.CLICK)
            applyMusicState(newState)
        }

        binding.btnSound.setOnClickListener {
            val newState = !GameProgress.isSoundEnabled(context)
            GameProgress.setSoundEnabled(context, newState)
            if (newState) SoundManager.play(context, SoundManager.Effect.CLICK)
            applySoundState(newState)
        }
    }

    private fun applyMusicState(enabled: Boolean) {
        binding.ivMusicPlate.setImageResource(
            if (enabled) R.drawable.btn_settings_plate else R.drawable.btn_settings_plate_off
        )
        binding.ivMusicIcon.alpha = if (enabled) 1f else 0.45f
    }

    private fun applySoundState(enabled: Boolean) {
        binding.ivSoundPlate.setImageResource(
            if (enabled) R.drawable.btn_settings_plate else R.drawable.btn_settings_plate_off
        )
        binding.ivSoundIcon.alpha = if (enabled) 1f else 0.45f
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
