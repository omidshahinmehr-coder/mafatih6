package ir.mafatih.aljinan.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import ir.mafatih.aljinan.R
import ir.mafatih.aljinan.databinding.ActivitySettingsBinding
import ir.mafatih.aljinan.util.FontResolver
import ir.mafatih.aljinan.util.PrefsManager

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: PrefsManager

    // A modest, print-friendly palette - enough variety without turning the
    // screen into a full RGB picker.
    private val duaSwatches = intArrayOf(
        0xFF0B2A54.toInt(), 0xFF123B71.toInt(), 0xFF1B5E20.toInt(),
        0xFF4E342E.toInt(), 0xFF000000.toInt(), 0xFF6A1B9A.toInt()
    )
    private val translateSwatches = intArrayOf(
        0xFF6B6357.toInt(), 0xFF616161.toInt(), 0xFF37474F.toInt(),
        0xFF795548.toInt(), 0xFF8D6E63.toInt(), 0xFF455A64.toInt()
    )
    private val bgSwatches = intArrayOf(
        0xFFFBF8F1.toInt(), 0xFFFFFFFF.toInt(), 0xFFF1E9D2.toInt(),
        0xFFE8F1E9.toInt(), 0xFFEFEFEF.toInt(), 0xFF1E1E1E.toInt()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = PrefsManager(this)
        binding.toolbar.title = getString(R.string.settings_title)
        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupFontChoice()
        setupFontSize()
        setupSwatches(binding.rowDuaColors, duaSwatches) { prefs.duaColor = it; refreshPreview() }
        setupSwatches(binding.rowTranslateColors, translateSwatches) { prefs.translateColor = it; refreshPreview() }
        setupSwatches(binding.rowBgColors, bgSwatches) { prefs.bgColor = it; refreshPreview() }
        setupTranslateSwitch()

        binding.btnReset.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(R.string.settings_reset)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    prefs.resetDisplayPrefs()
                    recreate()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
        }

        refreshPreview()
    }

    private fun setupFontChoice() {
        val checkedId = when (prefs.fontFamily) {
            PrefsManager.FONT_ESTEDAD -> binding.rbFontEstedad.id
            PrefsManager.FONT_NEIRIZI -> binding.rbFontNeirizi.id
            PrefsManager.FONT_ENTEZAR -> binding.rbFontEntezar.id
            PrefsManager.FONT_IRANIAN_SANS -> binding.rbFontIranianSans.id
            else -> binding.rbFontSystem.id
        }
        binding.rgFont.check(checkedId)
        binding.rgFont.setOnCheckedChangeListener { _, id ->
            prefs.fontFamily = when (id) {
                binding.rbFontEstedad.id -> PrefsManager.FONT_ESTEDAD
                binding.rbFontNeirizi.id -> PrefsManager.FONT_NEIRIZI
                binding.rbFontEntezar.id -> PrefsManager.FONT_ENTEZAR
                binding.rbFontIranianSans.id -> PrefsManager.FONT_IRANIAN_SANS
                else -> PrefsManager.FONT_SYSTEM
            }
            refreshPreview()
        }
    }

    private fun setupFontSize() {
        val min = PrefsManager.MIN_FONT_SIZE
        binding.seekFontSize.max = (PrefsManager.MAX_FONT_SIZE - min).toInt()
        binding.seekFontSize.progress = (prefs.fontSize - min).toInt()
        binding.tvFontSizeValue.text = prefs.fontSize.toInt().toString()

        binding.seekFontSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val size = min + progress
                prefs.fontSize = size
                binding.tvFontSizeValue.text = size.toInt().toString()
                refreshPreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupTranslateSwitch() {
        binding.switchShowTranslate.isChecked = prefs.showTranslate
        binding.switchShowTranslate.setOnCheckedChangeListener { _, isChecked ->
            prefs.showTranslate = isChecked
        }
    }

    private fun setupSwatches(row: LinearLayout, colors: IntArray, onPick: (Int) -> Unit) {
        row.removeAllViews()
        val sizeDp = 40
        val scale = resources.displayMetrics.density
        val sizePx = (sizeDp * scale).toInt()
        val marginPx = (6 * scale).toInt()

        colors.forEach { color ->
            val frame = FrameLayout(this)
            val params = LinearLayout.LayoutParams(sizePx, sizePx)
            params.setMargins(marginPx, marginPx, marginPx, marginPx)
            frame.layoutParams = params

            val circle = View(this)
            val circleParams = FrameLayout.LayoutParams(sizePx, sizePx)
            circle.layoutParams = circleParams
            val drawable = GradientDrawable()
            drawable.shape = GradientDrawable.OVAL
            drawable.setColor(color)
            drawable.setStroke((1 * scale).toInt(), Color.parseColor("#33000000"))
            circle.background = drawable

            frame.addView(circle)
            frame.setOnClickListener { onPick(color) }
            row.addView(frame)
        }
    }

    private fun refreshPreview() {
        binding.tvPreview.setTextColor(prefs.duaColor)
        binding.tvPreview.textSize = prefs.fontSize
        binding.tvPreview.setBackgroundColor(prefs.bgColor)
        binding.tvPreview.typeface = FontResolver.typeface(this, prefs.fontFamily)
    }
}
