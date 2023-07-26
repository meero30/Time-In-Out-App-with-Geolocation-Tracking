//package CPD2.scworker
//
//import android.content.BroadcastReceiver
//import android.content.ContentValues.TAG
//import android.content.Context
//import android.content.Intent
//import android.util.Log
//import com.google.android.gms.location.Geofence
//import com.google.android.gms.location.GeofenceStatusCodes
//import com.google.android.gms.location.GeofencingEvent
//
//class GeofenceBroadcastReceiver : BroadcastReceiver() {
//    // ...
//    override fun onReceive(context: Context?, intent: Intent?) {
//
//        val notificationHelper = NotificationHelper(context)
//
//        val geofencingEvent = intent?.let { GeofencingEvent.fromIntent(it) }
//        if (geofencingEvent != null) {
//            if (geofencingEvent.hasError()) {
//                val errorMessage = GeofenceStatusCodes
//                    .getStatusCodeString(geofencingEvent.errorCode)
//                Log.e(TAG, errorMessage)
//                return
//            }
//        }
//
//        // Get the transition type.
//        val geofenceTransition = geofencingEvent?.geofenceTransition
//
//        // Test that the reported transition was of interest.
//        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
//        geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
//
//            // Get the geofences that were triggered. A single event can trigger
//            // multiple geofences.
//            val triggeringGeofences = geofencingEvent.triggeringGeofences
//
//            // Get the transition details as a String.
//            val geofenceTransitionDetails = getGeofenceTransitionDetails(
//                this,
//                geofenceTransition,
//                triggeringGeofences
//            )
//
//            // Send notification and log the transition details.
//            sendNotification(geofenceTransitionDetails)
//            Log.i(TAG, geofenceTransitionDetails)
//        } else {
//            // Log the error.
//            Log.e(TAG, getString(R.string.geofence_transition_invalid_type,
//                geofenceTransition))
//        }
//    }
//}