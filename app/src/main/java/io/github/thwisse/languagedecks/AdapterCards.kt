package io.github.thwisse.languagedecks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.github.thwisse.languagedecks.databinding.LayoutItemCardBinding

class AdapterCards(private var cardList: List<SampleCard>) : RecyclerView.Adapter<AdapterCards.CardViewHolder>() {

    class CardViewHolder(val binding: LayoutItemCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = LayoutItemCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = cardList[position]
        holder.binding.tvWord.text = card.word
        holder.binding.tvMeaning.text = card.meaning
        holder.binding.switchLearned.isChecked = card.isLearned
    }

    override fun getItemCount(): Int {
        return cardList.size
    }

    // Yeni veri geldiğinde güncelleme yapmak için
    fun updateData(newCards: List<SampleCard>) {
        cardList = newCards
        notifyDataSetChanged()
    }
}
