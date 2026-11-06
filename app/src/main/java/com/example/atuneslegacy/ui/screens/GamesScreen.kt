package com.example.atuneslegacy.ui.screens

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.SoundPool
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.atuneslegacy.R
import com.example.atuneslegacy.ui.components.AppShortcutMetadataDialog
import com.example.atuneslegacy.ui.theme.LocalThemeColors
import com.example.atuneslegacy.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlin.math.abs

data class Brick(
    val rect: Rect,
    val color: Color,
    var isDestroyed: Boolean = false
)

@Composable
fun BrickGameScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var soundPool by remember { mutableStateOf<SoundPool?>(null) }
    var soundWallHit by remember { mutableIntStateOf(0) }
    var soundTileHit by remember { mutableIntStateOf(0) }
    var soundLifeLost by remember { mutableIntStateOf(0) }
    var soundLose by remember { mutableIntStateOf(0) }

    DisposableEffect(context) {
        val pool = SoundPool.Builder()
            .setMaxStreams(5)
            .build()
        soundWallHit = pool.load(context, R.raw.wall_hit, 1)
        soundTileHit = pool.load(context, R.raw.tile_hit, 1)
        soundLifeLost = pool.load(context, R.raw.life_lost, 1)
        soundLose = pool.load(context, R.raw.lose, 1)
        soundPool = pool

        onDispose {
            pool.release()
        }
    }

    fun playSound(soundId: Int) {
        if (soundId != 0) {
            soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)
        }
    }

    var score by remember { mutableIntStateOf(0) }
    var level by remember { mutableIntStateOf(1) }
    var lives by remember { mutableIntStateOf(3) }
    var isRunning by remember { mutableStateOf(false) }

    var ballSpeedMultiplier by remember { mutableFloatStateOf(1f) }

    var paddleX by remember { mutableFloatStateOf(500f) }
    val paddleWidth = 140f
    val paddleHeight = 20f
    val ballRadius = 14f

    var ballX by remember { mutableFloatStateOf(500f) }
    var ballY by remember { mutableFloatStateOf(0f) }
    var ballVx by remember { mutableFloatStateOf(4f) }
    var ballVy by remember { mutableFloatStateOf(-6f) }

    var bricks by remember { mutableStateOf<List<Brick>>(emptyList()) }
    var screenWidth by remember { mutableFloatStateOf(1000f) }
    var screenHeight by remember { mutableFloatStateOf(2000f) }

    fun initBricks(w: Float, h: Float) {
        if (w <= 0f || h <= 0f) return
        val rows = 5
        val cols = 10
        val brickWidth = (w - 40f) / cols
        val brickHeight = 24f
        val colors = listOf(Color(0xFFE53935), Color(0xFFFB8C00), Color(0xFFFDD835), Color(0xFF43A047), Color(0xFF1E88E5))

        val list = mutableListOf<Brick>()
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val left = 20f + c * brickWidth
                val top = 120f + r * (brickHeight + 6f)
                list.add(Brick(Rect(left, top, left + brickWidth - 4f, top + brickHeight), colors[r % colors.size]))
            }
        }
        bricks = list
    }

    LaunchedEffect(screenWidth, screenHeight) {
        if (!isRunning) {
            initBricks(screenWidth, screenHeight)
            ballX = screenWidth / 2f
            ballY = screenHeight - 120f
            paddleX = screenWidth / 2f - paddleWidth / 2f
        }
    }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(16L)

            // Ball movement
            var nx = ballX + ballVx * ballSpeedMultiplier
            var ny = ballY + ballVy * ballSpeedMultiplier

            // Wall collisions
            if (nx - ballRadius < 0f) {
                nx = ballRadius
                ballVx = -ballVx
                playSound(soundWallHit)
            } else if (nx + ballRadius > screenWidth) {
                nx = screenWidth - ballRadius
                ballVx = -ballVx
                playSound(soundWallHit)
            }

            if (ny - ballRadius < 0f) {
                ny = ballRadius
                ballVy = -ballVy
                playSound(soundWallHit)
            }

            // Paddle collision
            val paddleTop = screenHeight - 80f
            val paddleLeft = paddleX
            val paddleRight = paddleX + paddleWidth
            val paddleBottom = paddleTop + paddleHeight

            if (ny + ballRadius >= paddleTop && ny - ballRadius <= paddleBottom && nx >= paddleLeft && nx <= paddleRight && ballVy > 0) {
                ny = paddleTop - ballRadius
                val hitFactor = (nx - (paddleLeft + paddleWidth / 2f)) / (paddleWidth / 2f)
                ballVx = hitFactor * 7f
                ballVy = -abs(ballVy)
                playSound(soundTileHit)
            }

            // Bottom loss
            if (ny > screenHeight) {
                lives--
                playSound(soundLifeLost)
                if (lives <= 0) {
                    isRunning = false
                    playSound(soundLose)
                } else {
                    nx = screenWidth / 2f
                    ny = screenHeight - 120f
                    ballVx = 4f
                    ballVy = -6f
                }
            }

            // Brick collisions
            var hitOccurred = false
            val updatedBricks = bricks.map { brick ->
                if (!brick.isDestroyed && !hitOccurred) {
                    val bRect = brick.rect
                    if (nx + ballRadius >= bRect.left && nx - ballRadius <= bRect.right && ny + ballRadius >= bRect.top && ny - ballRadius <= bRect.bottom) {
                        hitOccurred = true
                        score += 10 * level
                        playSound(soundTileHit)
                        val overlapLeft = (nx + ballRadius) - bRect.left
                        val overlapRight = bRect.right - (nx - ballRadius)
                        val overlapTop = (ny + ballRadius) - bRect.top
                        val overlapBottom = bRect.bottom - (ny - ballRadius)
                        val minOverlapX = minOf(overlapLeft, overlapRight)
                        val minOverlapY = minOf(overlapTop, overlapBottom)

                        if (minOverlapX < minOverlapY) {
                            ballVx = -ballVx
                        } else {
                            ballVy = -ballVy
                        }
                        brick.copy(isDestroyed = true)
                    } else {
                        brick
                    }
                } else {
                    brick
                }
            }

            if (hitOccurred) {
                bricks = updatedBricks
                if (bricks.all { it.isDestroyed }) {
                    level++
                    ballSpeedMultiplier += 0.2f
                    initBricks(screenWidth, screenHeight)
                    nx = screenWidth / 2f
                    ny = screenHeight - 120f
                    ballVx = 4f * ballSpeedMultiplier
                    ballVy = -6f * ballSpeedMultiplier
                }
            }

            ballX = nx
            ballY = ny
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    paddleX = (paddleX + dragAmount.x).coerceIn(0f, screenWidth - paddleWidth)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            screenWidth = size.width
            screenHeight = size.height

            // Bricks
            for (brick in bricks) {
                if (!brick.isDestroyed) {
                    drawRect(color = brick.color, topLeft = Offset(brick.rect.left, brick.rect.top), size = Size(brick.rect.width, brick.rect.height))
                }
            }

            // Paddle
            val paddleTop = screenHeight - 80f
            drawRect(
                color = Color(0xFF3B82F6),
                topLeft = Offset(paddleX, paddleTop),
                size = Size(paddleWidth, paddleHeight)
            )

            // Ball
            drawCircle(
                color = Color.White,
                radius = ballRadius,
                center = Offset(ballX, ballY)
            )
        }

        // Top UI Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(text = "Score: $score", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = "Lives: $lives", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = "Level: $level", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        if (!isRunning && lives > 0 && score == 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(text = "BREAKOUT", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF3B82F6),
                        modifier = Modifier.clickable {
                            score = 0
                            level = 1
                            lives = 3
                            ballSpeedMultiplier = 1f
                            initBricks(screenWidth, screenHeight)
                            ballX = screenWidth / 2f
                            ballY = screenHeight - 120f
                            paddleX = screenWidth / 2f - paddleWidth / 2f
                            ballVx = 4f
                            ballVy = -6f
                            isRunning = true
                        }
                    ) {
                        Text(text = "START GAME", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp))
                    }
                }
            }
        }

        if (lives <= 0) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(text = "GAME OVER", color = Color.Red, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Final Score: $score", color = Color.White, fontSize = 20.sp)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF3B82F6),
                        modifier = Modifier.clickable {
                            score = 0
                            level = 1
                            lives = 3
                            ballSpeedMultiplier = 1f
                            initBricks(screenWidth, screenHeight)
                            ballX = screenWidth / 2f
                            ballY = screenHeight - 120f
                            paddleX = screenWidth / 2f - paddleWidth / 2f
                            ballVx = 4f
                            ballVy = -6f
                            isRunning = true
                        }
                    ) {
                        Text(text = "PLAY AGAIN", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DoodleJumpGameScreen(onBackClick: () -> Unit) {
    val bgImage = ImageBitmap.imageResource(id = R.drawable.doodle_background)
    val leftImage = ImageBitmap.imageResource(id = R.drawable.doodle_left)
    val rightImage = ImageBitmap.imageResource(id = R.drawable.doodle_right)
    val platformImage = ImageBitmap.imageResource(id = R.drawable.doodle_platform)

    var score by remember { mutableIntStateOf(0) }
    var isGameOver by remember { mutableStateOf(false) }
    var isStarted by remember { mutableStateOf(false) }

    var playerX by remember { mutableFloatStateOf(500f) }
    var playerY by remember { mutableFloatStateOf(500f) }
    var playerVy by remember { mutableFloatStateOf(0f) }
    var isFacingLeft by remember { mutableStateOf(false) }

    var platforms by remember { mutableStateOf<List<Rect>>(emptyList()) }
    var screenWidth by remember { mutableFloatStateOf(1000f) }
    var screenHeight by remember { mutableFloatStateOf(2000f) }

    fun initGame(w: Float, h: Float) {
        if (w <= 0f || h <= 0f) return
        val list = mutableListOf<Rect>()
        list.add(Rect(w / 2f - 60f, h - 150f, w / 2f + 60f, h - 120f))
        var curY = h - 250f
        while (curY > 100f) {
            val curX = (Math.random() * (w - 120f)).toFloat()
            list.add(Rect(curX, curY, curX + 120f, curY + 25f))
            curY -= (80f + Math.random() * 50f).toFloat()
        }
        platforms = list
        playerX = w / 2f - 35f
        playerY = h - 250f
        playerVy = -18f
        score = 0
        isGameOver = false
    }

    LaunchedEffect(screenWidth, screenHeight) {
        if (!isStarted) {
            initGame(screenWidth, screenHeight)
        }
    }

    LaunchedEffect(isStarted, isGameOver) {
        if (isStarted && !isGameOver) {
            while (true) {
                delay(16L)

                // Gravity
                playerVy += 0.8f
                var ny = playerY + playerVy

                // Horizontal screen wrapping
                var nx = playerX
                if (nx < -35f) nx = screenWidth - 35f
                else if (nx > screenWidth - 35f) nx = -35f

                // Scrolling up when player passes middle of screen
                val scrollThreshold = screenHeight / 2f
                if (ny < scrollThreshold) {
                    val diff = scrollThreshold - ny
                    ny = scrollThreshold
                    score += diff.toInt()
                    platforms = platforms.map { Rect(it.left, it.top + diff, it.right, it.bottom + diff) }
                        .filter { it.top < screenHeight + 50f }

                    // Add new platforms at top
                    while (platforms.size < 12) {
                        val topMostY = platforms.minOfOrNull { it.top } ?: 0f
                        if (topMostY > 50f) {
                            val newY = topMostY - (80f + Math.random() * 50f).toFloat()
                            val newX = (Math.random() * (screenWidth - 120f)).toFloat()
                            platforms = platforms + Rect(newX, newY, newX + 120f, newY + 25f)
                        } else {
                            break
                        }
                    }
                }

                // Platform collision (only when falling down)
                if (playerVy > 0) {
                    val playerFeetY = ny + 70f
                    val playerCenterX = nx + 35f
                    for (plat in platforms) {
                        if (playerFeetY >= plat.top && playerFeetY <= plat.bottom + 15f && playerCenterX >= plat.left - 15f && playerCenterX <= plat.right + 15f) {
                            playerVy = -16f
                            ny = plat.top - 70f
                            break
                        }
                    }
                }

                // Game Over if player falls below bottom
                if (ny > screenHeight) {
                    isGameOver = true
                }

                playerX = nx
                playerY = ny
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    playerX += dragAmount.x * 1.5f
                    if (dragAmount.x < 0) isFacingLeft = true
                    else if (dragAmount.x > 0) isFacingLeft = false
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            screenWidth = size.width
            screenHeight = size.height

            // Background
            drawImage(bgImage, dstSize = IntSize(screenWidth.toInt(), screenHeight.toInt()))

            // Platforms
            for (plat in platforms) {
                drawImage(
                    platformImage,
                    dstOffset = IntOffset(plat.left.toInt(), plat.top.toInt()),
                    dstSize = IntSize(plat.width.toInt(), plat.height.toInt())
                )
            }

            // Player
            val playerImageToDraw = if (isFacingLeft) leftImage else rightImage
            drawImage(
                playerImageToDraw,
                dstOffset = IntOffset(playerX.toInt(), playerY.toInt()),
                dstSize = IntSize(70.dp.toPx().toInt(), 80.dp.toPx().toInt())
            )
        }

        // Top UI
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(text = "Score: $score", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        if (!isStarted) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(text = "DOODLE JUMP", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF43A047),
                        modifier = Modifier.clickable {
                            initGame(screenWidth, screenHeight)
                            isStarted = true
                        }
                    ) {
                        Text(text = "TAP TO PLAY", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp))
                    }
                }
            }
        }

        if (isGameOver) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(text = "GAME OVER", color = Color.Red, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Score: $score", color = Color.White, fontSize = 24.sp)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF43A047),
                        modifier = Modifier.clickable {
                            initGame(screenWidth, screenHeight)
                            isGameOver = false
                        }
                    ) {
                        Text(text = "PLAY AGAIN", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp))
                    }
                }
            }
        }
    }
}

