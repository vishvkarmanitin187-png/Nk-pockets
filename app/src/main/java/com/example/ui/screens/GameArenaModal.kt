package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameCategory
import com.example.model.GameItem
import com.example.ui.theme.PrimaryGold
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.ErrorRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun GameArenaModal(
    game: GameItem,
    depositBalance: Double,
    winningBalance: Double,
    bonusBalance: Double,
    onDeductBet: (Double, String) -> Boolean,
    onCreditWin: (Double, String, Double) -> Unit,
    onOpenDeposit: () -> Unit,
    onClose: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val totalBalance = depositBalance + winningBalance + bonusBalance

    var selectedBet by remember { mutableStateOf(game.minBet.toDouble().coerceAtLeast(10.0)) }
    var isPlaying by remember { mutableStateOf(false) }

    // Crash / Multiplier specific states
    var crashMultiplier by remember { mutableStateOf(1.00) }
    var hasCashedOut by remember { mutableStateOf(false) }

    // Slot 3-Reel states
    val slotSymbols = remember(game.id) {
        when {
            game.title.contains("Gem", ignoreCase = true) -> listOf("💎", "💍", "🔮", "👑", "✨", "🪙")
            game.title.contains("Jungle", ignoreCase = true) || game.title.contains("Ape", ignoreCase = true) || game.title.contains("Jaguar", ignoreCase = true) -> listOf("🦁", "🦍", "🐆", "🌴", "🍌", "🪙")
            game.title.contains("Jurassic", ignoreCase = true) -> listOf("🦖", "🦕", "🌋", "🥚", "🦴", "💎")
            game.title.contains("Banditio", ignoreCase = true) -> listOf("🤠", "💰", "🌵", "🐎", "⭐", "🪙")
            game.title.contains("Book", ignoreCase = true) -> listOf("📖", "📜", "⚱️", "👑", "🔮", "💎")
            game.title.contains("777", ignoreCase = true) || game.title.contains("Lotto", ignoreCase = true) -> listOf("7️⃣", "⭐", "🔔", "🍒", "💎", "🪙")
            else -> listOf("⭐", "💎", "🔔", "🍒", "🍀", "🪙")
        }
    }
    var reel1 by remember { mutableStateOf(slotSymbols[0]) }
    var reel2 by remember { mutableStateOf(slotSymbols[1]) }
    var reel3 by remember { mutableStateOf(slotSymbols[2]) }
    var isReelSpinning by remember { mutableStateOf(false) }

    // Racing & Arcade states
    var raceProgress by remember { mutableStateOf(0f) }
    var diceValue by remember { mutableStateOf(6) }

    // Win status & celebrative overlay
    var winMessage by remember { mutableStateOf<String?>(null) }
    var winAmount by remember { mutableStateOf(0.0) }
    var isWinCelebration by remember { mutableStateOf(false) }

    // Glowing animation
    val infiniteTransition = rememberInfiniteTransition(label = "game_glow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_anim"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF070B14)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = game.icon, fontSize = 24.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = game.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = PrimaryGold
                        )
                        Text(
                            text = "${game.category.displayName} • Max Win: ${game.multiplier}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Live Game Stage Area
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(
                        2.dp,
                        Brush.verticalGradient(
                            if (isWinCelebration) listOf(SuccessGreen, PrimaryGold)
                            else listOf(Color(0xFFFFB703), Color(0xFF1E293B))
                        ),
                        RoundedCornerShape(20.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        // 1. CRASH / MULTIPLIER MODE (e.g. Aviator Crash, Power of Kraken)
                        game.category == GameCategory.CRASH || game.title.contains("Aviator", ignoreCase = true) || game.title.contains("Kraken", ignoreCase = true) -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (game.title.contains("Kraken", ignoreCase = true)) "🐙" else "✈️",
                                    fontSize = 72.sp,
                                    modifier = Modifier.scale(if (isPlaying && !hasCashedOut) glowPulse else 1f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "%.2fx".format(crashMultiplier),
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    color = when {
                                        hasCashedOut -> SuccessGreen
                                        winMessage?.contains("CRASHED") == true -> ErrorRed
                                        else -> PrimaryGold
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = winMessage ?: (if (isPlaying) "Multiplier Rising! Cash out anytime!" else "Place your bet & Tap Launch!"),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // 2. SLOTS & JACKPOTS 3-REEL MODE
                        game.category == GameCategory.SLOTS || game.category == GameCategory.JACKPOTS || game.title.contains("Lotto", ignoreCase = true) -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "🎰 3-REEL VEGAS MACHINE 🎰",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                    color = PrimaryGold
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                // 3 Reels Container
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ReelBox(symbol = reel1, isSpinning = isReelSpinning, isWin = isWinCelebration)
                                    ReelBox(symbol = reel2, isSpinning = isReelSpinning, isWin = isWinCelebration)
                                    ReelBox(symbol = reel3, isSpinning = isReelSpinning, isWin = isWinCelebration)
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = winMessage ?: (if (isReelSpinning) "Reels Spinning..." else "Tap SPIN to match 3 symbols!"),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isWinCelebration) SuccessGreen else Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // 3. RACING MODE (3D Car Racing, 3D Bike Racing, Phantom Mustang)
                        game.category == GameCategory.RACING -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (game.title.contains("Bike", ignoreCase = true)) "🏍️" else if (game.title.contains("Mustang", ignoreCase = true)) "🐎" else "🏎️",
                                    fontSize = 60.sp,
                                    modifier = Modifier.scale(if (isPlaying) glowPulse else 1f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                LinearProgressIndicator(
                                    progress = { raceProgress },
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp)),
                                    color = PrimaryGold,
                                    trackColor = Color(0xFF1E293B)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = winMessage ?: (if (isPlaying) "Racing to the Finish Line..." else "Pick your Bet & Start Race!"),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isWinCelebration) SuccessGreen else Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // 4. ARCADE / DICE / SCRATCH MODE
                        else -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1E293B))
                                        .border(2.dp, PrimaryGold, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (game.title.contains("Dice", ignoreCase = true)) when (diceValue) {
                                            1 -> "⚀"
                                            2 -> "⚁"
                                            3 -> "⚂"
                                            4 -> "⚃"
                                            5 -> "⚄"
                                            else -> "⚅"
                                        } else game.icon,
                                        fontSize = 44.sp,
                                        modifier = Modifier.scale(if (isPlaying) glowPulse else 1f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = winMessage ?: (if (isPlaying) "Running Action Round..." else "Tap PLAY to Unlock Rewards!"),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isWinCelebration) SuccessGreen else Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // Celebratory Win Banner Overlay
                    if (isWinCelebration && winAmount > 0) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF042F2E).copy(alpha = 0.95f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(2.dp, SuccessGreen, RoundedCornerShape(16.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🎉 WINNER! 🎉", color = Color(0xFFFFD700), fontWeight = FontWeight.Black, fontSize = 20.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("+₹%.2f".format(winAmount), color = SuccessGreen, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Credited instantly to your wallet winning balance!", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bet Selector & Action Controls
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131A2A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Bet Amount",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Wallet: ₹%.2f".format(totalBalance),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryGold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Bet Amount Selector Buttons: ₹10, ₹20, ₹50, ₹100, ₹250
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(10.0, 20.0, 50.0, 100.0, 250.0).forEach { bet ->
                            val isSelected = selectedBet == bet
                            OutlinedButton(
                                onClick = { if (!isPlaying) selectedBet = bet },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                enabled = !isPlaying,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) PrimaryGold.copy(alpha = 0.25f) else Color(0xFF0F172A),
                                    contentColor = if (isSelected) PrimaryGold else Color.White
                                ),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(
                                        if (isSelected) PrimaryGold else Color(0xFF334155)
                                    )
                                ),
                                contentPadding = PaddingValues(vertical = 6.dp)
                            ) {
                                Text("₹${bet.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ACTION BUTTON (Crash Cash Out or Spin / Play)
                    if (game.category == GameCategory.CRASH || game.title.contains("Aviator", ignoreCase = true) || game.title.contains("Kraken", ignoreCase = true)) {
                        // Crash Mode Controls
                        if (isPlaying && !hasCashedOut) {
                            Button(
                                onClick = {
                                    hasCashedOut = true
                                    isPlaying = false
                                    val win = selectedBet * crashMultiplier
                                    winAmount = win
                                    isWinCelebration = true
                                    winMessage = "🎉 Cashed Out at %.2fx! Won ₹%.2f".format(crashMultiplier, win)
                                    onCreditWin(win, game.title, crashMultiplier)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                            ) {
                                Text(
                                    text = "CASH OUT (₹%.2f)".format(selectedBet * crashMultiplier),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp,
                                    color = Color.White
                                )
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (totalBalance >= selectedBet) {
                                        val deducted = onDeductBet(selectedBet, game.title)
                                        if (deducted) {
                                            isPlaying = true
                                            hasCashedOut = false
                                            crashMultiplier = 1.00
                                            winAmount = 0.0
                                            isWinCelebration = false
                                            winMessage = "Launched! Multiplier climbing..."

                                            coroutineScope.launch {
                                                // Random crash point between 1.15x and 5.50x
                                                val crashPoint = 1.15 + (Random.nextDouble() * 3.85)
                                                while (crashMultiplier < crashPoint && !hasCashedOut) {
                                                    delay(90)
                                                    crashMultiplier += 0.05
                                                }
                                                if (!hasCashedOut) {
                                                    isPlaying = false
                                                    winMessage = "💥 CRASHED AT %.2fx! Try Again!".format(crashMultiplier)
                                                    isWinCelebration = false
                                                }
                                            }
                                        }
                                    }
                                },
                                enabled = !isPlaying && totalBalance >= selectedBet,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold)
                            ) {
                                Text(
                                    text = "LAUNCH (₹${selectedBet.toInt()})",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp,
                                    color = Color(0xFF0F172A)
                                )
                            }
                        }
                    } else if (game.category == GameCategory.SLOTS || game.category == GameCategory.JACKPOTS || game.title.contains("Lotto", ignoreCase = true)) {
                        // Slots 3-Reel Mode Controls (2-Second Spin Animation)
                        Button(
                            onClick = {
                                if (totalBalance >= selectedBet && !isPlaying) {
                                    val deducted = onDeductBet(selectedBet, game.title)
                                    if (deducted) {
                                        isPlaying = true
                                        isReelSpinning = true
                                        isWinCelebration = false
                                        winAmount = 0.0
                                        winMessage = "Reels Spinning..."

                                        coroutineScope.launch {
                                            // 2-second glowing spinning reel animation
                                            val spinJob = launch {
                                                while (isReelSpinning) {
                                                    reel1 = slotSymbols.random()
                                                    reel2 = slotSymbols.random()
                                                    reel3 = slotSymbols.random()
                                                    delay(70)
                                                }
                                            }

                                            delay(2000)
                                            isReelSpinning = false
                                            spinJob.cancel()

                                            // Determine real random outcome (42% win chance)
                                            val winRoll = Random.nextFloat()
                                            if (winRoll < 0.42f) {
                                                // 3 Match or 2 Match
                                                val isTriple = winRoll < 0.12f
                                                val winningSym = slotSymbols.random()
                                                if (isTriple) {
                                                    reel1 = winningSym
                                                    reel2 = winningSym
                                                    reel3 = winningSym
                                                    val mult = 5.0 + (Random.nextDouble() * 10.0)
                                                    val won = selectedBet * mult
                                                    winAmount = won
                                                    isWinCelebration = true
                                                    winMessage = "🎉 TRIPLE MATCH! WIN +₹%.2f".format(won)
                                                    onCreditWin(won, game.title, mult)
                                                } else {
                                                    reel1 = winningSym
                                                    reel2 = winningSym
                                                    reel3 = slotSymbols.filter { it != winningSym }.random()
                                                    val mult = 2.0
                                                    val won = selectedBet * mult
                                                    winAmount = won
                                                    isWinCelebration = true
                                                    winMessage = "🎉 DOUBLE MATCH! WIN +₹%.2f".format(won)
                                                    onCreditWin(won, game.title, mult)
                                                }
                                            } else {
                                                // No match
                                                val shuffled = slotSymbols.shuffled()
                                                reel1 = shuffled[0]
                                                reel2 = shuffled[1]
                                                reel3 = shuffled[2]
                                                winMessage = "TRY AGAIN! Better luck next spin."
                                                isWinCelebration = false
                                            }
                                            isPlaying = false
                                        }
                                    }
                                }
                            },
                            enabled = !isPlaying && totalBalance >= selectedBet,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold)
                        ) {
                            Text(
                                text = if (isPlaying) "SPINNING REELS..." else "SPIN (₹${selectedBet.toInt()})",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = Color(0xFF0F172A)
                            )
                        }
                    } else if (game.category == GameCategory.RACING) {
                        // Racing Mode Controls (2-second race progress)
                        Button(
                            onClick = {
                                if (totalBalance >= selectedBet && !isPlaying) {
                                    val deducted = onDeductBet(selectedBet, game.title)
                                    if (deducted) {
                                        isPlaying = true
                                        isWinCelebration = false
                                        raceProgress = 0f
                                        winAmount = 0.0
                                        winMessage = "Racers off the line! Full speed..."

                                        coroutineScope.launch {
                                            for (step in 1..20) {
                                                delay(100)
                                                raceProgress = step / 20f
                                            }
                                            val winRoll = Random.nextFloat()
                                            if (winRoll < 0.45f) {
                                                val mult = 2.5
                                                val won = selectedBet * mult
                                                winAmount = won
                                                isWinCelebration = true
                                                winMessage = "🏆 1ST PLACE FINISH! WIN +₹%.2f".format(won)
                                                onCreditWin(won, game.title, mult)
                                            } else {
                                                winMessage = "Finished 2nd Place! TRY AGAIN."
                                                isWinCelebration = false
                                            }
                                            isPlaying = false
                                        }
                                    }
                                }
                            },
                            enabled = !isPlaying && totalBalance >= selectedBet,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold)
                        ) {
                            Text(
                                text = if (isPlaying) "RACING..." else "START RACE (₹${selectedBet.toInt()})",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = Color(0xFF0F172A)
                            )
                        }
                    } else {
                        // Arcade / Dice Mode Controls (2-second action)
                        Button(
                            onClick = {
                                if (totalBalance >= selectedBet && !isPlaying) {
                                    val deducted = onDeductBet(selectedBet, game.title)
                                    if (deducted) {
                                        isPlaying = true
                                        isWinCelebration = false
                                        winAmount = 0.0
                                        winMessage = "Action in progress..."

                                        coroutineScope.launch {
                                            val rollAnim = launch {
                                                for (i in 1..10) {
                                                    diceValue = (1..6).random()
                                                    delay(120)
                                                }
                                            }
                                            delay(2000)
                                            rollAnim.cancel()

                                            val winRoll = Random.nextFloat()
                                            if (winRoll < 0.45f) {
                                                val mult = 2.2
                                                val won = selectedBet * mult
                                                winAmount = won
                                                isWinCelebration = true
                                                winMessage = "🎉 ACTION WIN! +₹%.2f".format(won)
                                                onCreditWin(won, game.title, mult)
                                            } else {
                                                winMessage = "Round Complete! TRY AGAIN."
                                                isWinCelebration = false
                                            }
                                            isPlaying = false
                                        }
                                    }
                                }
                            },
                            enabled = !isPlaying && totalBalance >= selectedBet,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold)
                        ) {
                            Text(
                                text = if (isPlaying) "PLAYING..." else "PLAY ROUND (₹${selectedBet.toInt()})",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }

                    // Prompt to Deposit if balance is insufficient
                    if (totalBalance < selectedBet) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Insufficient balance for ₹${selectedBet.toInt()}.",
                                color = ErrorRed,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Button(
                                onClick = {
                                    onClose()
                                    onOpenDeposit()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Deposit Money", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReelBox(
    symbol: String,
    isSpinning: Boolean,
    isWin: Boolean
) {
    Box(
        modifier = Modifier
            .size(74.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isWin) Color(0xFF1E3A8A) else Color(0xFF1E293B))
            .border(
                2.dp,
                if (isWin) Color(0xFFFFD700) else if (isSpinning) PrimaryGold else Color(0xFF334155),
                RoundedCornerShape(14.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            fontSize = 36.sp,
            modifier = if (isSpinning) Modifier.scale(0.85f) else Modifier.scale(1f)
        )
    }
}
