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
        val meaning = arguments?.getString("meaning")
        val definition = arguments?.getString("definition")
        val usage = arguments?.getString("usage")

        binding.textViewWord.text = word
        binding.textViewMeaning.text = meaning
        binding.textViewDefinition.text = definition
        binding.textViewUsage.text = usage

        if (binding.textViewDefinition.text.isNullOrEmpty()) {
            binding.textViewDefinition.visibility = View.GONE
        } else {
            binding.textViewDefinition.visibility = View.VISIBLE
        }

        if (binding.textViewUsage.text.isNullOrEmpty()) {
            binding.textViewUsage.visibility = View.GONE
        } else {
            binding.textViewUsage.visibility = View.VISIBLE
        }

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