enum class TetrominoType(val color: Color, val shape: List<List<Int>>) {
    I(Color(0xFF00F0F0), listOf(
        listOf(0,0,0,0),
        listOf(1,1,1,1),
        listOf(0,0,0,0),
        listOf(0,0,0,0)
    )),
    J(Color(0xFF0000F0), listOf(
        listOf(1,0,0),
        listOf(1,1,1),
        listOf(0,0,0)
    )),
    L(Color(0xFFF0A000), listOf(
        listOf(0,0,1),
        listOf(1,1,1),
        listOf(0,0,0)
    )),
    O(Color(0xFFF0F000), listOf(
        listOf(1,1),
        listOf(1,1)
    )),
    S(Color(0xFF00F000), listOf(
        listOf(0,1,1),
        listOf(1,1,0),
        listOf(0,0,0)
    )),
    T(Color(0xFFA000F0), listOf(
        listOf(0,1,0),
        listOf(1,1,1),
        listOf(0,0,0)
    )),
    Z(Color(0xFFF00000), listOf(
        listOf(1,1,0),
        listOf(0,1,1),
        listOf(0,0,0)
    ))
}

@Composable
fun TetrominoMiniPreview(type: TetrominoType) {
    val shape = type.shape
    val rSize = shape.size
    val cSize = shape[0].size
    Canvas(modifier = Modifier.size(36.dp)) {
        val sW = size.width / cSize
        val sH = size.height / rSize
        for (r in 0 until rSize) {
            for (c in 0 until cSize) {
                if (shape[r][c] != 0) {
                    drawRect(
                        color = type.color,
                        topLeft = Offset(c * sW + 0.5f, r * sH + 0.5f),
                        size = Size(sW - 1f, sH - 1f)
                    )
                }
            }
        }
    }
}

