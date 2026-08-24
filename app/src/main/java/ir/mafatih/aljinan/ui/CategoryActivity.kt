package ir.mafatih.aljinan.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import ir.mafatih.aljinan.R
import ir.mafatih.aljinan.databinding.ActivityCategoryBinding
import ir.mafatih.aljinan.db.DatabaseHelper
import ir.mafatih.aljinan.model.MItem

/**
 * Shows the flat M_items list for a chosen category (headers + dua rows).
 * Also reused (via subclassing) by FavoritesActivity / MontakhabActivity so
 * the same header/dua rendering and favorite-toggle logic isn't duplicated.
 *
 * A single MenuAdapter instance lives for the activity's lifetime; onResume
 * just re-submits fresh data through DiffUtil instead of rebuilding the
 * adapter/list from scratch on every return to this screen.
 */
open class CategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryBinding
    protected lateinit var db: DatabaseHelper
    private lateinit var adapter: MenuAdapter

    companion object {
        const val EXTRA_CAT_ID = "cat_id"
        const val EXTRA_CAT_TITLE = "cat_title"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper.getInstance(this)
        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.toolbar.title = intent.getStringExtra(EXTRA_CAT_TITLE) ?: toolbarTitle()
        binding.rvItems.layoutManager = LinearLayoutManager(this)

        adapter = MenuAdapter(
            emptyList(),
            onItemClick = { item -> openReading(item) },
            onFavClick = { item, holder ->
                item.fav = !item.fav
                db.setFavorite(item.rowId, item.fav)
                holder.fav.setImageResource(
                    if (item.fav) R.drawable.ic_star else R.drawable.ic_star_outline
                )
                Toast.makeText(
                    this,
                    if (item.fav) R.string.action_favorite else R.string.action_unfavorite,
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
        binding.rvItems.adapter = adapter

        loadItems()
    }

    protected open fun toolbarTitle(): String = getString(R.string.menu_categories)

    override fun onResume() {
        super.onResume()
        loadItems()
    }

    protected open fun fetchItems(): List<MItem> {
        val catId = intent.getIntExtra(EXTRA_CAT_ID, -1)
        return if (catId != -1) db.getItemsForCategory(catId) else emptyList()
    }

    private fun loadItems() {
        val items = fetchItems()
        binding.tvEmpty.visibility = if (items.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        adapter.submitList(items)
    }

    private fun openReading(item: MItem) {
        if (item.isHeader) return
        startActivity(
            Intent(this, ReadingActivity::class.java)
                .putExtra(ReadingActivity.EXTRA_M_ID, item.id)
                .putExtra(ReadingActivity.EXTRA_TITLE, item.name)
        )
    }
}
