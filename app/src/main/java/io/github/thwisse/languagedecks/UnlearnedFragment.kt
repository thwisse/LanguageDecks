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

        // Kart adaptörünü başlatıyoruz (Bunu loadDeckData() öncesinde yapıyoruz)
        cardAdapter = CardAdapter(cardList, { card ->
            Log.d("UnlearnedFragment KEKOD", "Clicked card: ${card.word}")
        }, { card ->
            showCardPopupMenu(card)
        })

        binding.rvUnlearned.adapter = cardAdapter
        binding.rvUnlearned.layoutManager = LinearLayoutManager(context)

        // Verileri yükle ve RecyclerView'i güncelle
        loadDeckData()

        // Kart ekleme butonu
        binding.fabAddCard.setOnClickListener {
            showAddCardDialog()
        }

        // Swipe-to-Refresh işlemi
        binding.swipeRefreshLayout.setOnRefreshListener {
            shuffleCards() // Kartları karıştırma işlemi
            binding.swipeRefreshLayout.isRefreshing = false // Swipe Refresh'i durdur
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.swipeRefreshLayout.setOnRefreshListener {
            shuffleCards() // Kartları karıştırma işlemi
            binding.swipeRefreshLayout.isRefreshing = false // Swipe Refresh'i durdur
        }
    }

    override fun onResume() {
        super.onResume()
        // Fragment geri döndüğünde verileri tekrar yükle
        loadDeckData()
        cardAdapter.notifyDataSetChanged()
    }

    private fun shuffleCards() {
        cardList.shuffle()

        // Her karta benzersiz bir order değeri ata
        val uniqueOrderSet = mutableSetOf<Int>()
        cardList.forEachIndexed { index, card ->
            var order = index + 1
            while (uniqueOrderSet.contains(order)) {
                order++ // Eğer order numarası kullanılmışsa bir sonrakine geç
            }
            card.order = order
            uniqueOrderSet.add(order) // Order numarasını kaydet
        }

        updateDeckInList() // Hafızayı güncelle

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

    fun loadDeckData() {
        val deckName = requireActivity().intent.getStringExtra("deckName")
        val deckList = sharedPreferencesManager.getDecks()
        currentDeck = deckList.find { it.deckName == deckName } ?: Deck(deckName ?: "")

        // Kart listesini önce temizleyelim, sonra yeniden dolduralım
        cardList.clear()
        cardList.addAll(currentDeck.cards.filter { !it.isLearned }.sortedBy { it.order }) // Order değerine göre sırala

        // RecyclerView'den önce sıralama doğru yapılmalı
        cardAdapter.notifyDataSetChanged()
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
                // Kartı ekleyelim ve order değerini en sona atayalım
                val newCard = Card(
                    word = etWord.text.toString(),
                    meaning1 = etMeaning1.text.toString(),
                    meaning2 = etMeaning2.text.toString(),
                    isLearned = false,
                    order = cardList.size + 1 // Order numarası en son sıraya ekleniyor
                )
                currentDeck.cards.add(newCard)
                updateDeckInList() // Hafızayı güncelle
                loadDeckData() // Verileri yeniden yükle
                cardAdapter.notifyDataSetChanged() // RecyclerView güncelle
            }
            setNegativeButton("Cancel") { dialog, which -> }
            show()
        }
    }


    private fun showCardPopupMenu(card: Card) {
        val popupMenu = PopupMenu(requireContext(), requireView())
        popupMenu.menuInflater.inflate(R.menu.menu_card_options, popupMenu.menu)

        // UnlearnedFragment'ta Toggle Learned menüsü
        val toggleMenuItem = popupMenu.menu.findItem(R.id.itemToggleLearned)
        toggleMenuItem.title = "Toggle Learned"

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
                R.id.itemToggleLearned -> {
                    toggleLearnedState(card)
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun toggleLearnedState(card: Card) {
        card.isLearned = true  // Kart artık "learned" olacak

        // Learned kartlar arasında sıralama sağlamak için en yüksek order'ı bul
        val maxOrderInLearned = sharedPreferencesManager.getDecks()
            .flatMap { it.cards }
            .filter { it.isLearned }
            .maxOfOrNull { it.order ?: 0 } ?: 0

        // Kartı learned listesine eklerken en büyük order'ın bir fazlasını ata
        card.order = maxOrderInLearned + 1
        // Hafızadaki verileri güncelle
        updateDeckInList()

        // Verileri yeniden yükle ve RecyclerView'i güncelle
        loadDeckData()
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
