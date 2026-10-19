package com.example.model

enum class GameCategory(val displayName: String) {
    ALL("All Games"),
    SLOTS("Slots"),
    JACKPOTS("Jackpots"),
    CRASH("Crash/Multiplier"),
    ARCADE("Arcade"),
    RACING("Racing")
}

data class GameItem(
    val id: Int,
    val title: String,
    val category: GameCategory,
    val icon: String,
    val multiplier: String,
    val minBet: Int = 10,
    val rating: Double = 4.8,
    val isHot: Boolean = false
)

object GameRepository {
    val gamesList: List<GameItem> = listOf(
        GameItem(1, "Aviator Crash", GameCategory.CRASH, "✈️", "100x", 10, 4.9, isHot = true),
        GameItem(2, "Jackpot Lotto", GameCategory.JACKPOTS, "🎟️", "500x", 25, 4.8, isHot = true),
        GameItem(3, "500x Lucky Gems", GameCategory.SLOTS, "💎", "500x", 20, 4.7),
        GameItem(4, "Power of Kraken", GameCategory.ARCADE, "🐙", "50x", 15, 4.6),
        GameItem(5, "Color Trading", GameCategory.CRASH, "🎨", "2x - 9x", 10, 4.9, isHot = true),
        GameItem(6, "Dice Roller", GameCategory.ARCADE, "🎲", "6x", 10, 4.7),
        GameItem(7, "3D Car Racing", GameCategory.RACING, "🏎️", "25x", 20, 4.8),
        GameItem(8, "3D Bike Racing", GameCategory.RACING, "🏍️", "20x", 20, 4.7),
        GameItem(9, "Jungle Delight", GameCategory.SLOTS, "🌴", "40x", 10, 4.6),
        GameItem(10, "Emoji Riches", GameCategory.ARCADE, "🤑", "35x", 15, 4.7),
        GameItem(11, "Uono Scratch", GameCategory.ARCADE, "🃏", "100x", 10, 4.6),
        GameItem(12, "Elf Archer", GameCategory.ARCADE, "🏹", "30x", 15, 4.5),
        GameItem(13, "Fortunate Jaguar", GameCategory.SLOTS, "🐆", "45x", 20, 4.8),
        GameItem(14, "Wild Banditio", GameCategory.SLOTS, "🤠", "50x", 25, 4.7, isHot = true),
        GameItem(15, "Big Stack Lumberjack", GameCategory.ARCADE, "🪓", "25x", 10, 4.5),
        GameItem(16, "Evil Eyes", GameCategory.SLOTS, "👁️", "60x", 20, 4.6),
        GameItem(17, "Fortune Jungle", GameCategory.SLOTS, "🦁", "55x", 15, 4.7),
        GameItem(18, "Samba Drums", GameCategory.ARCADE, "🥁", "30x", 10, 4.5),
        GameItem(19, "Lucky Gems IV", GameCategory.SLOTS, "🔮", "150x", 25, 4.8),
        GameItem(20, "Wild Ape", GameCategory.SLOTS, "🦍", "70x", 20, 4.7),
        GameItem(21, "Ace Ultra", GameCategory.ARCADE, "♠️", "80x", 20, 4.8),
        GameItem(22, "Phantom Mustang", GameCategory.RACING, "🐎", "35x", 25, 4.7),
        GameItem(23, "Poker Kingdom", GameCategory.ARCADE, "👑", "120x", 50, 4.9, isHot = true),
        GameItem(24, "Super 777", GameCategory.JACKPOTS, "🎰", "200x", 10, 5.0, isHot = true),
        GameItem(25, "Gems II Fortune", GameCategory.SLOTS, "💍", "90x", 20, 4.6),
        GameItem(26, "Book of Books", GameCategory.SLOTS, "📖", "100x", 25, 4.8),
        GameItem(27, "Pinata Wins", GameCategory.ARCADE, "🪅", "40x", 15, 4.6),
        GameItem(28, "Jurassic World", GameCategory.SLOTS, "🦖", "75x", 20, 4.7),
        GameItem(29, "Baking Master", GameCategory.ARCADE, "🧁", "20x", 10, 4.5),
        GameItem(30, "Dark Hunters", GameCategory.ARCADE, "🏹", "85x", 25, 4.8)
    )
}
