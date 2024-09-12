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

        // DeckActivity'ye gelen deckName bilgisini alıyoruz
        val deckName = intent.getStringExtra("deckName")

        // Deck'i SharedPreferences'dan alıyoruz
        val deckList = sharedPreferencesManager.getDecks()
        currentDeck = deckList.find { it.deckName == deckName } ?: Deck(deckName ?: "")

        // NavController'ı NavHostFragment'tan alıyoruz
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragmentView) as NavHostFragment
        val navController = navHostFragment.navController

        // BottomNavigationView ile NavController'ı ilişkilendiriyoruz
        binding.bottomNavigationView.setupWithNavController(navController)

        // Destenin içindeki tüm kartları loglamak
        logDeckCards(currentDeck)
    }

    private fun enableEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    private fun logDeckCards(deck: Deck) {
        Log.e("DeckActivity", "Deck: ${deck.deckName}, Card Count: ${deck.cards.size}")
        if (deck.cards.isEmpty()) {
            Log.e("DeckActivity", "This deck is empty.")
        } else {
            for (card in deck.cards) {
                Log.e("DeckActivity", "Word: ${card.word}, isLearned: ${card.isLearned}")
            }
        }
    }
}
