package com.tractorfieldtrip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.tractorfieldtrip.databinding.FragmentLevelSelectBinding

class LevelSelectFragment : Fragment() {

    private var _binding: FragmentLevelSelectBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLevelSelectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener {
            playBack()
            parentFragmentManager.popBackStack()
        }

        val context = requireContext()
        val levels = (1..GameProgress.TOTAL_LEVELS).map { level ->
            LevelTile(
                number = level,
                unlocked = GameProgress.isLevelUnlocked(context, level),
                stars = GameProgress.getLevelStars(context, level)
            )
        }

        binding.rvLevels.layoutManager = GridLayoutManager(context, GRID_COLUMNS)
        binding.rvLevels.adapter = LevelTileAdapter(levels) { tile ->
            if (tile.unlocked) {
                SoundManager.play(context, SoundManager.Effect.CLICK)
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, GameFragment.newInstance(tile.number))
                    .addToBackStack(null)
                    .commit()
            } else {
                SoundManager.play(context, SoundManager.Effect.NOTIFY, volume = 0.75f)
                Toast.makeText(context, R.string.level_locked_toast, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun playBack() {
        context?.let { SoundManager.play(it, SoundManager.Effect.BUTTON_BACK) }
    }

    override fun onResume() {
        super.onResume()
        binding.tvCoinCount.text = GameProgress.formatCoins(
            GameProgress.getCoins(requireContext())
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val GRID_COLUMNS = 5
    }
}
