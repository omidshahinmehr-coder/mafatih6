package ir.mafatih.aljinan.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ir.mafatih.aljinan.R
import ir.mafatih.aljinan.db.DatabaseHelper
import ir.mafatih.aljinan.util.FontResolver
import ir.mafatih.aljinan.util.PrefsManager
import kotlin.concurrent.thread

/**
 * Shows the Salawat (in Entezar font, on a night-sky-blue background) for at
 * least SPLASH_MIN_DURATION_MS while the bundled mafatih.db is copied to
 * internal storage in the background (first run only - instant afterwards).
 * Navigation to MainActivity waits for BOTH the minimum duration and the
 * database copy to finish, whichever takes longer.
 */
class SplashActivity : AppCompatActivity() {

    private var dbReady = false
    private var minTimeElapsed = false
    private var navigated = false

    companion object {
        private const val SPLASH_MIN_DURATION_MS = 2200L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        findViewById<TextView>(R.id.tvSalawat).typeface =
            FontResolver.typeface(this, PrefsManager.FONT_ENTEZAR)

        thread {
            // Triggers first-run copy + app table creation if needed.
            DatabaseHelper.getInstance(applicationContext)
            dbReady = true
            runOnUiThread { tryProceed() }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            minTimeElapsed = true
            tryProceed()
        }, SPLASH_MIN_DURATION_MS)
    }

    private fun tryProceed() {
        if (navigated || !dbReady || !minTimeElapsed) return
        navigated = true
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
