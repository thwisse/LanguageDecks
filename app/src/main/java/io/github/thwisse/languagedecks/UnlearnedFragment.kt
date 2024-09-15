package io.github.thwisse.languagedecks

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.thwisse.languagedecks.databinding.FragmentUnlearnedBinding
import java.io.ByteArrayOutputStream

class UnlearnedFragment : Fragment(), CardStateChangeListener {

    private var _binding: FragmentUnlearnedBinding? = null
    private val binding get() = _binding!!
    lateinit var cardAdapter: CardAdapter
    private val cardList: MutableList<Card> = mutableListOf()
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    private lateinit var currentDeck: Deck
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var selectedBitmap: Bitmap? = null
    private var selectedImageView: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val imageUri = result.data?.data
                selectedBitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageUri)
                selectedImageView?.setImageBitmap(selectedBitmap)
                selectedImageView?.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUnlearnedBinding.inflate(inflater, container, false)
        val view = binding.root

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sharedPreferencesManager = SharedPreferencesManager(requireContext())

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

        binding.rvUnlearned.adapter = cardAdapter
        binding.rvUnlearned.layoutManager = LinearLayoutManager(context)

        loadDeckData()

        binding.fabAddCard.setOnClickListener {
            showAddCardDialog()
            (activity as? DeckActivity)?.updateToolbar()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            shuffleCards()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.swipeRefreshLayout.setOnRefreshListener {
            shuffleCards()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        loadDeckData()
        cardAdapter.notifyDataSetChanged()
    }

    override fun onResume() {
        super.onResume()
        loadDeckData()
        cardAdapter.notifyDataSetChanged()
        (activity as? DeckActivity)?.updateToolbar()
    }

    private fun shuffleCards() {
        cardList.shuffle()

        val uniqueOrderSet = mutableSetOf<Int>()
        cardList.forEachIndexed { index, card ->
            var order = index + 1
            while (uniqueOrderSet.contains(order)) {
                order++
            }
            card.order = order
            uniqueOrderSet.add(order)
        }

        updateDeckInList()

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

        if (!sharedPreferencesManager.isImagesAssigned()) {
            assignImagesToCards()
            sharedPreferencesManager.setImagesAssigned(true)
        }

        cardList.clear()
        cardList.addAll(currentDeck.cards.filter { !it.isLearned }.sortedBy { it.order })

        cardAdapter.notifyDataSetChanged()

        (activity as? DeckActivity)?.updateToolbar()
    }

    private fun showAddCardDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_add_card, null)

        val etWord = dialogLayout.findViewById<EditText>(R.id.edtWord)
        val etMeaning = dialogLayout.findViewById<EditText>(R.id.edtMeaning)
        val etDefinition = dialogLayout.findViewById<EditText>(R.id.edtDefinition)
        val etUsage = dialogLayout.findViewById<EditText>(R.id.edtUsage)
        val btnSelectImage = dialogLayout.findViewById<Button>(R.id.btnSelectImage)
        val imgViewPreview = dialogLayout.findViewById<ImageView>(R.id.imgViewAddPreview)

        selectedImageView = imgViewPreview
        selectedImageView?.visibility = View.GONE

        btnSelectImage.setOnClickListener {
            val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intentToGallery)
        }

        builder.setView(dialogLayout)
            .setPositiveButton("Add") { dialog, _ ->
                val word = etWord.text.toString()
                val definition = etDefinition.text.toString()
                val meaning = etMeaning.text.toString()
                val usage = etUsage.text.toString()

                if (word.isNotEmpty() && meaning.isNotEmpty()) {
                    val maxOrder = currentDeck.cards.maxOfOrNull { it.order } ?: 0

                    val newCard = Card(
                        word = word,
                        meaning = meaning,
                        definition = definition,
                        image = selectedBitmap?.let { bitmapToBase64(it) },
                        isLearned = false,
                        order = maxOrder + 1,
                        usage = usage
                    )
                    currentDeck.cards.add(newCard)
                    updateDeckInList()
                    loadDeckData()
                    dialog.dismiss()

                    (activity as? DeckActivity)?.updateToolbar()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = builder.create()
        dialog.show()
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 75, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
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
        val imgViewPreview = dialogLayout.findViewById<ImageView>(R.id.imgViewEditPreview)

        etWord.setText(card.word)
        etMeaning.setText(card.meaning)
        etDefinition.setText(card.definition)
        etUsage.setText(card.usage)

        if (card.image != null) {
            val decodedByte = Base64.decode(card.image, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)
            imgViewPreview.setImageBitmap(bitmap)
            imgViewPreview.visibility = View.VISIBLE
        } else {
            imgViewPreview.visibility = View.GONE
        }

        btnSelectImage.setOnClickListener {
            val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intentToGallery)
            selectedImageView = imgViewPreview
        }

        with(builder) {
            setTitle("Edit Card")
            setView(dialogLayout)
            setPositiveButton("Save") { dialog, which ->
                card.word = etWord.text.toString()
                card.definition = etDefinition.text.toString()
                card.meaning = etMeaning.text.toString()
                card.usage = etUsage.text.toString()

                if (selectedBitmap != null) {
                    val base64Image = bitmapToBase64(selectedBitmap!!)
                    card.image = base64Image
                    selectedBitmap = null
                }

                updateDeckInList()
                loadDeckData()
                cardAdapter.notifyDataSetChanged()
            }
            setNegativeButton("Cancel") { dialog, which ->
                selectedBitmap = null
            }
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

    private fun assignImagesToCards() {
        for ((index, card) in currentDeck.cards.withIndex()) {
            val imageName = "image${index + 1}"
            val resourceId = resources.getIdentifier(imageName, "drawable", requireContext().packageName)

            if (resourceId != 0) {
                val bitmap = BitmapFactory.decodeResource(resources, resourceId)
                card.image = bitmapToBase64(bitmap)
            }
        }

        updateDeckInList()
    }
}
