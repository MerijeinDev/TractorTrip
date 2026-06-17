package com.tractorfieldtrip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.tractorfieldtrip.databinding.FragmentLevelCompleteBinding
import com.tractorfieldtrip.game.GameResult

class LevelCompleteFragment : Fragment() {

    private var _binding: FragmentLevelCompleteBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLevelCompleteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val level = requireArguments().getInt(ARG_LEVEL)
        val stars = requireArguments().getInt(ARG_STARS)
        val materialsPct = requireArguments().getInt(ARG_MATERIALS_PCT)
        val fuelLeftPct = requireArguments().getInt(ARG_FUEL_LEFT_PCT)
        val coinsEarned = requireArguments().getInt(ARG_COINS_EARNED)

        val context = requireContext()
        GameProgress.unlockLevel(context, level + 1)
        GameProgress.setLevelStars(context, level, stars)
        GameProgress.addCoins(context, coinsEarned)
        val livesLeft = requireArguments().getInt(ARG_LIVES_LEFT)
        val hadDualTractors = requireArguments().getBoolean(ARG_HAD_DUAL)
        AchievementCatalog.checkAfterLevelComplete(
            context, level, livesLeft, fuelLeftPct, materialsPct, hadDualTractors
        )

        binding.ivStarLeft.setImageResource(
            if (stars >= 1) R.drawable.sprite_star else R.drawable.ic_star_empty
        )
        binding.ivStarCenter.setImageResource(
            if (stars >= 2) R.drawable.sprite_star else R.drawable.ic_star_empty
        )
        binding.ivStarRight.setImageResource(
            if (stars >= 3) R.drawable.sprite_star else R.drawable.ic_star_empty
        )
        binding.tvCoinReward.text = GameProgress.formatCoins(coinsEarned)

        binding.btnNextLevel.setOnClickListener {
            SoundManager.play(context, SoundManager.Effect.CLICK)
            val nextLevel = (level + 1).coerceAtMost(GameProgress.TOTAL_LEVELS)
            if (GameProgress.isLevelUnlocked(context, nextLevel)) {
                navigateToGame(nextLevel)
            } else {
                navigateToLevelSelect()
            }
        }

        binding.btnHome.setOnClickListener {
            SoundManager.play(context, SoundManager.Effect.BUTTON_BACK)
            navigateToMainMenuClearingBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_LEVEL = "level"
        private const val ARG_STARS = "stars"
        private const val ARG_MATERIALS_PCT = "materials_pct"
        private const val ARG_FUEL_LEFT_PCT = "fuel_left_pct"
        private const val ARG_COINS_EARNED = "coins_earned"
        private const val ARG_LIVES_LEFT = "lives_left"
        private const val ARG_HAD_DUAL = "had_dual"

        fun newInstance(level: Int, result: GameResult): LevelCompleteFragment =
            LevelCompleteFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_LEVEL, level)
                    putInt(ARG_STARS, result.stars)
                    putInt(ARG_MATERIALS_PCT, result.materialsPct)
                    putInt(ARG_FUEL_LEFT_PCT, result.fuelLeftPct)
                    putInt(ARG_COINS_EARNED, result.coinsEarned)
                    putInt(ARG_LIVES_LEFT, result.livesLeft)
                    putBoolean(ARG_HAD_DUAL, result.hadDualTractors)
                }
            }
    }
}
