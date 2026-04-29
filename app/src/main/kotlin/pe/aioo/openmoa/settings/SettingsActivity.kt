package pe.aioo.openmoa.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import pe.aioo.openmoa.R

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(findViewById<MaterialToolbar>(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.settingsContainer, SettingsFragment())
                .commit()
        }
        findViewById<LinearLayout>(R.id.overlay_permission_banner).setOnClickListener {
            launchOverlayPermissionSettings()
        }
    }

    override fun onResume() {
        super.onResume()
        findViewById<LinearLayout>(R.id.overlay_permission_banner).visibility =
            if (Settings.canDrawOverlays(this)) View.GONE else View.VISIBLE
        if (Settings.canDrawOverlays(this)) {
            SettingsPreferences.setOverlayPermissionNotified(this, false)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun launchOverlayPermissionSettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName"),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent)
        } catch (_: Exception) {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
        }
    }
}
