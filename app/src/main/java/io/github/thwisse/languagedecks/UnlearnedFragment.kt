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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.thwisse.languagedecks.databinding.FragmentUnlearnedBinding
import java.io.ByteArrayOutputStream

class UnlearnedFragment : Fragment(), CardStateChangeListener {

    private var _binding: FragmentUnlearnedBinding? = null
    private val binding get() = _binding!!
    lateinit var cardAdapter: CardAdapter
    private val cardList: MutableList<Card> = mutableListOf()
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    private lateinit var currentDeck: Deck // Şu anda işlem yapılan deste
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
                // Eğer dialog açıkken resim seçilmişse imgViewPreview'e yansıt
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

        // SharedPreferencesManager'ı başlatıyoruz
        sharedPreferencesManager = SharedPreferencesManager(requireContext())

        cardAdapter = CardAdapter(cardList, { card ->
            // Kart tıklandığında yapılacaklar
            val dialogFragment = CardDetailDialogFragment()
            dialogFragment.setTargetFragment(this, 0)
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

        loadDeckData()  // Deck verilerini yükle
        cardAdapter.notifyDataSetChanged()  // Adapter'ı güncelle
    }

    override fun onResume() {
        super.onResume()
//        Log.d("UnlearnedFragment KEKOD", "onResume called - Deck data is being loaded.")
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
            // Log ile kartın resim bilgisini kaydetmeden önce kontrol edelim
            currentDeck.cards.forEach {
                Log.d("UnlearnedFragment KEKOD", "Card before saving: ${it.word}, Image length: ${it.image?.length}")
            }

            // Güncellenmiş deck'i listeye yerleştiriyoruz
            deckList[deckIndex] = currentDeck
            sharedPreferencesManager.saveDecks(deckList)

            Log.d("UnlearnedFragment KEKOD", "Deck updated and saved successfully. Number of cards: ${currentDeck.cards.size}")
            currentDeck.cards.forEach {
                Log.d("UnlearnedFragment KEKOD", "Updated card after saving: ${it.word}, Image length: ${it.image?.length}")
            }
        } else {
            Log.e("UnlearnedFragment KEKOD", "Error: Deck not found in deckList")
        }
    }


    fun loadDeckData() {
        val deckName = requireActivity().intent.getStringExtra("deckName")
        val deckList = sharedPreferencesManager.getDecks()
        currentDeck = deckList.find { it.deckName == deckName } ?: Deck(deckName ?: "")

        // Kart listesini temizleyip tekrar yükleyelim
        cardList.clear()
        cardList.addAll(currentDeck.cards.filter { !it.isLearned }.sortedBy { it.order })

        // RecyclerView'i güncelle
        cardAdapter.notifyDataSetChanged()

        Log.d("UnlearnedFragment KEKOD", "Deck data loaded, card list size: ${cardList.size}")
        currentDeck.cards.forEach {
            Log.d("UnlearnedFragment KEKOD", "Loaded card: ${it.word}, Image length: ${it.image?.length}")
        }
    }


    private fun showAddCardDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_add_card, null)

        val etWord = dialogLayout.findViewById<EditText>(R.id.edtWord)
        val etMeaning1 = dialogLayout.findViewById<EditText>(R.id.edtMeaning1)
        val etMeaning2 = dialogLayout.findViewById<EditText>(R.id.edtMeaning2)
        val btnSelectImage = dialogLayout.findViewById<Button>(R.id.btnSelectImage)
        val imgViewPreview = dialogLayout.findViewById<ImageView>(R.id.imgViewAddPreview)
        val btnAddCard = dialogLayout.findViewById<Button>(R.id.btnAddCard) // XML'deki Add butonu
        val btnCancelCard = dialogLayout.findViewById<Button>(R.id.btnCancelCard) // XML'deki Cancel butonu

        selectedImageView = imgViewPreview
        selectedImageView?.visibility = View.GONE

        btnSelectImage.setOnClickListener {
            val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intentToGallery)
        }

        val dialog = builder.setView(dialogLayout).create()

        btnAddCard.setOnClickListener {
            val word = etWord.text.toString()
            val meaning1 = etMeaning1.text.toString()
            val meaning2 = etMeaning2.text.toString()

            if (word.isNotEmpty() && meaning1.isNotEmpty()) {
                // Mevcut kartlar arasında en yüksek 'order' değerini bul
                val maxOrder = currentDeck.cards.maxOfOrNull { it.order } ?: 0

                // Yeni kart eklenirken order değerini maxOrder'dan bir fazlası olarak ata
                val newCard = Card(
                    word = word,
                    meaning1 = meaning1,
                    meaning2 = meaning2,
                    image = selectedBitmap?.let { bitmapToBase64(it) },
                    isLearned = false,
                    order = maxOrder + 1
                )
                Log.d("UnlearnedFragment KEKOD", "New card created: $newCard") // Log ile kartı kontrol edelim
                currentDeck.cards.add(newCard)
                updateDeckInList() // Güncellemeyi kaydediyoruz
                loadDeckData() // Verileri yeniden yüklüyoruz
                Log.d("UnlearnedFragment KEKOD", "Current deck after adding card: $currentDeck")
                dialog.dismiss()
            }
        }

        btnCancelCard.setOnClickListener {
            dialog.dismiss()
        }

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
        val btnSelectImage = dialogLayout.findViewById<Button>(R.id.btnEditImage)
        val imgViewPreview = dialogLayout.findViewById<ImageView>(R.id.imgViewEditPreview)

        etWord.setText(card.word)
        etMeaning1.setText(card.meaning1)
        etMeaning2.setText(card.meaning2)

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
            selectedImageView = imgViewPreview // Seçilen resmi burada gösterelim
        }

        with(builder) {
            setTitle("Edit Card")
            setView(dialogLayout)
            setPositiveButton("Save") { dialog, which ->
                card.word = etWord.text.toString()
                card.meaning1 = etMeaning1.text.toString()
                card.meaning2 = etMeaning2.text.toString()

                // Eğer yeni bir resim seçildiyse onu kaydet
                if (selectedBitmap != null) {
                    val base64Image = bitmapToBase64(selectedBitmap!!)
                    card.image = base64Image
                    Log.d("UnlearnedFragment KEKOD", "New image Base64 length: ${base64Image.length}")
                    selectedBitmap = null
                }

                updateDeckInList() // Güncellemeleri kaydet
                loadDeckData() // Verileri yeniden yükle
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
//        Log.d("UnlearnedFragment", "Card durumu değişti, veri yenilendi.")
    }
}
