package io.github.thwisse.languagedecks

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.github.thwisse.languagedecks.databinding.LayoutItemCardRvBinding

class AdapterCards(private var cardList: List<SampleCard>) : RecyclerView.Adapter<AdapterCards.CardViewHolder>() {

    class CardViewHolder(val binding: LayoutItemCardRvBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = LayoutItemCardRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = cardList[position]
        holder.binding.itemTvCardWord.text = card.word
        Log.e("AdapterCards", "Binding Card: ${card.word}")  // Hangi kartların gösterildiğini logla
    }

    override fun getItemCount(): Int {
        return cardList.size
    }

    // Yeni veri geldiğinde güncelleme yapmak için
    fun updateData(newCards: List<SampleCard>) {
        cardList = newCards
        notifyDataSetChanged()
        Log.e("AdapterCards", "Updated with ${newCards.size} cards")  // Verilerin güncellendiğini logla
    }
}

