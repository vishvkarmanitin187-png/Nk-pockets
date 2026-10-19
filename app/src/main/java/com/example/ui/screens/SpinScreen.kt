package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PrimaryGold
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SpinScreen(
    winningBalance: Double,
    bonusBalance: Double,
    depositBalance: Double,
    onSpin: (Double) -> Double
) {
    val coroutineScope = rememberCoroutineScope()
    var isSpinning by remember { mutableStateOf(false) }
    var lastPrize by remember { mutableStateOf<Double?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val totalBalance = winningBalance + bonusBalance + depositBalance
    val spinCost = 10.0

    val rotationController = remember { Animatable(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title Bar
        Text(
            text = "Lucky Spin & Win",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = PrimaryGold
        )
        Text(
            text = "Spin for ₹10 & win up to ₹100 instantly!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Spin Wheel UI container
        Box(
            modifier = Modifier
                .size(280.dp)
                .background(DarkSurface, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            val currentRotation by rotationController.asState()

            Canvas(
                modifier = Modifier
                    .size(260.dp)
                    .rotate(currentRotation)
            ) {
                val slices = 6
                val sweep = 360f / slices
                val colors = listOf(
                    Color(0xFF8B5CF6),
                    Color(0xFFEC4899),
                    Color(0xFF3B82F6),
                    Color(0xFF10B981),
                    Color(0xFFF59E0B),
                    Color(0xFFEF4444)
                )

                for (i in 0 until slices) {
                    drawArc(
                        color = colors[i % colors.size],
                        startAngle = i * sweep,
                        sweepAngle = sweep,
                        useCenter = true
                    )
                }
            }

            // Center Spin Button / Indicator
            Surface(
                shape = CircleShape,
                color = PrimaryGold,
                modifier = Modifier.size(70.dp),
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "SPIN",
                        color = DarkBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Spin Cost: ₹10.00",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (!isSpinning && totalBalance >= spinCost) {
                            isSpinning = true
                            coroutineScope.launch {
                                val randomExtra = (720..1440).random().toFloat()
                                val target = rotationController.value + randomExtra
                                rotationController.animateTo(
                                    targetValue = target,
                                    animationSpec = tween(durationMillis = 2500, easing = FastOutSlowInEasing)
                                )
                                val prize = onSpin(spinCost)
                                lastPrize = prize
                                isSpinning = false
                                showDialog = true
                            }
                        }
                    },
                    enabled = !isSpinning && totalBalance >= spinCost,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold)
                ) {
                    Text(
                        text = if (isSpinning) "Spinning..." else "Tap to Spin (₹10)",
                        color = DarkBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                if (totalBalance < spinCost) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Insufficient balance. Please add money to spin!",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }

    if (showDialog && lastPrize != null) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = if (lastPrize!! > 0) "🎉 You Won ₹${lastPrize}!" else "😢 Bad Luck!",
                    color = if (lastPrize!! > 0) SuccessGreen else MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = if (lastPrize!! > 0)
                        "Congratulations! ₹${lastPrize} has been added to your winning balance."
                    else
                        "You didn't win this time. Try again for another chance!"
                )
            },
            confirmButton = {
                Button(
                    onClick = { showDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold)
                ) {
                    Text("Awesome", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DarkSurface
        )
    }
}
