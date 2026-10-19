package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PrimaryGold
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.ErrorRed
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PredictionScreen(
    depositBalance: Double,
    winningBalance: Double,
    onPlayPrediction: (Double, String) -> Boolean
) {
    var selectedColor by remember { mutableStateOf("Green") }
    var selectedBetAmount by remember { mutableStateOf(10.0) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var isWinResult by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val totalAvailable = depositBalance + winningBalance

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Color Prediction Game",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = PrimaryGold
        )
        Text(
            text = "Pick a color, place your bet & win 2x instantly!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Color selection options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ColorCard(
                title = "Green",
                color = SuccessGreen,
                isSelected = selectedColor == "Green",
                modifier = Modifier.weight(1f),
                onClick = { selectedColor = "Green" }
            )
            ColorCard(
                title = "Violet",
                color = Color(0xFF8B5CF6),
                isSelected = selectedColor == "Violet",
                modifier = Modifier.weight(1f),
                onClick = { selectedColor = "Violet" }
            )
            ColorCard(
                title = "Red",
                color = ErrorRed,
                isSelected = selectedColor == "Red",
                modifier = Modifier.weight(1f),
                onClick = { selectedColor = "Red" }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bet Amount Selector
        Text(
            text = "Select Bet Amount",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(10.0, 50.0, 100.0, 500.0).forEach { amount ->
                OutlinedButton(
                    onClick = { selectedBetAmount = amount },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (selectedBetAmount == amount) PrimaryGold.copy(alpha = 0.2f) else DarkSurface,
                        contentColor = if (selectedBetAmount == amount) PrimaryGold else MaterialTheme.colorScheme.onSurface
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(if (selectedBetAmount == amount) PrimaryGold else DarkSurfaceVariant)
                    )
                ) {
                    Text("₹${amount.toInt()}", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Play Button
        Button(
            onClick = {
                if (!isPlaying && totalAvailable >= selectedBetAmount) {
                    isPlaying = true
                    resultMessage = "Analyzing round..."
                    coroutineScope.launch {
                        delay(1200)
                        val won = onPlayPrediction(selectedBetAmount, selectedColor)
                        isWinResult = won
                        resultMessage = if (won) "🎉 You Won ₹${selectedBetAmount * 2} on $selectedColor!" else "❌ You Lost ₹$selectedBetAmount. Try Again!"
                        isPlaying = false
                    }
                }
            },
            enabled = !isPlaying && totalAvailable >= selectedBetAmount,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold)
        ) {
            Text(
                text = if (isPlaying) "Spinning Result..." else "Place Bet (₹${selectedBetAmount.toInt()})",
                color = DarkSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        if (totalAvailable < selectedBetAmount) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Insufficient balance for ₹${selectedBetAmount.toInt()} bet.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (resultMessage != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isWinResult) SuccessGreen.copy(alpha = 0.2f) else ErrorRed.copy(alpha = 0.2f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = resultMessage!!,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isWinResult) SuccessGreen else ErrorRed
                    )
                }
            }
        }
    }
}

@Composable
fun ColorCard(
    title: String,
    color: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) PrimaryGold else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "2x Payout",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
