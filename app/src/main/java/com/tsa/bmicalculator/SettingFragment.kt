package com.tsa.bmicalculator

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.concurrent.TimeUnit

class SettingFragment : Fragment() {

    private data class Section(
        val layout: View,
        val contentView: View?,
        val arrow: ImageView,
        var isExpanded: Boolean = false
    )

    private val sections = mutableMapOf<Int, Section>()
    private lateinit var notificationSwitch: SwitchMaterial

    companion object {
        private const val PREF_NAME = "settings"
        private const val PREF_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val WORKER_TAG = "notification_worker"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize notification switch
        notificationSwitch = view.findViewById(R.id.notificationSwitch)
        setupNotificationSwitch()

        // Initialize all expandable sections
        initializeSections(view)

        // Setup click listeners for all sections
        setupSectionClickListeners()
    }

    private fun setupNotificationSwitch() {
        // Load saved preference
        val isEnabled = loadNotificationPreference()
        notificationSwitch.isChecked = isEnabled

        // Set listener for switch toggle
        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            saveNotificationPreference(isChecked)
            if (isChecked) {
                scheduleNotifications()
            } else {
                cancelNotifications()
            }
        }
    }

    private fun saveNotificationPreference(enabled: Boolean) {
        context?.let { ctx ->
            val sharedPreferences = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            sharedPreferences.edit()
                .putBoolean(PREF_NOTIFICATIONS_ENABLED, enabled)
                .apply()
        }
    }

    private fun loadNotificationPreference(): Boolean {
        return context?.let { ctx ->
            val sharedPreferences = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            sharedPreferences.getBoolean(PREF_NOTIFICATIONS_ENABLED, false)
        } ?: false
    }

    private fun scheduleNotifications() {
        val workManager = WorkManager.getInstance(requireContext())
        val morningRequest = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(calculateDelay(0, 6), TimeUnit.MILLISECONDS) // 9:00 AM
            .addTag(WORKER_TAG)
            .build()

        val eveningRequest = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(calculateDelay(0, 7), TimeUnit.MILLISECONDS) // 6:00 PM
            .addTag(WORKER_TAG)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "morning_notification",
            ExistingPeriodicWorkPolicy.UPDATE,
            morningRequest
        )

        workManager.enqueueUniquePeriodicWork(
            "evening_notification",
            ExistingPeriodicWorkPolicy.UPDATE ,
            eveningRequest
        )
    }

    private fun cancelNotifications() {
        WorkManager.getInstance(requireContext()).cancelAllWorkByTag(WORKER_TAG)
    }

    private fun calculateDelay(hour: Int, minute: Int): Long {
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = now
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }

        if (calendar.timeInMillis <= now) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1) // Schedule for next day
        }

        return calendar.timeInMillis - now
    }

    private fun initializeSections(view: View) {
        // Privacy sections
        setupSection(
            view,
            R.id.dataPrivacyLayout,
            R.id.privacyDetailsTextView,
            view.findViewById(R.id.dataPrivacyArrow)
        )

        // Help sections
        setupSection(
            view,
            R.id.helpCenterLayout,
            R.id.helpCenterTextView,
            view.findViewById(R.id.helpCenterArrow)
        )

        setupSection(
            view,
            R.id.contactSupportLayout,
            R.id.contactSupportTextView,
            view.findViewById(R.id.contactSupportArrow)
        )
    }

    private fun setupSection(
        view: View,
        layoutId: Int,
        contentViewId: Int,
        arrow: ImageView
    ) {
        val layout = view.findViewById<View>(layoutId)
        val contentView = view.findViewById<View>(contentViewId)

        contentView?.visibility = View.GONE

        sections[layoutId] = Section(
            layout = layout,
            contentView = contentView,
            arrow = arrow,
            isExpanded = false
        )
    }

    private fun setupSectionClickListeners() {
        sections.forEach { (_, section) ->
            section.layout.setOnClickListener {
                toggleSection(section)
            }
        }
    }

    private fun toggleSection(section: Section) {
        section.isExpanded = !section.isExpanded

        section.contentView?.let { content ->
            if (section.isExpanded) {
                content.visibility = View.VISIBLE
                content.alpha = 0f
                content.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start()
            } else {
                content.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction {
                        content.visibility = View.GONE
                    }
                    .start()
            }
        }

        section.arrow.animate()
            .rotation(if (section.isExpanded) 90f else 0f)
            .setDuration(200)
            .start()
    }
}

// NotificationWorker class
class NotificationWorker(context: Context, params: WorkerParameters) :
    Worker(context, params) {

    override fun doWork(): Result {
        val messages = listOf(
            "Is your name Wi-Fi? Because I'm feeling a strong connection right now. \uD83D\uDCF6\uD83D\uDE09",
            "Why don’t scientists trust atoms? Because they make up everything!",
            "Are you made of copper and tellurium? Because you’re Cu-Te. 🧪😍",
            "I told my doctor I hear buzzing all the time. He said, ‘That’s just your inner bee-ing.’",
            "Are you a camera? Because every time I look at you, I smile. \uD83D\uDCF8\uD83D\uDE0A",
            "Why don’t skeletons fight? They don’t have the guts.",
            "Are you a keyboard? Because you’re just my type. ⌨\uFE0F\uD83D\uDC98",
            "Did you hear about the claustrophobic astronaut? He needed a little space.",
            "If I could rearrange the alphabet, I’d put U and I together. \uD83D\uDCDA❤\uFE0F",
            "Why did the tomato turn red? Because it saw the salad dressing!",
            "Someone can't stop thinking about you \uD83D\uDCAD",
            "They say laughter is the best medicine. Unless you have broken ribs—then it just hurts.",
            "Why did the bicycle fall over? It was two tired.",
            "My fitness app told me to run a mile every morning. Now I just wake up and laugh at it.",
            "Why don’t eggs tell jokes? They’d crack each other up.",
            "I tried to write a diet book, but the pages kept disappearing… along with my willpower.",
            "I once had a job as a mirror installer. It’s a job I can really see myself doing.",
            "Why did the coffee file a police report? It got mugged!",
            "What do you call fake spaghetti? An impasta!",
            "I asked my yoga instructor if I was doing the pose right. She said, ‘You’re perfectly out of balance!’",
            "Why did the nurse bring a red pen to work? In case she needed to draw blood!"
        )


        val randomMessage = messages.random()
        sendNotification("Health Tip", randomMessage)

        return Result.success()
    }

    private fun sendNotification(title: String, message: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        // Using NotificationCompat.Builder for backward compatibility
        val notification = NotificationCompat.Builder(applicationContext, "health_channel")
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_health)  // Ensure this icon exists
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)  // Automatically dismiss notification when tapped
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
