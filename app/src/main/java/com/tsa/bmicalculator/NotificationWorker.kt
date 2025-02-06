package com.tsa.bmicalculator

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

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
            "I asked my yoga instructor if I was doing the pose right. She said, ‘You’re perfectly out of balance!’",
            "Why did the nurse bring a red pen to work? In case she needed to draw blood!"
        )

        val randomMessage = messages.random()
        sendNotification("Health Tip", randomMessage)
        return Result.success()
    }

    private fun sendNotification(title: String, message: String) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // For API 26+, ensure the channel exists (this is a safety net in case it wasn’t already created)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "health_channel",
                "Health Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, "health_channel")
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_health)  // Make sure the drawable ic_health exists in your project
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        // Use a unique notification ID – here using current time converted to Int
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
