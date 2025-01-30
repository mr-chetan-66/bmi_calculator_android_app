package com.tsa.bmicalculator

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bumptech.glide.Glide
import com.tsa.bmicalculator.databinding.FragmentHomeBinding
import java.text.DecimalFormat

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val imageSwitchViewModel: ImageSwitchViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        setupObservers()
        setupListeners()
        return binding.root
    }

    private fun setupObservers() {
        // Observe the image resource from ImageSwitchViewModel
        imageSwitchViewModel.imageResource.observe(viewLifecycleOwner) { imageRes ->
            fadeAndSwitchImage(imageRes)
        }

        // Restore user inputs from SharedViewModel
        sharedViewModel.weightInput.observe(viewLifecycleOwner) { weight ->
            if (!weight.isNullOrEmpty()) binding.etWeight.setText(weight)
        }
        sharedViewModel.feetInput.observe(viewLifecycleOwner) { feet ->
            if (!feet.isNullOrEmpty()) binding.etFeet.setText(feet)
        }
        sharedViewModel.inchesInput.observe(viewLifecycleOwner) { inches ->
            if (!inches.isNullOrEmpty()) binding.etInches.setText(inches)
        }
        sharedViewModel.ageInput.observe(viewLifecycleOwner) { age ->
            if (!age.isNullOrEmpty()) binding.etAge.setText(age)
        }
    }

    private fun setupListeners() {
        // Listener for BMI calculation
        binding.btnCalculate.setOnClickListener {
            saveInputsToViewModel() // Save inputs before calculation
            calculateBMI()
        }
    }

    private fun saveInputsToViewModel() {
        val weight = binding.etWeight.text.toString()
        val feet = binding.etFeet.text.toString()
        val inches = binding.etInches.text.toString()
        val age = binding.etAge.text.toString()
        sharedViewModel.setUserInputs(weight, feet, inches, age)
    }

    private fun calculateBMI() {
        val weightInput = binding.etWeight.text.toString()
        val feetInput = binding.etFeet.text.toString()
        val inchesInput = binding.etInches.text.toString()
        val ageInput = binding.etAge.text.toString()

        if (weightInput.isBlank() || feetInput.isBlank() || inchesInput.isBlank() || ageInput.isBlank()) {
            Toast.makeText(requireContext(), "Please fill in all the fields.", Toast.LENGTH_SHORT).show()
            return
        }

        val weight = weightInput.toDoubleOrNull()
        val feet = feetInput.toIntOrNull()
        val inches = inchesInput.toIntOrNull()
        val age = ageInput.toIntOrNull()

        if (weight == null || feet == null || inches == null || age == null || weight <= 0 || feet < 0 || inches < 0 || age <= 0 || age > 110) {
            Toast.makeText(requireContext(), "Invalid Input Format.", Toast.LENGTH_SHORT).show()
            return
        }

        val totalInches = (feet * 12) + inches
        val heightMeters = totalInches * 0.0254
        val bmi = weight / (heightMeters * heightMeters)

        // Save height and weight to SharedViewModel for ProfileFragment
        sharedViewModel.setUserStats(heightMeters.toInt(), weight.toInt())

        if (age < 18) {
            displayMinor(bmi)
        } else {
            calculateWeightToGainOrLose(bmi, heightMeters, weight)
        }
    }

    private fun displayMinor(bmi: Double) {
        val bmiResult = formatBMI(bmi)
        imageSwitchViewModel.underage()

        val selectedChipId = binding.cgGender.checkedChipId
        val genderText = when (selectedChipId) {
            R.id.chipMale -> "boys"
            R.id.chipFemale -> "girls"
            else -> {
                Toast.makeText(requireContext(), "Please select your gender.", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val resultString = "$bmiResult - As you are under 18, please consult your doctor for a healthy range for $genderText!"
        binding.tvResult.setTextColor(Color.RED)
        binding.tvResult.text = resultString
        binding.resultCardView.visibility = View.VISIBLE
    }

    private fun calculateWeightToGainOrLose(bmi: Double, heightMeters: Double, weightKg: Double) {
        val bmiResult = formatBMI(bmi)
        imageSwitchViewModel.updateImageBasedOnBmi(bmiResult.toFloat())
        val normalBmiMin = 18.5
        val normalBmiMax = 24.9
        val resultString: String

        val weightForNormalBmiMin = normalBmiMin * (heightMeters * heightMeters)
        val weightForNormalBmiMax = normalBmiMax * (heightMeters * heightMeters)

        resultString = when {
            bmi < normalBmiMin -> {
                val weightToGain = weightForNormalBmiMin - weightKg
                sharedViewModel.setBmi(bmi, weightToGain)
                "You are underweight.\nGain %.2f kg to reach a healthy BMI.".format(weightToGain)
            }
            bmi > normalBmiMax -> {
                val weightToLose = weightKg - weightForNormalBmiMax
                sharedViewModel.setBmi(bmi, -weightToLose)
                "You are overweight.\nLose %.2f kg to reach a normal BMI.".format(weightToLose)
            }
            else -> {
                sharedViewModel.setBmi(bmi, 99.99) // No weight adjustment needed
                "$bmiResult - BMI is in the normal range!\nYou have a healthy weight."
            }
        }

        binding.tvResult.text = resultString
        binding.tvResult.setTextColor(if (bmi in normalBmiMin..normalBmiMax) Color.GREEN else Color.RED)
        binding.resultCardView.visibility = View.VISIBLE
    }

    private fun formatBMI(bmi: Double): String {
        val decimalFormat = DecimalFormat("0.00")
        return decimalFormat.format(bmi)
    }

    private fun fadeAndSwitchImage(imageRes: Int) {
        val fadeOut = AlphaAnimation(1f, 0f).apply { duration = 250 }
        binding.imageView.startAnimation(fadeOut)
        Glide.with(binding.imageView.context)
            .load(imageRes)
            .into(binding.imageView)
        val fadeIn = AlphaAnimation(0f, 1f).apply { duration = 250 }
        binding.imageView.startAnimation(fadeIn)
    }

    override fun onPause() {
        super.onPause()
        imageSwitchViewModel.stopImageSwitching()
    }

    override fun onResume() {
        super.onResume()
        if (!imageSwitchViewModel.isImageSwitchingActive()) {
            imageSwitchViewModel.startImageSwitching()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        imageSwitchViewModel.cleanup()
    }
}


class ImageSwitchViewModel : ViewModel() {
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private var currentImageIndex = 0
    private val _imageResource = MutableLiveData<Int>()
    val imageResource: LiveData<Int> = _imageResource
    private var isImageSwitchingStarted = false

    private val images = arrayOf(
        R.drawable.girl_yoga_profile,
        R.drawable.girl_workout_profile,
        R.drawable.boy_yoga_profile,
        R.drawable.bmi_range,
        R.drawable.bmi_scale,
        R.drawable.boy_getting_ready,
        R.drawable.weight
    )

    init {
        startImageSwitching()
    }

    fun startImageSwitching() {
        if (!isImageSwitchingStarted) {
            isImageSwitchingStarted = true
            switchImages()
        }
    }

    private fun switchImages() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                _imageResource.value = images[currentImageIndex]
                currentImageIndex = (currentImageIndex + 1) % images.size
                handler.postDelayed(this, 2000)
            }
        }, 2000)
    }

    fun stopImageSwitching() {
        isImageSwitchingStarted = false
        handler.removeCallbacksAndMessages(null)
    }

    fun resumeImageSwitchingAfterDelay(delayMillis: Long) {
        handler.postDelayed({
            startImageSwitching()
        }, delayMillis)
    }

    fun underage() {
        _imageResource.value = R.drawable.underage
        stopImageSwitching()
        resumeImageSwitchingAfterDelay(2500)
    }

    fun updateImageBasedOnBmi(bmi: Float) {
        when {
            bmi < 18.5 -> {
                _imageResource.value = R.drawable.healthy_food_image
                stopImageSwitching()
                resumeImageSwitchingAfterDelay(2500)
            }
            bmi > 24.9 -> {
                _imageResource.value = R.drawable.gym_direction_profile
                stopImageSwitching()
                resumeImageSwitchingAfterDelay(2500)
            }
            else -> {
                startImageSwitching()
                _imageResource.value = images[currentImageIndex]
            }
        }
    }

    fun isImageSwitchingActive(): Boolean {
        return isImageSwitchingStarted
    }

    fun cleanup() {
        handler.removeCallbacksAndMessages(null)
    }

    override fun onCleared() {
        super.onCleared()
        cleanup()
    }
}
