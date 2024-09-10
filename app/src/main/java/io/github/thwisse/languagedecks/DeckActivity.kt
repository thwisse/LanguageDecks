package io.github.thwisse.languagedecks

import android.os.Bundle
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

    // Kartları fragment'lara gönderme
    private fun loadDeckCards(navHostFragment: NavHostFragment, cardsJson: String) {
        // Kartları JSON formatından çevir
        val type = object : TypeToken<MutableList<SampleCard>>() {}.type
        val deckCards = Gson().fromJson<MutableList<SampleCard>>(cardsJson, type)

        // Kartları ayır: unlearned ve learned
        val unlearnedCards = deckCards.filter { !it.isLearned }
        val learnedCards = deckCards.filter { it.isLearned }

        // Fragment'lara verileri gönder
        val unlearnedBundle = Bundle().apply {
            putString("unlearnedCards", Gson().toJson(unlearnedCards))
        }

        val learnedBundle = Bundle().apply {
            putString("learnedCards", Gson().toJson(learnedCards))
        }

        // UnlearnedFragment ve LearnedFragment'ı al
        val navController = navHostFragment.navController
        val currentFragment = navHostFragment.childFragmentManager.primaryNavigationFragment

        if (currentFragment is UnlearnedFragment) {
            currentFragment.arguments = unlearnedBundle
        } else if (currentFragment is LearnedFragment) {
            currentFragment.arguments = learnedBundle
        }
    }
}
