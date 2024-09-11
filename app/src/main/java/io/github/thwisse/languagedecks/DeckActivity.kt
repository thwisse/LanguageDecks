package io.github.thwisse.languagedecks

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.github.thwisse.languagedecks.databinding.ActivityDeckBinding

class DeckActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeckBinding
    private var selectedDeck: String? = null  // Seçilen deste
    private var unlearnedCards: List<SampleCard> = emptyList()
    private var learnedCards: List<SampleCard> = emptyList()

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

        // Tab itemlarına tıklanınca fragment'ları gösterelim
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.unlearnedFragment -> {
                    showUnlearnedFragment()
                    true
                }
                R.id.learnedFragment -> {
                    showLearnedFragment()
                    true
                }
                else -> false
            }
        }

        // Varsayılan olarak Unlearned fragment'ı gösterelim
        showUnlearnedFragment()
    }

    private fun loadDeckCards(cardsJson: String) {
        val type = object : TypeToken<List<SampleCard>>() {}.type
        val deckCards = Gson().fromJson<List<SampleCard>>(cardsJson, type)

        // Kartları ayır: unlearned ve learned
        unlearnedCards = deckCards.filter { !it.isLearned }
        learnedCards = deckCards.filter { it.isLearned }

        Log.e("DeckActivity", "Unlearned Cards: ${unlearnedCards.size}, Learned Cards: ${learnedCards.size}")
    }

    private fun showUnlearnedFragment() {
        val fragment = UnlearnedFragment()
        val bundle = Bundle().apply {
            putString("unlearnedCards", Gson().toJson(unlearnedCards))
        }
        fragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.navHostFragmentView, fragment)
            .commit()

        Log.e("DeckActivity", "UnlearnedFragment gösteriliyor")
    }

    private fun showLearnedFragment() {
        val fragment = LearnedFragment()
        val bundle = Bundle().apply {
            putString("learnedCards", Gson().toJson(learnedCards))
        }
        fragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.navHostFragmentView, fragment)
            .commit()

        Log.e("DeckActivity", "LearnedFragment gösteriliyor")
    }
}
