package CPD2.scworker

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.JobIntentService
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject


class GeofenceJobIntentService : JobIntentService() {
    override fun onHandleWork(intent: Intent) {
        if (intent.action == LocationTrackingService.ACTION_USER_OUTSIDE_GEOFENCE) {
            // Call your function here (autoTimeOut)
            autoTimeOut()
            Log.d("MyTag", "GeofenceJobIntentService is working and initiated.")
        }
    }

    private fun autoTimeOut() {
        val sharedPreferences = getSharedPreferences(prefsName, Context.MODE_PRIVATE)

        val isTimeInPageCreated = sharedPreferences.getBoolean("TimeInPageCreated", false)

        if (isTimeInPageCreated) {
            // The TimeInPage activity is created or present
            val intent = Intent("com.CPD2.scworker.BUTTON_CLICK")
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

        } else {
            // The TimeInPage activity is not created or present
            stopCountdownTimer()
            val queue = Volley.newRequestQueue(applicationContext)
            val url = "http://192.168.254.131/login/timeout.php"
            val stringRequest: StringRequest =
                object : StringRequest(
                    Method.POST, url,
                    Response.Listener { response ->
                        try {
                            val jsonObject = JSONObject(response)
                            val status: String = jsonObject.getString("status")
                            val message: String = jsonObject.getString("message")
                            if (status == "success") {
                                stopLocationUpdateService()
                                stopLocationTrackingService()

                                val editor = sharedPreferences.edit()
                                editor.putString("texterror_message", "Timed out Successfully, Thank you for your work")
                                editor.putString("logged", "true")
                                editor.putBoolean("isTimeIn", true)
                                editor.apply()

                                // Store other UI updates in sharedPrefs for the activity to pick up later
                                editor.putString("timeinButton_text", "Time In")
                                editor.putString("timeinButton_color", "green")
                                editor.apply()
                            } else {
                                // Inform the user of the error using a notification
                                Log.e("MyError", message)
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener { error ->
                        // Inform the user of the error using a notification
                        Log.e("MyError", "Error: ${error.message}")

                    }) {
                    override fun getParams(): Map<String, String>? {
                        val paramV: MutableMap<String, String> = HashMap()
                        paramV["phone"] = sharedPreferences.getString("phone", "") ?: ""
                        return paramV
                    }
                }
            queue.add(stringRequest)
        }


    }

    companion object {
        private const val JOB_ID = 1001

        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, GeofenceJobIntentService::class.java, JOB_ID, work)
        }
    }

    private fun stopLocationUpdateService() {
        val intent = Intent(this, LocationUpdateService::class.java)
        stopService(intent)
    }

    private fun stopLocationTrackingService() {
        val intent = Intent(this, LocationTrackingService::class.java)
        stopService(intent)
    }
}

