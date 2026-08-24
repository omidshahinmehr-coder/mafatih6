package ir.mafatih.aljinan.ui

import ir.mafatih.aljinan.R
import ir.mafatih.aljinan.model.MItem

class MontakhabActivity : CategoryActivity() {
    override fun fetchItems(): List<MItem> = db.getMontakhab()
    override fun toolbarTitle(): String = getString(R.string.menu_montakhab)
}
