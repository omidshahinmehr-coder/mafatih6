package ir.mafatih.aljinan.util

/**
 * Normalizes Arabic/Persian text for diacritic-insensitive search:
 * - strips tashkeel (harakat) and tatweel
 * - unifies different forms of alef / kaf / ye
 * Used both on the user's search query and (defensively) on stored text,
 * so search still works even though Lines.L_fors is already pre-stripped.
 */
object TextNormalizer {

    private val DIACRITICS = Regex("[\u064B-\u0652\u0670\u06D6-\u06ED\u0640]")

    fun normalize(input: String): String {
        var s = input.trim()
        s = DIACRITICS.replace(s, "")
        s = s.replace('\u0623', '\u0627') // أ -> ا
            .replace('\u0625', '\u0627') // إ -> ا
            .replace('\u0622', '\u0627') // آ -> ا
            .replace('\u0629', '\u0647') // ة -> ه
            .replace('\u064A', '\u06CC') // ي -> ی
            .replace('\u0643', '\u06A9') // ك -> ک
            .replace("‌", " ")           // ZWNJ -> space
        return s.replace(Regex("\\s+"), " ").trim()
    }
}
