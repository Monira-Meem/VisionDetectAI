package com.visiondetect.ai.presentation
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.visiondetect.ai.R
import com.visiondetect.ai.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var b: ActivityMainBinding
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)
        val nav = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
        b.bottomNavigation.setupWithNavController(nav)
        nav.addOnDestinationChangedListener { _, dest, _ ->
            b.bottomNavigation.visibility = if (dest.id == R.id.detectionFragment) View.GONE else View.VISIBLE
        }
    }
}
