package io.github.thwisse.languagedecks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.github.thwisse.languagedecks.databinding.FragmentUnlearnedBinding

class UnlearnedFragment : Fragment() {

    private var _binding: FragmentUnlearnedBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AdapterCards
    private var unlearnedList = mutableListOf<SampleCard>()  // Unlearned kartlar burada tutulacak

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUnlearnedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView ayarları
        adapter = AdapterCards(unlearnedList)
        binding.recyclerViewUnlearned.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewUnlearned.adapter = adapter

        // Kartları DeckActivity'den alıp fragment'a yansıtmak için arguments kullanacağız
        val deckJson = arguments?.getString("unlearnedCards")
        if (deckJson != null) {
            val type = object : TypeToken<MutableList<SampleCard>>() {}.type
            unlearnedList = Gson().fromJson(deckJson, type)
            adapter.updateData(unlearnedList)  // Adapter'i güncelle
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
