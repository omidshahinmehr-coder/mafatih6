package ir.mafatih.aljinan.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import ir.mafatih.aljinan.R
import ir.mafatih.aljinan.databinding.ActivityBookmarksBinding
import ir.mafatih.aljinan.db.DatabaseHelper

class BookmarksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookmarksBinding
    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookmarksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper.getInstance(this)
        binding.toolbar.title = getString(R.string.menu_bookmarks)
        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.rvBookmarks.layoutManager = LinearLayoutManager(this)

        loadBookmarks()
    }

    override fun onResume() {
        super.onResume()
        loadBookmarks()
    }

    private fun loadBookmarks() {
        val bookmarks = db.getBookmarks()
        binding.tvEmpty.visibility = if (bookmarks.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        binding.rvBookmarks.adapter = BookmarkAdapter(
            bookmarks,
            onClick = { bm ->
                startActivity(
                    Intent(this, ReadingActivity::class.java)
                        .putExtra(ReadingActivity.EXTRA_M_ID, bm.mItemId)
                        .putExtra(ReadingActivity.EXTRA_TITLE, bm.itemTitle)
                        .putExtra(ReadingActivity.EXTRA_SCROLL_TO_LINE, bm.lineRowId)
                )
            },
            onDelete = { bm, _ ->
                db.removeBookmark(bm.id)
                loadBookmarks()
            }
        )
    }
}
