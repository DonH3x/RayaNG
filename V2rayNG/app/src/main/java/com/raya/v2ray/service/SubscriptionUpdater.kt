package com.raya.v2ray.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.raya.v2ray.AppConfig
import com.raya.v2ray.AppConfig.SUBSCRIPTION_UPDATE_CHANNEL
import com.raya.v2ray.AppConfig.SUBSCRIPTION_UPDATE_CHANNEL_NAME
import com.raya.v2ray.R
import com.raya.v2ray.handler.AngConfigManager.updateConfigViaSub
import com.raya.v2ray.handler.MmkvManager

object SubscriptionUpdater {

    class UpdateTask(context: Context, params: WorkerParameters) :
        CoroutineWorker(context, params) {

        private val notificationManager = NotificationManagerCompat.from(applicationContext)
        private val notification =
            NotificationCompat.Builder(applicationContext, SUBSCRIPTION_UPDATE_CHANNEL)
                .setWhen(0)
                .setTicker("Update")
                .setContentTitle(context.getString(R.string.title_pref_auto_update_subscription))
                .setSmallIcon(R.drawable.ic_stat_name)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        /**
         * Performs the subscription update work.
         * @return The result of the work.
         */
        @SuppressLint("MissingPermission")
        override suspend fun doWork(): Result {
            Log.i(AppConfig.TAG, "subscription automatic update starting")

            val subs = MmkvManager.decodeSubscriptions().filter { it.second.autoUpdate }

            for (sub in subs) {
                val subItem = sub.second

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    notification.setChannelId(SUBSCRIPTION_UPDATE_CHANNEL)
                    val channel =
                        NotificationChannel(
                            SUBSCRIPTION_UPDATE_CHANNEL,
                            SUBSCRIPTION_UPDATE_CHANNEL_NAME,
                            NotificationManager.IMPORTANCE_MIN
                        )
                    notificationManager.createNotificationChannel(channel)
                }
                notificationManager.notify(3, notification.build())
                Log.i(AppConfig.TAG, "subscription automatic update: ---${subItem.remarks}")
                updateConfigViaSub(Pair(sub.first, subItem))
                notification.setContentText("Updating ${subItem.remarks}")
            }
            notificationManager.cancel(3)
            return Result.success()
        }
    }
}