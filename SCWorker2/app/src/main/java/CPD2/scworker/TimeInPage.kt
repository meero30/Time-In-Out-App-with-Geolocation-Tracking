package CPD2.scworker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import org.json.JSONException

var currentLatitude: Double = 0.0
var currentLongitude: Double = 0.0


// Washington National Cathedral 38.8951 and longitude -77.0364
var assignedLatitude: Double = 0.0
var assignedLongitude: Double = 0.0
var radius: Double = 2000.0 // In meters
val prefsName = "MyPreferences"


class TimeInPage : AppCompatActivity() {

    private var isTimeIn = true
    private var isFetched = true
    var started = false
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val destinationLatitude = 37.4226711
    private val destinationLongitude = -122.0849872

    //private val destinationLatitude = 37.422
    //private val destinationLongitude = 122.0785
    var distance: Double = 0.0

    private val buttonClickReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Simulate the button click here
            timeinButton.performClick()
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_ACCESS_LOCATION = 100
        const val NOTIFICATION_CHANNEL_ID = "geofence_channel_id"
    }

    private val sharedPreferences by lazy {
        getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.timein_page)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val texterror: TextView = findViewById(R.id.error)
        val fetchfullname: TextView = findViewById(R.id.fetchfullname)
        val fetchphone: TextView = findViewById(R.id.fetchphone)
        val timeRecordsTable: TableLayout = findViewById(R.id.timeRecordsTable)
        val logButton: Button = findViewById(R.id.weeklyLog)
        val overtimeStatus: TextView = findViewById(R.id.overtimeStatusTextView)
        val requestOvertimeButton: Button = findViewById(R.id.requestButton)
        val progress: ProgressBar = findViewById(R.id.progress)
        countdown = findViewById(R.id.timer)
        timeinButton = findViewById(R.id.timeinButton)
        texterror.visibility = View.GONE
        overtimeStatus.text = ""
        overtimeStatus.visibility = View.GONE
        timerPrompt = findViewById(R.id.overtimeStatusTextView)
        timerPrompt.text = ""
        timerPrompt.visibility = View.GONE
        timerPrompt.visibility = View.GONE
        requestButton = findViewById(R.id.requestButton)


        LocalBroadcastManager.getInstance(this)
            .registerReceiver(buttonClickReceiver, IntentFilter("com.CPD2.scworker.BUTTON_CLICK"))


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Geofence Notifications"
            val descriptionText = "Notifications related to geofencing events"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel =
                NotificationChannel(TimeInPage.NOTIFICATION_CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }

            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }




        sharedPreferences.edit().putBoolean("TimeInPageCreated", true).apply()

        texterror.text = sharedPreferences.getString("texterror_message", "Default Message")
        isTimeIn = sharedPreferences.getBoolean("isTimeIn", true)
        val fullName = sharedPreferences.getString("fullname", "")
        val phone = sharedPreferences.getString("phone", "")
        fetchfullname.text = "Good Day! $fullName"
        fetchphone.text = "($phone)"
        if (sharedPreferences.getString("logged", "false").equals("false")) {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }

        if (isTimeIn) {
            timeinButton.text = "Time In"
            timeinButton.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
        } else {
            timeinButton.text = "Time Out"
            timeinButton.setBackgroundColor(Color.RED)
            countdown.visibility = View.VISIBLE
            val (savedRemainingTime, isTimerRunning) = restoreTimerState(this)
            startCountdownTimer(initialTimeMillis = savedRemainingTime, this)
        }

        timeinButton.setOnClickListener {
            Log.d("MyNum", "${phone}")
            val queue = Volley.newRequestQueue(applicationContext)
            val url = "http://192.168.254.131/login/getJobSite.php"

            val stringRequest: StringRequest =
                object : StringRequest(
                    Method.POST, url,
                    Response.Listener { response ->

                        try {
                            val jsonObject = JSONObject(response)
                            val status: String = jsonObject.getString("status")
                            val message: String = jsonObject.getString("message")

                            if (status == "success") {
                                texterror.visibility = View.VISIBLE
                                texterror.text = "Coordinates fetched"
                                texterror.setTextColor(ContextCompat.getColor(this, R.color.green))
                                assignedLatitude = jsonObject.getDouble("latitude")
                                assignedLongitude = jsonObject.getDouble("longitude")

                                Log.d("MyCoords", "${assignedLatitude}  ${assignedLongitude}" )


                            } else {
                                texterror.text = message
                                texterror.visibility = View.VISIBLE
                            }

                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener { error ->
                        progress.visibility = View.GONE
                        texterror.text = "Error: ${error.message}"
                        texterror.visibility = View.VISIBLE
                    }) {
                    override fun getParams(): Map<String, String>? {
                        val paramV: MutableMap<String, String> = HashMap()
                        paramV["phone"] = "${phone}"
                        return paramV
                    }
                }
            queue.add(stringRequest)


            timeRecordsTable.visibility = View.INVISIBLE
            timerPrompt.visibility = View.GONE
            if (isTimeIn) {

                // Check if location is within geofence
                startLocationUpdateServiceWrapper()

                Handler(Looper.getMainLooper()).postDelayed({
                    // This block of code will be executed after 7.5 seconds
                    Log.d("MyCoords", "${currentLatitude}  ${currentLongitude}" )
                    if (isInsideGeoFence(
                            assignedLatitude,
                            assignedLongitude,
                            currentLatitude,
                            currentLongitude,
                            radius
                        )
                    ) {
                        // if within geofence, get time and date and proceed to send data to php server
                        startLocationTrackingService()
                        Toast.makeText(
                            this,
                            "Location Updates and Tracking Services Started",
                            Toast.LENGTH_SHORT
                        ).show()

                        val queue = Volley.newRequestQueue(applicationContext)
                        val url = "http://192.168.254.131/login/timein.php"

                        val stringRequest: StringRequest =
                            object : StringRequest(
                                Method.POST, url,
                                Response.Listener { response ->
                                    progress.visibility = View.GONE
                                    try {
                                        val jsonObject = JSONObject(response)
                                        val status: String = jsonObject.getString("status")
                                        val message: String = jsonObject.getString("message")

                                        if (status == "success") {

                                            countdown.visibility =
                                                View.VISIBLE // Make the countdown TextView visible


                                            if (!started) {
                                                timerPrompt.visibility = View.GONE
                                                startCountdownTimer(
                                                    initialTimeMillis = 5 * 60 * 60 * 1000L,
                                                    this
                                                ) //Regular
                                                texterror.visibility = View.VISIBLE
                                                texterror.text =
                                                    "Timed in Successfully, Have a blessed day at Work"
                                                texterror.setTextColor(
                                                    ContextCompat.getColor(
                                                        this,
                                                        R.color.green
                                                    )
                                                )
                                            } else {
                                                timerPrompt.visibility = View.GONE
                                                startCountdownTimer(
                                                    initialTimeMillis = 10 * 1000L,
                                                    this,
                                                    true
                                                ) //OT
                                                texterror.visibility = View.VISIBLE
                                                texterror.text =
                                                    "OT started!"
                                                texterror.setTextColor(
                                                    ContextCompat.getColor(
                                                        this,
                                                        R.color.green
                                                    )
                                                )
                                            }
                                            val editor = sharedPreferences.edit()
                                            editor.putBoolean("isTimeIn", false)
                                            editor.apply()

                                            // Update button text and color
                                            timeinButton.text = "Time Out"
                                            timeinButton.setBackgroundColor(Color.RED)
                                            timeRecordsTable.visibility = View.INVISIBLE
                                            isTimeIn = false


                                        } else {
                                            // if Post is not successful
                                            stopLocationUpdateService()
                                            stopLocationTrackingService()
                                            texterror.setTextColor(Color.RED)
                                            texterror.text = message
                                            texterror.visibility = View.VISIBLE
                                        }
                                    } catch (e: JSONException) {
                                        e.printStackTrace()
                                    }
                                },
                                Response.ErrorListener { error ->
                                    progress.visibility = View.GONE
                                    texterror.text = "Error: ${error.message}"
                                    texterror.visibility = View.VISIBLE
                                }) {
                                override fun getParams(): Map<String, String> {
                                    val paramV: MutableMap<String, String> = HashMap()
                                    paramV["phone"] = sharedPreferences.getString("phone", "") ?: ""
                                    return paramV
                                }
                            }
                        queue.add(stringRequest)
                        // end of sending data block


                    } else {
                        // if not within geofence, send a pop up that warns the user that they need to be withing the geofence
                        stopLocationUpdateService()
                        Toast.makeText(
                            this,
                            "Time-In Failure",
                            Toast.LENGTH_SHORT
                        ).show()
                        // Distance greater than or equal to 1km
                        texterror.visibility = View.VISIBLE
                        texterror.text =
                            "Invalid time in, not in the assigned job site!"
                        texterror.setTextColor(Color.RED)
                    }
                }, 7500)


            } else {
                stopCountdownTimer()
                texterror.visibility = View.GONE


                val queue = Volley.newRequestQueue(applicationContext)
                val url = "http://192.168.254.131/login/timeout.php"
                val stringRequest: StringRequest =
                    object : StringRequest(
                        Method.POST, url,
                        Response.Listener { response ->
                            progress.visibility = View.GONE
                            try {
                                val jsonObject = JSONObject(response)
                                val status: String = jsonObject.getString("status")
                                val message: String = jsonObject.getString("message")
                                if (status == "success") {

                                    // calling stop location updates first because locationCallback Global var will be replaced with a new instantiation.
                                    // This fixes the bug that location keeps updating due to it being replaced before being stopped. I'm assuming each instantiation has different ID's
                                    stopLocationUpdateService()
                                    stopLocationTrackingService()


                                    Toast.makeText(
                                        this,
                                        "Location Update and Tracking Services stopped",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    texterror.visibility = View.VISIBLE
                                    texterror.text =
                                        "Timed out Successfully, Thank you for your work"
                                    texterror.setTextColor(
                                        ContextCompat.getColor(
                                            this,
                                            R.color.green
                                        )
                                    )

                                    val editor = sharedPreferences.edit()
                                    editor.putString("logged", "true")
                                    editor.putBoolean("isTimeIn", true)
                                    editor.apply()

                                    timeinButton.text = "Time In"
                                    timeinButton.setBackgroundColor(
                                        ContextCompat.getColor(
                                            this,
                                            R.color.green
                                        )
                                    )
                                    isTimeIn = true
                                    isTimerFinished = true


                                } else {
                                    //if post fails
                                    texterror.text = message
                                    texterror.visibility = View.VISIBLE
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        },
                        Response.ErrorListener { error ->
                            progress.visibility = View.GONE
                            texterror.text = "Error: ${error.message}"
                            texterror.visibility = View.VISIBLE
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



        logButton.setOnClickListener {
            if (isFetched) {
                val phone = sharedPreferences.getString("phone", "") ?: ""
                val queue = Volley.newRequestQueue(applicationContext)
                val url = "http://192.168.254.131/login/fetchweek.php"

                val stringRequest = object : StringRequest(
                    Method.POST, url,
                    Response.Listener { response ->
                        try {
                            val jsonResponse = JSONObject(response)
                            val status = jsonResponse.getString("status")
                            val message = jsonResponse.getString("message")

                            if (status == "success") {
                                countdown.visibility = View.INVISIBLE
                                timerPrompt.visibility = View.INVISIBLE

                                // Clear the existing table rows
                                timeRecordsTable.removeAllViews()
                                timeRecordsTable.visibility = View.VISIBLE
                                logButton.text = "Hide History"
                                logButton.setBackgroundColor(Color.RED)
                                isFetched = false
                                val dataArray = jsonResponse.getJSONArray("data")

                                val headerRow = TableRow(this)
                                val headerTimeIn = TextView(this)
                                val headerTimeOut = TextView(this)
                                val headerTotalWorkingTime = TextView(this)
                                headerTimeIn.text = "Time In"
                                headerTimeOut.text = "Time Out"
                                headerTotalWorkingTime.text = "Total Time"
                                // Set font style and size for header cells
                                val boldTypeface = Typeface.defaultFromStyle(Typeface.BOLD)
                                headerTimeIn.setTypeface(boldTypeface)
                                headerTimeOut.setTypeface(boldTypeface)
                                headerTotalWorkingTime.setTypeface(boldTypeface)

                                val fontSize =
                                    resources.getDimensionPixelSize(R.dimen.header_text_size)
                                        .toFloat()
                                headerTimeIn.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
                                headerTimeOut.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
                                headerTotalWorkingTime.setTextSize(
                                    TypedValue.COMPLEX_UNIT_PX,
                                    fontSize
                                )
                                val cellParams =
                                    TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT)
                                cellParams.weight = 1f // This indicates equal width for all columns
                                cellParams.marginEnd =
                                    16 // Add spacing between columns (adjust as needed)
                                headerTimeIn.layoutParams = cellParams
                                headerTimeOut.layoutParams = cellParams
                                headerTotalWorkingTime.layoutParams = cellParams
                                headerRow.addView(headerTimeIn)
                                headerRow.addView(headerTimeOut)
                                headerRow.addView(headerTotalWorkingTime)
                                timeRecordsTable.addView(headerRow)
                                // Set the font size for the data cells
                                val dfontSize =
                                    resources.getDimensionPixelSize(R.dimen.data_text_size)
                                        .toFloat()

                                for (i in 0 until dataArray.length()) {
                                    val entry = dataArray.getJSONObject(i)
                                    val timeIn = entry.optString("time_in")
                                    val timeOut = entry.optString("time_out")
                                    val totalWorkingTime = entry.getString("total_working_time")

                                    val dataRow = TableRow(this)
                                    val cellTimeIn = TextView(this)
                                    val cellTimeOut = TextView(this)
                                    val cellTotalWorkingTime = TextView(this)

                                    // Set the text size for the data cells
                                    cellTimeIn.setTextSize(TypedValue.COMPLEX_UNIT_PX, dfontSize)
                                    cellTimeOut.setTextSize(TypedValue.COMPLEX_UNIT_PX, dfontSize)
                                    cellTotalWorkingTime.setTextSize(
                                        TypedValue.COMPLEX_UNIT_PX,
                                        dfontSize
                                    )
                                    cellTimeIn.text =
                                        if (timeIn != "null") formatDate(timeIn) else ""
                                    cellTimeOut.text =
                                        if (timeOut != "null") formatDate(timeOut) else ""
                                    cellTotalWorkingTime.text = totalWorkingTime
                                    // Set layout parameters for the cells
                                    cellTimeIn.layoutParams = cellParams
                                    cellTimeOut.layoutParams = cellParams
                                    cellTotalWorkingTime.layoutParams = cellParams

                                    dataRow.addView(cellTimeIn)
                                    dataRow.addView(cellTimeOut)
                                    dataRow.addView(cellTotalWorkingTime)
                                    timeRecordsTable.addView(dataRow)
                                }
                            } else {
                                Log.d("ServerResponse", "Error message: $message")
                                texterror.text = "Error: $message"
                                texterror.setTextColor(Color.RED)
                                texterror.visibility = View.VISIBLE // Show the error TextView
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            Log.e("ServerResponse", "JSON parsing error", e)
                            texterror.text = "Error: JSON parsing error"
                            texterror.visibility = View.VISIBLE

                        }
                    },
                    Response.ErrorListener { error ->
                        Log.e("ServerResponse", "Volley error: ${error.message}", error)
                        error.printStackTrace()
                        texterror.text = "Error: ${error.message}"
                        texterror.visibility = View.VISIBLE // Show the error TextView
                    }
                ) {
                    override fun getParams(): Map<String, String> {
                        val params: MutableMap<String, String> = HashMap()
                        params["phone"] = phone
                        return params
                    }
                }
                queue.add(stringRequest)
            } else {
                timeRecordsTable.visibility = View.INVISIBLE
                logButton.text = "See History"
                logButton.setBackgroundColor(ContextCompat.getColor(this, R.color.blue))
                isFetched = true
                if (!isTimerFinished) {
                    countdown.visibility = View.VISIBLE
                }
            }
        }

        var otRequestStatus = 0 // Initialize otRequestStatus with 0 initially

        requestButton.setOnClickListener {
            Log.d("RequestButton", "isTimeIn: $isTimeIn")
            if (isTimeIn && requestButton.text != "Request OT") { // Check if status is 0 or 1
                Log.d("RequestButton", "Inside otRequestStatus check")
                val checkOTRequestUrl = "http://192.168.254.131/login/check_ot_request.php"
                val checkqueue = Volley.newRequestQueue(applicationContext)
                val checkOTRequestStringRequest = object : StringRequest(
                    Method.POST, checkOTRequestUrl,
                    Response.Listener { response ->
                        try {
                            val jsonObject = JSONObject(response)
                            val status: String = jsonObject.getString("status")
                            val message: String = jsonObject.getString("message")

                            if (status == "success") {
                                otRequestStatus =
                                    jsonObject.getInt("ot_request_status") // Update status
                                when (otRequestStatus) {
                                    1 -> {
                                        timerPrompt.text = "Overtime Request Pending"
                                        timerPrompt.visibility = View.VISIBLE
                                        requestButton.text = "Update Status"
                                    }

                                    2 -> {
                                        if (!started) {
                                            timerPrompt.text = "Overtime Approved!"
                                            timerPrompt.visibility = View.VISIBLE
                                            // Update the button text and background color here
                                            requestButton.text = "Start OT"
                                            requestButton.setBackgroundColor(
                                                ContextCompat.getColor(this, R.color.green)
                                            )
                                            started = true
                                        } else {
                                            Log.d("RequestButton", "Inside request OT")
                                            val queue = Volley.newRequestQueue(applicationContext)
                                            val url =
                                                "http://192.168.254.131/login/finished_request.php"
                                            val stringRequest: StringRequest =
                                                object : StringRequest(
                                                    Method.POST, url,
                                                    Response.Listener { response ->
                                                        progress.visibility = View.GONE
                                                        try {
                                                            val jsonObject = JSONObject(response)
                                                            val status: String =
                                                                jsonObject.getString("status")
                                                            val message: String =
                                                                jsonObject.getString("message")

                                                            if (status == "success") {
                                                                timerPrompt.text = "OT requested"
                                                                requestButton.text = "update Status"

                                                            } else {
                                                                texterror.text = message
                                                                texterror.visibility = View.VISIBLE
                                                            }

                                                        } catch (e: JSONException) {
                                                            e.printStackTrace()
                                                        }
                                                    },
                                                    Response.ErrorListener { error ->
                                                        progress.visibility = View.GONE
                                                        texterror.text = "Error: ${error.message}"
                                                        texterror.visibility = View.VISIBLE
                                                    }) {
                                                    override fun getParams(): Map<String, String>? {
                                                        val paramV: MutableMap<String, String> =
                                                            HashMap()
                                                        paramV["phone"] = phone ?: ""
                                                        return paramV
                                                    }
                                                }
                                            queue.add(stringRequest)
                                            timeinButton.performClick()
                                        }
                                    }

                                    else -> {
                                        timerPrompt.text = "No Overtime Requested"
                                        timerPrompt.visibility = View.VISIBLE
                                        // Update the button text and background color here
                                    }
                                }
                            } else {
                                texterror.setTextColor(Color.RED)
                                texterror.text = message
                                texterror.visibility = View.VISIBLE
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener { error ->
                        progress.visibility = View.GONE
                        texterror.text = "Error: ${error.message}"
                        texterror.visibility = View.VISIBLE
                    }) {
                    override fun getParams(): Map<String, String>? {
                        val paramV: MutableMap<String, String> = HashMap()
                        paramV["phone"] = sharedPreferences.getString("phone", "") ?: ""
                        return paramV
                    }
                }
                checkqueue.add(checkOTRequestStringRequest)
            } else {

                Log.d("RequestButton", "Inside request OT")
                val queue = Volley.newRequestQueue(applicationContext)
                val url = "http://192.168.254.131/login/request.php"
                val stringRequest: StringRequest =
                    object : StringRequest(
                        Method.POST, url,
                        Response.Listener { response ->
                            progress.visibility = View.GONE
                            try {
                                val jsonObject = JSONObject(response)
                                val status: String = jsonObject.getString("status")
                                val message: String = jsonObject.getString("message")

                                if (status == "success") {
                                    timerPrompt.text = "OT requested"
                                    requestButton.text = "update Status"

                                } else {
                                    texterror.text = message
                                    texterror.visibility = View.VISIBLE
                                }

                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        },
                        Response.ErrorListener { error ->
                            progress.visibility = View.GONE
                            texterror.text = "Error: ${error.message}"
                            texterror.visibility = View.VISIBLE
                        }) {
                        override fun getParams(): Map<String, String>? {
                            val paramV: MutableMap<String, String> = HashMap()
                            paramV["phone"] = phone ?: ""
                            return paramV
                        }
                    }
                queue.add(stringRequest)

            }
        }
    }


    private fun startLocationUpdateServiceWrapper() {
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
                startLocationUpdateService()

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

    private fun startLocationUpdateService() {
        val serviceIntent = Intent(this, LocationUpdateService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.startForegroundService(serviceIntent)
        } else {
            this.startService(serviceIntent)
        }
    }

    private fun stopLocationUpdateService() {
        val intent = Intent(this, LocationUpdateService::class.java)
        stopService(intent)
    }


    private fun startLocationTrackingService() {
        val serviceIntent = Intent(this, LocationTrackingService::class.java)
        serviceIntent.putExtra("serviceType", "location") // Add the service type here
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

    }

    private fun stopLocationTrackingService() {
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
                startLocationUpdateServiceWrapper()   // loop again to the function
            } else {
                Toast.makeText(applicationContext, "Denied", Toast.LENGTH_SHORT).show()
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sharedPreferences.edit().putBoolean("TimeInPageCreated", false).apply()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(buttonClickReceiver)
    }


    fun getJobSiteCoords() {


    }


}


