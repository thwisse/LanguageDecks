package io.github.thwisse.languagedecks

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.thwisse.languagedecks.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapterDecks: AdapterDecks

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

        // Test verisi için basit bir liste oluştur
        val deckList = listOf("Deck 1", "Deck 2", "Deck 3")

        // Adapter'i oluştur ve RecyclerView'a bağla
        adapterDecks = AdapterDecks(deckList)
        recyclerView.adapter = adapterDecks

        binding.fabAddDeck.setOnClickListener {
            Toast.makeText(this, "FAB Clicked", Toast.LENGTH_SHORT).show()
        }

    }
}