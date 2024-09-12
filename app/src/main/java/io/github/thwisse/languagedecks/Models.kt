package io.github.thwisse.languagedecks

data class Deck(
    val deckName: String,
    val cards: MutableList<Card> = mutableListOf()
)

data class Card(
    val word: String,
    val meaning1: String,
    val meaning2: String,
    val image: String? = null, // Base64 olarak resim saklanacak
    var isLearned: Boolean = false
)