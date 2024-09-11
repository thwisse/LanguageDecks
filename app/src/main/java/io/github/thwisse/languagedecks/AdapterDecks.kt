import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.github.thwisse.languagedecks.databinding.LayoutItemDeckRvBinding

class AdapterDecks(
    private val deckList: List<String>,
    private val clickListener: (String) -> Unit  // Tıklama işlemi için listener
) : RecyclerView.Adapter<AdapterDecks.DeckViewHolder>() {

    class DeckViewHolder(val binding: LayoutItemDeckRvBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeckViewHolder {
        val binding = LayoutItemDeckRvBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeckViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeckViewHolder, position: Int) {
        val deckName = deckList[position]
        holder.binding.itemTvDeckName.text = deckName

        // Deste adı tıklandığında
        holder.itemView.setOnClickListener {
            clickListener(deckName)
        }
    }

    override fun getItemCount(): Int {
        return deckList.size
    }
}
