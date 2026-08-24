package ir.mafatih.aljinan.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import ir.mafatih.aljinan.R
import ir.mafatih.aljinan.databinding.ActivitySearchBinding
import ir.mafatih.aljinan.db.DatabaseHelper

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var db: DatabaseHelper

    private val debounceHandler = Handler(Looper.getMainLooper())
    private var pendingSearch: Runnable? = null

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 250L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper.getInstance(this)
        binding.toolbar.title = getString(R.string.menu_search)
        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.rvResults.layoutManager = LinearLayoutManager(this)

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // Debounced: only actually query the DB once typing pauses for
                // a beat, instead of running a query on every keystroke.
                pendingSearch?.let { debounceHandler.removeCallbacks(it) }
                val query = s?.toString().orEmpty()
                val runnable = Runnable { runSearch(query) }
                pendingSearch = runnable
                debounceHandler.postDelayed(runnable, SEARCH_DEBOUNCE_MS)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        pendingSearch?.let { debounceHandler.removeCallbacks(it) }
    }

    private fun runSearch(query: String) {
        if (query.trim().length < 2) {
            binding.rvResults.adapter = SearchResultAdapter(emptyList()) {}
            binding.tvEmpty.visibility = android.view.View.GONE
            return
        }
        val results = db.search(query)
        binding.tvEmpty.visibility = if (results.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        binding.rvResults.adapter = SearchResultAdapter(results) { r ->
            startActivity(
                Intent(this, ReadingActivity::class.java)
                    .putExtra(ReadingActivity.EXTRA_M_ID, r.mItemId)
                    .putExtra(ReadingActivity.EXTRA_TITLE, r.itemTitle)
                    .putExtra(ReadingActivity.EXTRA_SCROLL_TO_LINE, r.lineRowId)
            )
        }
    }
}
