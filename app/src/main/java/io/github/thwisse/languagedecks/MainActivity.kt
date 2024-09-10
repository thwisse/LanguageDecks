package io.github.thwisse.languagedecks

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
import io.github.thwisse.languagedecks.databinding.ActivityMainBinding
import io.github.thwisse.languagedecks.databinding.DialogAddDeckBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapterDecks: AdapterDecks
    private var deckList = mutableListOf("Turkish - English")

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
        val dialogBinding = DialogAddDeckBinding.inflate(LayoutInflater.from(this))

        // AlertDialog oluştur
        val dialog = AlertDialog.Builder(this)
            .setTitle("Add New Deck")
            .setView(dialogBinding.root)
            .setPositiveButton("Add") { dialogInterface, _ ->
                val deckName = dialogBinding.edtEnterDeckName.text.toString()

                // Deste ismi boş değilse listeye ekle
                if (deckName.isNotEmpty()) {
                    deckList.add(deckName)
                    adapterDecks.notifyDataSetChanged() // RecyclerView'ı güncelle
                }
                dialogInterface.dismiss()
            }
            .setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.show()
    }
}