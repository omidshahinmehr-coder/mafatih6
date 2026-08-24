package ir.mafatih.aljinan.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import ir.mafatih.aljinan.R
import ir.mafatih.aljinan.databinding.ActivityReadingBinding
import ir.mafatih.aljinan.db.DatabaseHelper
import ir.mafatih.aljinan.model.Line
import ir.mafatih.aljinan.model.MItem
import ir.mafatih.aljinan.util.PrefsManager

class ReadingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReadingBinding
    private lateinit var db: DatabaseHelper
    private lateinit var prefs: PrefsManager

    private var mId: Int = -1
    private var itemTitle: String = ""
    private var currentItem: MItem? = null
    private var lines: List<Line> = emptyList()
    private var bookmarkedLineIds: MutableSet<Long> = mutableSetOf()
    private var layoutManager: LinearLayoutManager? = null
    private var lineAdapter: LineAdapter? = null
    private var lastAppliedPrefsSnapshot: String = ""

    companion object {
        const val EXTRA_M_ID = "m_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_SCROLL_TO_LINE = "scroll_to_line"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper.getInstance(this)
        prefs = PrefsManager(this)

        mId = intent.getIntExtra(EXTRA_M_ID, -1)
        itemTitle = intent.getStringExtra(EXTRA_TITLE) ?: ""

        binding.toolbar.title = itemTitle
        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.inflateMenu(R.menu.menu_reading)
        binding.toolbar.setOnMenuItemClickListener { onReadingMenuItem(it) }

        applyBackground()
        loadContent()

        val scrollTo = intent.getLongExtra(EXTRA_SCROLL_TO_LINE, -1L)
        if (scrollTo != -1L) scrollToLine(scrollTo)
    }

    override fun onResume() {
        super.onResume()
        // Only re-render if a display preference (font/size/color/translate
        // toggle) actually changed since we last drew this screen - NOT on
        // every resume. Previously this ran unconditionally, which also
        // fired every time the Share button opened Android's share sheet
        // (since that pauses/resumes this Activity too) and forced a full
        // rebind of every visible row for no reason, breaking text-selection
        // on those rows until the screen was fully re-entered.
        val snapshot = currentPrefsSnapshot()
        if (snapshot != lastAppliedPrefsSnapshot) {
            lastAppliedPrefsSnapshot = snapshot
            applyBackground()
            binding.rvLines.adapter?.notifyDataSetChanged()
        }
    }

    private fun currentPrefsSnapshot(): String =
        "${prefs.fontFamily}|${prefs.fontSize}|${prefs.duaColor}|${prefs.translateColor}|${prefs.bgColor}|${prefs.showTranslate}"

    private fun applyBackground() {
        binding.rvLines.setBackgroundColor(prefs.bgColor)
    }

    private fun loadContent() {
        currentItem = db.getReadableItemByMId(mId)
        lines = db.getLinesForItem(mId)
        bookmarkedLineIds = db.getBookmarks()
            .filter { it.mItemId == mId }
            .map { it.lineRowId }
            .toMutableSet()

        layoutManager = LinearLayoutManager(this)
        binding.rvLines.layoutManager = layoutManager
        lineAdapter = LineAdapter(
            lines = lines,
            prefs = prefs,
            shareChooserTitle = itemTitle,
            isBookmarked = { bookmarkedLineIds.contains(it.rowId) },
            onBookmarkClick = { line -> toggleBookmark(line) }
        )
        binding.rvLines.adapter = lineAdapter
        lastAppliedPrefsSnapshot = currentPrefsSnapshot()

        updateFavoriteIcon()
    }

    private fun toggleBookmark(line: Line) {
        if (bookmarkedLineIds.contains(line.rowId)) {
            db.removeBookmark(mId, line.rowId)
            bookmarkedLineIds.remove(line.rowId)
            Toast.makeText(this, R.string.bookmark_removed, Toast.LENGTH_SHORT).show()
        } else {
            val snippet = line.text
            db.addBookmark(mId, line.rowId, itemTitle, snippet)
            bookmarkedLineIds.add(line.rowId)
            Toast.makeText(this, R.string.bookmark_saved, Toast.LENGTH_SHORT).show()
        }
        // Only re-bind this one row instead of the whole screen.
        lineAdapter?.updateBookmark(line.rowId)
    }

    private fun scrollToLine(lineRowId: Long) {
        val index = lines.indexOfFirst { it.rowId == lineRowId }
        if (index >= 0) {
            binding.rvLines.post { layoutManager?.scrollToPositionWithOffset(index, 40) }
        }
    }

    private fun onReadingMenuItem(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_favorite -> {
                toggleFavorite()
                true
            }
            R.id.action_share -> {
                shareDua()
                true
            }
            else -> false
        }
    }

    private fun toggleFavorite() {
        val cur = currentItem ?: return
        cur.fav = !cur.fav
        db.setFavorite(cur.rowId, cur.fav)
        updateFavoriteIcon()
        Toast.makeText(
            this,
            if (cur.fav) R.string.action_favorite else R.string.action_unfavorite,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun updateFavoriteIcon() {
        val fav = currentItem?.fav ?: false
        binding.toolbar.menu.findItem(R.id.action_favorite)?.setIcon(
            if (fav) R.drawable.ic_star else R.drawable.ic_star_outline
        )
    }

    private fun shareDua() {
        // Mirrors what's currently visible on screen: include each line's
        // translation right under it, but only when the user has the
        // translation toggle on (Settings) - so sharing matches what they see.
        val body = lines.filter { !it.isNote }.joinToString("\n\n") { line ->
            if (prefs.showTranslate && line.hasTranslation) {
                "${line.text}\n${line.translate}"
            } else {
                line.text
            }
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, itemTitle)
            putExtra(Intent.EXTRA_TEXT, "$itemTitle\n\n$body")
        }
        startActivity(Intent.createChooser(intent, itemTitle))
    }

    private fun saveLastPosition() {
        val lm = layoutManager ?: return
        val pos = lm.findFirstVisibleItemPosition()
        if (pos in lines.indices) {
            prefs.saveLastPosition(mId, itemTitle, lines[pos].rowId)
        }
    }

    override fun onPause() {
        super.onPause()
        saveLastPosition()
    }
}
