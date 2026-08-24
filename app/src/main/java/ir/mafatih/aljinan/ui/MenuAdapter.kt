package ir.mafatih.aljinan.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ir.mafatih.aljinan.R
import ir.mafatih.aljinan.model.MItem

/**
 * Renders a flat list of M_items rows: M_type == 2 rows are non-clickable
 * section headers (distinct color), M_type == 1 rows are clickable dua
 * entries that also show a (tappable) favorite star.
 *
 * Data is refreshed via submitList() (DiffUtil), not by recreating the
 * adapter, so returning to an already-open list (e.g. after toggling a
 * favorite elsewhere) only re-binds the rows that actually changed.
 */
class MenuAdapter(
    initialItems: List<MItem>,
    private val onItemClick: (MItem) -> Unit,
    private val onFavClick: (MItem, VHDua) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<MItem> = initialItems

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_DUA = 1
    }

    class VHHeader(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvHeaderTitle)
    }

    class VHDua(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvItemTitle)
        val fav: ImageView = itemView.findViewById(R.id.ivFav)
    }

    fun submitList(newItems: List<MItem>) {
        val old = items
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = old.size
            override fun getNewListSize() = newItems.size
            override fun areItemsTheSame(oldPos: Int, newPos: Int) =
                old[oldPos].rowId == newItems[newPos].rowId
            override fun areContentsTheSame(oldPos: Int, newPos: Int) =
                old[oldPos] == newItems[newPos]
        })
        items = newItems
        diff.dispatchUpdatesTo(this)
    }

    override fun getItemViewType(position: Int): Int =
        if (items[position].isHeader) TYPE_HEADER else TYPE_DUA

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            VHHeader(inflater.inflate(R.layout.item_menu_header, parent, false))
        } else {
            VHDua(inflater.inflate(R.layout.item_menu_dua, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (holder is VHHeader) {
            holder.title.text = item.name
        } else if (holder is VHDua) {
            holder.title.text = item.name
            holder.fav.setImageResource(
                if (item.fav) R.drawable.ic_star else R.drawable.ic_star_outline
            )
            holder.itemView.setOnClickListener { onItemClick(item) }
            holder.fav.setOnClickListener { onFavClick(item, holder) }
        }
    }

    override fun getItemCount(): Int = items.size
}
