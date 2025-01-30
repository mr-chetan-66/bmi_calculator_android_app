package com.tsa.bmicalculator

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class EntryAnim : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_entry_anim)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        supportActionBar?.hide()
        val t = Thread {
            try {
                SystemClock.sleep(3500) // Sleep for 1 second
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                // Ensure that UI updates are done on the main thread
                runOnUiThread {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
        }
        t.start()
    }
}
