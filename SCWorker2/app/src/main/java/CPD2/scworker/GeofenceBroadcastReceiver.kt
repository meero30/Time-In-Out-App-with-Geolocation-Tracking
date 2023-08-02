//
//
//package CPD2.scworker
//
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.content.BroadcastReceiver
//import android.content.Context
//import android.content.Intent
//import android.util.Log
//import androidx.core.content.ContextCompat
//import android.content.ContentValues.TAG
//import android.os.Build
//import com.google.android.gms.location.Geofence
//import com.google.android.gms.location.GeofenceStatusCodes
//import com.google.android.gms.location.GeofencingEvent
//
///*
// * Triggered by the Geofence.  Since we only have one active Geofence at once, we pull the request
// * ID from the first Geofence, and locate it within the registered landmark data in our
// * GeofencingConstants within GeofenceUtils, which is a linear string search. If we had  very large
// * numbers of Geofence possibilities, it might make sense to use a different data structure.  We
// * then pass the Geofence index into the notification, which allows us to have a custom "found"
// * message associated with each Geofence.
// */
//
//private const val NOTIFICATION_ID = 33
//private const val CHANNEL_ID = "GeofenceChannel"
//
//class GeofenceBroadcastReceiver : BroadcastReceiver() {
//
//
//    override fun onReceive(context: Context?, intent: Intent?) {
//        val geofencingEvent = intent?.let { GeofencingEvent.fromIntent(it) }
//        if (geofencingEvent != null) {
//            if (geofencingEvent.hasError()) {
//                val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
//                Log.e(TAG, errorMessage)
//                return
//            }
//        }
//
//        val geofenceTransition = geofencingEvent?.geofenceTransition
//        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
//            val triggeringGeofences = geofencingEvent.triggeringGeofences
//
//            // Creating and sending notification
//            val notificationManager = ContextCompat.getSystemService(
//                context!!, NotificationManager::class.java
//            ) as NotificationManager
//
//            notificationManager.sendGeofenceEnteredNotification(context)
//        } else {
//            Log.e(TAG, "Invalid type transition $geofenceTransition")
//        }
//    }
//
//
//
//    fun createChannel(context: Context) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val notificationChannel =
//                NotificationChannel(CHANNEL_ID, "Channel1", NotificationManager.IMPORTANCE_HIGH)
//            val notificationManager = context.getSystemService(NotificationManager::class.java)
//            notificationManager.createNotificationChannel(notificationChannel)
//        }
//    }
//    // extension function
//    fun NotificationManager.sendGeofenceEnteredNotification(context: Context) {
//
//        // Opening the notification
//        val contentIntent = Intent(context, MapsActivity::class.java)
//        val contentPendingIntent = PendingIntent.getActivity(
//            context,
//            NOTIFICATION_ID,
//            contentIntent,
//            PendingIntent.FLAG_UPDATE_CURRENT
//        )
//
//        // Building the notification
//        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
//            .setContentTitle(context.getString(R.string.app_name))
//            .setContentText("You have entered a geofence area")
//            .setSmallIcon(R.drawable.ic_baseline_notifications_24)
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .setContentIntent(contentPendingIntent)
//            .build()
//
//        this.notify(NOTIFICATION_ID, builder)
//    }
//}
//
//
