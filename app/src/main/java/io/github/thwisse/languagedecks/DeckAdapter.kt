package io.github.thwisse.languagedecks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.github.thwisse.languagedecks.databinding.LayoutItemDeckRvBinding

class DeckAdapter(
    private val deckList: List<Deck>,
    private val onItemClick: (Deck) -> Unit,
    private val onLongClick: (Deck, View) -> Unit
) : RecyclerView.Adapter<DeckAdapter.DeckViewHolder>() {

    inner class DeckViewHolder(val binding: LayoutItemDeckRvBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeckViewHolder {
        val binding = LayoutItemDeckRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeckViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeckViewHolder, position: Int) {
        val deck = deckList[position]
        holder.binding.itemTvDeckName.text = deck.deckName
        holder.binding.root.setOnClickListener {
            onItemClick(deck)
        }

        holder.binding.root.setOnLongClickListener {
            onLongClick(deck, it)
            true
        }
    }

    override fun getItemCount(): Int = deckList.size
}
