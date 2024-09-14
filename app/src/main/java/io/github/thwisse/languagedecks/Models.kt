package io.github.thwisse.languagedecks

data class Deck(
    var deckName: String,
    val cards: MutableList<Card> = mutableListOf()
)

data class Card(
    var word: String,
    var meaning1: String,
    var meaning2: String,
    var image: String? = null,
    var isLearned: Boolean = false,
    var order: Int = 0,
)