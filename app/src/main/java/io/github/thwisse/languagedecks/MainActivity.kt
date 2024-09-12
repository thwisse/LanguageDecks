package io.github.thwisse.languagedecks

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import io.github.thwisse.languagedecks.databinding.ActivityMainBinding
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var deckAdapter: DeckAdapter
    private val deckList: MutableList<Deck> = mutableListOf()
    private lateinit var sharedPreferencesManager: SharedPreferencesManager

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

        // SharedPreferencesManager oluşturulması
        sharedPreferencesManager = SharedPreferencesManager(this)

        // SharedPreferences'tan yüklenen deck listesi
        deckList.addAll(sharedPreferencesManager.getDecks())

        // Eğer daha önce örnek deste eklenmemişse ekle
        if (deckList.isEmpty()) {
            val sampleDeck = loadSampleDeck(this)
            sampleDeck?.let {
                deckList.add(it)
                sharedPreferencesManager.saveDecks(deckList)
            }
        }

        deckAdapter = DeckAdapter(deckList, { deck ->
            val intent = Intent(this, DeckActivity::class.java)
            intent.putExtra("deckName", deck.deckName)
            startActivity(intent)
        }, { deck, view ->
            showPopupMenu(deck, view)
        })

        binding.rvDecks.adapter = deckAdapter
        binding.rvDecks.layoutManager = LinearLayoutManager(this)

        // Yeni deste eklemek için bir dialog oluşturma
        binding.fabAddDeck.setOnClickListener {
            showAddDeckDialog()
        }
    }

    private fun showAddDeckDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_add_deck, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.edtDeckName)

        with(builder) {
            setTitle("Create New Deck")
            setView(dialogLayout)
            setPositiveButton("Create") { dialog, which ->
                val newDeck = Deck(editText.text.toString())
                deckList.add(newDeck)
                deckAdapter.notifyDataSetChanged()
                sharedPreferencesManager.saveDecks(deckList)
            }
            setNegativeButton("Cancel") { dialog, which ->
                // İptal
            }
            show()
        }
    }

    fun loadSampleDeck(context: Context): Deck? {
        val jsonString: String
        try {
            jsonString = context.assets.open("sample_deck.json")
                .bufferedReader()
                .use { it.readText() }
            val gson = Gson()
            return gson.fromJson(jsonString, Deck::class.java)
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
    }

    private fun showPopupMenu(deck: Deck, view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.menu_deck_options, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.itemEdit -> {
                    showEditDeckDialog(deck)
                    true
                }
                R.id.itemDelete -> {
                    showDeleteConfirmationDialog(deck)
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun showEditDeckDialog(deck: Deck) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_add_deck, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.edtDeckName)
        editText.setText(deck.deckName)

        with(builder) {
            setTitle("Edit Deck Name")
            setView(dialogLayout)
            setPositiveButton("Save") { dialog, which ->
                deck.deckName = editText.text.toString()
                sharedPreferencesManager.saveDecks(deckList)
                deckAdapter.notifyDataSetChanged()
            }
            setNegativeButton("Cancel") { dialog, which -> }
            show()
        }
    }

    private fun showDeleteConfirmationDialog(deck: Deck) {
        val builder = AlertDialog.Builder(this)

        with(builder) {
            setTitle("Delete Deck")
            setMessage("Are you sure?")
            setPositiveButton("Yes") { dialog, which ->
                deckList.remove(deck)
                sharedPreferencesManager.saveDecks(deckList)
                deckAdapter.notifyDataSetChanged()
            }
            setNegativeButton("Cancel") { dialog, which -> }
            show()
        }
    }
}
