package com.tractorfieldtrip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.tractorfieldtrip.databinding.FragmentLeaderboardBinding

class LeaderboardFragment : Fragment() {

    private var _binding: FragmentLeaderboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener {
            context?.let { SoundManager.play(it, SoundManager.Effect.BUTTON_BACK) }
            parentFragmentManager.popBackStack()
        }
        refreshStats()
    }

    override fun onResume() {
        super.onResume()
        refreshStats()
    }

    private fun refreshStats() {
        val context = requireContext()
        val items = listOf(
            LeaderboardStat(
                iconRes = R.drawable.sprite_star,
                title = getString(R.string.lb_levels_completed),
                value = getString(
                    R.string.lb_fraction_format,
                    GameProgress.getLevelsCompleted(context),
                    GameProgress.TOTAL_LEVELS
                )
            ),
            LeaderboardStat(
                iconRes = R.drawable.sprite_star,
                title = getString(R.string.lb_total_stars),
                value = GameProgress.getTotalStars(context).toString()
            ),
            LeaderboardStat(
                iconRes = R.drawable.ic_award,
                title = getString(R.string.lb_achievements),
                value = getString(
                    R.string.lb_fraction_format,
                    GameProgress.getAchievementsUnlockedCount(context),
                    AchievementCatalog.all.size
                )
            ),
            LeaderboardStat(
                iconRes = R.drawable.ic_coin,
                title = getString(R.string.lb_coins),
                value = GameProgress.formatCoins(GameProgress.getCoins(context))
            ),
            LeaderboardStat(
                iconRes = R.drawable.item_fuel,
                title = getString(R.string.lb_distance),
                value = getString(
                    R.string.lb_distance_format,
                    GameProgress.getTotalDistance(context)
                )
            )
        )
        binding.rvStats.layoutManager = LinearLayoutManager(context)
        binding.rvStats.adapter = LeaderboardAdapter(items)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class LeaderboardStat(
    val iconRes: Int,
    val title: String,
    val value: String
)
