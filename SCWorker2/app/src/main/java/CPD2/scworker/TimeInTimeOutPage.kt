package CPD2.scworker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.instacart.library.truetime.TrueTimeRx
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.localbroadcastmanager.content.LocalBroadcastManager


//const val  GEOFENCE_RADIUS_IN_METERS: Float = 100.0F
//const val  GEOFENCE_EXPIRATION_IN_MILLISECONDS: Float = 8 * 60 * 60 * 1000F // 8 hours in milliseconds
var currentLatitude : Double = 0.0
var currentLongitude : Double = 0.0

// Washington National Cathedral 38.8951 and longitude -77.0364
var assignedLatitude : Double =  38.9072
var assignedLongitude : Double = -77.0369
var radius : Double = 1000.0 // In meters



class TimeInTimeOutPage : AppCompatActivity() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var tvLatitude: TextView
    private lateinit var tvLongitude: TextView
    private lateinit var etLatitude : EditText
    private lateinit var etLongitude : EditText
    private lateinit var geofenceList : MutableList<Geofence>
    private lateinit var tvGeofenceResults : TextView
    private lateinit var locationCallback: LocationCallback
    private lateinit var timeInButton: Button
    private lateinit var timeOutButton: Button
    private var disposable: Disposable? = null

    private val geofenceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action) {
                LocationTrackingService.ACTION_USER_OUTSIDE_GEOFENCE -> {
                    // Call your function here (autoTimeOut)
                    autoTimeOut()
                }
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_ACCESS_LOCATION = 100
        const val NOTIFICATION_CHANNEL_ID = "geofence_channel_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.time_in_time_out_page)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Geofence Notifications"
            val descriptionText = "Notifications related to geofencing events"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val imageView: ImageView = findViewById(R.id.logo2)
        imageView.setOnClickListener {
            onBackPressed()
        }

        val updateResults: Button = findViewById(R.id.btUpdateResults)
        updateResults.setOnClickListener {
            isInsideGeoFence(assignedLatitude, assignedLongitude, currentLatitude, currentLongitude, radius)
        }

        val resetTimeButtons : Button = findViewById(R.id.btResetTimer)
//        resetTimeButtons.setOnClickListener{
//            timeInButton.isClickable = true
//            timeInButton.alpha = 1.0f
//            timeOutButton.isClickable = false
//            timeOutButton.alpha = 0.5f
//        }



        timeInButton  = findViewById(R.id.btTimeIn)
        timeInButton.setOnClickListener {
            // Check if location is within geofence
            getCurrentLocation()
            Handler(Looper.getMainLooper()).postDelayed({
                // This block of code will be executed after 7.5 seconds
                if (isInsideGeoFence(assignedLatitude, assignedLongitude, currentLatitude, currentLongitude, radius)) {
                    // if within geofence, get time and date and proceed to send data to php server
                    startLocationService()
                    var date =  getCurrentTimeAndDate()
                    // send data
                    Toast.makeText(this, "Timed-In Successfully", Toast.LENGTH_SHORT).show()
                    handleTimeIn()
                } else {
                    // if not within geofence, send a pop up that warns the user that they need to be withing the geofence
                    stopLocationUpdates()
                    Toast.makeText(this, "Time-In Failure", Toast.LENGTH_SHORT).show()

                }
            }, 7500)


        }

        timeOutButton = findViewById(R.id.btTimeOut)
        timeOutButton.setOnClickListener {
            // calling stop location updates first because locationCallback Global var will be replaced with a new instantiation.
            // This fixes the bug that location keeps updating due to it being replaced before being stopped. I'm assuming each instantiation has different ID's
            stopLocationUpdates()
            getCurrentLocation()  // sets up permissions, location updates, and edits global variables for the user lat an lng
            Handler(Looper.getMainLooper()).postDelayed({
                // This block of code will be executed after 7.5 seconds

                // Check if location is within geofence
                if (isInsideGeoFence(assignedLatitude, assignedLongitude, currentLatitude, currentLongitude, radius)) {
                    // if within geofence, get time and date and proceed to send data to php server
                    var date =  getCurrentTimeAndDate()
                    // send data

                    stopLocationService()
                    stopLocationUpdates()
                    Toast.makeText(this, "Timed-Out Successfully", Toast.LENGTH_SHORT).show()
                    handleTimeOut()

                } else {
                    // if not within geofence, send a pop up that warns the user that they need to be withing the geofence
                    stopLocationUpdates()
                    Toast.makeText(this, "Time-Out Failure", Toast.LENGTH_SHORT).show()
                }
            }, 7500)

        }

        // Set initial state
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isTimeInEnabled = sharedPreferences.getBoolean("isTimeInEnabled", true)

        if (isTimeInEnabled) {
            timeInButton.isClickable = true
            timeInButton.alpha = 1.0f
            timeOutButton.isClickable = false
            timeOutButton.alpha = 0.5f
        } else {
            timeInButton.isClickable = false
            timeInButton.alpha = 0.5f
            timeOutButton.isClickable = true
            timeOutButton.alpha = 1.0f
        }

        
        // Get GeoFence coordinates lat,long
        etLatitude = findViewById(R.id.etLatitude)
        etLongitude = findViewById(R.id.etLongitude)

        tvLatitude = findViewById(R.id.tvLatitude)
        tvLongitude = findViewById(R.id.tvLongitude)

        tvGeofenceResults = findViewById(R.id.tvGeofenceResults)


    }

    private fun popUpWarning() {

    }

    private fun getCurrentTimeAndDate(): Single<String> {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return Single.create { emitter ->
            try {
                TrueTimeRx.build()
                    .initializeRx("time.google.com")
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                        { date ->
                            emitter.onSuccess(format.format(date))
                        },
                        { error ->
                            emitter.onSuccess(format.format(Date()))
                        }
                    )
                    .also { disposable = it } // store the Disposable
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy() // or any other appropriate lifecycle event
        disposable?.dispose()
    }

    fun handleTimeIn() {
        timeInButton.isClickable = false
        timeInButton.alpha = 0.5f

        timeOutButton.isClickable = true
        timeOutButton.alpha = 1.0f

        saveButtonState(false)  // Saving that the Time In button is not enabled
    }

    fun handleTimeOut() {
        timeOutButton.isClickable = false
        timeOutButton.alpha = 0.5f

        timeInButton.isClickable = true
        timeInButton.alpha = 1.0f

        saveButtonState(true)  // Saving that the Time In button is enabled
    }

    fun saveButtonState(isTimeInEnabled: Boolean) {
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isTimeInEnabled", isTimeInEnabled)
        editor.apply()
    }


    private fun getCurrentLocation() {
        // Check if permissions are enabled
        if (checkPermissions()) {

            if (isLocationEnabled()) {
                Toast.makeText(this, "Location is enabled", Toast.LENGTH_SHORT).show()
                // Get location here
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermission()
                    return
                }

                // This is where to put the geolocation functions
                val locationRequest = LocationRequest.create()
                locationRequest.setInterval(10000)
                locationRequest.setFastestInterval(5000)
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)


                locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        for (location in locationResult.locations) {
                            if (location != null) {

                                // Such as:
//                                Toast.makeText(this@TimeInTimeOutPage, "Location Successfully Retrieved", Toast.LENGTH_SHORT).show()
                                tvLatitude.text = "" + location.latitude
                                tvLongitude.text = "" + location.longitude

                                currentLatitude = location.latitude
                                currentLongitude = location.longitude

                            }
                            else {
                                Toast.makeText(this@TimeInTimeOutPage, "location is Null", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, null)


            } else {
                // Open settings here to enable location
                Toast.makeText(this, "Turn on Location", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            // TODO: Pop up explaining the importance of permissions and tutorial on how to enable background location
            // Request Permission
            requestPermission()
        }
    }

    private fun stopLocationUpdates() {
        if (::locationCallback.isInitialized) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    private fun requestPermission() {

        ActivityCompat.requestPermissions(
            this, arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_REQUEST_ACCESS_LOCATION
        )

    }


    private fun startLocationService() {
        val intent = Intent(this, LocationTrackingService::class.java)
        startService(intent)
    }

    private fun stopLocationService() {
        val intent = Intent(this, LocationTrackingService::class.java)
        stopService(intent)
    }


    private fun checkPermissions(): Boolean {

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED

        ) {

            return true
        }

        return false
    }


    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_ACCESS_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, "Granted", Toast.LENGTH_SHORT).show()
                getCurrentLocation()   // loop again to the function
            } else {
                Toast.makeText(applicationContext, "Denied", Toast.LENGTH_SHORT).show()
            }

        }
    }

    // TODO: Make the app monitor geofencing, if user disables permissions or location, automatic time out will execute.
    private fun autoTimeOut() {
        getCurrentLocation()
        Handler(Looper.getMainLooper()).postDelayed({
            // This block of code will be executed after 7.5 seconds

            var date =  getCurrentTimeAndDate()
            // send data
            stopLocationService()
            stopLocationUpdates()
            Toast.makeText(this, "Auto Timed Out", Toast.LENGTH_SHORT).show()
            handleTimeOut()
        }, 7500)

    }

    private fun isInsideGeoFence(assignedLatitude: Double, assignedLongitude: Double, currentLatitude: Double, currentLongitude : Double, radius : Double): Boolean {

        var distanceInMeters : FloatArray = floatArrayOf(0f)

        Location.distanceBetween(assignedLatitude, assignedLongitude, currentLatitude, currentLongitude, distanceInMeters)
        var placeholder : String = ""

        if (distanceInMeters[0].toDouble() < radius) {
            // User is inside the Geo-fence
//            showNotificationEvent.call()
            placeholder = "User is inside Geofence " + distanceInMeters[0].toString()
            tvGeofenceResults.text = placeholder
            return true

        } else {
            // User is outside Geo-fence
            placeholder = "User is outside Geofence " + distanceInMeters[0].toString()
            tvGeofenceResults.text = placeholder
            return false
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(geofenceReceiver, IntentFilter(LocationTrackingService.ACTION_USER_OUTSIDE_GEOFENCE))
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(geofenceReceiver)
    }


}