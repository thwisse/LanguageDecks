package io.github.thwisse.languagedecks

import AdapterDecks
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.github.thwisse.languagedecks.databinding.ActivityMainBinding
import io.github.thwisse.languagedecks.databinding.DialogAddDeckBinding
import java.io.File
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapterDecks: AdapterDecks
    private var deckList = mutableListOf<String>() // Boş liste, SharedPreferences'tan veri okuyacağız

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root

        enableEdgeToEdge()

        setContentView(view)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Örnek desteyi yükle (ilk açılışta gerekli)
        loadSampleDeckIfNeeded()

        // RecyclerView'ı bul ve layoutManager'ı ayarla
        recyclerView = binding.rvDecks
        recyclerView.layoutManager = LinearLayoutManager(this)

        // SharedPreferences'tan desteleri oku
        loadDecksFromPreferences()

        // Adapter'i oluştur ve RecyclerView'a bağla
        adapterDecks = AdapterDecks(deckList) { selectedDeck ->
            openDeckActivity(selectedDeck)  // Tıklanan desteyi aç
        }
        recyclerView.adapter = adapterDecks

        binding.fabAddDeck.setOnClickListener {
            showAddDeckDialog()
        }

    }

    // RecyclerView itemına tıklanınca DeckActivity'yi aç
    private fun openDeckActivity(deckName: String) {
        val intent = Intent(this, DeckActivity::class.java)
        intent.putExtra("deckName", deckName)  // Deste adını intent ile gönder

        // Eğer bu örnek deste ise, kartlarını da DeckActivity'ye gönderelim
        val sharedPreferences = getSharedPreferences("DecksPrefs", Context.MODE_PRIVATE)
        val cardsJson = sharedPreferences.getString("unlearnedList", null)

        // Kartları intent ile DeckActivity'ye gönder
        intent.putExtra("cardsJson", cardsJson)

        startActivity(intent)
    }

    // SharedPreferences'tan desteleri yükle
    private fun loadDecksFromPreferences() {
        val sharedPreferences = getSharedPreferences("DecksPrefs", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("deckList", null)

        if (json != null) {
            val type = object : TypeToken<MutableList<String>>() {}.type
            deckList = Gson().fromJson(json, type)
        }
    }

    private fun saveDecksToPreferences() {
        val sharedPreferences = getSharedPreferences("DecksPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val json = Gson().toJson(deckList)
        editor.putString("deckList", json)
        editor.apply() // Veriyi kaydet
    }

    // Yeni bir deste eklemek için dialog oluşturma
    private fun showAddDeckDialog() {
        val dialogBinding = DialogAddDeckBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add New Deck")
            .setView(dialogBinding.root)
            .setPositiveButton("Add") { dialogInterface, _ ->
                val deckName = dialogBinding.edtDeckName.text.toString()

                if (deckName.isNotEmpty()) {
                    deckList.add(deckName)
                    adapterDecks.notifyDataSetChanged() // RecyclerView'ı güncelle
                    saveNewDeckToFile(deckName)  // Yeni desteyi JSON dosyasına kaydet
                    saveDecksToPreferences() // SharedPreferences'a kaydet
                }
                dialogInterface.dismiss()
            }
            .setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.show()
    }

    // Örnek desteyi ilk açılışta yükleme
    private fun loadSampleDeckIfNeeded() {
        val sharedPreferences = getSharedPreferences("DecksPrefs", Context.MODE_PRIVATE)

        // Örnek desteyi yalnızca ilk kez çalıştırıldığında yükle
        if (!sharedPreferences.contains("deckList")) {
            try {
                // Örnek desteyi assets klasöründen yükle
                val inputStream = assets.open("sample_deck.json")
                val reader = InputStreamReader(inputStream)
                val sampleDeck = Gson().fromJson(reader, SampleDeck::class.java)

                // Desteyi SharedPreferences'a kaydet
                val editor = sharedPreferences.edit()
                val deckJson = Gson().toJson(listOf(sampleDeck.deckName)) // Örnek deste adını kaydet
                val cardsJson = Gson().toJson(sampleDeck.cards) // Örnek kartları kaydet
                editor.putString("deckList", deckJson)
                editor.putString("unlearnedList", cardsJson) // Kartları SharedPreferences'a kaydediyoruz
                editor.apply()

                reader.close() // InputStream'i kapatmayı unutmayın!

                // Yeni eklenen desteyi listeye ekleyelim ve RecyclerView'u güncelleyelim
                deckList.add(sampleDeck.deckName)
                adapterDecks.notifyDataSetChanged()

            } catch (e: Exception) {
                e.printStackTrace() // Hata varsa log'ları kontrol edin
            }
        }
    }

    private fun saveNewDeckToFile(deckName: String) {
        // Yeni deste için JSON dosyası oluştur
        val fileName = "$deckName.json"
        val file = File(filesDir, fileName)

        // Deste boş bir kart listesiyle başlatılacak
        val cards = mutableListOf<SampleCard>() // Kartları boş başlatıyoruz
        val deck = SampleDeck(deckName, cards)

        // JSON'a çevir ve dosyaya yaz
        val json = Gson().toJson(deck)
        file.writeText(json)
    }
}
