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

    private val _weight = MutableLiveData<Int>()
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
    private val _weightInput = MutableLiveData<String>()
    val weightInput: LiveData<String> get() = _weightInput

    private val _feetInput = MutableLiveData<String>()
    val feetInput: LiveData<String> get() = _feetInput

    private val _inchesInput = MutableLiveData<String>()
    val inchesInput: LiveData<String> get() = _inchesInput

    private val _ageInput = MutableLiveData<String>()
    val ageInput: LiveData<String> get() = _ageInput

    // Methods to update user inputs
    fun setUserInputs(weight: String, feet: String, inches: String, age: String) {
        _weightInput.value = weight
        _feetInput.value = feet
        _inchesInput.value = inches
        _ageInput.value = age
    }
}
