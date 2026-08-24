package ir.mafatih.aljinan.model

data class Bookmark(
    val id: Long,
    val mItemId: Int,
    val lineRowId: Long,
    val itemTitle: String,
    val snippet: String,
    val createdAt: Long
)

data class SearchResult(
    val mItemId: Int,
    val lineRowId: Long,
    val itemTitle: String,
    val snippet: String
)
