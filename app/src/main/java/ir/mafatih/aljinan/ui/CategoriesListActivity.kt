package ir.mafatih.aljinan.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import ir.mafatih.aljinan.R
import ir.mafatih.aljinan.databinding.ActivityCategoriesListBinding
import ir.mafatih.aljinan.db.DatabaseHelper

/**
 * The "فهرست مفاتیح الجنان" screen: the 7 rows of the categories table.
 * Tapping one opens CategoryActivity with that category's M_items.
 */
class CategoriesListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoriesListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoriesListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = DatabaseHelper.getInstance(this)

        binding.toolbar.title = getString(R.string.menu_categories)
        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.rvCategories.layoutManager = LinearLayoutManager(this)
        binding.rvCategories.adapter = CategoryAdapter(db.getTopCategories()) { category ->
            startActivity(
                Intent(this, CategoryActivity::class.java)
                    .putExtra(CategoryActivity.EXTRA_CAT_ID, category.id)
                    .putExtra(CategoryActivity.EXTRA_CAT_TITLE, category.title)
            )
        }
    }
}
