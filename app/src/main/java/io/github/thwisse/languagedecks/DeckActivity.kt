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

        // Kartları intent'ten al
        val cardsJson = intent.getStringExtra("cardsJson")
        Log.e("DeckActivity", "Alınan kartlar: $cardsJson")  // Kartların gelip gelmediğini loglayalım

        if (cardsJson != null) {
            loadDeckCards(cardsJson)
        }
    }

    private fun loadDeckCards(cardsJson: String) {
        val type = object : TypeToken<List<SampleCard>>() {}.type
        val deckCards = Gson().fromJson<List<SampleCard>>(cardsJson, type)

        // Kartları ayır: unlearned ve learned
        val unlearnedCards = deckCards.filter { !it.isLearned }
        val learnedCards = deckCards.filter { it.isLearned }

        Log.e("DeckActivity", "Unlearned Cards: ${unlearnedCards.size}, Learned Cards: ${learnedCards.size}")

        // Bundle oluştur
        val unlearnedBundle = Bundle().apply {
            putString("unlearnedCards", Gson().toJson(unlearnedCards))
        }

        val learnedBundle = Bundle().apply {
            putString("learnedCards", Gson().toJson(learnedCards))
        }

        // NavController'ı al
        val navController = (supportFragmentManager.findFragmentById(R.id.navHostFragmentView) as NavHostFragment).navController

        // İlk önce unlearnedFragment'a navigate et
        navController.navigate(R.id.unlearnedFragment, unlearnedBundle)
        Log.e("DeckActivity", "UnlearnedFragment'a yönlendirildi")

        // 200ms delay ile learnedFragment'a geç
        Handler(Looper.getMainLooper()).postDelayed({
            navController.navigate(R.id.learnedFragment, learnedBundle)
            Log.e("DeckActivity", "LearnedFragment'a yönlendirildi")
        }, 200)
    }


}
