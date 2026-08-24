package ir.mafatih.aljinan.model

/**
 * Represents a row of the M_items table.
 * M_type == 2  -> this row is a section header (not clickable, styled differently)
 * M_type == 1  -> this row is an actual dua/text that opens the reading screen
 */
data class MItem(
    val rowId: Long, // SQLite internal rowid - the true unique key for this table (M_id/_id is only unique among M_type=1 rows)
    val id: Int,
    val name: String,
    val catId: Int,
    var fav: Boolean,
    val type: Int,
    val prio: Int,
    val montakhab: Boolean,
    val position: String
) {
    val isHeader: Boolean get() = type == 2
}
