package com.tsa.bmicalculator

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView
    private var backPressedTime = 0L
    private var currentFragment: Fragment? = null // Keep track of the current fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Toast.makeText(this, "Welcome to BMI Calculator!", Toast.LENGTH_SHORT).show()

        bottomNavigationView = findViewById(R.id.bottom_navigation)

        val sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE)
        val isFormCompleted = sharedPreferences.getBoolean("IsFormCompleted", false)

        if (!isFormCompleted) {
            val intent = Intent(this, FormActivity::class.java)
            startActivity(intent)
            finish()
        } else if (savedInstanceState != null) {
            // Restore the fragment
            val restoredFragment = supportFragmentManager.getFragment(savedInstanceState, "currentFragment")
            if (restoredFragment != null) {
                loadFragment(restoredFragment)
            }

            // Restore the selected navigation item
            val selectedItemId = savedInstanceState.getInt("selectedItemId", R.id.nav_home)
            bottomNavigationView.selectedItemId = selectedItemId
        } else {
            loadFragment(HomeFragment())
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            val selectedFragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_about -> AboutFragment()
                R.id.nav_profile -> ProfileFragment()
                R.id.nav_setting -> SettingFragment()
                else -> HomeFragment()
            }

            // Only load the fragment if it isn't already loaded
            if (currentFragment != null && currentFragment!!::class.java != selectedFragment::class.java) {
                loadFragment(selectedFragment)
            }
            true
        }

        handleBackPress()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Save the currently loaded fragment
        currentFragment?.let {
            supportFragmentManager.putFragment(outState, "currentFragment", it)
        }

        // Save the selected navigation item
        outState.putInt("selectedItemId", bottomNavigationView.selectedItemId)
    }

    private fun loadFragment(newFragment: Fragment) {
        // If the fragment to be loaded is the same as the current one, return
        if (newFragment::class.java == currentFragment?.javaClass) return

        currentFragment = newFragment // Update the current fragment

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            .replace(R.id.fragment_container, newFragment)
            .commit()
    }

    private fun handleBackPress() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentTime = System.currentTimeMillis()
                if (currentTime - backPressedTime <= 2000) {
                    finish()
                } else {
                    Toast.makeText(this@MainActivity, "Press back again to exit", Toast.LENGTH_SHORT).show()
                    backPressedTime = currentTime
                }
            }
        })
    }
}

