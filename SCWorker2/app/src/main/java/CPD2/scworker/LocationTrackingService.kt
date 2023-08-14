package CPD2.scworker

import CPD2.scworker.TimeInPage.Companion.NOTIFICATION_CHANNEL_ID
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.CountDownTimer
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.location.LocationManagerCompat.getCurrentLocation
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.util.Timer
import java.util.TimerTask

class LocationTrackingService : Service() {

    private val timer = Timer()
    private var isOutsideGeofence = false
    private var isFiveMinuteTimerInitialized = false

    // 60000, 60000
    private val oneMinuteTimer = object : CountDownTimer(5000, 5000) {
        override fun onTick(millisUntilFinished: Long) {
            // This won't be called since the interval is set to the full duration
        }
        override fun onFinish() {
            if (isUserOutsideGeofence()) {
                sendGeofenceAlert("You are outside the Geofence. You will be timed out within five minutes", true, 2)
                startFiveMinuteTimer()
                isFiveMinuteTimerInitialized = true
            }
        }
    }
    // 300000 , 300000
    private val fiveMinuteTimer = object : CountDownTimer(5000, 5000) {
        override fun onTick(millisUntilFinished: Long) {
            // This won't be called since the interval is set to the full duration
        }

        override fun onFinish() {
            if (isUserOutsideGeofence()) {
                sendGeofenceAlert("Auto time-out initiated", false, 3)
                callExternalFunction()
            }
        }
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val serviceType = intent.getStringExtra("serviceType")
            if (serviceType == "location") {
                // Handle the location tracking service
                startForegroundServiceWithNotification()
                sendGeofenceAlert("Starting Foreground Service", false, 4)
                // Start timer task to check geofence status


                timer.scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        checkGeofenceStatus()
                    }
                }, 0, 10000) // Check every 10 seconds
            }
        }
        return START_STICKY

    }

    private fun startForegroundServiceWithNotification() {
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Location Tracking")
            .setContentText("Tracking your location for geofence...")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Your app icon
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    private fun checkGeofenceStatus() {
        if (isUserOutsideGeofence()) { // Your geofence checker function
            if (!isOutsideGeofence) {
                isOutsideGeofence = true
                startOutsideGeofenceTimer()
                // Send another notification or update the current one
            }
        } else {
            isOutsideGeofence = false
            // Reset or stop the timer for outside geofence
            oneMinuteTimer.cancel()
            fiveMinuteTimer.cancel()

            // Notifies user once they are back at the geofence
            if (isFiveMinuteTimerInitialized) {
                sendGeofenceAlert("You are inside the Geofence, any time-out operations will be cancelled", false, 3)
                isFiveMinuteTimerInitialized = false
            }

            dismissNotification(2) // Dismissing non-removable notification
        }
    }

    private fun isUserOutsideGeofence(): Boolean {
        // Your custom geofence checker logic here
        var distanceInMeters : FloatArray = floatArrayOf(0f)

        Location.distanceBetween(assignedLatitude, assignedLongitude, currentLatitude, currentLongitude, distanceInMeters)
        var placeholder : String = ""

        if (distanceInMeters[0].toDouble() < radius) {
            // User is inside the Geo-fence
            return false
        }
        return true
    }

    private fun sendGeofenceAlert(message : String, onGoingValue : Boolean, idNum : Int) {
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Geofence Alert")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Your app icon
            .setOngoing(onGoingValue)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(idNum, notification) // 2 is the notification id for this particular notification
    }


    private fun startOutsideGeofenceTimer() {
        oneMinuteTimer.start()
    }

    private fun startFiveMinuteTimer() {
        fiveMinuteTimer.start()
    }

    private fun dismissNotification(idNum: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(idNum) // 2 is the notification id for the outside geofence alert
    }


    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
        dismissNotification(1) // Dismissing the "You are being tracked" Notification
        // Handle any other cleanup
    }

    companion object {
        const val ACTION_USER_OUTSIDE_GEOFENCE = "com.CPD2.scworker.ACTION_USER_OUTSIDE_GEOFENCE"
    }

//    private fun callExternalFunction() {
//        val intent = Intent(ACTION_USER_OUTSIDE_GEOFENCE)
//        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
//    }

    private fun callExternalFunction() {
        val intent = Intent(this, GeofenceJobIntentService::class.java)
        intent.action = ACTION_USER_OUTSIDE_GEOFENCE
        GeofenceJobIntentService.enqueueWork(this, intent)
    }


}