package ir.mafatih.aljinan.ui

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ir.mafatih.aljinan.R
import ir.mafatih.aljinan.model.Line
import ir.mafatih.aljinan.util.FontResolver
import ir.mafatih.aljinan.util.PrefsManager
import ir.mafatih.aljinan.util.enableSelectableShare

class LineAdapter(
    private val lines: List<Line>,
    private val prefs: PrefsManager,
    private val shareChooserTitle: String,
    private val isBookmarked: (Line) -> Boolean,
    private val onBookmarkClick: (Line) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_DUA = 0
        private const val TYPE_NOTE = 1
    }

    /** Re-binds only the row for this line (bookmark icon/highlight), instead
     *  of redrawing the whole reading screen after a bookmark toggle. */
    fun updateBookmark(lineRowId: Long) {
        val idx = lines.indexOfFirst { it.rowId == lineRowId }
        if (idx >= 0) notifyItemChanged(idx)
    }

    class VHDua(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val container: View = itemView.findViewById(R.id.lineContainer)
        val dua: TextView = itemView.findViewById(R.id.tvDuaText)
        val translate: TextView = itemView.findViewById(R.id.tvTranslateText)
        val bookmark: ImageView = itemView.findViewById(R.id.ivBookmarkLine)
    }

    class VHNote(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val note: TextView = itemView.findViewById(R.id.tvNoteText)
    }

    override fun getItemViewType(position: Int): Int =
        if (lines[position].isNote) TYPE_NOTE else TYPE_DUA

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_NOTE) {
            VHNote(inflater.inflate(R.layout.item_line_note, parent, false))
        } else {
            VHDua(inflater.inflate(R.layout.item_line_dua, parent, false))
        }
    }

    private fun typeface(context: android.content.Context): Typeface? =
        FontResolver.typeface(context, prefs.fontFamily)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val line = lines[position]
        val tf = typeface(holder.itemView.context)

        if (holder is VHNote) {
            holder.note.text = line.text
            holder.note.typeface = tf
            holder.note.setTextColor(prefs.translateColor)
            holder.note.enableSelectableShare(holder.itemView.context, shareChooserTitle)
        } else if (holder is VHDua) {
            holder.dua.text = line.text
            holder.dua.typeface = tf
            holder.dua.textSize = prefs.fontSize
            holder.dua.setTextColor(prefs.duaColor)
            holder.dua.enableSelectableShare(holder.itemView.context, shareChooserTitle)

            if (prefs.showTranslate && line.hasTranslation) {
                holder.translate.visibility = View.VISIBLE
                holder.translate.text = line.translate
                holder.translate.setTextColor(prefs.translateColor)
                holder.translate.textSize = prefs.fontSize * 0.72f
                holder.translate.enableSelectableShare(holder.itemView.context, shareChooserTitle)
            } else {
                holder.translate.visibility = View.GONE
            }

            holder.container.setBackgroundColor(
                if (isBookmarked(line)) 0x1AC9A227 else 0x00000000
            )
            holder.bookmark.setImageResource(
                if (isBookmarked(line)) R.drawable.ic_bookmark else R.drawable.ic_bookmark_outline
            )
            holder.bookmark.setOnClickListener { onBookmarkClick(line) }
        }
    }

    override fun getItemCount(): Int = lines.size
}
