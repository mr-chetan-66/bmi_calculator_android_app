package com.tsa.bmicalculator

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {

    // LiveData for current BMI and weight needed
    private val _bmi = MutableLiveData<Double>()
    val bmi: LiveData<Double> get() = _bmi

    private val _weightNeeded = MutableLiveData<Double>()
    val weightNeeded: LiveData<Double> get() = _weightNeeded

    // LiveData for BMI history
    private val _bmiHistory = MutableLiveData<List<Double>>(emptyList())
    val bmiHistory: LiveData<List<Double>> get() = _bmiHistory

    // LiveData for height, weight, and check-ins count
    private val _height = MutableLiveData<Int>()
    val height: LiveData<Int> get() = _height

    private val _weight = MutableLiveData(-1)
    val weight: LiveData<Int> get() = _weight

    private val _checkinsCount = MutableLiveData<Int>(0)
    val checkinsCount: LiveData<Int> get() = _checkinsCount

    // Update BMI and add to history
    fun setBmi(bmiValue: Double, weightNeededValue: Double) {
        _bmi.value = bmiValue
        _weightNeeded.value = weightNeededValue

        // Add the new BMI value to history
        val updatedHistory = _bmiHistory.value.orEmpty().toMutableList()
        updatedHistory.add(bmiValue)
        _bmiHistory.value = updatedHistory

        // Increment check-ins count
        incrementCheckinsCount()
    }

    // Update height and weight
    fun setUserStats(heightValue: Int, weightValue: Int) {
        _height.value = heightValue
        _weight.value = weightValue
    }

    // Increment check-ins count
    private fun incrementCheckinsCount() {
        _checkinsCount.value = (_checkinsCount.value ?: 0) + 1
    }

    // LiveData for user inputs

    private val _feetInput = MutableLiveData<String>()
    val feetInput: LiveData<String> get() = _feetInput

    private val _inchesInput = MutableLiveData<String>()
    val inchesInput: LiveData<String> get() = _inchesInput

    private val _ageInput = MutableLiveData<String>()
    val ageInput: LiveData<String> get() = _ageInput

    // LiveData for computed height in cm
    private val _heightInCm = MutableLiveData("N/A")
    val heightInCm: LiveData<String> get() = _heightInCm

    // Method to update user inputs and calculate height in cm
    fun setUserInputs(weight: String, feet: String, inches: String, age: String) {
        _feetInput.value = feet
        _inchesInput.value = inches
        _ageInput.value = age

        // Convert feet & inches to cm
        val feetToCm = feet.toIntOrNull()?.times(30.48) ?: 0.0
        val inchesToCm = inches.toIntOrNull()?.times(2.54) ?: 0.0
        val totalHeight = feetToCm + inchesToCm

        _heightInCm.value = if (totalHeight > 0) "%.1f cm".format(totalHeight) else "N/A"

        // Update weight
        _weight.value = weight.toIntOrNull() ?: -1
    }

    private val _goalWeight = MutableLiveData<String>()
    private val _bmiImprovement = MutableLiveData<String>()

    val goalWeight: LiveData<String> get() = _goalWeight
    val bmiImprovement: LiveData<String> get() = _bmiImprovement
    private val _achievements = MutableLiveData<String>()
    val achievements: LiveData<String> get() = _achievements


    fun updateAchievements() {
        val checkins = _checkinsCount.value ?: 0
        val bmiHistoryList = _bmiHistory.value.orEmpty()

        val bmiImprovement = if (bmiHistoryList.size > 1) {
            (bmiHistoryList.last() - bmiHistoryList.first()).toString()
        } else {
            "0.0"
        }

        // Update the LiveData for the TextViews
        _achievements.value = """
        • Consistent Check-ins: $checkins times
        • Goal Weight Maintained: ${if (checkins >= 2) "Yes" else "No"}
        • BMI Improvement: $bmiImprovement points
    """.trimIndent()

        // Also update specific LiveData for Goal Weight and BMI Improvement
        _goalWeight.value = if (checkins >= 2) "Yes" else "No"
        _bmiImprovement.value = bmiImprovement
    }



}
