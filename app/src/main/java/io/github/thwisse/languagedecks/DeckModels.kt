package io.github.thwisse.languagedecks

data class SampleDeck(
    val deckName: String,
    val cards: List<SampleCard>
)

data class SampleCard(
    val word: String,
    val meaning: String,
    val isLearned: Boolean
)
