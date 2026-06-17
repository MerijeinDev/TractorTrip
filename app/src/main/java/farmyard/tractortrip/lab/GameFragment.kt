package farmyard.tractortrip.lab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import farmyard.tractortrip.lab.databinding.FragmentGameBinding
import farmyard.tractortrip.lab.game.GameListener
import farmyard.tractortrip.lab.game.GameOutcome
import farmyard.tractortrip.lab.game.GameResult
import farmyard.tractortrip.lab.game.HudSnapshot
import farmyard.tractortrip.lab.game.MazeBoardView

class GameFragment : Fragment(), GameListener, PauseHost {

    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!

    private var level: Int = 1
    private var mazeBoard: MazeBoardView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        level = arguments?.getInt(ARG_LEVEL, 1) ?: 1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvLevelTitle.text = getString(R.string.level_format, level)

        val board = MazeBoardView(requireContext(), level, this)
        mazeBoard = board
        binding.mazeContainer.addView(
            board,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        binding.btnPause.setOnClickListener {
            context?.let { SoundManager.play(it, SoundManager.Effect.CLICK) }
            showPause()
        }
    }

    override fun onResume() {
        super.onResume()
        context?.let { SoundManager.startAmbient(it) }
    }

    override fun onPause() {
        SoundManager.stopAmbient()
        super.onPause()
    }

    override fun onHudUpdate(snapshot: HudSnapshot) {
        if (_binding == null) return
        setFuelMarker(snapshot.fuelPct)
        binding.tvCountCrates.text = getString(
            R.string.counter_format,
            snapshot.cratesCollected,
            snapshot.crateTotal
        )
        binding.tvCountSacks.text = getString(
            R.string.counter_format,
            snapshot.sacksCollected,
            snapshot.sackTotal
        )
        binding.tvCountBricks.text = getString(
            R.string.counter_format,
            snapshot.bricksCollected,
            snapshot.brickTotal
        )
        binding.ivHeart1.isVisible = snapshot.lives >= 1
        binding.ivHeart2.isVisible = snapshot.lives >= 2
        binding.ivHeart3.isVisible = snapshot.lives >= 3
    }

    override fun onGameFinished(result: GameResult) {
        val root = view ?: return
        root.post {
            if (_binding == null) return@post
            val ctx = requireContext()
            SoundManager.stopAmbient()
            SoundManager.playOutcome(ctx, result)
            val overlay = when (result.outcome()) {
                GameOutcome.CLEARED -> LevelCompleteFragment.newInstance(level, result)
                GameOutcome.LOST_LIVES, GameOutcome.LOST_FUEL ->
                    GameOverFragment.newInstance(level, result.outcome())
            }
            childFragmentManager.beginTransaction()
                .add(R.id.game_overlay, overlay)
                .commitNowAllowingStateLoss()
        }
    }

    override fun onMaterialCollected() {
        context?.let {
            SoundManager.play(it, SoundManager.Effect.COIN)
            AchievementCatalog.onFirstMaterial(it)
        }
    }

    override fun onDistanceTraveled(cellsMoved: Int) {
        val ctx = context ?: return
        GameProgress.addDistance(ctx, cellsMoved)
        AchievementCatalog.onDistanceUpdated(ctx)
    }

    override fun onFuelCollected() {
        context?.let { SoundManager.play(it, SoundManager.Effect.LEVEL_UP) }
    }

    override fun onCraneBlocked() {
        context?.let { SoundManager.play(it, SoundManager.Effect.NOTIFY) }
    }

    override fun onDamageTaken() {
        context?.let { SoundManager.play(it, SoundManager.Effect.NOTIFY, volume = 0.85f) }
    }

    override fun onTurnInput() {
        context?.let { SoundManager.play(it, SoundManager.Effect.JUMP, volume = 0.7f) }
    }

    override fun onTractorSwitched() {
        context?.let { SoundManager.play(it, SoundManager.Effect.JUMP) }
    }

    override fun onPauseContinue() {
        mazeBoard?.resumeGame()
    }

    private fun showPause() {
        mazeBoard?.pauseGame()
        if (childFragmentManager.findFragmentById(R.id.game_overlay) != null) return
        childFragmentManager.beginTransaction()
            .add(R.id.game_overlay, PauseFragment.newInstance(level))
            .addToBackStack("pause")
            .commit()
    }

    private fun setFuelMarker(pct: Float) {
        val clamped = pct.coerceIn(0f, 1f)
        val params = binding.ivFuelMarker.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams
        params.horizontalBias = clamped
        binding.ivFuelMarker.layoutParams = params
    }

    override fun onDestroyView() {
        SoundManager.stopAmbient()
        mazeBoard = null
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val ARG_LEVEL = "level"

        fun newInstance(level: Int): GameFragment = GameFragment().apply {
            arguments = Bundle().apply { putInt(ARG_LEVEL, level) }
        }
    }
}
