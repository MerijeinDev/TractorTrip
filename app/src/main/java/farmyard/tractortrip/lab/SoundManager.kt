package farmyard.tractortrip.lab

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import farmyard.tractortrip.lab.game.GameOutcome
import farmyard.tractortrip.lab.game.GameResult

object SoundManager {

    enum class Effect {
        CLICK,
        BUTTON_BACK,
        COIN,
        NOTIFY,
        JUMP,
        LEVEL_CLEARED,
        LEVEL_FAILED,
        LEVEL_UP
    }

    private var soundPool: SoundPool? = null
    private val effectIds = mutableMapOf<Effect, Int>()
    private var ambientPlayer: MediaPlayer? = null
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        val appContext = context.applicationContext
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(8)
            .setAudioAttributes(attrs)
            .build()
        val pool = soundPool ?: return
        effectIds[Effect.CLICK] = pool.load(appContext, R.raw.click, 1)
        effectIds[Effect.BUTTON_BACK] = pool.load(appContext, R.raw.button_back, 1)
        effectIds[Effect.COIN] = pool.load(appContext, R.raw.coin, 1)
        effectIds[Effect.NOTIFY] = pool.load(appContext, R.raw.notify, 1)
        effectIds[Effect.JUMP] = pool.load(appContext, R.raw.jump, 1)
        effectIds[Effect.LEVEL_CLEARED] = pool.load(appContext, R.raw.level_cleared, 1)
        effectIds[Effect.LEVEL_FAILED] = pool.load(appContext, R.raw.level_failed, 1)
        effectIds[Effect.LEVEL_UP] = pool.load(appContext, R.raw.level_up, 1)
        initialized = true
    }

    fun play(context: Context, effect: Effect, volume: Float = 1f) {
        if (!GameProgress.isSoundEnabled(context)) return
        val id = effectIds[effect] ?: return
        soundPool?.play(id, volume, volume, 1, 0, 1f)
    }

    fun playOutcome(context: Context, result: GameResult) {
        when (result.outcome()) {
            GameOutcome.CLEARED -> {
                play(context, Effect.LEVEL_CLEARED)
                if (result.coinsEarned > 0) {
                    play(context, Effect.COIN, volume = 0.8f)
                }
            }
            GameOutcome.LOST_LIVES, GameOutcome.LOST_FUEL -> play(context, Effect.LEVEL_FAILED)
        }
    }

    fun startAmbient(context: Context) {
        if (!GameProgress.isSoundEnabled(context)) return
        stopAmbient()
        val player = MediaPlayer.create(context.applicationContext, R.raw.ambient) ?: return
        player.isLooping = true
        player.setVolume(0.25f, 0.25f)
        player.start()
        ambientPlayer = player
    }

    fun stopAmbient() {
        ambientPlayer?.run {
            if (isPlaying) stop()
            release()
        }
        ambientPlayer = null
    }

    fun release() {
        stopAmbient()
        soundPool?.release()
        soundPool = null
        effectIds.clear()
        initialized = false
    }
}
