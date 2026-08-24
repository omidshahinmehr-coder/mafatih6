package ir.mafatih.aljinan.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ir.mafatih.aljinan.R
import ir.mafatih.aljinan.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    companion object {
        private const val DEVELOPER_EMAIL = "omidshahinmehr@gmail.com"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.title = getString(R.string.menu_about)
        binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val versionName = try {
            packageManager.getPackageInfo(packageName, 0).versionName
        } catch (e: Exception) {
            null
        }
        binding.tvVersion.text = getString(R.string.about_version_label) + ": " + (versionName ?: "1.0")

        binding.tvEmail.text = DEVELOPER_EMAIL
        binding.rowEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$DEVELOPER_EMAIL")
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.about_app_name))
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                // No email client installed - silently ignore, the address is
                // still visible on screen for the user to copy manually.
            }
        }
    }
}
