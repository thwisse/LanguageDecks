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

    // Yeni deste ekleme işlemi
    private fun saveNewDeckToFile(deckName: String) {
        // Yeni deste için kartlar listesi oluştur
        val cards = mutableListOf<SampleCard>()  // Yeni destenin kartları, başlangıçta boş olabilir

        // Bu destenin kartlarını JSON olarak kaydet
        val sharedPreferences = getSharedPreferences("DecksPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val cardsJson = Gson().toJson(cards)  // Kartlar JSON formatında kaydedilecek
        editor.putString(deckName, cardsJson)
        editor.apply()  // Değişiklikleri uygula
    }

    private fun saveDeckToSharedPreferences(deckName: String, cards: List<SampleCard>) {
        val sharedPreferences = getSharedPreferences("DecksPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Kartları JSON formatında kaydet
        val cardsJson = Gson().toJson(cards)
        editor.putString(deckName, cardsJson)  // Her desteye ait kartlar, o destenin adıyla kaydedilecek
        editor.apply()
    }



    private fun loadDeckFromSharedPreferences(deckName: String): List<SampleCard> {
        val sharedPreferences = getSharedPreferences("DecksPrefs", Context.MODE_PRIVATE)
        val cardsJson = sharedPreferences.getString(deckName, null)

        return if (cardsJson != null) {
            val type = object : TypeToken<List<SampleCard>>() {}.type
            Gson().fromJson(cardsJson, type)
        } else {
            emptyList()  // Eğer kartlar yoksa boş liste döner
        }
    }

    private fun openDeckActivity(deckName: String) {
        val intent = Intent(this, DeckActivity::class.java)
        intent.putExtra("deckName", deckName)

        // Kartları SharedPreferences'tan okuyalım
        val cards = loadDeckFromSharedPreferences(deckName)

        // Kartları JSON olarak intent ile gönder
        intent.putExtra("cardsJson", Gson().toJson(cards))
        startActivity(intent)
    }


    private fun loadSampleDeckIfNeeded() {
        val sharedPreferences = getSharedPreferences("DecksPrefs", Context.MODE_PRIVATE)

        // Eğer daha önce hiç deste yoksa (örneğin, ilk defa yüklendiyse)
        if (!sharedPreferences.contains("English / Turkish")) {
            val inputStream = assets.open("sample_deck.json")
            val reader = InputStreamReader(inputStream)
            val sampleDeck = Gson().fromJson(reader, SampleDeck::class.java)

            // Örnek desteyi SharedPreferences'a kaydet
            saveDeckToSharedPreferences(sampleDeck.deckName, sampleDeck.cards)

            reader.close()  // InputStream'i kapat
        }
    }

    private fun loadDecksFromPreferences() {
        val sharedPreferences = getSharedPreferences("DecksPrefs", Context.MODE_PRIVATE)
        val allDecks = sharedPreferences.all.keys.filter { it != "deckList" }  // "deckList" adıyla kayıtlı bir veri varsa, bunu hariç tutun

        deckList.clear()
        deckList.addAll(allDecks)
    }



}
