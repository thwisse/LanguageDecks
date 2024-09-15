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

        //TODO burdan cozmeye calis resim isini.
        cardAdapter = CardAdapter(cardList, { card ->
            val dialogFragment = CardDetailDialogFragment()
            dialogFragment.setTargetFragment(this, 0)
            dialogFragment.arguments = Bundle().apply {
                putString("word", card.word)
                putString("meaning", card.meaning)
                putString("definition", card.definition)
                putString("usage", card.usage)
            }
            dialogFragment.show(parentFragmentManager, "CardDetailDialogFragment")
        }, { card ->
            showCardPopupMenu(card)
        })

        binding.rvLearned.adapter = cardAdapter
        binding.rvLearned.layoutManager = LinearLayoutManager(context)

        loadDeckData()

        return view
    }

    override fun onResume() {
        super.onResume()
        loadDeckData()
        cardAdapter.notifyDataSetChanged()
        (activity as? DeckActivity)?.updateToolbar()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadDeckData()
        cardAdapter.notifyDataSetChanged()
    }

    private fun updateDeckInList() {
        val deckList = sharedPreferencesManager.getDecks()
        val deckIndex = deckList.indexOfFirst { it.deckName == currentDeck.deckName }
        if (deckIndex != -1) {
            deckList[deckIndex] = currentDeck
            sharedPreferencesManager.saveDecks(deckList)
        }
    }

    fun loadDeckData() {
        val deckName = requireActivity().intent.getStringExtra("deckName")
        val deckList = sharedPreferencesManager.getDecks()
        currentDeck = deckList.find { it.deckName == deckName } ?: Deck(deckName ?: "")

        cardList.clear()

        cardList.addAll(currentDeck.cards.filter { it.isLearned }.sortedBy { it.order })

        cardAdapter.notifyDataSetChanged()

        (activity as? DeckActivity)?.updateToolbar()
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
        val etMeaning = dialogLayout.findViewById<EditText>(R.id.edtEditMeaning)
        val etDefinition = dialogLayout.findViewById<EditText>(R.id.edtEditDefinition)
        val etUsage = dialogLayout.findViewById<EditText>(R.id.edtEditUsage)
        val btnSelectImage = dialogLayout.findViewById<Button>(R.id.btnEditImage)

        etWord.setText(card.word)
        etMeaning.setText(card.meaning)
        etDefinition.setText(card.definition)
        etUsage.setText(card.usage)

        btnSelectImage.setOnClickListener {}

        with(builder) {
            setTitle("Edit Card")
            setView(dialogLayout)
            setPositiveButton("Save") { dialog, which ->
                card.word = etWord.text.toString()
                card.meaning = etMeaning.text.toString()
                card.definition = etDefinition.text.toString()
                card.usage = etUsage.text.toString()

                updateDeckInList()
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
                updateDeckInList()
                loadDeckData()
                cardAdapter.notifyDataSetChanged()
                (activity as? DeckActivity)?.updateToolbar()
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
        loadDeckData()
        cardAdapter.notifyDataSetChanged()
    }
}
