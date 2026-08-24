package ir.mafatih.aljinan.model

/**
 * Represents a row of the Lines table.
 * L_type == 1 -> main dua/prayer text
 * L_type == 2 -> a note / description line
 * L_Translate == "0" (or empty) -> no translation available for this line
 */
data class Line(
    val rowId: Long,
    val id: Int,
    val mId: Int,
    val text: String,
    val translate: String,
    val prio: Int,
    val type: Int,
    val fors: String
) {
    val isNote: Boolean get() = type == 2
    val hasTranslation: Boolean get() = translate.isNotBlank() && translate != "0"
}
