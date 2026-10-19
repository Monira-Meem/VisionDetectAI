package com.visiondetect.ai.presentation.splash
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.visiondetect.ai.R
import com.visiondetect.ai.databinding.ActivitySplashBinding
import com.visiondetect.ai.presentation.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    private lateinit var b: ActivitySplashBinding
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(b.root)
        b.ivLogo.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in))
        b.tvAppName.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in))
        b.tvTagline.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up_fade_in))
        lifecycleScope.launch {
            delay(2500L)
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            finish()
        }
    }
}
