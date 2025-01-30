package com.tsa.bmicalculator

import android.animation.ValueAnimator
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var greetingTextView: TextView
    private lateinit var currentBmiTextView: TextView
    private lateinit var requiredChangeTextView: TextView
    private lateinit var healthTipsTextView: TextView
    private lateinit var profileImageView: ImageView
    private lateinit var bmiChart: LineChart
    private lateinit var heightValueTextView: TextView
    private lateinit var weightValueTextView: TextView
    private lateinit var checkinsCountTextView: TextView
    private lateinit var factTextView: TextView
    private lateinit var bmiCategoryTextView: TextView
    private val sharedViewModel: SharedViewModel by activityViewModels()

    private var typewriterJob: Job? = null
    private var texts = listOf("Hii CHETAN !")

    private val dailyFacts = listOf(
        "Drink plenty of water every day!",
        "Regular exercise can improve mental health.",
        "Getting enough sleep is essential for overall health.",
        "A balanced diet keeps you fit and energetic.",
        "Stretch daily to maintain flexibility."
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        greetingTextView = view.findViewById(R.id.account_name)
        currentBmiTextView = view.findViewById(R.id.current_bmi_no)
        requiredChangeTextView = view.findViewById(R.id.weight_change_no)
        healthTipsTextView = view.findViewById(R.id.health_tips_content)
        profileImageView = view.findViewById(R.id.profile_picture)
        factTextView = view.findViewById(R.id.fact_text_view)
        bmiChart = view.findViewById(R.id.bmi_chart)
        heightValueTextView = view.findViewById(R.id.height_value)
        weightValueTextView = view.findViewById(R.id.weight_value)
        checkinsCountTextView = view.findViewById(R.id.checkins_count)
        bmiCategoryTextView = view.findViewById(R.id.bmi_category)

        bmiCategory()
        startTypewriterEffect()
        setProfile()
        updateQuickStats()
        displayBmiInfo()
        showNewDailyFact()
        setupChart()
        return view
    }

    private fun bmiCategory() {
        sharedViewModel.bmi.observe(viewLifecycleOwner) { bmi ->
            if (bmi != null) {
                // Classify BMI and set text accordingly
                bmiCategoryTextView.text = classifyBmi(bmi)
            } else {
                // If BMI is not available, show "Not Classified" message
                bmiCategoryTextView.text = "Not Classified"
            }
        }
    }
    private fun classifyBmi(bmi: Double): String {
        return when {
            bmi < 18.5 -> "Underweight"
            bmi in 18.5..24.9 -> "Normal Weight"
            bmi in 25.0..29.9 -> "Overweight"
            else -> "Obesity"
        }
    }

    private fun setupChart() {
        bmiChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setPinchZoom(true)
            setDrawGridBackground(false)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "Day ${value.toInt() + 1}"
                }
            }
            axisRight.isEnabled = false
            axisLeft.setDrawGridLines(false)
            xAxis.setDrawGridLines(false)
        }

        updateChart()
    }

    private fun updateChart() {
        sharedViewModel.bmiHistory.observe(viewLifecycleOwner) { bmiHistory ->
            val entries = bmiHistory.mapIndexed { index, bmi ->
                Entry(index.toFloat(), bmi.toFloat())
            }

            val dataSet = LineDataSet(entries, "BMI Progress").apply {
                color = Color.BLUE
                valueTextColor = Color.BLACK
                lineWidth = 2f
                setCircleColor(Color.RED)
                circleRadius = 5f
            }

            bmiChart.data = LineData(dataSet)
            bmiChart.invalidate()
        }
    }

    private fun updateQuickStats() {
        sharedViewModel.height.observe(viewLifecycleOwner) { height ->
            heightValueTextView.text = "${height} cm"
        }

        sharedViewModel.weight.observe(viewLifecycleOwner) { weight ->
            weightValueTextView.text = "${weight} kg"
        }

        sharedViewModel.checkinsCount.observe(viewLifecycleOwner) { count ->
            checkinsCountTextView.text = count.toString()
        }
    }

    private fun setProfile() {
        val sharedPreferences = requireActivity().getSharedPreferences("UserProfile", MODE_PRIVATE)
        val gender = sharedPreferences.getString("gender", "Unknown")
        when (gender) {
            "Male" -> profileImageView.setImageResource(R.drawable.profile_boy)
            else -> profileImageView.setImageResource(R.drawable.profile_girl)
        }
    }

    private fun startTypewriterEffect() {
        val sharedPreferences = requireActivity().getSharedPreferences("UserProfile", MODE_PRIVATE)
        val name = sharedPreferences.getString("name", "Buddy") ?: "Buddy"

        texts = listOf("Hii $name !")
        greetingTextView.text = ""
        var textIndex = 0

        typewriterJob?.cancel()

        typewriterJob = lifecycleScope.launch {
            while (true) {
                val text = texts[textIndex]
                typeText(text)
                textIndex = (textIndex + 1) % texts.size
                delay(1500)
            }
        }
    }

    private suspend fun typeText(text: String) {
        greetingTextView.text = ""
        for (char in text) {
            greetingTextView.append(char.toString())
            delay(130)
        }
    }

    private fun displayBmiInfo() {
        currentBMI()
        weightChange()
    }

    private fun weightChange() {
        sharedViewModel.weightNeeded.observe(viewLifecycleOwner) { change ->
            if (change != null) {
                if (change == 99.99) {
                    requiredChangeTextView.text = "N/A"
                } else if (change >= 0) {
                    requiredChangeTextView.setTextColor(Color.GREEN)
                    animateNumber(requiredChangeTextView, 0f, change.toFloat())
                } else {
                    requiredChangeTextView.setTextColor(Color.RED)
                    animateNumber(requiredChangeTextView, 0f, change.toFloat())
                }
            } else {
                requiredChangeTextView.text = "N/A"
            }
        }
    }

    private fun currentBMI() {
        sharedViewModel.bmi.observe(viewLifecycleOwner) { bmiValue ->
            displayHealthTips(bmiValue)
            if (bmiValue != null) {
                when {
                    bmiValue < 18.4 -> currentBmiTextView.setTextColor(Color.RED)
                    bmiValue > 24.9 -> currentBmiTextView.setTextColor(Color.RED)
                    else -> currentBmiTextView.setTextColor(Color.GREEN)
                }
                currentBmiTextView.text = "%.2f".format(bmiValue)
            } else {
                currentBmiTextView.text = "N/A"
            }
        }
    }

    private fun displayHealthTips(currentBmi: Double?) {
        val healthTip = when {
            currentBmi == null -> "Calculate BMI to receive personalized tips."
            currentBmi < 18.5 -> """
                • Underweight: Increase calorie intake.
                  - Focus on nutrient-dense foods (nuts, avocados).
                  - Add protein (lean meats, dairy, legumes).
                  - Eat frequent small meals and snacks.
                  - Consider smoothies or shakes for extra calories.
            """.trimIndent()
            currentBmi in 18.5..24.9 -> """
                • Healthy weight: Stay active.
                  - Maintain a balanced diet with fruits and vegetables.
                  - Incorporate strength training to build muscle.
                  - Stay hydrated and avoid excessive sugar.
                  - Regular check-ups to monitor health.
            """.trimIndent()
            currentBmi < 29.9 -> """
                • Overweight: Exercise regularly.
                  - Aim for at least 150 minutes of moderate exercise weekly.
                  - Focus on portion control and mindful eating.
                  - Include more whole foods (grains, fruits, veggies).
                  - Limit processed foods and sugary drinks.
            """.trimIndent()
            else -> """
                • Obese: Seek professional advice.
                  - Consult a healthcare provider for a tailored plan.
                  - Consider a balanced diet and regular exercise.
                  - Track food intake and activity levels.
                  - Support groups can help with accountability.
            """.trimIndent()
        }
        healthTipsTextView.setTextColor(Color.WHITE)
        healthTipsTextView.text = healthTip
    }

    private fun showNewDailyFact() {
        val sharedPrefs = requireContext().getSharedPreferences("DailyFacts", Context.MODE_PRIVATE)
        val lastFactIndex = sharedPrefs.getInt("last_fact_index", -1)
        val nextFactIndex = (lastFactIndex + 1) % dailyFacts.size

        factTextView.text = dailyFacts[nextFactIndex]
        sharedPrefs.edit().putInt("last_fact_index", nextFactIndex).apply()
    }

    private fun animateNumber(textView: TextView, start: Float, end: Float) {
        val animator = ValueAnimator.ofFloat(start, end)
        animator.duration = 1100
        animator.interpolator = LinearInterpolator()
        animator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Float
            if (end >= 0) {
                textView.text = String.format("+%.2f kg", animatedValue)
            } else {
                textView.text = String.format("%.2f kg", animatedValue)
            }
        }
        animator.start()
    }

    override fun onPause() {
        super.onPause()
        typewriterJob?.cancel()
    }
}
