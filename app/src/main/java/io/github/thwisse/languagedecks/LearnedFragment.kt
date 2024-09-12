package io.github.thwisse.languagedecks

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
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
        loadDeckData()

        val deckName = requireActivity().intent.getStringExtra("deckName")
        val deckList = sharedPreferencesManager.getDecks()
        currentDeck = deckList.find { it.deckName == deckName } ?: Deck(deckName ?: "")

        // Kart listesini önce temizleyelim, sonra yeniden dolduralım
        cardList.clear()

        // Sadece öğrenilmiş kartları listele ve loglayalım
        val learnedCards = currentDeck.cards.filter { it.isLearned }
//        for (card in learnedCards) {
//            Log.d("LearnedFragment KEKOD", "Word: ${card.word}, isLearned: ${card.isLearned}")
//        }

        cardList.addAll(learnedCards)

        // Kart adaptörünü başlatıyoruz
        cardAdapter = CardAdapter(cardList, { card ->
            // Kart tıklama işlemi
            Log.d("LearnedFragment KEKOD", "Clicked on: ${card.word}")
        }, { card ->
            // Kart uzun basma işlemi
            showCardPopupMenu(card)
        })

        binding.rvLearned.adapter = cardAdapter
        binding.rvLearned.layoutManager = LinearLayoutManager(context)

        return view
    }

    override fun onResume() {
        super.onResume()
        // Fragment geri döndüğünde verileri tekrar yükle
        loadDeckData()
        cardAdapter.notifyDataSetChanged()
    }

    private fun loadDeckData() {
        val deckName = requireActivity().intent.getStringExtra("deckName")
        val deckList = sharedPreferencesManager.getDecks()
        currentDeck = deckList.find { it.deckName == deckName } ?: Deck(deckName ?: "")

        // Kart listesini önce temizleyelim, sonra yeniden dolduralım
        cardList.clear()
        cardList.addAll(currentDeck.cards.filter { it.isLearned })
    }

    private fun showCardPopupMenu(card: Card) {
        val popupMenu = PopupMenu(requireContext(), requireView())
        popupMenu.menuInflater.inflate(R.menu.menu_card_options, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.itemEdit -> {
                    showEditCardDialog(card)
                    true
                }
                R.id.itemDelete -> {
                    showDeleteCardDialog(card)
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun showEditCardDialog(card: Card) {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_edit_card, null)

        val etWord = dialogLayout.findViewById<EditText>(R.id.edtEditWord)
        val etMeaning1 = dialogLayout.findViewById<EditText>(R.id.edtEditMeaning1)
        val etMeaning2 = dialogLayout.findViewById<EditText>(R.id.edtEditMeaning2)
        val btnSelectImage = dialogLayout.findViewById<Button>(R.id.btnEditImage)

        // Var olan kart verilerini doldur
        etWord.setText(card.word)
        etMeaning1.setText(card.meaning1)
        etMeaning2.setText(card.meaning2)

        btnSelectImage.setOnClickListener {
            // Galeri açma işlemi burada
        }

        with(builder) {
            setTitle("Edit Card")
            setView(dialogLayout)
            setPositiveButton("Save") { dialog, which ->
                card.word = etWord.text.toString()
                card.meaning1 = etMeaning1.text.toString()
                card.meaning2 = etMeaning2.text.toString()

                // Kartı güncelle ve kaydet
                sharedPreferencesManager.saveDecks(sharedPreferencesManager.getDecks())
                loadDeckData()
                cardAdapter.notifyDataSetChanged()
            }
            setNegativeButton("Cancel") { dialog, which -> }
            show()
        }
    }

    private fun showDeleteCardDialog(card: Card) {
        val builder = AlertDialog.Builder(requireContext())
        with(builder) {
            setTitle("Delete Card")
            setMessage("Are you sure?")
            setPositiveButton("Yes") { dialog, which ->
                currentDeck.cards.remove(card)
                sharedPreferencesManager.saveDecks(sharedPreferencesManager.getDecks())
                loadDeckData()
                cardAdapter.notifyDataSetChanged()
            }
            setNegativeButton("Cancel") { dialog, which -> }
            show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
