package com.tractorfieldtrip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tractorfieldtrip.databinding.FragmentGameOverBinding
import com.tractorfieldtrip.game.GameOutcome

class GameOverFragment : Fragment() {

    private var _binding: FragmentGameOverBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameOverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val level = requireArguments().getInt(ARG_LEVEL)
        val outcome = requireArguments().getString(ARG_OUTCOME)?.let { name ->
            GameOutcome.entries.firstOrNull { it.name == name }
        } ?: GameOutcome.LOST_LIVES

        when (outcome) {
            GameOutcome.LOST_FUEL -> {
                binding.ivDecoration.setImageResource(R.drawable.item_fuel)
                binding.ivTitle.setImageResource(R.drawable.text_out_of_fuel)
            }
            GameOutcome.LOST_LIVES, GameOutcome.CLEARED -> {
                binding.ivDecoration.setImageResource(R.drawable.ic_heart_big)
                binding.ivTitle.setImageResource(R.drawable.text_out_of_lives)
            }
        }

        binding.btnRetry.setOnClickListener {
            context?.let { SoundManager.play(it, SoundManager.Effect.CLICK) }
            navigateToGame(level)
        }
        binding.btnMenu.setOnClickListener {
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
        private const val ARG_OUTCOME = "outcome"

        fun newInstance(level: Int, outcome: GameOutcome): GameOverFragment =
            GameOverFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_LEVEL, level)
                    putString(ARG_OUTCOME, outcome.name)
                }
            }
    }
}
