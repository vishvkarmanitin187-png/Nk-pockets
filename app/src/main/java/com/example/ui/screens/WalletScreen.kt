package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Transaction
import com.example.model.TransactionType
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PrimaryGold
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.ErrorRed

@Composable
fun WalletScreen(
    depositBalance: Double,
    winningBalance: Double,
    bonusBalance: Double,
    transactions: List<Transaction>,
    onAddMoney: (Double, String) -> Boolean,
    onWithdraw: (Double, String) -> Boolean
) {
    var selectedWalletTab by remember { mutableStateOf(0) } // 0: Deposit, 1: Withdrawal

    // Deposit state
    var depositAmountStr by remember { mutableStateOf("500") }
    var depositUpi by remember { mutableStateOf("user@okhdfcbank") }
    var depositSuccessMsg by remember { mutableStateOf<String?>(null) }
    var depositErrorMsg by remember { mutableStateOf<String?>(null) }

    // Withdrawal state
    var withdrawAmountStr by remember { mutableStateOf("100") }
    var withdrawUpi by remember { mutableStateOf("user@paytm") }
    var withdrawSuccessMsg by remember { mutableStateOf<String?>(null) }
    var withdrawErrorMsg by remember { mutableStateOf<String?>(null) }

    val totalBalance = depositBalance + winningBalance + bonusBalance

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Text(
                text = "Real-Money Wallet",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = PrimaryGold
            )
        }

        // Balance Breakdown Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Total Available Balance",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "₹%.2f".format(totalBalance),
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryGold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = DarkSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        WalletBalanceItem(title = "Deposit Cash", amount = depositBalance)
                        WalletBalanceItem(title = "Winnings (Withdrawable)", amount = winningBalance)
                        WalletBalanceItem(title = "Bonus Cash", amount = bonusBalance)
                    }
                }
            }
        }

        // Deposit & Withdrawal Tabs
        item {
            TabRow(
                selectedTabIndex = selectedWalletTab,
                containerColor = Color(0xFF131A2A),
                contentColor = PrimaryGold,
                divider = { Divider(color = Color(0xFF1E293B)) }
            ) {
                Tab(
                    selected = selectedWalletTab == 0,
                    onClick = {
                        selectedWalletTab = 0
                        depositSuccessMsg = null
                        depositErrorMsg = null
                    },
                    text = {
                        Text(
                            text = "Deposit",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (selectedWalletTab == 0) PrimaryGold else Color.Gray
                        )
                    },
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = null, tint = if (selectedWalletTab == 0) PrimaryGold else Color.Gray) }
                )
                Tab(
                    selected = selectedWalletTab == 1,
                    onClick = {
                        selectedWalletTab = 1
                        withdrawSuccessMsg = null
                        withdrawErrorMsg = null
                    },
                    text = {
                        Text(
                            text = "Withdrawal",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (selectedWalletTab == 1) PrimaryGold else Color.Gray
                        )
                    },
                    icon = { Icon(Icons.Default.ArrowOutward, contentDescription = null, tint = if (selectedWalletTab == 1) PrimaryGold else Color.Gray) }
                )
            }
        }

        // Tab Content: Deposit or Withdrawal
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    if (selectedWalletTab == 0) {
                        // DEPOSIT SECTION
                        Text(
                            text = "Add Money to Deposit Wallet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Quick-select an amount or enter custom amount:",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Deposit Buttons: ₹100, ₹500, ₹1000, ₹5000
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(100.0, 500.0, 1000.0, 5000.0).forEach { quickAmt ->
                                val isSelected = depositAmountStr == quickAmt.toInt().toString()
                                OutlinedButton(
                                    onClick = { depositAmountStr = quickAmt.toInt().toString() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isSelected) PrimaryGold.copy(alpha = 0.25f) else Color(0xFF1E293B),
                                        contentColor = if (isSelected) PrimaryGold else Color.White
                                    ),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(
                                            if (isSelected) PrimaryGold else Color(0xFF334155)
                                        )
                                    ),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    Text("₹${quickAmt.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = depositAmountStr,
                            onValueChange = {
                                depositAmountStr = it
                                depositSuccessMsg = null
                                depositErrorMsg = null
                            },
                            label = { Text("Deposit Amount (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryGold,
                                unfocusedBorderColor = DarkSurfaceVariant
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = depositUpi,
                            onValueChange = { depositUpi = it },
                            label = { Text("UPI ID (e.g. yourname@upi)") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryGold,
                                unfocusedBorderColor = DarkSurfaceVariant
                            )
                        )

                        if (depositSuccessMsg != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(depositSuccessMsg!!, color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }

                        if (depositErrorMsg != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Error, contentDescription = null, tint = ErrorRed)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(depositErrorMsg!!, color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val amt = depositAmountStr.toDoubleOrNull() ?: 0.0
                                if (amt <= 0) {
                                    depositErrorMsg = "Please enter a valid amount greater than ₹0"
                                    depositSuccessMsg = null
                                } else if (depositUpi.isBlank()) {
                                    depositErrorMsg = "Please enter a valid UPI ID"
                                    depositSuccessMsg = null
                                } else {
                                    val ok = onAddMoney(amt, depositUpi)
                                    if (ok) {
                                        depositSuccessMsg = "✅ ₹%.2f deposited successfully via UPI!".format(amt)
                                        depositErrorMsg = null
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold)
                        ) {
                            Text(
                                text = "Instant Deposit (₹${depositAmountStr.toDoubleOrNull()?.toInt() ?: 0})",
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    } else {
                        // WITHDRAWAL SECTION
                        Text(
                            text = "Withdraw Winnings to UPI",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Available Winning Balance: ₹%.2f (Min: ₹100)".format(winningBalance),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = SuccessGreen
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Withdraw Chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(100.0, 500.0, 1000.0).forEach { quickAmt ->
                                val isSelected = withdrawAmountStr == quickAmt.toInt().toString()
                                OutlinedButton(
                                    onClick = { withdrawAmountStr = quickAmt.toInt().toString() },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (isSelected) PrimaryGold.copy(alpha = 0.25f) else Color(0xFF1E293B),
                                        contentColor = if (isSelected) PrimaryGold else Color.White
                                    ),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(
                                        brush = androidx.compose.ui.graphics.SolidColor(
                                            if (isSelected) PrimaryGold else Color(0xFF334155)
                                        )
                                    ),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    Text("₹${quickAmt.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                            OutlinedButton(
                                onClick = {
                                    if (winningBalance >= 100) {
                                        withdrawAmountStr = winningBalance.toInt().toString()
                                    }
                                },
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF1E293B), contentColor = PrimaryGold),
                                border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF334155))),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                Text("All Winnings", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = withdrawAmountStr,
                            onValueChange = {
                                withdrawAmountStr = it
                                withdrawSuccessMsg = null
                                withdrawErrorMsg = null
                            },
                            label = { Text("Withdrawal Amount (Min ₹100)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryGold,
                                unfocusedBorderColor = DarkSurfaceVariant
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = withdrawUpi,
                            onValueChange = { withdrawUpi = it },
                            label = { Text("Your UPI ID (e.g. yourname@paytm)") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryGold,
                                unfocusedBorderColor = DarkSurfaceVariant
                            )
                        )

                        if (withdrawSuccessMsg != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(withdrawSuccessMsg!!, color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }

                        if (withdrawErrorMsg != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Error, contentDescription = null, tint = ErrorRed)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(withdrawErrorMsg!!, color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val amt = withdrawAmountStr.toDoubleOrNull() ?: 0.0
                                if (amt < 100.0) {
                                    withdrawErrorMsg = "❌ Minimum withdrawal limit is ₹100.00"
                                    withdrawSuccessMsg = null
                                } else if (amt > winningBalance) {
                                    withdrawErrorMsg = "❌ Insufficient winning balance! Available: ₹%.2f".format(winningBalance)
                                    withdrawSuccessMsg = null
                                } else if (withdrawUpi.isBlank() || !withdrawUpi.contains("@")) {
                                    withdrawErrorMsg = "❌ Please enter a valid UPI ID (e.g. mobile@upi)"
                                    withdrawSuccessMsg = null
                                } else {
                                    val ok = onWithdraw(amt, withdrawUpi)
                                    if (ok) {
                                        withdrawSuccessMsg = "✅ ₹%.2f withdrawn successfully to $withdrawUpi!".format(amt)
                                        withdrawErrorMsg = null
                                    } else {
                                        withdrawErrorMsg = "❌ Withdrawal failed. Please check your balance."
                                        withdrawSuccessMsg = null
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold)
                        ) {
                            Text(
                                text = "Request Withdrawal (₹${withdrawAmountStr.toDoubleOrNull()?.toInt() ?: 0})",
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // Transaction History header
        item {
            Text(
                text = "Transaction History",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(transactions) { tx ->
                TransactionCard(tx = tx)
            }
        }
    }
}

@Composable
fun TransactionCard(tx: Transaction) {
    val isPositive = tx.type in listOf(TransactionType.BONUS, TransactionType.WIN, TransactionType.REWARD, TransactionType.DEPOSIT)
    val color = if (isPositive) SuccessGreen else ErrorRed

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = color.copy(alpha = 0.15f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = when (tx.type) {
                                TransactionType.BONUS -> Icons.Default.CardGiftcard
                                TransactionType.WIN -> Icons.Default.EmojiEvents
                                TransactionType.LOSS -> Icons.Default.Close
                                TransactionType.REWARD -> Icons.Default.PlayCircle
                                TransactionType.DEPOSIT -> Icons.Default.Add
                                TransactionType.WITHDRAW -> Icons.Default.ArrowOutward
                            },
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = tx.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tx.timestamp,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "${if (isPositive) "+" else "-"}₹%.2f".format(tx.amount),
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}

