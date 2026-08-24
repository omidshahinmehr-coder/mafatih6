package ir.mafatih.aljinan.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ir.mafatih.aljinan.R
import ir.mafatih.aljinan.model.SearchResult

class SearchResultAdapter(
    private val items: List<SearchResult>,
    private val onClick: (SearchResult) -> Unit
) : RecyclerView.Adapter<SearchResultAdapter.VH>() {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvResultTitle)
        val snippet: TextView = itemView.findViewById(R.id.tvResultSnippet)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_search_result, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val r = items[position]
        holder.title.text = r.itemTitle
        holder.snippet.text = r.snippet
        holder.itemView.setOnClickListener { onClick(r) }
    }

    override fun getItemCount(): Int = items.size
}
