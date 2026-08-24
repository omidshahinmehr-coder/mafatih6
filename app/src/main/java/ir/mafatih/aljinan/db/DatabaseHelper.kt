package ir.mafatih.aljinan.db

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import ir.mafatih.aljinan.model.Bookmark
import ir.mafatih.aljinan.model.Category
import ir.mafatih.aljinan.model.Line
import ir.mafatih.aljinan.model.MItem
import ir.mafatih.aljinan.model.SearchResult
import ir.mafatih.aljinan.util.TextNormalizer
import java.io.File
import java.io.FileOutputStream

/**
 * Wraps the prebuilt "mafatih.db" (Mafatih al-Jinan text) that ships inside
 * assets/databases. On first launch the file is copied verbatim into the
 * app's private database directory (this preserves the 677 M_items rows,
 * 5378 Lines rows and 7 categories exactly as supplied). Two extra tables
 * -- bookmarks and app_state -- are created on top of it for app features
 * that are not part of the original content database.
 */
class DatabaseHelper private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val dbFile: File = appContext.getDatabasePath(DB_NAME)
    private var database: SQLiteDatabase

    init {
        if (!dbFile.exists() || appContext.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getInt(KEY_DB_VERSION, -1) != DB_CONTENT_VERSION
        ) {
            copyDatabaseFromAssets()
            appContext.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
                .putInt(KEY_DB_VERSION, DB_CONTENT_VERSION).apply()
        }
        database = SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READWRITE)
        createAppTables(database)
    }

    private fun copyDatabaseFromAssets() {
        dbFile.parentFile?.mkdirs()
        appContext.assets.open("databases/$DB_NAME").use { input ->
            FileOutputStream(dbFile).use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun createAppTables(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS bookmarks (
                _id INTEGER PRIMARY KEY AUTOINCREMENT,
                m_id INTEGER NOT NULL,
                line_rowid INTEGER NOT NULL,
                item_title TEXT,
                snippet TEXT,
                created_at INTEGER,
                UNIQUE(m_id, line_rowid)
            )"""
        )

        // Indices on the columns that are actually filtered/joined on. These are
        // no-ops (IF NOT EXISTS) on every launch after the first, so it's safe
        // to just always run them here rather than gating on DB_CONTENT_VERSION.
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_mitems_cat ON M_items(M_cat_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_mitems_fav ON M_items(M_fav)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_mitems_montakhab ON M_items(M_montakhab)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_mitems_mid_type ON M_items(_id, M_type)")
        db.execSQL("CREATE INDEX IF NOT EXISTS idx_lines_mid ON Lines(L_M_id)")

        setupSearchIndex(db)
    }

    /**
     * FTS4 mirror of Lines(L_M_id, L_text, L_fors) keyed by the *same* rowid as
     * the source table, so a match's rowid maps straight back to a real Lines
     * row (needed for bookmarking/scroll-to). FTS4 (not FTS5) is used because
     * it's reliably bundled with the SQLite build on every Android version
     * back to API 21, whereas FTS5 availability varies by OEM/API level.
     */
    private fun setupSearchIndex(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE VIRTUAL TABLE IF NOT EXISTS lines_fts USING fts4(l_m_id, l_text, l_fors, tokenize=unicode61)"
        )
        val c = db.rawQuery("SELECT COUNT(*) FROM lines_fts", null)
        val isEmpty = c.use { it.moveToFirst() && it.getInt(0) == 0 }
        if (isEmpty) {
            db.execSQL(
                """INSERT INTO lines_fts(rowid, l_m_id, l_text, l_fors)
                   SELECT rowid, L_M_id, L_text, L_fors FROM Lines"""
            )
        }
    }

    // ---------------------------------------------------------------- categories

    fun getTopCategories(): List<Category> {
        val list = mutableListOf<Category>()
        val c = database.rawQuery(
            "SELECT id, title, parent_id FROM categories ORDER BY id", null
        )
        c.use {
            while (it.moveToNext()) {
                list.add(Category(it.getInt(0), it.getString(1) ?: "", it.getInt(2)))
            }
        }
        return list
    }

    fun getCategory(id: Int): Category? {
        val c = database.rawQuery("SELECT id, title, parent_id FROM categories WHERE id=?", arrayOf(id.toString()))
        c.use {
            if (it.moveToFirst()) return Category(it.getInt(0), it.getString(1) ?: "", it.getInt(2))
        }
        return null
    }

    // ---------------------------------------------------------------- M_items

    fun getItemsForCategory(catId: Int): List<MItem> {
        val list = mutableListOf<MItem>()
        val c = database.rawQuery(
            """SELECT rowid, _id, M_name, M_cat_id, M_fav, M_type, M_prio, M_montakhab, M_position
               FROM M_items WHERE M_cat_id=? ORDER BY M_prio ASC""",
            arrayOf(catId.toString())
        )
        c.use { while (it.moveToNext()) list.add(cursorToItem(it)) }
        return list
    }

    fun getItemByRowId(rowId: Long): MItem? {
        val c = database.rawQuery(
            """SELECT rowid, _id, M_name, M_cat_id, M_fav, M_type, M_prio, M_montakhab, M_position
               FROM M_items WHERE rowid=?""",
            arrayOf(rowId.toString())
        )
        c.use { if (it.moveToFirst()) return cursorToItem(it) }
        return null
    }

    /** M_id ("_id") is only guaranteed unique among M_type=1 rows - safe for opening a dua to read. */
    fun getReadableItemByMId(mId: Int): MItem? {
        val c = database.rawQuery(
            """SELECT rowid, _id, M_name, M_cat_id, M_fav, M_type, M_prio, M_montakhab, M_position
               FROM M_items WHERE _id=? AND M_type=1""",
            arrayOf(mId.toString())
        )
        c.use { if (it.moveToFirst()) return cursorToItem(it) }
        return null
    }

    fun getFavorites(): List<MItem> {
        val list = mutableListOf<MItem>()
        val c = database.rawQuery(
            """SELECT rowid, _id, M_name, M_cat_id, M_fav, M_type, M_prio, M_montakhab, M_position
               FROM M_items WHERE M_fav=1 AND M_type=1 ORDER BY M_name""", null
        )
        c.use { while (it.moveToNext()) list.add(cursorToItem(it)) }
        return list
    }

    fun getMontakhab(): List<MItem> {
        val list = mutableListOf<MItem>()
        val c = database.rawQuery(
            """SELECT rowid, _id, M_name, M_cat_id, M_fav, M_type, M_prio, M_montakhab, M_position
               FROM M_items WHERE M_montakhab=1 AND M_type=1 ORDER BY M_prio""", null
        )
        c.use { while (it.moveToNext()) list.add(cursorToItem(it)) }
        return list
    }

    fun setFavorite(rowId: Long, fav: Boolean) {
        database.execSQL(
            "UPDATE M_items SET M_fav=? WHERE rowid=?",
            arrayOf(if (fav) 1 else 0, rowId)
        )
    }

    private fun cursorToItem(c: Cursor): MItem = MItem(
        rowId = c.getLong(0),
        id = c.getInt(1),
        name = c.getString(2) ?: "",
        catId = c.getInt(3),
        fav = c.getInt(4) == 1,
        type = c.getInt(5),
        prio = c.getInt(6),
        montakhab = c.getInt(7) == 1,
        position = c.getString(8) ?: ""
    )

    // ---------------------------------------------------------------- Lines

    fun getLinesForItem(mId: Int): List<Line> {
        val list = mutableListOf<Line>()
        val c = database.rawQuery(
            """SELECT rowid, _id, L_M_id, L_text, L_Translate, L_prio, L_type, L_fors
               FROM Lines WHERE L_M_id=? ORDER BY L_prio ASC""",
            arrayOf(mId.toString())
        )
        c.use { while (it.moveToNext()) list.add(cursorToLine(it)) }
        return list
    }

    fun getLineByRowId(rowId: Long): Line? {
        val c = database.rawQuery(
            """SELECT rowid, _id, L_M_id, L_text, L_Translate, L_prio, L_type, L_fors
               FROM Lines WHERE rowid=?""",
            arrayOf(rowId.toString())
        )
        c.use { if (it.moveToFirst()) return cursorToLine(it) }
        return null
    }

    private fun cursorToLine(c: Cursor): Line = Line(
        rowId = c.getLong(0),
        id = c.getInt(1),
        mId = c.getInt(2),
        text = c.getString(3) ?: "",
        translate = c.getString(4) ?: "",
        prio = c.getInt(5),
        type = c.getInt(6),
        fors = c.getString(7) ?: ""
    )

    // ---------------------------------------------------------------- search

    /**
     * Tries the FTS4 index first (fast, prefix-matched per word). If that
     * comes back empty - e.g. the term sits in the middle of a word, which
     * tokenized FTS won't match - falls back to the previous substring LIKE
     * scan so search quality doesn't regress for those cases.
     */
    fun search(query: String, limit: Int = 200): List<SearchResult> {
        val norm = TextNormalizer.normalize(query)
        if (norm.length < 2) return emptyList()

        val ftsResults = searchFts(norm, limit)
        if (ftsResults.isNotEmpty()) return ftsResults
        return searchLike(norm, limit)
    }

    private fun searchFts(norm: String, limit: Int): List<SearchResult> {
        val matchQuery = norm.split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .joinToString(" ") { "${escapeFtsToken(it)}*" }
        if (matchQuery.isBlank()) return emptyList()

        val results = mutableListOf<SearchResult>()
        try {
            val c = database.rawQuery(
                """SELECT lines_fts.rowid, lines_fts.l_m_id, M.M_name, lines_fts.l_text
                   FROM lines_fts JOIN M_items M ON M._id = lines_fts.l_m_id AND M.M_type = 1
                   WHERE lines_fts MATCH ?
                   ORDER BY M.M_prio LIMIT ?""",
                arrayOf(matchQuery, limit.toString())
            )
            c.use {
                while (it.moveToNext()) {
                    val text = it.getString(3) ?: ""
                    results.add(
                        SearchResult(
                            mItemId = it.getInt(1),
                            lineRowId = it.getLong(0),
                            itemTitle = it.getString(2) ?: "",
                            snippet = if (text.length > 140) text.substring(0, 140) + "…" else text
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Malformed MATCH syntax (rare edge-case punctuation) - fall through to LIKE.
            return emptyList()
        }
        return results
    }

    private fun escapeFtsToken(token: String): String =
        token.replace("\"", "").replace("*", "")

    private fun searchLike(norm: String, limit: Int): List<SearchResult> {
        val like = "%$norm%"
        val results = mutableListOf<SearchResult>()
        val c = database.rawQuery(
            """SELECT L.rowid, L.L_M_id, M.M_name, L.L_text
               FROM Lines L JOIN M_items M ON M._id = L.L_M_id AND M.M_type = 1
               WHERE L.L_fors LIKE ? OR L.L_text LIKE ? OR M.M_name LIKE ?
               ORDER BY M.M_prio, L.L_prio LIMIT ?""",
            arrayOf(like, like, like, limit.toString())
        )
        c.use {
            while (it.moveToNext()) {
                val text = it.getString(3) ?: ""
                results.add(
                    SearchResult(
                        mItemId = it.getInt(1),
                        lineRowId = it.getLong(0),
                        itemTitle = it.getString(2) ?: "",
                        snippet = if (text.length > 140) text.substring(0, 140) + "…" else text
                    )
                )
            }
        }
        return results
    }

    // ---------------------------------------------------------------- bookmarks

    fun addBookmark(mId: Int, lineRowId: Long, itemTitle: String, snippet: String) {
        val short = if (snippet.length > 140) snippet.substring(0, 140) + "…" else snippet
        database.execSQL(
            """INSERT OR REPLACE INTO bookmarks (m_id, line_rowid, item_title, snippet, created_at)
               VALUES (?,?,?,?,?)""",
            arrayOf(mId, lineRowId, itemTitle, short, System.currentTimeMillis())
        )
    }

    fun removeBookmark(id: Long) {
        database.execSQL("DELETE FROM bookmarks WHERE _id=?", arrayOf(id))
    }

    fun removeBookmark(mId: Int, lineRowId: Long) {
        database.execSQL("DELETE FROM bookmarks WHERE m_id=? AND line_rowid=?", arrayOf(mId, lineRowId))
    }

    fun isBookmarked(mId: Int, lineRowId: Long): Boolean {
        val c = database.rawQuery(
            "SELECT _id FROM bookmarks WHERE m_id=? AND line_rowid=?",
            arrayOf(mId.toString(), lineRowId.toString())
        )
        c.use { return it.moveToFirst() }
    }

    fun getBookmarks(): List<Bookmark> {
        val list = mutableListOf<Bookmark>()
        val c = database.rawQuery(
            "SELECT _id, m_id, line_rowid, item_title, snippet, created_at FROM bookmarks ORDER BY created_at DESC",
            null
        )
        c.use {
            while (it.moveToNext()) {
                list.add(
                    Bookmark(
                        id = it.getLong(0),
                        mItemId = it.getInt(1),
                        lineRowId = it.getLong(2),
                        itemTitle = it.getString(3) ?: "",
                        snippet = it.getString(4) ?: "",
                        createdAt = it.getLong(5)
                    )
                )
            }
        }
        return list
    }

    companion object {
        private const val DB_NAME = "mafatih.db"
        private const val PREF = "mafatih_db_meta"
        private const val KEY_DB_VERSION = "db_version"

        /** Bump this if a newer MyMafatih.zip / mafatih.db asset is shipped, to force re-copy. */
        private const val DB_CONTENT_VERSION = 1

        @Volatile
        private var instance: DatabaseHelper? = null

        fun getInstance(context: Context): DatabaseHelper =
            instance ?: synchronized(this) {
                instance ?: DatabaseHelper(context).also { instance = it }
            }
    }
}
