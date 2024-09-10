package io.github.thwisse.languagedecks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.github.thwisse.languagedecks.databinding.LayoutItemRvBinding

class AdapterDecks(private val deckList: List<String>) : RecyclerView.Adapter<AdapterDecks.DeckViewHolder>() {

    // ViewHolder sınıfı, ViewBinding ile
    class DeckViewHolder(val binding: LayoutItemRvBinding) : RecyclerView.ViewHolder(binding.root)

    // onCreateViewHolder, LayoutItemRvBinding ile çalışacak şekilde güncellendi
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeckViewHolder {
        val binding = LayoutItemRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeckViewHolder(binding)
    }

    // Veriyi ViewHolder'a bağlar
    override fun onBindViewHolder(holder: DeckViewHolder, position: Int) {
        holder.binding.itemTvDeckName.text = deckList[position]
    }

    // Listenin boyutunu döner
    override fun getItemCount(): Int {
        return deckList.size
    }
}
