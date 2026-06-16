package com.tractorfieldtrip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.tractorfieldtrip.databinding.FragmentAchievementsBinding

class AchievementsFragment : Fragment() {

    private var _binding: FragmentAchievementsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAchievementsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener {
            context?.let { SoundManager.play(it, SoundManager.Effect.BUTTON_BACK) }
            parentFragmentManager.popBackStack()
        }

        val context = requireContext()
        val items = AchievementCatalog.all.map { achievement ->
            AchievementItem(
                achievement = achievement,
                unlocked = GameProgress.isAchievementUnlocked(context, achievement.id)
            )
        }
        binding.rvAchievements.layoutManager = LinearLayoutManager(context)
        binding.rvAchievements.adapter = AchievementAdapter(items)
        binding.tvPlaceholder.isVisible = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class AchievementItem(
    val achievement: Achievement,
    val unlocked: Boolean
)
