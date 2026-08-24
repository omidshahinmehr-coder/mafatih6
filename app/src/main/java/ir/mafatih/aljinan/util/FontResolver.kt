package ir.mafatih.aljinan.util

import android.content.Context
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import ir.mafatih.aljinan.R

/**
 * Resolves the font family key stored in PrefsManager to an actual Typeface.
 * Add a new font by: dropping the .ttf into res/font, adding a FONT_* constant
 * in PrefsManager, and mapping it here (plus a radio button in Settings).
 */
object FontResolver {

    // Typeface objects are cheap to reuse and app-lifetime-safe (they don't
    // hold a reference to the Context that created them), so caching one per
    // font family key avoids repeated ResourcesCompat/font-file lookups on
    // every single RecyclerView row bind.
    private val cache = mutableMapOf<String, Typeface?>()

    fun typeface(context: Context, fontFamily: String): Typeface? =
        cache.getOrPut(fontFamily) { loadTypeface(context, fontFamily) }

    private fun loadTypeface(context: Context, fontFamily: String): Typeface? = when (fontFamily) {
        PrefsManager.FONT_ESTEDAD -> ResourcesCompat.getFont(context, R.font.estedad)
        PrefsManager.FONT_NEIRIZI -> ResourcesCompat.getFont(context, R.font.neirizi)
        PrefsManager.FONT_ENTEZAR -> ResourcesCompat.getFont(context, R.font.entezar)
        PrefsManager.FONT_IRANIAN_SANS -> ResourcesCompat.getFont(context, R.font.a_iranian_sans)
        else -> Typeface.DEFAULT
    }

    fun displayName(fontFamily: String): String = when (fontFamily) {
        PrefsManager.FONT_ESTEDAD -> "استعداد"
        PrefsManager.FONT_NEIRIZI -> "نیریزی"
        PrefsManager.FONT_ENTEZAR -> "انتظار"
        PrefsManager.FONT_IRANIAN_SANS -> "ایرانیان سنس"
        else -> "قلم سیستم"
    }
}
