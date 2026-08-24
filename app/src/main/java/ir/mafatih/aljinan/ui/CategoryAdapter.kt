package ir.mafatih.aljinan.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ir.mafatih.aljinan.R
import ir.mafatih.aljinan.model.Category

class CategoryAdapter(
    private val items: List<Category>,
    private val onClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.VH>() {

    inner class VH(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvCategoryTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_category, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val cat = items[position]
        holder.title.text = cat.title
        holder.itemView.setOnClickListener { onClick(cat) }
    }

    override fun getItemCount(): Int = items.size
}
