package com.tsa.bmicalculator

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.ChipGroup

class FormActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form) // Replace with your actual layout

        val etName = findViewById<EditText>(R.id.etName)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)
        val cgGender = findViewById<ChipGroup>(R.id.cgGender)

        btnSubmit.setOnClickListener {
            val name = etName.text.toString().trim()
            val genderChipId = cgGender.checkedChipId

            // Validate name
            if (name.isEmpty()) {
                etName.error = "Name is required"
                etName.requestFocus()
                return@setOnClickListener
            }

            // Validate gender selection
            if (genderChipId == -1) { // No chip is selected
                Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val gender = when (genderChipId) {
                R.id.chipMale -> "Male"
                R.id.chipFemale -> "Female"
                else -> "Unknown"
            }

            // Save name and gender to SharedPreferences
            val sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("name", name)
            editor.putString("gender", gender)
            editor.putBoolean("IsFormCompleted", true)
            editor.apply()

            // Navigate to MainActivity after saving the data
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Optionally, finish FormActivity so the user can't go back
        }
    }
}


