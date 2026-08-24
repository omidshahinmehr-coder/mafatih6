package ir.mafatih.aljinan.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ir.mafatih.aljinan.R
import ir.mafatih.aljinan.model.Bookmark

class BookmarkAdapter(
    private val items: List<Bookmark>,
    private val onClick: (Bookmark) -> Unit,
    private val onDelete: (Bookmark, Int) -> Unit
) : RecyclerView.Adapter<BookmarkAdapter.VH>() {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvBookmarkTitle)
        val snippet: TextView = itemView.findViewById(R.id.tvBookmarkSnippet)
        val delete: View = itemView.findViewById(R.id.ivDeleteBookmark)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_bookmark, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val bm = items[position]
        holder.title.text = bm.itemTitle
        holder.snippet.text = bm.snippet
        holder.itemView.setOnClickListener { onClick(bm) }
        holder.delete.setOnClickListener { onDelete(bm, holder.bindingAdapterPosition) }
    }

    override fun getItemCount(): Int = items.size
}
