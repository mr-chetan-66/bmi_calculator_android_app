package com.tsa.bmicalculator

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.concurrent.TimeUnit

private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001

class SettingFragment : Fragment() {

    // Map to keep track of expandable sections
    private val sections = mutableMapOf<Int, Section>()
    private lateinit var notificationSwitch: SwitchMaterial
    private lateinit var privacyDetailsInput: TextView
    private lateinit var helpCenterInput: TextView
    private lateinit var contactSupportInput: TextView

    // Data class to hold section details
    private data class Section(
        val layout: View,
        val contentView: View?,
        val arrow: ImageView,
        var isExpanded: Boolean = false
    )

    companion object {
        private const val PREF_NAME = "settings"
        private const val PREF_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val WORKER_TAG = "notification_worker"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate your fragment layout (ensure the layout name matches your XML file)
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize TextViews for content (IDs must match those in your XML)
        privacyDetailsInput = view.findViewById(R.id.privacyDetailsTextView)
        helpCenterInput = view.findViewById(R.id.helpCenterTextView)
        contactSupportInput = view.findViewById(R.id.contactSupportTextView)
        // Initialize the notification switch
        notificationSwitch = view.findViewById(R.id.notificationSwitch)

        // Populate sections with content from strings.xml
        populateSections()

        // Create notification channel (for API 26+)
        createNotificationChannel()

        // Set up the notification switch (load saved preference and listener)
        setupNotificationSwitch()

        // Initialize expandable sections (for privacy, help, and support)
        initializeSections(view)
        setupSectionClickListeners()
        requestNotificationPermission()
    }
    private fun populateSections() {
        privacyDetailsInput.text = Html.fromHtml(getString(R.string.privacy_policy_content), Html.FROM_HTML_MODE_LEGACY)
        helpCenterInput.text = Html.fromHtml(getString(R.string.help_center_content), Html.FROM_HTML_MODE_LEGACY)
        contactSupportInput.text = Html.fromHtml(getString(R.string.contact_support_content), Html.FROM_HTML_MODE_LEGACY)
        contactSupportInput.movementMethod = LinkMovementMethod.getInstance()  // Makes links clickable
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // 'requireContext()' provides the current Context, and since this is a fragment,
            // you can use requireActivity() for requesting permissions.
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    // Optionally, override onRequestPermissionsResult to handle the permission request result:
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted. Notifications can be shown.
            } else {
                // Permission denied. Inform the user appropriately.
            }
        }
    }

    private fun setupNotificationSwitch() {
        val isEnabled = loadNotificationPreference()
        notificationSwitch.isChecked = isEnabled

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
        // Schedule a morning notification at 9:00 AM
        val morningRequest = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(calculateDelay(9, 0), TimeUnit.MILLISECONDS)
            .addTag(WORKER_TAG)
            .build()
        // Schedule an evening notification at 6:00 PM
        val eveningRequest = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(calculateDelay(18, 0), TimeUnit.MILLISECONDS)
            .addTag(WORKER_TAG)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "morning_notification",
            ExistingPeriodicWorkPolicy.UPDATE,
            morningRequest
        )
        workManager.enqueueUniquePeriodicWork(
            "evening_notification",
            ExistingPeriodicWorkPolicy.UPDATE,
            eveningRequest
        )
    }

    private fun cancelNotifications() {
        WorkManager.getInstance(requireContext()).cancelAllWorkByTag(WORKER_TAG)
    }

    /**
     * Calculates the delay in milliseconds from now until the next occurrence
     * of the specified hour and minute.
     */
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
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.timeInMillis - now
    }

    private fun initializeSections(view: View) {
        // Set up the Privacy section
        setupSection(
            view,
            R.id.dataPrivacyLayout,
            R.id.privacyDetailsTextView,
            view.findViewById(R.id.dataPrivacyArrow)
        )
        // Set up the Help Center section
        setupSection(
            view,
            R.id.helpCenterLayout,
            R.id.helpCenterTextView,
            view.findViewById(R.id.helpCenterArrow)
        )
        // Set up the Contact Support section
        setupSection(
            view,
            R.id.contactSupportLayout,
            R.id.contactSupportTextView,
            view.findViewById(R.id.contactSupportArrow)
        )
    }

    private fun setupSection(view: View, layoutId: Int, contentViewId: Int, arrow: ImageView) {
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
            section.layout.setOnClickListener { toggleSection(section) }
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
                    .setDuration(600)
                    .start()
            } else {
                content.animate()
                    .alpha(0f)
                    .setDuration(300)
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

    /**
     * Creates a notification channel for API 26+ devices.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "health_channel",
                "Health Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
