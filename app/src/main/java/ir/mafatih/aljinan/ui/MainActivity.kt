package ir.mafatih.aljinan.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ir.mafatih.aljinan.R
import ir.mafatih.aljinan.databinding.ActivityMainBinding
import ir.mafatih.aljinan.db.DatabaseHelper
import ir.mafatih.aljinan.util.PrefsManager

/**
 * Home screen: exactly 4 top-level menu rows (فهرست مفاتیح الجنان، فهرست
 * منتخب، فهرست برگزیده، جستجو) plus a settings button in the toolbar. An
 * optional "ادامه مطالعه" card appears above them when a reading position
 * was previously saved.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: DatabaseHelper
    private lateinit var prefs: PrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper.getInstance(this)
        prefs = PrefsManager(this)

        binding.toolbar.title = getString(R.string.app_name)
        setSupportActionBar(binding.toolbar)

        binding.rowMafatihList.tvRowTitle.text = getString(R.string.menu_categories)
        binding.rowMafatihList.ivRowIcon.setImageResource(R.drawable.ic_menu_book)
        binding.rowMafatihList.root.setOnClickListener {
            startActivity(Intent(this, CategoriesListActivity::class.java))
        }

        binding.rowMontakhab.tvRowTitle.text = getString(R.string.menu_montakhab)
        binding.rowMontakhab.ivRowIcon.setImageResource(R.drawable.ic_star)
        binding.rowMontakhab.root.setOnClickListener {
            startActivity(Intent(this, MontakhabActivity::class.java))
        }

        binding.rowFavorites.tvRowTitle.text = getString(R.string.menu_favorites)
        binding.rowFavorites.ivRowIcon.setImageResource(R.drawable.ic_star_outline)
        binding.rowFavorites.root.setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }

        binding.rowSearch.tvRowTitle.text = getString(R.string.menu_search)
        binding.rowSearch.ivRowIcon.setImageResource(R.drawable.ic_search)
        binding.rowSearch.root.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        binding.cardContinue.setOnClickListener {
            val mId = prefs.getLastItemId()
            if (mId != -1) {
                startActivity(
                    Intent(this, ReadingActivity::class.java)
                        .putExtra(ReadingActivity.EXTRA_M_ID, mId)
                        .putExtra(ReadingActivity.EXTRA_TITLE, prefs.getLastItemTitle())
                        .putExtra(ReadingActivity.EXTRA_SCROLL_TO_LINE, prefs.getLastLineRowId())
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val lastTitle = prefs.getLastItemTitle()
        if (lastTitle != null) {
            binding.cardContinue.visibility = android.view.View.VISIBLE
            binding.tvContinueTitle.text = lastTitle
        } else {
            binding.cardContinue.visibility = android.view.View.GONE
        }
        invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                return true
            }
            R.id.action_bookmarks -> {
                startActivity(Intent(this, BookmarksActivity::class.java))
                return true
            }
            R.id.action_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
