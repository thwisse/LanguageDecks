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
import io.github.thwisse.languagedecks.databinding.FragmentUnlearnedBinding

class UnlearnedFragment : Fragment() {

    private var _binding: FragmentUnlearnedBinding? = null
    private val binding get() = _binding!!
    private lateinit var cardAdapter: CardAdapter
    private val cardList: MutableList<Card> = mutableListOf()
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    private lateinit var currentDeck: Deck // Şu anda işlem yapılan deste

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUnlearnedBinding.inflate(inflater, container, false)
        val view = binding.root

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // SharedPreferencesManager'ı başlatıyoruz
        sharedPreferencesManager = SharedPreferencesManager(requireContext())
        loadDeckData()

        Log.d("UnlearnedFragment KEKOD", "onCreateView() called, currentDeck: ${currentDeck.deckName}, Cards: ${currentDeck.cards.size}")

        // Fragment'e gelen deste ismini alıyoruz
        val deckName = requireActivity().intent.getStringExtra("deckName")
        val deckList = sharedPreferencesManager.getDecks()
        currentDeck = deckList.find { it.deckName == deckName } ?: Deck(deckName ?: "")

        // Kart listesini önce temizleyelim, sonra yeniden dolduralım
        cardList.clear()

        // Sadece öğrenilmemiş kartları listele ve loglayalım
        val unlearnedCards = currentDeck.cards.filter { !it.isLearned }
//        for (card in unlearnedCards) {
//            Log.d("UnlearnedFragment KEKOD", "Word: ${card.word}, isLearned: ${card.isLearned}")
//        }

        cardList.addAll(unlearnedCards)

        // Kart adaptörünü başlatıyoruz
        cardAdapter = CardAdapter(cardList, { card ->
            Log.d("UnlearnedFragment KEKOD", "Clicked card: ${card.word}")
        }, { card ->
            showCardPopupMenu(card)
        })

        binding.rvUnlearned.adapter = cardAdapter
        binding.rvUnlearned.layoutManager = LinearLayoutManager(context)

        // Kart ekleme butonu
        binding.fabAddCard.setOnClickListener {
            showAddCardDialog()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        // Fragment geri döndüğünde verileri tekrar yükle
        loadDeckData()
        cardAdapter.notifyDataSetChanged()
    }

    private fun updateDeckInList() {
        val deckList = sharedPreferencesManager.getDecks()
        val deckIndex = deckList.indexOfFirst { it.deckName == currentDeck.deckName }
        if (deckIndex != -1) {
            deckList[deckIndex] = currentDeck // currentDeck'teki güncellemeleri tüm listeye aktar
            sharedPreferencesManager.saveDecks(deckList)
            Log.d("UnlearnedFragment KEKOD", "Deck updated in deckList and saved.")
        } else {
            Log.e("UnlearnedFragment KEKOD", "Error: Deck not found in deckList")
        }
    }

    private fun loadDeckData() {
        val deckName = requireActivity().intent.getStringExtra("deckName")
        val deckList = sharedPreferencesManager.getDecks()
        currentDeck = deckList.find { it.deckName == deckName } ?: Deck(deckName ?: "")

        Log.d("UnlearnedFragment KEKOD", "loadDeckData() - Current deck: ${currentDeck.deckName}, Cards: ${currentDeck.cards.size}")

        // Kart listesini önce temizleyelim, sonra yeniden dolduralım
        cardList.clear()
        cardList.addAll(currentDeck.cards.filter { !it.isLearned })
    }

    private fun showAddCardDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_add_card, null)

        val etWord = dialogLayout.findViewById<EditText>(R.id.edtWord)
        val etMeaning1 = dialogLayout.findViewById<EditText>(R.id.edtMeaning1)
        val etMeaning2 = dialogLayout.findViewById<EditText>(R.id.edtMeaning2)

        with(builder) {
            setTitle("Add New Card")
            setView(dialogLayout)
            setPositiveButton("Add") { dialog, which ->
                val newCard = Card(
                    word = etWord.text.toString(),
                    meaning1 = etMeaning1.text.toString(),
                    meaning2 = etMeaning2.text.toString(),
                    isLearned = false // Yeni eklenen kart öğrenilmemiş olarak ekleniyor
                )
                currentDeck.cards.add(newCard) // Kartı currentDeck'e ekle
                updateDeckInList() // Değişiklikleri hafızaya yaz
                loadDeckData() // Verileri tekrar yükle
                cardAdapter.notifyDataSetChanged() // RecyclerView'i güncelle
            }
            setNegativeButton("Cancel") { dialog, which -> }
            show()
        }
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

        etWord.setText(card.word)
        etMeaning1.setText(card.meaning1)
        etMeaning2.setText(card.meaning2)

        with(builder) {
            setTitle("Edit Card")
            setView(dialogLayout)
            setPositiveButton("Save") { dialog, which ->
                card.word = etWord.text.toString()
                card.meaning1 = etMeaning1.text.toString()
                card.meaning2 = etMeaning2.text.toString()

                updateDeckInList() // Değişiklikleri hafızaya yaz
                loadDeckData() // Verileri tekrar yükle
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
                updateDeckInList() // Değişiklikleri hafızaya yaz
                loadDeckData() // Verileri tekrar yükle
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
