package CPD2.scworker

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

//data class LocationData(val latitude: Double, val longitude: Double)
//
//const val  GEOFENCE_RADIUS_IN_METERS: Float = 100.0F
//const val  GEOFENCE_EXPIRATION_IN_MILLISECONDS: Float = 8 * 60 * 60 * 1000F // 8 hours in milliseconds


class TimeInTimeOutPage : AppCompatActivity() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var tvLatitude: TextView
    private lateinit var tvLongitude: TextView
//    private lateinit var geofencingClient: GeofencingClient
    private lateinit var etLatitude : EditText
    private lateinit var etLongitude : EditText
//    private lateinit var coordinates: LocationData
    private lateinit var geofenceList : MutableList<Geofence>

//    private val geofencePendingIntent: PendingIntent by lazy {
//        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
//        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
//        // addGeofences() and removeGeofences().
//        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
//    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.time_in_time_out_page)

//        geofencingClient = LocationServices.getGeofencingClient(this)

        // Get GeoFence coordinates lat,long
        etLatitude = findViewById(R.id.etLatitude)
        etLongitude = findViewById(R.id.etLongitude)
//
//        var assignedLatitude = etLatitude.text.toString().toDouble()
//        var assignedLongitude = etLongitude.text.toString().toDouble()

//        coordinates = LocationData(assignedLatitude,assignedLongitude)
//
//        val geofenceData: Map<String, LocationData> = mapOf(
//            "geofence_id_1" to coordinates,  // assigned coordinates
//        )

//        addToGeofenceList()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        tvLatitude = findViewById(R.id.tvLatitude)
        tvLongitude = findViewById(R.id.tvLongitude)
        getCurrentLocation()



    }

//    private fun getGeofencingRequest(): GeofencingRequest {
//        return GeofencingRequest.Builder().apply {
//            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
//            addGeofences(geofenceList)
//        }.build()
//    }

//    private fun addToGeofenceList() {
//        geofenceList.add(Geofence.Builder()
//            // Set the request ID of the geofence. This is a string to identify this
//            // geofence.
//            .setRequestId("geofence_id_1")
//
//            // Set the circular region of this geofence.
//            .setCircularRegion(
//                coordinates.latitude,
//                coordinates.latitude,
//                GEOFENCE_RADIUS_IN_METERS
//            )
//            // Set the expiration duration of the geofence. This geofence gets automatically
//            // removed after this period of time.
//            .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS.toLong())
//
//            // Set the transition types of interest. Alerts are only generated for these
//            // transition. We track entry and exit transitions in this sample.
//            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
//
//            // Create the geofence.
//            .build())
//    }






    //  TODO: Complete the getCurrentLocation() Function
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


                val locationCallback: LocationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        for (location in locationResult.locations) {
                            if (location != null) {

                                // Such as:
                                Toast.makeText(this@TimeInTimeOutPage, "Location Successfully Retrieved", Toast.LENGTH_SHORT).show()
                                tvLatitude.text = "" + location.latitude
                                tvLongitude.text = "" + location.longitude
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

    private fun requestPermission() {

        ActivityCompat.requestPermissions(
            this, arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_REQUEST_ACCESS_LOCATION
        )

    }

    fun createLocationRequest() {
        val locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }


    companion object {
        private const val PERMISSION_REQUEST_ACCESS_LOCATION = 100
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

}