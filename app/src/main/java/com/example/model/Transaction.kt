package com.example.model

data class Transaction(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val timestamp: String = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
)

enum class TransactionType {
    BONUS, WIN, LOSS, REWARD, DEPOSIT, WITHDRAW
}
