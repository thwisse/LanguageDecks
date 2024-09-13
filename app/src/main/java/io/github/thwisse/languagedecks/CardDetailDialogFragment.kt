package io.github.thwisse.languagedecks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import io.github.thwisse.languagedecks.databinding.FragmentCardDetailBinding

class CardDetailDialogFragment : DialogFragment() {

    private lateinit var binding: FragmentCardDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCardDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Kart detaylarını almak ve göstermek
        val word = arguments?.getString("word")
        val meaning1 = arguments?.getString("meaning1")
        val meaning2 = arguments?.getString("meaning2")

        binding.textViewWord.text = word
        binding.textViewMeaning1.text = meaning1
        binding.textViewMeaning2.text = meaning2
    }
}

