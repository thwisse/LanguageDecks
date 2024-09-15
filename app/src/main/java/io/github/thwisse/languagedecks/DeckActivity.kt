package io.github.thwisse.languagedecks

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import io.github.thwisse.languagedecks.databinding.ActivityDeckBinding

class DeckActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeckBinding
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    private lateinit var currentDeck: Deck

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeckBinding.inflate(layoutInflater)
        val view = binding.root

        enableEdgeToEdge()

        setContentView(view)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sharedPreferencesManager = SharedPreferencesManager(this)

        val deckName = intent.getStringExtra("deckName")

        val deckList = sharedPreferencesManager.getDecks()
        currentDeck = deckList.find { it.deckName == deckName } ?: Deck(deckName ?: "")

        updateToolbar()

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragmentView) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNavigationView.setupWithNavController(navController)
    }

    fun updateToolbar() {
        val currentDeck = getCurrentDeck()
        val cardCount = currentDeck.cards.size
        binding.materialToolbarDeck.title = currentDeck.deckName
        binding.materialToolbarDeck.subtitle = "$cardCount words"
    }

    fun getCurrentDeck(): Deck {
        val deckList = sharedPreferencesManager.getDecks()
        val deckName = intent.getStringExtra("deckName")
        return deckList.find { it.deckName == deckName } ?: Deck(deckName ?: "")
    }


    private fun enableEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
}
