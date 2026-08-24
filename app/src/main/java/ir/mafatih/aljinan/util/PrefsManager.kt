package ir.mafatih.aljinan.util

import android.content.Context

/**
 * All user-facing display preferences: font, font size, colors, translation
 * visibility, plus the "continue reading" bookmark of the last opened line.
 */
class PrefsManager(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("mafatih_prefs", Context.MODE_PRIVATE)

    companion object {
        const val FONT_ESTEDAD = "estedad"
        const val FONT_NEIRIZI = "neirizi"
        const val FONT_ENTEZAR = "entezar"
        const val FONT_IRANIAN_SANS = "iranian_sans"
        const val FONT_SYSTEM = "system"

        private const val KEY_FONT = "font_family"
        private const val KEY_FONT_SIZE = "font_size"
        private const val KEY_DUA_COLOR = "dua_color"
        private const val KEY_TRANSLATE_COLOR = "translate_color"
        private const val KEY_BG_COLOR = "bg_color"
        private const val KEY_SHOW_TRANSLATE = "show_translate"

        private const val KEY_LAST_ITEM_ID = "last_item_id"
        private const val KEY_LAST_ITEM_TITLE = "last_item_title"
        private const val KEY_LAST_LINE_ROWID = "last_line_rowid"

        const val DEFAULT_FONT_SIZE = 20f
        const val MIN_FONT_SIZE = 12f
        const val MAX_FONT_SIZE = 34f

        const val DEFAULT_DUA_COLOR = 0xFF0B2A54.toInt()
        const val DEFAULT_TRANSLATE_COLOR = 0xFF6B6357.toInt()
        const val DEFAULT_BG_COLOR = 0xFFFBF8F1.toInt()
    }

    var fontFamily: String
        get() = prefs.getString(KEY_FONT, FONT_ESTEDAD) ?: FONT_ESTEDAD
        set(value) = prefs.edit().putString(KEY_FONT, value).apply()

    var fontSize: Float
        get() = prefs.getFloat(KEY_FONT_SIZE, DEFAULT_FONT_SIZE)
        set(value) = prefs.edit().putFloat(KEY_FONT_SIZE, value).apply()

    var duaColor: Int
        get() = prefs.getInt(KEY_DUA_COLOR, DEFAULT_DUA_COLOR)
        set(value) = prefs.edit().putInt(KEY_DUA_COLOR, value).apply()

    var translateColor: Int
        get() = prefs.getInt(KEY_TRANSLATE_COLOR, DEFAULT_TRANSLATE_COLOR)
        set(value) = prefs.edit().putInt(KEY_TRANSLATE_COLOR, value).apply()

    var bgColor: Int
        get() = prefs.getInt(KEY_BG_COLOR, DEFAULT_BG_COLOR)
        set(value) = prefs.edit().putInt(KEY_BG_COLOR, value).apply()

    var showTranslate: Boolean
        get() = prefs.getBoolean(KEY_SHOW_TRANSLATE, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_TRANSLATE, value).apply()

    fun saveLastPosition(itemId: Int, itemTitle: String, lineRowId: Long) {
        prefs.edit()
            .putInt(KEY_LAST_ITEM_ID, itemId)
            .putString(KEY_LAST_ITEM_TITLE, itemTitle)
            .putLong(KEY_LAST_LINE_ROWID, lineRowId)
            .apply()
    }

    fun getLastItemId(): Int = prefs.getInt(KEY_LAST_ITEM_ID, -1)
    fun getLastItemTitle(): String? = prefs.getString(KEY_LAST_ITEM_TITLE, null)
    fun getLastLineRowId(): Long = prefs.getLong(KEY_LAST_LINE_ROWID, -1L)

    fun resetDisplayPrefs() {
        prefs.edit()
            .remove(KEY_FONT)
            .remove(KEY_FONT_SIZE)
            .remove(KEY_DUA_COLOR)
            .remove(KEY_TRANSLATE_COLOR)
            .remove(KEY_BG_COLOR)
            .remove(KEY_SHOW_TRANSLATE)
            .apply()
    }
}
