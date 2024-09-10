package io.github.thwisse.languagedecks

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
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

        // RecyclerView'ı bul ve layoutManager'ı ayarla
        recyclerView = binding.rvDecks
        recyclerView.layoutManager = LinearLayoutManager(this)

        // SharedPreferences'tan desteleri oku
        loadDecksFromPreferences()

        // Adapter'i oluştur ve RecyclerView'a bağla
        adapterDecks = AdapterDecks(deckList)
        recyclerView.adapter = adapterDecks

        binding.fabAddDeck.setOnClickListener {
            showAddDeckDialog()
        }

    }

    // Yeni bir deste eklemek için dialog oluşturma
    private fun showAddDeckDialog() {
        // DialogAddDeckBinding ile dialog layout'unu bağla
        val dialogBinding = DialogAddDeckBinding.inflate(layoutInflater)

        // AlertDialog oluştur
        val dialog = AlertDialog.Builder(this)
            .setTitle("Add New Deck")
            .setView(dialogBinding.root)
            .setPositiveButton("Add") { dialogInterface, _ ->
                val deckName = dialogBinding.edtEnterDeckName.text.toString()

                // Deste ismi boş değilse listeye ve SharedPreferences'a ekle
                if (deckName.isNotEmpty()) {
                    deckList.add(deckName)
                    adapterDecks.notifyDataSetChanged() // RecyclerView'ı güncelle
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

    // SharedPreferences'tan desteleri yükle
    private fun loadDecksFromPreferences() {
        val sharedPreferences = getSharedPreferences("DecksPrefs", Context.MODE_PRIVATE)
        val json = sharedPreferences.getString("deckList", null)

        if (json != null) {
            val type = object : TypeToken<MutableList<String>>() {}.type
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
}