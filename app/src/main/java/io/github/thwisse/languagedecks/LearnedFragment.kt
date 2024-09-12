package io.github.thwisse.languagedecks

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.thwisse.languagedecks.databinding.FragmentLearnedBinding

class LearnedFragment : Fragment() {

    private var _binding: FragmentLearnedBinding? = null
    private val binding get() = _binding!!
    private lateinit var cardAdapter: CardAdapter
    private val cardList: MutableList<Card> = mutableListOf()
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    private lateinit var currentDeck: Deck

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLearnedBinding.inflate(inflater, container, false)
        val view = binding.root

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sharedPreferencesManager = SharedPreferencesManager(requireContext())

        val deckName = requireActivity().intent.getStringExtra("deckName")
        val deckList = sharedPreferencesManager.getDecks()
        currentDeck = deckList.find { it.deckName == deckName } ?: Deck(deckName ?: "")

        // Kart listesini önce temizleyelim, sonra yeniden dolduralım
        cardList.clear()

        // Sadece öğrenilmiş kartları listele ve loglayalım
        val learnedCards = currentDeck.cards.filter { it.isLearned }
        for (card in learnedCards) {
            Log.e("LearnedFragment", "Word: ${card.word}, isLearned: ${card.isLearned}")
        }

        cardList.addAll(learnedCards)

        cardAdapter = CardAdapter(cardList) { card ->
            // Kart tıklama işlemi
        }

        binding.rvLearned.adapter = cardAdapter
        binding.rvLearned.layoutManager = LinearLayoutManager(context)

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
