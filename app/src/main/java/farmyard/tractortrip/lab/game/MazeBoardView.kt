package farmyard.tractortrip.lab.game

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import farmyard.tractortrip.lab.GameProgress
import farmyard.tractortrip.lab.R
import farmyard.tractortrip.lab.TractorCatalog
import kotlin.math.min

class MazeBoardView(
    context: Context,
    private val levelNumber: Int,
    private val listener: GameListener
) : View(context) {

    private val perks = TractorCatalog.perksFor(GameProgress.getSelectedSkinId(context))
    private val level = LevelCatalog.getLevel(levelNumber)
    private val useDual = levelNumber >= perks.dualTractorFromLevel() && level.secondStartCell != null
    private val engine = GameEngine(level, perks, useDual)

    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.caution_yellow)
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    private val wallBitmap: Bitmap? = BitmapFactory.decodeResource(resources, R.drawable.tile_stone)
    private val entityBitmaps: Map<EntityType, Bitmap?> = mapOf(
        EntityType.CRATE to BitmapFactory.decodeResource(resources, R.drawable.item_crate),
        EntityType.SACK to BitmapFactory.decodeResource(resources, R.drawable.item_sack),
        EntityType.BRICK to BitmapFactory.decodeResource(resources, R.drawable.item_brick),
        EntityType.FUEL to BitmapFactory.decodeResource(resources, R.drawable.item_fuel),
        EntityType.CRANE to BitmapFactory.decodeResource(resources, R.drawable.obstacle_crane),
        EntityType.PIT to BitmapFactory.decodeResource(resources, R.drawable.obstacle_pit),
        EntityType.BARREL to BitmapFactory.decodeResource(resources, R.drawable.obstacle_barrel)
    )
    private val blueTractorBitmaps: Map<Direction, Bitmap?> = mapOf(
        Direction.UP to BitmapFactory.decodeResource(resources, R.drawable.tractor_blue_up),
        Direction.DOWN to BitmapFactory.decodeResource(resources, R.drawable.tractor_blue_down),
        Direction.LEFT to BitmapFactory.decodeResource(resources, R.drawable.tractor_blue_left),
        Direction.RIGHT to BitmapFactory.decodeResource(resources, R.drawable.tractor_blue_right)
    )
    private val yellowTractorBitmaps: Map<Direction, Bitmap?> = mapOf(
        Direction.UP to BitmapFactory.decodeResource(resources, R.drawable.tractor_yellow_up),
        Direction.DOWN to BitmapFactory.decodeResource(resources, R.drawable.tractor_yellow_down),
        Direction.LEFT to BitmapFactory.decodeResource(resources, R.drawable.tractor_yellow_left),
        Direction.RIGHT to BitmapFactory.decodeResource(resources, R.drawable.tractor_yellow_right)
    )

    private val handler = Handler(Looper.getMainLooper())
    private val animFrom = mutableMapOf<Int, Pair<Int, Int>>()
    private val animTo = mutableMapOf<Int, Pair<Int, Int>>()
    private var moveAnimStart = 0L
    private var isAnimating = false
    private var finished = false

    private val moveRunnable = object : Runnable {
        override fun run() {
            if (finished) return
            if (engine.phase != GamePhase.RUNNING) {
                scheduleMove()
                return
            }
            if (!isAnimating) {
                engine.tractorStates().forEachIndexed { index, tractor ->
                    animFrom[index] = tractor.row to tractor.col
                }
                val moved = engine.step()
                notifyHud()
                if (!moved || engine.phase != GamePhase.RUNNING) {
                    checkEnd()
                    scheduleMove()
                    return
                }
                engine.tractorStates().forEachIndexed { index, tractor ->
                    animTo[index] = tractor.row to tractor.col
                }
                isAnimating = true
                moveAnimStart = SystemClock.uptimeMillis()
            }
            invalidate()
            scheduleMove()
        }
    }

    private val fuelRunnable = object : Runnable {
        override fun run() {
            if (!finished) {
                engine.tickFuel()
                notifyHud()
                checkEnd()
                handler.postDelayed(this, FUEL_TICK_MS)
            }
        }
    }

    private val gestureDetector = GestureDetector(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(event: MotionEvent): Boolean = true

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false
                val dx = e2.x - e1.x
                val dy = e2.y - e1.y
                if (kotlin.math.abs(dx) > kotlin.math.abs(dy)) {
                    engine.queueTurn(if (dx > 0) Direction.RIGHT else Direction.LEFT)
                    listener.onTurnInput()
                } else {
                    if (dy < 0 && engine.tractorCount > 1 && kotlin.math.abs(dy) > kotlin.math.abs(dx)) {
                        engine.switchActiveTractor()
                        listener.onTractorSwitched()
                        notifyHud()
                    } else {
                        engine.queueTurn(if (dy > 0) Direction.DOWN else Direction.UP)
                        listener.onTurnInput()
                    }
                }
                invalidate()
                return true
            }
        }
    )

    init {
        engine.onMaterialCollected = { listener.onMaterialCollected() }
        engine.onCellsMoved = { count -> listener.onDistanceTraveled(count) }
        engine.onFuelCollected = { listener.onFuelCollected() }
        engine.onCraneBlocked = {
            listener.onCraneBlocked()
            notifyHud()
        }
        engine.onDamageTaken = {
            listener.onDamageTaken()
            notifyHud()
        }
        engine.onPhaseChanged = { phase ->
            notifyHud()
            if (phase == GamePhase.CLEARED ||
                phase == GamePhase.LOST_FUEL ||
                phase == GamePhase.LOST_LIVES
            ) {
                checkEnd()
            }
        }
        engine.tractorStates().forEachIndexed { index, tractor ->
            animFrom[index] = tractor.row to tractor.col
            animTo[index] = tractor.row to tractor.col
        }
        notifyHud()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        handler.post(moveRunnable)
        handler.postDelayed(fuelRunnable, FUEL_TICK_MS)
    }

    override fun onDetachedFromWindow() {
        handler.removeCallbacks(moveRunnable)
        handler.removeCallbacks(fuelRunnable)
        super.onDetachedFromWindow()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean =
        gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width == 0 || height == 0) return

        val cellSize = min(width.toFloat() / level.width, height.toFloat() / level.height)
        val boardWidth = cellSize * level.width
        val boardHeight = cellSize * level.height
        val offsetX = (width - boardWidth) / 2f
        val offsetY = (height - boardHeight) / 2f

        for (row in 0 until level.height) {
            for (col in 0 until level.width) {
                val cell = Cell(row, col)
                val left = offsetX + col * cellSize
                val top = offsetY + row * cellSize
                val rect = RectF(left, top, left + cellSize, top + cellSize)
                if (cell in level.walls) {
                    drawBitmap(canvas, wallBitmap, rect, 0.92f)
                } else {
                    engine.entityAt(cell)?.let { entity ->
                        drawBitmap(canvas, entityBitmaps[entity], rect, 0.78f)
                    }
                }
            }
        }

        val animT = if (isAnimating) {
            val elapsed = SystemClock.uptimeMillis() - moveAnimStart
            (elapsed.toFloat() / GameEngine.MOVE_INTERVAL_MS).coerceIn(0f, 1f).also { t ->
                if (t >= 1f) isAnimating = false
            }
        } else {
            1f
        }

        engine.tractorStates().forEachIndexed { index, tractor ->
            val from = animFrom[index] ?: (tractor.row to tractor.col)
            val to = animTo[index] ?: (tractor.row to tractor.col)
            val drawRow = from.first + (to.first - from.first) * animT
            val drawCol = from.second + (to.second - from.second) * animT

            val tractorRect = RectF(
                offsetX + drawCol * cellSize + cellSize * 0.08f,
                offsetY + drawRow * cellSize + cellSize * 0.08f,
                offsetX + (drawCol + 1) * cellSize - cellSize * 0.08f,
                offsetY + (drawRow + 1) * cellSize - cellSize * 0.08f
            )
            if (index == engine.activeTractorIndex()) {
                canvas.drawOval(
                    tractorRect.left - 6f,
                    tractorRect.top - 6f,
                    tractorRect.right + 6f,
                    tractorRect.bottom + 6f,
                    highlightPaint
                )
            }
            val bitmaps = if (tractor.isSecondary) yellowTractorBitmaps else blueTractorBitmaps
            drawBitmap(canvas, bitmaps[tractor.direction], tractorRect, 0.88f)
        }
    }

    private fun drawBitmap(canvas: Canvas, bitmap: Bitmap?, rect: RectF, scale: Float) {
        if (bitmap == null) return
        val cx = rect.centerX()
        val cy = rect.centerY()
        val halfW = rect.width() * scale / 2f
        val halfH = rect.height() * scale / 2f
        canvas.drawBitmap(
            bitmap,
            null,
            RectF(cx - halfW, cy - halfH, cx + halfW, cy + halfH),
            null
        )
    }

    fun pauseGame() = engine.pause()

    fun resumeGame() = engine.resume()

    private fun scheduleMove() {
        handler.removeCallbacks(moveRunnable)
        if (!finished) {
            handler.postDelayed(moveRunnable, if (isAnimating) 16L else GameEngine.MOVE_INTERVAL_MS)
        }
    }

    private fun notifyHud() = listener.onHudUpdate(engine.hudSnapshot())

    private fun checkEnd() {
        if (finished) return
        when (engine.phase) {
            GamePhase.CLEARED, GamePhase.LOST_FUEL, GamePhase.LOST_LIVES -> {
                finished = true
                handler.removeCallbacks(moveRunnable)
                handler.removeCallbacks(fuelRunnable)
                listener.onGameFinished(engine.buildResult())
            }
            else -> Unit
        }
    }

    companion object {
        private const val FUEL_TICK_MS = 1000L
    }
}
