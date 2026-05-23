package com.pham0326.flinders.zootreasurehunt.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

class CongratulationWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    private fun triggerNotification(animalName: String) {
        val channelId = "zoo_hunt_channel"

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Zoo Hunt Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Great Job!")
            .setContentText("You found the $animalName!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)

        notificationManager.notify(1, builder.build())
    }

    override fun doWork(): Result {
        val animalName = inputData.getString("ANIMAL_NAME") ?: "Animal"
        triggerNotification(animalName)
        return Result.success()
    }
}