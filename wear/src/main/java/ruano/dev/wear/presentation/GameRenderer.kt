package ruano.dev.wear.presentation

import android.graphics.Paint as AndroidPaint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlin.math.sin
import ruano.dev.wear.domain.model.Coin
import ruano.dev.wear.domain.model.GameState
import ruano.dev.wear.domain.model.Obstacle
import ruano.dev.wear.domain.model.Player
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Paleta de colores centralizada del juego */
data class GameColors(
    val sky1: Color = Color(0xFF0D1B4A),
    val sky2: Color = Color(0xFF1A237E),
    val ground: Color = Color(0xFF3E2723),
    val coin: Color = Color(0xFFFFD54F),
    val obstacle: Color = Color(0xFFC62828),
    val heart: Color = Color(0xFFE53935),
    val text: Color = Color.White
)

/** GameRenderer: SOLO dibuja. No toca la lógica de juego. */
object GameRenderer {

    private val COLORS = GameColors()
    private const val FLOOR_Y = 160f + 20f

    fun draw(canvas: Canvas, size: Size, state: GameState, frame: Long) {
        drawBackground(canvas, size)
        drawGround(canvas, size)
        drawCoins(canvas, state.coins, frame)
        drawObstacles(canvas, state.obstacles)
        drawPlayer(canvas, state.player, frame)
        drawHUD(canvas, size, state)
    }

    private fun drawBackground(canvas: Canvas, size: Size) {
        val paint = Paint().apply {
            shader = LinearGradientShader(
                from = Offset(0f, 0f), to = Offset(0f, size.height),
                colors = listOf(COLORS.sky1, COLORS.sky2)
            )
        }
        canvas.drawRect(Rect(Offset.Zero, size), paint)
    }

    private fun drawGround(canvas: Canvas, size: Size) {
        val paint = Paint().apply { color = COLORS.ground }
        canvas.drawRect(
            Rect(Offset(0f, FLOOR_Y), Offset(size.width, FLOOR_Y + 40f)),
            paint
        )
    }

    private fun drawCoins(canvas: Canvas, coins: List<Coin>, frame: Long) {
        val paint = Paint().apply { color = COLORS.coin }
        val bob = sin(frame * 0.2f) * 4f
        coins.filterNot { it.collected }.forEach { c ->
            canvas.drawRect(
                Rect(c.x - 6f, c.y - 6f + bob, c.x + 6f, c.y + 6f + bob),
                paint
            )
        }
    }

    private fun drawObstacles(canvas: Canvas, obstacles: List<Obstacle>) {
        val paint = Paint().apply { color = COLORS.obstacle }
        obstacles.forEach { o ->
            canvas.drawRect(
                Rect(o.x, FLOOR_Y - o.height, o.x + o.width, FLOOR_Y),
                paint
            )
        }
    }

    private fun drawPlayer(canvas: Canvas, player: Player, frame: Long) {
        val alpha = if (player.isInvincible && (frame / 4) % 2 == 0L) 0.3f else 1f
        val yPos = player.y

        val bodyPaint = Paint().apply {
            color = Color(0xFFE65100).copy(alpha = alpha)
        }
        canvas.drawRect(Rect(player.x - 6f, yPos - 10f, player.x + 14f, yPos + 14f), bodyPaint)

        val helmetPaint = Paint().apply { color = Color(0xFF1A237E).copy(alpha = alpha) }
        canvas.drawRect(Rect(player.x - 5f, yPos - 24f, player.x + 13f, yPos - 14f), helmetPaint)
    }

    private fun drawHUD(canvas: Canvas, size: Size, state: GameState) {
        val cx = size.width / 2f
        drawCenteredText(canvas, getSystemTime(), cx, 22f, 14.sp)
        drawCenteredText(canvas, "${state.score} pts", cx, size.height - 14f, 11.sp)
        repeat(state.lives) { i ->
            drawHeart(canvas, 8f + i * 16f, 36f)
        }
    }

    /** Dibuja texto centrado usando el Canvas nativo de Android (bridge desde Compose) */
    private fun drawCenteredText(
        canvas: Canvas, text: String, x: Float, y: Float, size: TextUnit
    ) {
        val nativePaint = AndroidPaint().apply {
            color = COLORS.text.toArgb()
            textSize = size.value * 3.2f // aprox. sp -> px en densidad estándar
            textAlign = AndroidPaint.Align.CENTER
            isAntiAlias = true
        }
        canvas.nativeCanvas.drawText(text, x, y, nativePaint)
    }

    private fun drawHeart(canvas: Canvas, x: Float, y: Float) {
        val paint = Paint().apply { color = COLORS.heart }
        // Simplificación: corazón como rombo/rect pequeño
        canvas.drawRect(Rect(x - 5f, y - 5f, x + 5f, y + 5f), paint)
    }

    private fun getSystemTime(): String =
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
}