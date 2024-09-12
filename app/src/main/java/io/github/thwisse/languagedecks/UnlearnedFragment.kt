package io.github.thwisse.languagedecks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.thwisse.languagedecks.databinding.FragmentUnlearnedBinding

class UnlearnedFragment : Fragment() {

    private var _binding: FragmentUnlearnedBinding? = null
    private val binding get() = _binding!!
    private lateinit var cardAdapter: CardAdapter
    private val cardList: MutableList<Card> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUnlearnedBinding.inflate(inflater, container, false)
        val view = binding.root

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        cardAdapter = CardAdapter(cardList) { card ->
            // Tıklanan kart ile ilgili işlemleri burada yapacağız
        }

        binding.rvUnlearned.adapter = cardAdapter
        binding.rvUnlearned.layoutManager = LinearLayoutManager(context)

        loadCards()

        return view
    }

    private fun loadCards() {
        // Sabit veri ekleyelim
        cardList.add(Card("Dog", "Köpek", "Hund"))
        cardList.add(Card("Cat", "Kedi", "Katze"))
        cardAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
