package io.github.thwisse.languagedecks

data class Deck(
    var deckName: String,
    val cards: MutableList<Card> = mutableListOf()
)

data class Card(
    var word: String,
    var meaning1: String,
    var meaning2: String,
    var image: String? = null, // Base64 olarak resim saklanacak
    var isLearned: Boolean = false
)