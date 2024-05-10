package com.example.notificationreplyservice

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : AppCompatActivity() {

    companion object {

        const val TAG = "MainActivity"
        const val NOTIFICATION_REPLY = "NotificationReply"
        const val CHANNNEL_ID = "Default"
        const val CHANNEL_NAME = "Default"
        const val CHANNEL_DESC = "This is a channel for Simplified Coding Notifications"

        const val KEY_INTENT_MORE = "keyintentmore"
        const val KEY_INTENT_HELP = "keyintenthelp"

        const val REQUEST_CODE_MORE = 100
        const val REQUEST_CODE_HELP = 101
        const val NOTIFICATION_ID = 200

    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            Log.d(TAG, token.toString())

        })


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mNotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(CHANNNEL_ID, CHANNEL_NAME, importance)
            mChannel.description = CHANNEL_DESC
            mChannel.enableLights(true)
            mChannel.lightColor = Color.RED
            mChannel.enableVibration(true)
            mChannel.vibrationPattern =
                longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            mNotificationManager.createNotificationChannel(mChannel)
        }

        findViewById<View>(R.id.textView).setOnClickListener { displayNotification() }
    }

    @SuppressLint("LaunchActivityFromNotification")
    fun displayNotification() {

        //Pending intent for a notification button named More
        val morePendingIntent = PendingIntent.getBroadcast(
            this@MainActivity,
            REQUEST_CODE_MORE,
            Intent(this@MainActivity, NotificationReceiver::class.java)
                .putExtra(KEY_INTENT_MORE, REQUEST_CODE_MORE),
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        //Pending intent for a notification button help
        val helpPendingIntent = PendingIntent.getBroadcast(
            this@MainActivity,
            REQUEST_CODE_HELP,
            Intent(this@MainActivity, NotificationReceiver::class.java)
                .putExtra(KEY_INTENT_HELP, REQUEST_CODE_HELP),
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )


        //We need this object for getting direct input from notification
        val remoteInput: RemoteInput = RemoteInput.Builder(NOTIFICATION_REPLY)
            .setLabel("Please enter your name")
            .build()


        //For the remote input we need this action object
        val action = NotificationCompat.Action.Builder(
            android.R.drawable.ic_delete,
            "Reply Now...", helpPendingIntent
        )
            .addRemoteInput(remoteInput)
            .build()

        //Creating the notifiction builder object
        val mBuilder = NotificationCompat.Builder(this, CHANNNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle("Hey this is Simplified Coding...")
            .setContentText("Please share your name with us")
            .setAutoCancel(true)
            .setContentIntent(helpPendingIntent)
            .addAction(action)
            .addAction(android.R.drawable.ic_menu_compass, "More", morePendingIntent)
            .addAction(android.R.drawable.ic_menu_directions, "Help", helpPendingIntent)


        //finally displaying the notification
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, mBuilder.build())
    }
}