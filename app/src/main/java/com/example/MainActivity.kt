package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.GameItem
import com.example.ui.screens.*
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryGold
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
                val mobileNumber by viewModel.mobileNumber.collectAsStateWithLifecycle()
                val depositBalance by viewModel.depositBalance.collectAsStateWithLifecycle()
                val winningBalance by viewModel.winningBalance.collectAsStateWithLifecycle()
                val bonusBalance by viewModel.bonusBalance.collectAsStateWithLifecycle()
                val transactions by viewModel.transactions.collectAsStateWithLifecycle()
                val welcomeBonusAmount by viewModel.welcomeBonusAmount.collectAsStateWithLifecycle()
                val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
                val progressiveJackpot by viewModel.progressiveJackpot.collectAsStateWithLifecycle()
                val liveWinners by viewModel.liveWinners.collectAsStateWithLifecycle()

                var showWatchAdsDialog by remember { mutableStateOf(false) }
                var selectedGameForPlay by remember { mutableStateOf<GameItem?>(null) }

                if (!isLoggedIn) {
                    AuthScreen(
                        onLogin = { mobile -> viewModel.login(mobile) }
                    )
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = DarkBackground,
                        bottomBar = {
                            NavigationBar(
                                containerColor = DarkSurface
                            ) {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                    label = { Text("Home") },
                                    selected = selectedTab == 0,
                                    onClick = { viewModel.setSelectedTab(0) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PrimaryGold,
                                        selectedTextColor = PrimaryGold,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        indicatorColor = PrimaryGold.copy(alpha = 0.2f)
                                    )
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Casino, contentDescription = "Jackpot") },
                                    label = { Text("Jackpot") },
                                    selected = selectedTab == 1,
                                    onClick = { viewModel.setSelectedTab(1) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PrimaryGold,
                                        selectedTextColor = PrimaryGold,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        indicatorColor = PrimaryGold.copy(alpha = 0.2f)
                                    )
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.FilterTiltShift, contentDescription = "Spin") },
                                    label = { Text("Spin") },
                                    selected = selectedTab == 2,
                                    onClick = { viewModel.setSelectedTab(2) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PrimaryGold,
                                        selectedTextColor = PrimaryGold,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        indicatorColor = PrimaryGold.copy(alpha = 0.2f)
                                    )
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Palette, contentDescription = "Predict") },
                                    label = { Text("Predict") },
                                    selected = selectedTab == 3,
                                    onClick = { viewModel.setSelectedTab(3) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PrimaryGold,
                                        selectedTextColor = PrimaryGold,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        indicatorColor = PrimaryGold.copy(alpha = 0.2f)
                                    )
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Wallet") },
                                    label = { Text("Wallet") },
                                    selected = selectedTab == 4,
                                    onClick = { viewModel.setSelectedTab(4) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = PrimaryGold,
                                        selectedTextColor = PrimaryGold,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        indicatorColor = PrimaryGold.copy(alpha = 0.2f)
                                    )
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            when (selectedTab) {
                                0 -> HomeScreen(
                                    depositBalance = depositBalance,
                                    winningBalance = winningBalance,
                                    bonusBalance = bonusBalance,
                                    progressiveJackpot = progressiveJackpot,
                                    mobileNumber = mobileNumber,
                                    onNavigateTab = { viewModel.setSelectedTab(it) },
                                    onOpenAddMoney = { viewModel.setSelectedTab(4) },
                                    onOpenWithdraw = { viewModel.setSelectedTab(4) },
                                    onOpenWatchAds = { showWatchAdsDialog = true },
                                    onSelectGame = { game -> selectedGameForPlay = game }
                                )
                                1 -> JackpotScreen(
                                    depositBalance = depositBalance,
                                    winningBalance = winningBalance,
                                    bonusBalance = bonusBalance,
                                    progressiveJackpot = progressiveJackpot,
                                    liveWinners = liveWinners,
                                    onSpinJackpot = { bet -> viewModel.spinJackpot(bet) },
                                    onOpenAddMoney = { viewModel.setSelectedTab(4) }
                                )
                                2 -> SpinScreen(
                                    winningBalance = winningBalance,
                                    bonusBalance = bonusBalance,
                                    depositBalance = depositBalance,
                                    onSpin = { cost -> viewModel.spinWheel(cost) }
                                )
                                3 -> PredictionScreen(
                                    depositBalance = depositBalance,
                                    winningBalance = winningBalance,
                                    onPlayPrediction = { amt, choice -> viewModel.playPrediction(amt, choice) }
                                )
                                4 -> WalletScreen(
                                    depositBalance = depositBalance,
                                    winningBalance = winningBalance,
                                    bonusBalance = bonusBalance,
                                    transactions = transactions,
                                    onAddMoney = { amt, upi -> viewModel.addMoney(amt, upi) },
                                    onWithdraw = { amt, upi -> viewModel.requestWithdrawal(amt, upi) }
                                )
                            }
                        }
                    }
                }

                // Welcome Bonus Dialog
                if (welcomeBonusAmount != null) {
                    AlertDialog(
                        onDismissRequest = { viewModel.dismissWelcomeBonus() },
                        title = { Text("🎉 Welcome Bonus Credited!", color = PrimaryGold) },
                        text = {
                            Text(
                                text = "Congratulations! You received ₹${"%.2f".format(welcomeBonusAmount)} welcome bonus instantly credited to your bonus balance.",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = { viewModel.dismissWelcomeBonus() },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold)
                            ) {
                                Text("Start Playing", color = DarkBackground, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            }
                        },
                        containerColor = DarkSurface
                    )
                }

                // Watch Ads & Earn Dialog
                if (showWatchAdsDialog) {
                    Dialog(
                        onDismissRequest = { showWatchAdsDialog = false },
                        properties = DialogProperties(usePlatformDefaultWidth = false)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = DarkBackground
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                WatchAdsScreen(
                                    onClaimReward = { viewModel.claimAdReward() }
                                )
                                IconButton(
                                    onClick = { showWatchAdsDialog = false },
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = PrimaryGold)
                                }
                            }
                        }
                    }
                }

                // 30 Games Arena Modal
                if (selectedGameForPlay != null) {
                    Dialog(
                        onDismissRequest = { selectedGameForPlay = null },
                        properties = DialogProperties(usePlatformDefaultWidth = false)
                    ) {
                        GameArenaModal(
                            game = selectedGameForPlay!!,
                            depositBalance = depositBalance,
                            winningBalance = winningBalance,
                            bonusBalance = bonusBalance,
                            onDeductBet = { bet, title -> viewModel.deductBet(bet, title) },
                            onCreditWin = { won, title, mult -> viewModel.creditWinnings(won, title, mult) },
                            onOpenDeposit = { viewModel.setSelectedTab(4) },
                            onClose = { selectedGameForPlay = null }
                        )
                    }
                }
            }
        }
    }
}
