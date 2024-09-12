package io.github.thwisse.languagedecks

import android.os.Bundle
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.thwisse.languagedecks.databinding.ActivityMainBinding

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

        deckAdapter = DeckAdapter(deckList) { deck ->
            // Tıklanan deste ile ilgili işlemleri burada yapacağız
        }

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
            setTitle("Yeni Deste Ekle")
            setView(dialogLayout)
            setPositiveButton("Ekle") { dialog, which ->
                val newDeck = Deck(editText.text.toString())
                deckList.add(newDeck)
                deckAdapter.notifyDataSetChanged()
                sharedPreferencesManager.saveDecks(deckList)
            }
            setNegativeButton("İptal") { dialog, which ->
                // İptal
            }
            show()
        }
    }
}
