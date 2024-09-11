package io.github.thwisse.languagedecks

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.github.thwisse.languagedecks.databinding.ActivityDeckBinding

class DeckActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeckBinding
    private var selectedDeck: String? = null  // Seçilen deste

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDeckBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // MainActivity'den gelen deste ismini al
        selectedDeck = intent.getStringExtra("deckName")

        // NavController'ı al
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragmentView) as NavHostFragment

        // Kartları intent'ten al
        val cardsJson = intent.getStringExtra("cardsJson")
        if (cardsJson != null) {
            loadDeckCards(navHostFragment, cardsJson)
        }
    }

    private fun loadDeckCards(navHostFragment: NavHostFragment, cardsJson: String) {
        val type = object : TypeToken<List<SampleCard>>() {}.type
        val deckCards = Gson().fromJson<List<SampleCard>>(cardsJson, type)

        // Kartları ayır: unlearned ve learned
        val unlearnedCards = deckCards.filter { !it.isLearned }
        val learnedCards = deckCards.filter { it.isLearned }

        Log.e("DeckActivity", "Unlearned Cards: ${unlearnedCards.size}, Learned Cards: ${learnedCards.size}")

        val navController = navHostFragment.navController

        // Sırasıyla navigate işlemi yap
        val unlearnedBundle = Bundle().apply {
            putString("unlearnedCards", Gson().toJson(unlearnedCards))
        }
        navController.navigate(R.id.unlearnedFragment, unlearnedBundle)

        // Delay ekleyerek learned fragment'a git
        Handler(Looper.getMainLooper()).postDelayed({
            val learnedBundle = Bundle().apply {
                putString("learnedCards", Gson().toJson(learnedCards))
            }
            navController.navigate(R.id.learnedFragment, learnedBundle)
        }, 100)
    }
}