@Composable
fun ControlButton(text: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF334155),
        border = BorderStroke(1.dp, Color(0xFF475569)),
        shadowElevation = 2.dp,
        modifier = Modifier
            .clickable(onClick = onClick)
            .height(44.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TechminoTetrisGameScreen(
    onBackClick: () -> Unit
) {
    val cols = 10
    val rows = 20

    var board by remember { mutableStateOf(Array(rows) { Array(cols) { Color.Transparent } }) }
    var currentType by remember { mutableStateOf(TetrominoType.values().random()) }
    var currentRotation by remember { mutableIntStateOf(0) }
    var currentX by remember { mutableIntStateOf(3) }
    var currentY by remember { mutableIntStateOf(0) }

    var holdType by remember { mutableStateOf<TetrominoType?>(null) }
    var canHold by remember { mutableStateOf(true) }

    var nextQueue by remember { mutableStateOf(List(3) { TetrominoType.values().random() }) }

    var score by remember { mutableIntStateOf(0) }
    var linesCleared by remember { mutableIntStateOf(0) }
    var level by remember { mutableIntStateOf(1) }
    var isRunning by remember { mutableStateOf(true) }
    var isGameOver by remember { mutableStateOf(false) }

    fun getRotatedShape(type: TetrominoType, rot: Int): List<List<Int>> {
        var shape = type.shape
        repeat(rot % 4) {
            val r = shape.size
            val c = shape[0].size
            val rotated = List(c) { rC -> List(r) { cC -> shape[r - 1 - cC][rC] } }
            shape = rotated
        }
        return shape
    }

    fun isValidPosition(x: Int, y: Int, shape: List<List<Int>>): Boolean {
        for (r in shape.indices) {
            for (c in shape[r].indices) {
                if (shape[r][c] != 0) {
                    val boardX = x + c
                    val boardY = y + r
                    if (boardX < 0 || boardX >= cols || boardY >= rows) return false
                    if (boardY >= 0 && board.getOrNull(boardY)?.getOrNull(boardX) != Color.Transparent) return false
                }
            }
        }
        return true
    }

    fun spawnNewPiece(type: TetrominoType) {
        currentType = type
        currentRotation = 0
        currentX = 3
        currentY = 0
        canHold = true
        if (!isValidPosition(currentX, currentY, getRotatedShape(currentType, currentRotation))) {
            isGameOver = true
            isRunning = false
        }
    }

    fun advanceQueue() {
        val next = nextQueue.first()
        val remaining = nextQueue.drop(1) + TetrominoType.values().random()
        nextQueue = remaining
        spawnNewPiece(next)
    }

    fun lockPiece() {
        val shape = getRotatedShape(currentType, currentRotation)
        val newBoard = board.map { it.clone() }.toTypedArray()
        for (r in shape.indices) {
            for (c in shape[r].indices) {
                if (shape[r][c] != 0) {
                    val bx = currentX + c
                    val by = currentY + r
                    if (by in 0 until rows && bx in 0 until cols) {
                        newBoard[by][bx] = currentType.color
                    }
                }
            }
        }

        // Check clear lines
        var cleared = 0
        val filteredBoard = newBoard.filter { row ->
            if (row.all { it != Color.Transparent }) {
                cleared++
                false
            } else {
                true
            }
        }.toTypedArray()

        if (cleared > 0) {
            linesCleared += cleared
            score += when (cleared) {
                1 -> 100 * level
                2 -> 300 * level
                3 -> 500 * level
                else -> 800 * level
            }
            level = 1 + linesCleared / 10
        }

        val finalBoard = Array(rows) { r ->
            if (r < cleared) Array(cols) { Color.Transparent }
            else filteredBoard[r - cleared]
        }
        board = finalBoard

        advanceQueue()
    }

    val dropInterval = maxOf(100L, 800L - (level - 1) * 70L)
    LaunchedEffect(isRunning, level) {
        while (isRunning && !isGameOver) {
            delay(dropInterval)
            val shape = getRotatedShape(currentType, currentRotation)
            if (isValidPosition(currentX, currentY + 1, shape)) {
                currentY++
            } else {
                lockPiece()
            }
        }
    }

    val ghostY = remember(currentX, currentY, currentRotation, currentType, board) {
        val shape = getRotatedShape(currentType, currentRotation)
        var gy = currentY
        while (isValidPosition(currentX, gy + 1, shape)) {
            gy++
        }
        gy
    }

    fun moveLeft() {
        val shape = getRotatedShape(currentType, currentRotation)
        if (isValidPosition(currentX - 1, currentY, shape)) currentX--
    }

    fun moveRight() {
        val shape = getRotatedShape(currentType, currentRotation)
        if (isValidPosition(currentX + 1, currentY, shape)) currentX++
    }

    fun rotate() {
        val nextRot = currentRotation + 1
        val shape = getRotatedShape(currentType, nextRot)
        if (isValidPosition(currentX, currentY, shape)) {
            currentRotation = nextRot
        } else if (isValidPosition(currentX - 1, currentY, shape)) {
            currentX--
            currentRotation = nextRot
        } else if (isValidPosition(currentX + 1, currentY, shape)) {
            currentX++
            currentRotation = nextRot
        }
    }

    fun softDrop() {
        val shape = getRotatedShape(currentType, currentRotation)
        if (isValidPosition(currentX, currentY + 1, shape)) {
            currentY++
            score += 1
        } else {
            lockPiece()
        }
    }

    fun hardDrop() {
        var dropDist = 0
        val shape = getRotatedShape(currentType, currentRotation)
        while (isValidPosition(currentX, currentY + 1, shape)) {
            currentY++
            dropDist++
        }
        score += dropDist * 2
        lockPiece()
    }

    fun holdPiece() {
        if (!canHold) return
        canHold = false
        val prevHold = holdType
        holdType = currentType
        if (prevHold == null) {
            advanceQueue()
        } else {
            spawnNewPiece(prevHold)
        }
    }

    val screenBg = Brush.verticalGradient(
        listOf(
            Color(0xFF0F172A),
            Color(0xFF1E293B)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "TECHMINO", color = Color(0xFF38BDF8), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Score: $score | Level: $level", color = Color.White, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.width(48.dp))
            }

            Row(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(70.dp)
                ) {
                    Text(text = "HOLD", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.size(60.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            holdType?.let { hType ->
                                TetrominoMiniPreview(type = hType)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF020617),
                    border = BorderStroke(2.dp, Color(0xFF334155)),
                    modifier = Modifier
                        .width(220.dp)
                        .fillMaxHeight(0.9f)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cellW = size.width / cols
                        val cellH = size.height / rows

                        for (r in 0 until rows) {
                            for (c in 0 until cols) {
                                drawRect(
                                    color = Color(0xFF090D16),
                                    topLeft = Offset(c * cellW, r * cellH),
                                    size = Size(cellW - 1f, cellH - 1f)
                                )
                            }
                        }

                        for (r in 0 until rows) {
                            for (c in 0 until cols) {
                                val col = board[r][c]
                                if (col != Color.Transparent) {
                                    drawRect(
                                        color = col,
                                        topLeft = Offset(c * cellW + 1f, r * cellH + 1f),
                                        size = Size(cellW - 2f, cellH - 2f)
                                    )
                                }
                            }
                        }

                        val activeShape = getRotatedShape(currentType, currentRotation)
                        for (r in activeShape.indices) {
                            for (c in activeShape[r].indices) {
                                if (activeShape[r][c] != 0) {
                                    val gx = currentX + c
                                    val gy = ghostY + r
                                    if (gy in 0 until rows && gx in 0 until cols) {
                                        drawRect(
                                            color = currentType.color.copy(alpha = 0.3f),
                                            topLeft = Offset(gx * cellW + 1f, gy * cellH + 1f),
                                            size = Size(cellW - 2f, cellH - 2f)
                                        )
                                    }
                                }
                            }
                        }

                        for (r in activeShape.indices) {
                            for (c in activeShape[r].indices) {
                                if (activeShape[r][c] != 0) {
                                    val ax = currentX + c
                                    val ay = currentY + r
                                    if (ay in 0 until rows && ax in 0 until cols) {
                                        drawRect(
                                            color = currentType.color,
                                            topLeft = Offset(ax * cellW + 1f, ay * cellH + 1f),
                                            size = Size(cellW - 2f, cellH - 2f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(70.dp)
                ) {
                    Text(text = "NEXT", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.size(60.dp, 160.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(4.dp),
                            verticalArrangement = Arrangement.SpaceAround,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            for (nType in nextQueue) {
                                Box(modifier = Modifier.size(45.dp), contentAlignment = Alignment.Center) {
                                    TetrominoMiniPreview(type = nType)
                                }
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ControlButton(text = "HOLD", onClick = { holdPiece() })
                    ControlButton(text = "ROTATE", onClick = { rotate() })
                    ControlButton(text = "DROP", onClick = { hardDrop() })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ControlButton(text = "◀ LEFT", onClick = { moveLeft() })
                    ControlButton(text = "▼ SOFT", onClick = { softDrop() })
                    ControlButton(text = "RIGHT ▶", onClick = { moveRight() })
                }
            }
        }

        if (isGameOver) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(text = "GAME OVER", color = Color.Red, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Final Score: $score", color = Color.White, fontSize = 20.sp)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.clickable {
                            board = Array(rows) { Array(cols) { Color.Transparent } }
                            score = 0
                            linesCleared = 0
                            level = 1
                            isGameOver = false
                            isRunning = true
                            nextQueue = List(3) { TetrominoType.values().random() }
                            spawnNewPiece(TetrominoType.values().random())
                        }
                    ) {
                        Text(text = "PLAY AGAIN", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GamesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val importedPackages by viewModel.importedGamePackages.collectAsState()
    val removedPackages by viewModel.removedGamePackages.collectAsState()
    val allShorts by viewModel.allGameShortcuts.collectAsState()
    var editingApp by remember { mutableStateOf<ApplicationInfo?>(null) }
    var showBrickGame by remember { mutableStateOf(false) }
    var showDoodleGame by remember { mutableStateOf(false) }
    var showTechminoGame by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    if (showBrickGame) {
        BrickGameScreen(onBackClick = { showBrickGame = false })
        return
    }

    if (showDoodleGame) {
        DoodleJumpGameScreen(onBackClick = { showDoodleGame = false })
        return
    }

    if (showTechminoGame) {
        TechminoTetrisGameScreen(onBackClick = { showTechminoGame = false })
        return
    }

    val installedApps = remember(importedPackages, removedPackages) {
        val pm: PackageManager = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        packages.filter { app ->
            importedPackages.contains(app.packageName) && !removedPackages.contains(app.packageName)
        }.sortedBy { app -> app.loadLabel(pm).toString() }
    }

    val screenBg = Brush.verticalGradient(
        listOf(
            Color(0xFFEAEFF5),
            Color(0xFFD0D7E5),
            Color(0xFFB5BECE)
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(screenBg)
    ) {
        // Top Bar (iOS 6 style)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF91A5CA), Color(0xFF5E759E), Color(0xFF3E547D))
                    )
                )
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.width(60.dp))

            Text(
                text = "Games",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            // Search Bar
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFB0BACB)),
                modifier = Modifier
                    .width(140.dp)
                    .height(30.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF95A5A6),
                        modifier = Modifier.size(14.dp)
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search",
                                color = Color(0xFF95A5A6),
                                fontSize = 12.sp
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 12.sp,
                                color = Color(0xFF1C2833)
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // List Content
        LazyColumn(
            modifier = Modifier.fillMaxSize().weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Breakout Game Row
            if (searchQuery.isBlank() || "breakout".contains(searchQuery, true) || "brick".contains(searchQuery, true)) {
                item {
                    GameRowCard(
                        title = "Breakout",
                        iconModel = R.drawable.brick_logo,
                        onClick = { showBrickGame = true }
                    )
                }
            }

            // 2. Doodle Jump Game Row
            if (searchQuery.isBlank() || "doodle jump".contains(searchQuery, true)) {
                item {
                    GameRowCard(
                        title = "Doodle Jump",
                        iconModel = R.drawable.logo_of_dj,
                        onClick = { showDoodleGame = true }
                    )
                }
            }

            // 3. Techmino Tetris Game Row
            if (searchQuery.isBlank() || "techmino".contains(searchQuery, true) || "tetris".contains(searchQuery, true)) {
                item {
                    GameRowCard(
                        title = "Techmino Tetris",
                        iconModel = R.drawable.tetris_logo,
                        onClick = { showTechminoGame = true }
                    )
                }
            }

            // 4. Installed Apps Rows
            val filteredApps = installedApps.filter { app ->
                val pm = context.packageManager
                val shortcutEntity = allShorts.firstOrNull { it.packageName == app.packageName }
                val appName = shortcutEntity?.customName?.takeIf { it.isNotBlank() } ?: app.loadLabel(pm).toString()
                appName.contains(searchQuery, true)
            }

            items(filteredApps) { app ->
                val pm = context.packageManager
                val shortcutEntity = allShorts.firstOrNull { it.packageName == app.packageName }
                val appName = shortcutEntity?.customName?.takeIf { it.isNotBlank() } ?: app.loadLabel(pm).toString()
                val appIcon = shortcutEntity?.customImageUri?.takeIf { it.isNotBlank() } ?: app.loadIcon(pm)

                GameRowCard(
                    title = appName,
                    iconModel = appIcon,
                    onClick = {
                        val intent = pm.getLaunchIntentForPackage(app.packageName)
                        if (intent != null) {
                            context.startActivity(intent)
                        }
                    },
                    onLongClick = {
                        editingApp = app
                    }
                )
            }
        }
    }

    editingApp?.let { app ->
        val pm = context.packageManager
        val shortcutEntity = allShorts.firstOrNull { it.packageName == app.packageName }
        val currentName = shortcutEntity?.customName?.takeIf { it.isNotBlank() } ?: app.loadLabel(pm).toString()
        val currentImage = shortcutEntity?.customImageUri ?: ""

        AppShortcutMetadataDialog(
            initialName = currentName,
            initialImageUri = currentImage,
            onDismiss = { editingApp = null },
            onSave = { newName, newImageUri ->
                viewModel.updateGameShortcutMetadata(app.packageName, newName, newImageUri)
                editingApp = null
            },
            onRemove = {
                viewModel.removeGameShortcutCustom(app.packageName)
                editingApp = null
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameRowCard(
    title: String,
    iconModel: Any,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFFAFAFB),
        border = BorderStroke(1.dp, Color(0xFFB0BACB)),
        shadowElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Game Icon
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 4.dp,
                    border = BorderStroke(0.5.dp, Color(0xFF7F8C8D)),
                    modifier = Modifier
                        .size(64.dp)
                ) {
                    AsyncImage(
                        model = iconModel,
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Game Title
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1C2833),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // PLAY Button
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF3379D5),
                shadowElevation = 4.dp,
                border = BorderStroke(1.dp, Color(0xFF11438B)),
                modifier = Modifier.height(36.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF74A9EC), Color(0xFF3379D5), Color(0xFF1B5EB9))
                            )
                        )
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "PLAY",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
