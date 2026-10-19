package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.JackpotSpinResult
import com.example.viewmodel.LiveWinner
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun JackpotScreen(
    depositBalance: Double,
    winningBalance: Double,
    bonusBalance: Double,
    progressiveJackpot: Double,
    liveWinners: List<LiveWinner>,
    onSpinJackpot: (Double) -> JackpotSpinResult?,
    onOpenAddMoney: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val totalBalance = depositBalance + winningBalance + bonusBalance

    var selectedBet by remember { mutableStateOf(10.0) }
    var isSpinning by remember { mutableStateOf(false) }
    var isAutoSpinning by remember { mutableStateOf(false) }
    var autoSpinsLeft by remember { mutableStateOf(0) }

    // Reel display states
    var reel1Symbol by remember { mutableStateOf("7️⃣") }
    var reel2Symbol by remember { mutableStateOf("7️⃣") }
    var reel3Symbol by remember { mutableStateOf("7️⃣") }

    // Reel spinning individual states for staggered stop effect
    var reel1Spinning by remember { mutableStateOf(false) }
    var reel2Spinning by remember { mutableStateOf(false) }
    var reel3Spinning by remember { mutableStateOf(false) }

    var lastResult by remember { mutableStateOf<JackpotSpinResult?>(null) }
    var showWinDialog by remember { mutableStateOf(false) }
    var showPaytableDialog by remember { mutableStateOf(false) }

    val allSymbols = listOf("7️⃣", "💎", "🪙", "🔔", "🍒", "🍀")

    // Pulsing animation for the Mega Jackpot marquee
    val infiniteTransition = rememberInfiniteTransition(label = "marquee_pulse")
    val marqueeGlow by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Function to run a single spin with realistic staggered timing
    fun executeSpin() {
        if (totalBalance < selectedBet) return
        isSpinning = true
        reel1Spinning = true
        reel2Spinning = true
        reel3Spinning = true
        lastResult = null

        coroutineScope.launch {
            // Rapid symbol rolling on all reels
            val rollJob1 = launch {
                while (reel1Spinning) {
                    reel1Symbol = allSymbols.random()
                    delay(70)
                }
            }
            val rollJob2 = launch {
                while (reel2Spinning) {
                    reel2Symbol = allSymbols.random()
                    delay(70)
                }
            }
            val rollJob3 = launch {
                while (reel3Spinning) {
                    reel3Symbol = allSymbols.random()
                    delay(70)
                }
            }

            // Call backend spin calculation
            val result = onSpinJackpot(selectedBet)

            if (result != null) {
                // Staggered stop sequence:
                // Reel 1 stops first (800ms)
                delay(800)
                reel1Spinning = false
                rollJob1.cancel()
                reel1Symbol = result.reel1

                // Reel 2 stops second (1400ms)
                delay(600)
                reel2Spinning = false
                rollJob2.cancel()
                reel2Symbol = result.reel2

                // Reel 3 stops last with peak suspense (2000ms)
                delay(600)
                reel3Spinning = false
                rollJob3.cancel()
                reel3Symbol = result.reel3

                lastResult = result
                isSpinning = false

                if (result.payout > 0) {
                    showWinDialog = true
                }

                // Check auto-spin continuation
                if (isAutoSpinning) {
                    if (autoSpinsLeft > 1 && totalBalance >= selectedBet) {
                        autoSpinsLeft -= 1
                        delay(1200)
                        executeSpin()
                    } else {
                        isAutoSpinning = false
                        autoSpinsLeft = 0
                    }
                }
            } else {
                reel1Spinning = false
                reel2Spinning = false
                reel3Spinning = false
                isSpinning = false
                isAutoSpinning = false
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Grand Progressive Jackpot Marquee Header
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E112A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(marqueeGlow)
                    .border(
                        width = 2.dp,
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFFFFD700), Color(0xFFFF8C00), Color(0xFFFFD700))
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("✨", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "GRAND MEGA JACKPOT",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp
                            ),
                            color = Color(0xFFFFD700)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("✨", fontSize = 18.sp)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "₹%,.2f".format(progressiveJackpot),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        ),
                        color = Color(0xFFFFEA79)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Hit 7️⃣-7️⃣-7️⃣ to Win 50x + Progressive Cash!",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFFE2E8F0)
                    )
                }
            }
        }

        // Slot Machine Casing & 3-Reels Display
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF13111C)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 3.dp,
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFFFFB703), Color(0xFFFB8500), Color(0xFF8338EC))
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .shadow(12.dp, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Slot Machine Lights Banner
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            repeat(4) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (isSpinning) Color(0xFFFF0055) else Color(0xFFFFD700))
                                )
                            }
                        }

                        Text(
                            text = "🎰 JACKPOT 777 🎰",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.5.sp
                            ),
                            color = PrimaryGold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            repeat(4) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(if (isSpinning) Color(0xFF00FF66) else Color(0xFFFFD700))
                                )
                            }
                        }
                    }

                    // Reels Box Container with Payline Indicator
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF08060D))
                            .border(2.dp, Color(0xFF2D2545), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Center Laser Payline
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color.Transparent, Color(0xFFFF0055), Color(0xFFFF0055), Color.Transparent)
                                    )
                                )
                        )

                        // The 3 Reels
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SingleReelView(
                                symbol = reel1Symbol,
                                isSpinning = reel1Spinning,
                                isWinning = (lastResult?.payout ?: 0.0) > 0 && !reel1Spinning && !isSpinning
                            )
                            Divider(
                                modifier = Modifier
                                    .fillMaxHeight(0.8f)
                                    .width(2.dp),
                                color = Color(0xFF261E38)
                            )
                            SingleReelView(
                                symbol = reel2Symbol,
                                isSpinning = reel2Spinning,
                                isWinning = (lastResult?.payout ?: 0.0) > 0 && !reel2Spinning && !isSpinning
                            )
                            Divider(
                                modifier = Modifier
                                    .fillMaxHeight(0.8f)
                                    .width(2.dp),
                                color = Color(0xFF261E38)
                            )
                            SingleReelView(
                                symbol = reel3Symbol,
                                isSpinning = reel3Spinning,
                                isWinning = (lastResult?.payout ?: 0.0) > 0 && !reel3Spinning && !isSpinning
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Result Status Bar
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                lastResult?.isJackpot == true -> Color(0xFF5B21B6)
                                (lastResult?.payout ?: 0.0) > 0 -> Color(0xFF065F46)
                                else -> Color(0xFF1E1B2E)
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when {
                                    isSpinning -> "🎰 Reels are rolling..."
                                    lastResult != null -> lastResult!!.winMessage
                                    else -> "Spin & Match 3 Symbols for Mega Win!"
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = when {
                                    (lastResult?.payout ?: 0.0) > 0 -> Color(0xFFFFD700)
                                    else -> Color.White
                                },
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Betting & Action Controls
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Bet Amount Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Bet Amount",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        TextButton(
                            onClick = { showPaytableDialog = true },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = PrimaryGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Paytable", color = PrimaryGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(10.0, 25.0, 50.0, 100.0, 250.0).forEach { bet ->
                            val isSelected = selectedBet == bet
                            OutlinedButton(
                                onClick = {
                                    if (!isSpinning) selectedBet = bet
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                enabled = !isSpinning,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) PrimaryGold.copy(alpha = 0.25f) else Color(0xFF131A2A),
                                    contentColor = if (isSelected) PrimaryGold else Color.White
                                ),
                                border = ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(
                                        if (isSelected) PrimaryGold else Color(0xFF2A3447)
                                    )
                                )
                            ) {
                                Text(
                                    text = "₹${bet.toInt()}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Spin Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Auto Spin Button
                        OutlinedButton(
                            onClick = {
                                if (!isSpinning) {
                                    if (!isAutoSpinning) {
                                        isAutoSpinning = true
                                        autoSpinsLeft = 5
                                        executeSpin()
                                    } else {
                                        isAutoSpinning = false
                                        autoSpinsLeft = 0
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isAutoSpinning) Color(0xFFDC2626) else Color(0xFF1E293B)
                            ),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    if (isAutoSpinning) Color.Red else Color(0xFF475569)
                                )
                            )
                        ) {
                            Text(
                                text = if (isAutoSpinning) "Stop ($autoSpinsLeft)" else "Auto 5x",
                                color = if (isAutoSpinning) Color.White else PrimaryGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        // Main Big Glowing SPIN Button
                        Button(
                            onClick = { executeSpin() },
                            enabled = !isSpinning && totalBalance >= selectedBet,
                            modifier = Modifier
                                .weight(2f)
                                .height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryGold,
                                disabledContainerColor = Color(0xFF334155)
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Casino, contentDescription = null, tint = DarkBackground)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isSpinning) "SPINNING..." else "SPIN (₹${selectedBet.toInt()})",
                                    color = DarkBackground,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

                    if (totalBalance < selectedBet) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Insufficient balance for ₹${selectedBet.toInt()} bet.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                            TextButton(
                                onClick = onOpenAddMoney,
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("+ Add Money", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Live Real-Time Jackpot Winners Marquee Ticker
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = PrimaryGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Live Jackpot Winners",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(liveWinners) { winner ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                modifier = Modifier.width(180.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = winner.userPhone,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = winner.timeAgo,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = PrimaryGold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "+₹%,.2f".format(winner.amount),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = SuccessGreen
                                    )
                                    Text(
                                        text = winner.gameName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Big Win & Mega Jackpot Celebratory Dialog
    if (showWinDialog && lastResult != null) {
        val res = lastResult!!
        AlertDialog(
            onDismissRequest = { showWinDialog = false },
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (res.isJackpot) "👑 MEGA JACKPOT! 👑" else "🎉 BIG WINNER! 🎉",
                        color = Color(0xFFFFD700),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${res.reel1}  ${res.reel2}  ${res.reel3}",
                        fontSize = 32.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "You Won",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹%,.2f".format(res.payout),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = SuccessGreen
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = res.winMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = PrimaryGold,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showWinDialog = false },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold)
                ) {
                    Text(
                        text = "Collect to Wallet",
                        color = DarkBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            },
            containerColor = Color(0xFF1E1736)
        )
    }

    // Paytable & Rules Dialog
    if (showPaytableDialog) {
        AlertDialog(
            onDismissRequest = { showPaytableDialog = false },
            title = {
                Text(
                    text = "🎰 Jackpot 777 Paytable",
                    color = PrimaryGold,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaytableRow(symbols = "7️⃣ 7️⃣ 7️⃣", payout = "50x + MEGA JACKPOT Pool", highlight = true)
                    PaytableRow(symbols = "💎 💎 💎", payout = "25x Diamond Payout")
                    PaytableRow(symbols = "🪙 🪙 🪙", payout = "15x Golden Coins")
                    PaytableRow(symbols = "🔔 🔔 🔔", payout = "10x Lucky Bells")
                    PaytableRow(symbols = "🍒 🍒 🍒", payout = "6x Cherry Blast")
                    PaytableRow(symbols = "🍀 🍀 🍀", payout = "4x Lucky Clover")
                    Divider(color = Color(0xFF334155))
                    PaytableRow(symbols = "7️⃣ 7️⃣ Any", payout = "3x Double Sevens")
                    PaytableRow(symbols = "Any 2 Match", payout = "1.5x Match Payout")
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPaytableDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold)
                ) {
                    Text("Got It", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
fun SingleReelView(
    symbol: String,
    isSpinning: Boolean,
    isWinning: Boolean
) {
    Box(
        modifier = Modifier
            .size(76.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isWinning) Color(0xFF4C1D95) else Color(0xFF181528))
            .border(
                width = if (isWinning) 2.dp else 1.dp,
                color = if (isWinning) Color(0xFFFFD700) else Color(0xFF2A2440),
                shape = RoundedCornerShape(14.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            fontSize = 38.sp,
            modifier = if (isSpinning) Modifier.scale(0.9f) else Modifier.scale(1f)
        )
    }
}

@Composable
fun PaytableRow(
    symbols: String,
    payout: String,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = symbols,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = payout,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (highlight) Color(0xFFFFD700) else SuccessGreen
        )
    }
}
