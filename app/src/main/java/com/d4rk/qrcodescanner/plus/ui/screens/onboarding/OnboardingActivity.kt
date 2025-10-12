package com.d4rk.qrcodescanner.plus.ui.screens.onboarding

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.d4rk.qrcodescanner.plus.R
import com.d4rk.qrcodescanner.plus.data.onboarding.OnboardingPreferences
import com.d4rk.qrcodescanner.plus.ui.screens.main.MainActivity

class OnboardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (OnboardingPreferences.isOnboardingComplete(this)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }
        setContentView(R.layout.activity_onboarding)

        findViewById<Button>(R.id.button_skip_onboarding).setOnClickListener {
            OnboardingPreferences.setOnboardingComplete(this, true)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
