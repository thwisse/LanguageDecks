package io.github.thwisse.languagedecks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.github.thwisse.languagedecks.databinding.LayoutItemCardRvBinding

class CardAdapter(
    private val cardList: List<Card>,
    private val onItemClick: (Card) -> Unit
) : RecyclerView.Adapter<CardAdapter.CardViewHolder>() {

    inner class CardViewHolder(val binding: LayoutItemCardRvBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = LayoutItemCardRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = cardList[position]
        holder.binding.itemTvCardWord.text = card.word
        holder.binding.root.setOnClickListener {
            onItemClick(card)
        }
    }

    override fun getItemCount(): Int = cardList.size
}

