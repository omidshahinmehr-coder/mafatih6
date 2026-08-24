package ir.mafatih.aljinan.ui

import ir.mafatih.aljinan.R
import ir.mafatih.aljinan.model.MItem

class FavoritesActivity : CategoryActivity() {
    override fun fetchItems(): List<MItem> = db.getFavorites()
    override fun toolbarTitle(): String = getString(R.string.menu_favorites)
}
