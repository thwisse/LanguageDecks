package io.github.thwisse.languagedecks

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import io.github.thwisse.languagedecks.databinding.FragmentCardDetailBinding

class CardDetailDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentCardDetailBinding
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    private lateinit var currentCard: Card
    private lateinit var currentDeck: Deck

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCardDetailBinding.inflate(inflater, container, false)
        sharedPreferencesManager = SharedPreferencesManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val word = arguments?.getString("word")
        val meaning1 = arguments?.getString("meaning1")
        val meaning2 = arguments?.getString("meaning2")

        binding.textViewWord.text = word
        binding.textViewMeaning1.text = meaning1
        binding.textViewMeaning2.text = meaning2

        val deckList = sharedPreferencesManager.getDecks()
        currentDeck = deckList.find { it.cards.any { card -> card.word == word } }!!
        currentCard = currentDeck.cards.find { it.word == word }!!

        updateToggleButton()

        binding.btnIslearningChanger.setOnClickListener {
            toggleLearnedState(currentCard)
            sharedPreferencesManager.saveDecks(deckList)
            dismiss()
            updateFragmentData()
        }

        if (currentCard.image != null) {
            val decodedByte = Base64.decode(currentCard.image, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)
            binding.imgViewCardImage.setImageBitmap(bitmap)
            binding.imgViewCardImage.visibility = View.VISIBLE
        } else {
            binding.imgViewCardImage.visibility = View.GONE
        }
    }

    private fun updateToggleButton() {
        if (currentCard.isLearned) {
            binding.btnIslearningChanger.text = "Toggle Unlearned"
            binding.btnIslearningChanger.setBackgroundColor(resources.getColor(R.color.red))
        } else {
            binding.btnIslearningChanger.text = "Toggle Learned"
            binding.btnIslearningChanger.setBackgroundColor(resources.getColor(R.color.green))
        }
    }

    private fun toggleLearnedState(card: Card) {
        if (card.isLearned) {
            card.isLearned = false
            val maxOrderInUnlearned = currentDeck.cards
                .filter { !it.isLearned }
                .maxOfOrNull { it.order } ?: 0
            card.order = maxOrderInUnlearned + 1
        } else {
            card.isLearned = true
            val maxOrderInLearned = currentDeck.cards
                .filter { it.isLearned }
                .maxOfOrNull { it.order } ?: 0
            card.order = maxOrderInLearned + 1
        }
    }

    private fun updateFragmentData() {
        val parentFragment = targetFragment
        if (parentFragment is CardStateChangeListener) {
            parentFragment.onCardStateChanged()
        }
    }
}
