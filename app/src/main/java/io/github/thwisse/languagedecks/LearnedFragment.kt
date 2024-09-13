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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.thwisse.languagedecks.databinding.FragmentLearnedBinding

class LearnedFragment : Fragment(), CardStateChangeListener {

    private var _binding: FragmentLearnedBinding? = null
    private val binding get() = _binding!!
    lateinit var cardAdapter: CardAdapter
    private val cardList: MutableList<Card> = mutableListOf()
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    private lateinit var currentDeck: Deck

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLearnedBinding.inflate(inflater, container, false)
        val view = binding.root

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sharedPreferencesManager = SharedPreferencesManager(requireContext())

        cardAdapter = CardAdapter(cardList, { card ->
            // Kart tıklandığında yapılacaklar
            val dialogFragment = CardDetailDialogFragment()
            dialogFragment.setTargetFragment(this, 0) // targetFragment ayarlandı
            dialogFragment.arguments = Bundle().apply {
                putString("word", card.word)
                putString("meaning1", card.meaning1)
                putString("meaning2", card.meaning2)
            }
            dialogFragment.show(parentFragmentManager, "CardDetailDialogFragment")
        }, { card ->
            // Kart uzun basıldığında yapılacaklar
            showCardPopupMenu(card)
        })


        binding.rvLearned.adapter = cardAdapter
        binding.rvLearned.layoutManager = LinearLayoutManager(context)

        loadDeckData()

        return view
    }

    override fun onResume() {
        super.onResume()
        Log.d("LearnedFragment KEKOD", "onResume called - Deck data is being loaded.")
        loadDeckData()
        cardAdapter.notifyDataSetChanged()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadDeckData()  // Deck verilerini yükle
        cardAdapter.notifyDataSetChanged()  // Adapter'ı güncelle
    }


    private fun updateDeckInList() {
        val deckList = sharedPreferencesManager.getDecks()
        val deckIndex = deckList.indexOfFirst { it.deckName == currentDeck.deckName }
        if (deckIndex != -1) {
            deckList[deckIndex] = currentDeck // currentDeck'teki güncellemeleri tüm listeye aktar
            sharedPreferencesManager.saveDecks(deckList)
//            Log.d("LearnedFragment KEKOD", "Deck updated in deckList and saved.")
        } else {
//            Log.e("LearnedFragment KEKOD", "Error: Deck not found in deckList")
        }
    }

    fun loadDeckData() {
        val deckName = requireActivity().intent.getStringExtra("deckName")
        val deckList = sharedPreferencesManager.getDecks()
        currentDeck = deckList.find { it.deckName == deckName } ?: Deck(deckName ?: "")

        // Kart listesini önce temizleyelim, sonra yeniden dolduralım
        cardList.clear()

        // Sıralamayı burada 'order' değerine göre yapalım
        cardList.addAll(currentDeck.cards.filter { it.isLearned }.sortedBy { it.order })

        // RecyclerView'i güncelle
        cardAdapter.notifyDataSetChanged()
    }


    private fun showCardPopupMenu(card: Card) {
        val popupMenu = PopupMenu(requireContext(), requireView())
        popupMenu.menuInflater.inflate(R.menu.menu_card_options, popupMenu.menu)

        // LearnedFragment'ta Toggle Unlearned menüsü
        val toggleMenuItem = popupMenu.menu.findItem(R.id.itemToggleLearned)
        toggleMenuItem.title = "Toggle Unlearned"

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
                    toggleUnlearnedState(card) // Unlearned'e taşı
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun toggleUnlearnedState(card: Card) {
        card.isLearned = false  // Kart artık "unlearned" olacak

        // Unlearned kartlara geçtiğinde order'ı en sona ekle
        val maxOrderInUnlearned = sharedPreferencesManager.getDecks()
            .flatMap { it.cards }
            .filter { !it.isLearned }
            .maxOfOrNull { it.order ?: 0 } ?: 0

        card.order = maxOrderInUnlearned + 1

        // Hafızadaki verileri güncelle
        updateDeckInList()

        // Kartı learned listesinden kaldır
        cardList.remove(card)
        cardAdapter.notifyDataSetChanged()

        // Unlearned fragment'teki listeyi güncelle
        (activity as DeckActivity).supportFragmentManager.findFragmentById(R.id.navHostFragmentView)?.let { fragment ->
            if (fragment is UnlearnedFragment) {
                fragment.loadDeckData()  // UnlearnedFragment güncelleniyor
            }
        }
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

    override fun onCardStateChanged() {
        // Verileri güncelleyip RecyclerView'i yenileme
        loadDeckData()
        cardAdapter.notifyDataSetChanged()
        Log.d("LearnedFragment", "Card durumu değişti, veri yenilendi.")
    }

}
