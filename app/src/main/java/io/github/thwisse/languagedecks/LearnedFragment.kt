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
import io.github.thwisse.languagedecks.databinding.FragmentLearnedBinding

class LearnedFragment : Fragment() {

    private var _binding: FragmentLearnedBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AdapterCards
    private var learnedList = mutableListOf<SampleCard>()  // Learned kartlar burada tutulacak

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLearnedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView ayarları
        adapter = AdapterCards(learnedList)
        binding.rvLearned.layoutManager = LinearLayoutManager(context)
        binding.rvLearned.adapter = adapter

        // Kartları arguments'tan alıp fragment'a yansıtmak için kontrol edelim
        val deckJson = arguments?.getString("learnedCards")
        Log.e("LearnedFragment", "Learned Cards Json: $deckJson")

        if (deckJson != null && deckJson != "null") {
            val type = object : TypeToken<List<SampleCard>>() {}.type
            learnedList = Gson().fromJson(deckJson, type)
            adapter.updateData(learnedList)
            Log.e("LearnedFragment", "Learned Cards List: $learnedList")
        } else {
            Log.e("LearnedFragment", "Learned Cards Json is null or empty")
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
