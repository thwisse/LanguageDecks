package io.github.thwisse.languagedecks

import android.os.Bundle
import android.util.Log
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("unlearnedCards", Gson().toJson(unlearnedList))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView ayarları
        adapter = AdapterCards(unlearnedList)
        binding.rvUnlearned.layoutManager = LinearLayoutManager(context)
        binding.rvUnlearned.adapter = adapter

        // SavedInstanceState kullanarak veriyi koruyalım
        val deckJson = savedInstanceState?.getString("unlearnedCards") ?: arguments?.getString("unlearnedCards")
        Log.e("UnlearnedFragment", "Unlearned Cards Json: $deckJson")
        if (deckJson != null) {
            val type = object : TypeToken<List<SampleCard>>() {}.type
            unlearnedList = Gson().fromJson(deckJson, type)
            adapter.updateData(unlearnedList)  // Adapter'i güncelle
            Log.e("UnlearnedFragment", "Unlearned Cards List: $unlearnedList")
        } else {
            Log.e("UnlearnedFragment", "Unlearned Cards Json is null")
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}