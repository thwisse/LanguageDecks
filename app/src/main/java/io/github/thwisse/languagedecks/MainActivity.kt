package io.github.thwisse.languagedecks

import android.content.Context
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

        // RecyclerView ayarları
        recyclerView = binding.rvDecks
        recyclerView.layoutManager = LinearLayoutManager(this)

        // İlk kez çalıştırılıyorsa örnek deste yükle
        loadSampleDeckIfNeeded()

        // SharedPreferences'tan desteleri yükle
        loadDecksFromPreferences()

        // Adapter'i oluştur ve RecyclerView'a bağla
        adapterDecks = AdapterDecks(deckList)
        recyclerView.adapter = adapterDecks

        binding.fabAddDeck.setOnClickListener {
            showAddDeckDialog()
        }

    }

    private fun showAddDeckDialog() {
        val dialogBinding = DialogAddDeckBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add New Deck")
            .setView(dialogBinding.root)
            .setPositiveButton("Add") { dialogInterface, _ ->
                val deckName = dialogBinding.edtEnterDeckName.text.toString()

                if (deckName.isNotEmpty()) {
                    deckList.add(deckName)
                    adapterDecks.notifyDataSetChanged()

                    // Yeni deste JSON dosyasına kaydedilsin
                    saveNewDeckToFile(deckName)

                    // SharedPreferences'a kaydet
                    saveDecksToPreferences()
                }
                dialogInterface.dismiss()
            }
            .setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.show()
    }


    private fun loadSampleDeckIfNeeded() {
        val sharedPreferences = getSharedPreferences("DecksPrefs", Context.MODE_PRIVATE)

        // Örnek desteyi yalnızca ilk kez çalıştırıldığında yükle
        if (!sharedPreferences.contains("deckList")) {
            // Örnek desteyi assets klasöründen yükle
            val inputStream = assets.open("sample_deck.json")
            val reader = InputStreamReader(inputStream)
            val sampleDeck = Gson().fromJson(reader, SampleDeck::class.java)

            // Desteyi SharedPreferences'a kaydet
            val editor = sharedPreferences.edit()
            val deckJson = Gson().toJson(listOf(sampleDeck.deckName))
            val cardsJson = Gson().toJson(sampleDeck.cards)
            editor.putString("deckList", deckJson)
            editor.putString("unlearnedList", cardsJson)
            editor.apply()
        }
    }

    private fun loadDecksFromPreferences() {
        val sharedPreferences = getSharedPreferences("DecksPrefs", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("deckList", null)

        if (json != null) {
            val type = object : com.google.gson.reflect.TypeToken<MutableList<String>>() {}.type
            deckList = Gson().fromJson(json, type)
        }
    }

    // SharedPreferences'a desteleri kaydet
    private fun saveDecksToPreferences() {
        val sharedPreferences = getSharedPreferences("DecksPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        val json = Gson().toJson(deckList)
        editor.putString("deckList", json)
        editor.apply() // Veriyi kaydet
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