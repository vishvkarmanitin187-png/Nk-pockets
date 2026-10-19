package com.example.viewmodel

import androidx.lifecycle.ViewModel
import com.example.model.Transaction
import com.example.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.random.Random

data class JackpotSpinResult(
    val reel1: String,
    val reel2: String,
    val reel3: String,
    val payout: Double,
    val isJackpot: Boolean,
    val isBigWin: Boolean,
    val multiplier: Double,
    val winMessage: String
)

data class LiveWinner(
    val userPhone: String,
    val amount: Double,
    val gameName: String,
    val timeAgo: String
)

class MainViewModel : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _mobileNumber = MutableStateFlow("")
    val mobileNumber: StateFlow<String> = _mobileNumber.asStateFlow()

    private val _depositBalance = MutableStateFlow(0.0)
    val depositBalance: StateFlow<Double> = _depositBalance.asStateFlow()

    private val _winningBalance = MutableStateFlow(0.0)
    val winningBalance: StateFlow<Double> = _winningBalance.asStateFlow()

    private val _bonusBalance = MutableStateFlow(0.0)
    val bonusBalance: StateFlow<Double> = _bonusBalance.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _welcomeBonusAmount = MutableStateFlow<Double?>(null)
    val welcomeBonusAmount: StateFlow<Double?> = _welcomeBonusAmount.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Progressive Mega Jackpot Pool (grows on each spin)
    private val _progressiveJackpot = MutableStateFlow(87450.00)
    val progressiveJackpot: StateFlow<Double> = _progressiveJackpot.asStateFlow()

    private val _lastJackpotResult = MutableStateFlow<JackpotSpinResult?>(null)
    val lastJackpotResult: StateFlow<JackpotSpinResult?> = _lastJackpotResult.asStateFlow()

    private val _liveWinners = MutableStateFlow<List<LiveWinner>>(
        listOf(
            LiveWinner("98****4120", 5000.0, "Mega 777 Jackpot", "Just now"),
            LiveWinner("87****9914", 1250.0, "Triple Diamonds", "1m ago"),
            LiveWinner("91****2381", 750.0, "Golden Bells", "3m ago"),
            LiveWinner("70****8165", 2500.0, "Lucky 7s", "5m ago"),
            LiveWinner("99****3411", 500.0, "Cherry Blast", "8m ago")
        )
    )
    val liveWinners: StateFlow<List<LiveWinner>> = _liveWinners.asStateFlow()

    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun login(mobile: String) {
        if (mobile.length >= 10 || mobile.contains("@")) {
            _mobileNumber.value = mobile
            _isLoggedIn.value = true
            // Crediting random welcome bonus between 3 and 99
            val bonus = Random.nextInt(3, 100).toDouble()
            _bonusBalance.update { it + bonus }
            _welcomeBonusAmount.value = bonus
            
            val tx = Transaction(
                title = "Signup Welcome Bonus",
                amount = bonus,
                type = TransactionType.BONUS
            )
            _transactions.update { listOf(tx) + it }
        }
    }

    fun dismissWelcomeBonus() {
        _welcomeBonusAmount.value = null
    }

    fun dismissJackpotResult() {
        _lastJackpotResult.value = null
    }

    /**
     * Real 3-Reel Jackpot Slot Machine Logic:
     * Symbols: 7️⃣ (Lucky 7), 💎 (Diamond), 🪙 (Gold Coin), 🔔 (Bell), 🍒 (Cherry), 🍀 (Clover)
     */
    fun spinJackpot(bet: Double): JackpotSpinResult? {
        val totalBalance = _bonusBalance.value + _depositBalance.value + _winningBalance.value
        if (totalBalance < bet) {
            return null
        }

        // Deduct bet: prioritize Bonus -> Deposit -> Winning
        var remainingCost = bet
        if (_bonusBalance.value >= remainingCost) {
            _bonusBalance.update { it - remainingCost }
            remainingCost = 0.0
        } else {
            remainingCost -= _bonusBalance.value
            _bonusBalance.value = 0.0
        }

        if (remainingCost > 0 && _depositBalance.value >= remainingCost) {
            _depositBalance.update { it - remainingCost }
            remainingCost = 0.0
        } else if (remainingCost > 0) {
            remainingCost -= _depositBalance.value
            _depositBalance.value = 0.0
        }

        if (remainingCost > 0) {
            _winningBalance.update { (it - remainingCost).coerceAtLeast(0.0) }
        }

        // Add 5% of bet to the progressive jackpot pool
        _progressiveJackpot.update { it + (bet * 0.08) }

        // Authentic reel symbols
        val symbols = listOf("7️⃣", "💎", "🪙", "🔔", "🍒", "🍀")
        // Weighted probability (lower index = rarer)
        // 7️⃣: 8%, 💎: 12%, 🪙: 16%, 🔔: 18%, 🍒: 22%, 🍀: 24%
        val weights = listOf(8, 12, 16, 18, 22, 24)

        fun rollSymbol(): String {
            val totalWeight = weights.sum()
            var r = Random.nextInt(totalWeight)
            for (i in symbols.indices) {
                r -= weights[i]
                if (r < 0) return symbols[i]
            }
            return symbols.last()
        }

        // Guaranteed thrilling experience with realistic win rate (~40%)
        val forceWinRoll = Random.nextFloat()
        val s1: String
        val s2: String
        val s3: String

        if (forceWinRoll < 0.12f) {
            // Jackpot / Triple Match!
            val luckyPick = if (Random.nextFloat() < 0.25f) "7️⃣" else rollSymbol()
            s1 = luckyPick
            s2 = luckyPick
            s3 = luckyPick
        } else if (forceWinRoll < 0.42f) {
            // Double Match (2 out of 3)
            val match = rollSymbol()
            val other = symbols.filter { it != match }.random()
            val pattern = Random.nextInt(3)
            when (pattern) {
                0 -> { s1 = match; s2 = match; s3 = other }
                1 -> { s1 = match; s2 = other; s3 = match }
                else -> { s1 = other; s2 = match; s3 = match }
            }
        } else {
            // No match
            val shuffled = symbols.shuffled()
            s1 = shuffled[0]
            s2 = shuffled[1]
            s3 = shuffled[2]
        }

        // Calculate payout
        var payout = 0.0
        var isJackpot = false
        var isBigWin = false
        var multiplier = 0.0
        var winMsg = ""

        if (s1 == s2 && s2 == s3) {
            // 3 matching symbols!
            when (s1) {
                "7️⃣" -> {
                    // MEGA PROGRESSIVE JACKPOT!
                    val jackpotShare = (_progressiveJackpot.value * 0.10).coerceAtLeast(1000.0)
                    payout = (bet * 50.0) + jackpotShare
                    multiplier = 50.0
                    isJackpot = true
                    isBigWin = true
                    winMsg = "🏆 MEGA 777 JACKPOT HIT! Won ₹%.2f".format(payout)
                    _progressiveJackpot.update { (it - jackpotShare).coerceAtLeast(25000.0) }
                }
                "💎" -> {
                    multiplier = 25.0
                    payout = bet * multiplier
                    isBigWin = true
                    winMsg = "💎 TRIPLE DIAMOND WIN! (25x)"
                }
                "🪙" -> {
                    multiplier = 15.0
                    payout = bet * multiplier
                    isBigWin = true
                    winMsg = "🪙 GOLDEN COINS WIN! (15x)"
                }
                "🔔" -> {
                    multiplier = 10.0
                    payout = bet * multiplier
                    isBigWin = true
                    winMsg = "🔔 LUCKY BELLS WIN! (10x)"
                }
                "🍒" -> {
                    multiplier = 6.0
                    payout = bet * multiplier
                    winMsg = "🍒 CHERRY FEVER! (6x)"
                }
                "🍀" -> {
                    multiplier = 4.0
                    payout = bet * multiplier
                    winMsg = "🍀 LUCKY CLOVER! (4x)"
                }
            }
        } else if ((s1 == s2 && s1 == "7️⃣") || (s2 == s3 && s2 == "7️⃣") || (s1 == s3 && s1 == "7️⃣")) {
            // 2 Sevens
            multiplier = 3.0
            payout = bet * multiplier
            winMsg = "✨ DOUBLE 7s MATCH! (3x)"
        } else if (s1 == s2 || s2 == s3 || s1 == s3) {
            // Any 2 match
            multiplier = 1.5
            payout = bet * multiplier
            winMsg = "🎉 DOUBLE MATCH WIN! (1.5x)"
        } else {
            winMsg = "Better Luck Next Spin!"
        }

        if (payout > 0) {
            _winningBalance.update { it + payout }
            val tx = Transaction(
                title = "Jackpot 777 Win: $winMsg",
                amount = payout,
                type = TransactionType.WIN
            )
            _transactions.update { listOf(tx) + it }

            // Add to live winners feed
            val newWinner = LiveWinner(
                userPhone = if (_mobileNumber.value.length >= 6) _mobileNumber.value.take(2) + "****" + _mobileNumber.value.takeLast(4) else "You",
                amount = payout,
                gameName = if (isJackpot) "Mega Jackpot" else "Jackpot 777",
                timeAgo = "Just now"
            )
            _liveWinners.update { listOf(newWinner) + it.take(4) }
        } else {
            val tx = Transaction(
                title = "Jackpot 777 Spin",
                amount = bet,
                type = TransactionType.LOSS
            )
            _transactions.update { listOf(tx) + it }
        }

        val result = JackpotSpinResult(
            reel1 = s1,
            reel2 = s2,
            reel3 = s3,
            payout = payout,
            isJackpot = isJackpot,
            isBigWin = isBigWin,
            multiplier = multiplier,
            winMessage = winMsg
        )
        _lastJackpotResult.value = result
        return result
    }

    fun spinWheel(cost: Double): Double {
        var remainingCost = cost
        if (_bonusBalance.value >= remainingCost) {
            _bonusBalance.update { it - remainingCost }
            remainingCost = 0.0
        } else {
            remainingCost -= _bonusBalance.value
            _bonusBalance.value = 0.0
        }

        if (remainingCost > 0 && _depositBalance.value >= remainingCost) {
            _depositBalance.update { it - remainingCost }
            remainingCost = 0.0
        } else if (remainingCost > 0) {
            remainingCost -= _depositBalance.value
            _depositBalance.value = 0.0
        }

        if (remainingCost > 0) {
            _winningBalance.update { (it - remainingCost).coerceAtLeast(0.0) }
        }

        val prizes = listOf(0.0, 5.0, 10.0, 20.0, 50.0, 100.0)
        val weights = listOf(30, 25, 25, 12, 6, 2)
        val prize = getRandomWeighted(prizes, weights)

        if (prize > 0) {
            _winningBalance.update { it + prize }
            val tx = Transaction(
                title = "Spin & Win Prize",
                amount = prize,
                type = TransactionType.WIN
            )
            _transactions.update { listOf(tx) + it }
        } else {
            val tx = Transaction(
                title = "Spin & Win",
                amount = 0.0,
                type = TransactionType.LOSS
            )
            _transactions.update { listOf(tx) + it }
        }
        return prize
    }

    fun playPrediction(betAmount: Double, choice: String): Boolean {
        if (_depositBalance.value >= betAmount) {
            _depositBalance.update { it - betAmount }
        } else if (_winningBalance.value >= betAmount) {
            _winningBalance.update { it - betAmount }
        } else {
            return false
        }

        val isWin = Random.nextFloat() < 0.46
        if (isWin) {
            val winnings = betAmount * 2.0
            _winningBalance.update { it + winnings }
            val tx = Transaction(
                title = "Prediction Win ($choice)",
                amount = winnings,
                type = TransactionType.WIN
            )
            _transactions.update { listOf(tx) + it }
            return true
        } else {
            val tx = Transaction(
                title = "Prediction Loss ($choice)",
                amount = betAmount,
                type = TransactionType.LOSS
            )
            _transactions.update { listOf(tx) + it }
            return false
        }
    }

    fun claimAdReward() {
        val reward = 0.50
        _winningBalance.update { it + reward }
        val tx = Transaction(
            title = "Watch Ad Reward",
            amount = reward,
            type = TransactionType.REWARD
        )
        _transactions.update { listOf(tx) + it }
    }

    fun addMoney(amount: Double, upiId: String): Boolean {
        if (amount <= 0 || upiId.isBlank()) return false
        _depositBalance.update { it + amount }
        val tx = Transaction(
            title = "Add Money via UPI ($upiId)",
            amount = amount,
            type = TransactionType.DEPOSIT
        )
        _transactions.update { listOf(tx) + it }
        return true
    }

    fun deductBet(bet: Double, gameTitle: String): Boolean {
        val totalBalance = _bonusBalance.value + _depositBalance.value + _winningBalance.value
        if (totalBalance < bet) return false

        var remainingCost = bet
        if (_bonusBalance.value >= remainingCost) {
            _bonusBalance.update { it - remainingCost }
            remainingCost = 0.0
        } else {
            remainingCost -= _bonusBalance.value
            _bonusBalance.value = 0.0
        }

        if (remainingCost > 0 && _depositBalance.value >= remainingCost) {
            _depositBalance.update { it - remainingCost }
            remainingCost = 0.0
        } else if (remainingCost > 0) {
            remainingCost -= _depositBalance.value
            _depositBalance.value = 0.0
        }

        if (remainingCost > 0) {
            _winningBalance.update { (it - remainingCost).coerceAtLeast(0.0) }
        }

        val tx = Transaction(
            title = "$gameTitle Bet Placement",
            amount = bet,
            type = TransactionType.LOSS
        )
        _transactions.update { listOf(tx) + it }
        return true
    }

    fun creditWinnings(winnings: Double, gameTitle: String, multiplier: Double) {
        if (winnings > 0) {
            _winningBalance.update { it + winnings }
            val tx = Transaction(
                title = "$gameTitle Win (%.1fx)".format(multiplier),
                amount = winnings,
                type = TransactionType.WIN
            )
            _transactions.update { listOf(tx) + it }
        }
    }

    fun playCustomGame(bet: Double, gameTitle: String, multiplier: Double): Double {
        val totalBalance = _bonusBalance.value + _depositBalance.value + _winningBalance.value
        if (totalBalance < bet) return 0.0

        // Deduct bet from bonus -> deposit -> winning
        var remainingCost = bet
        if (_bonusBalance.value >= remainingCost) {
            _bonusBalance.update { it - remainingCost }
            remainingCost = 0.0
        } else {
            remainingCost -= _bonusBalance.value
            _bonusBalance.value = 0.0
        }

        if (remainingCost > 0 && _depositBalance.value >= remainingCost) {
            _depositBalance.update { it - remainingCost }
            remainingCost = 0.0
        } else if (remainingCost > 0) {
            remainingCost -= _depositBalance.value
            _depositBalance.value = 0.0
        }

        if (remainingCost > 0) {
            _winningBalance.update { (it - remainingCost).coerceAtLeast(0.0) }
        }

        val winnings = bet * multiplier
        if (winnings > 0) {
            _winningBalance.update { it + winnings }
            val tx = Transaction(
                title = "$gameTitle Win (%.1fx)".format(multiplier),
                amount = winnings,
                type = TransactionType.WIN
            )
            _transactions.update { listOf(tx) + it }
            return winnings
        } else {
            val tx = Transaction(
                title = "$gameTitle Play",
                amount = bet,
                type = TransactionType.LOSS
            )
            _transactions.update { listOf(tx) + it }
            return 0.0
        }
    }

    fun requestWithdrawal(amount: Double, upiId: String): Boolean {
        if (amount < 100.0 || upiId.isBlank() || _winningBalance.value < amount) return false
        _winningBalance.update { it - amount }
        val tx = Transaction(
            title = "Withdrawal to UPI ($upiId)",
            amount = amount,
            type = TransactionType.WITHDRAW
        )
        _transactions.update { listOf(tx) + it }
        return true
    }

    private fun getRandomWeighted(items: List<Double>, weights: List<Int>): Double {
        val totalWeight = weights.sum()
        var r = Random.nextInt(totalWeight)
        for (i in items.indices) {
            r -= weights[i]
            if (r < 0) return items[i]
        }
        return items.first()
    }
}
