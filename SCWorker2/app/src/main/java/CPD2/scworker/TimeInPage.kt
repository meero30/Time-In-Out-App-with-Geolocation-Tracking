package CPD2.scworker

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import org.json.JSONException


class TimeInPage : AppCompatActivity() {

    private var isTimeIn = true
    private var isFetched = true
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val destinationLatitude = 37.4226711
    private val destinationLongitude = -122.0849872
    //private val destinationLatitude = 37.422
    //private val destinationLongitude = 122.0785

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
        val requestOvertimeButton: Button = findViewById(R.id.overtimeButton)
        val progress: ProgressBar = findViewById(R.id.progress)
        countdown = findViewById(R.id.timer)
        timeinButton= findViewById(R.id.timeinButton)
        texterror.visibility = View.GONE
        overtimeStatus.text = ""
        overtimeStatus.visibility = View.GONE

        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
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
            startCountdownTimer(initialTimeMillis = savedRemainingTime,this)
        }



        timeinButton.setOnClickListener {
            timeRecordsTable.visibility = View.INVISIBLE
            if (isTimeIn) {
                fetchLocation(this, fusedLocationProviderClient) { latitude, longitude ->
                    Log.d(
                        "LocationDebug",
                        "Latitude: $latitude, Longitude: $longitude"
                    ) // Log latitude and longitude
                    val distance = calculateDistance(
                        this,
                        latitude,
                        longitude,
                        destinationLatitude,
                        destinationLongitude
                    )
                    Log.d("DistanceDebug", "Distance: $distance meters") // Log the distance

                    if (distance < 1000) { // Distance less than 1km (in meters)
                        val queue = Volley.newRequestQueue(applicationContext)
                        val url = "http://192.168.100.13/login/timein.php"

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
                                            countdown.visibility = View.VISIBLE // Make the countdown TextView visible
                                            startCountdownTimer(initialTimeMillis = 10000,this)
                                            texterror.visibility = View.VISIBLE
                                            texterror.text =
                                                "Timed in Successfully, Have a blessed day at Work"
                                            texterror.setTextColor(ContextCompat.getColor(this, R.color.green))
                                            val editor = sharedPreferences.edit()
                                            editor.putBoolean("isTimeIn", false)
                                            editor.apply()

                                            // Update button text and color
                                            timeinButton.text = "Time Out"
                                            timeinButton.setBackgroundColor(Color.RED)
                                            timeRecordsTable.visibility = View.INVISIBLE
                                            isTimeIn = false
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
                                override fun getParams(): Map<String, String> {
                                    val paramV: MutableMap<String, String> = HashMap()
                                    paramV["phone"] = sharedPreferences.getString("phone", "") ?: ""
                                    return paramV
                                }
                            }
                        queue.add(stringRequest)
                    } else {
                        // Distance greater than or equal to 1km
                        texterror.visibility = View.VISIBLE
                        texterror.text = "Invalid time in, not in the assigned job site!"
                        texterror.setTextColor(Color.RED)
                    }
                }
            } else {
                stopCountdownTimer()
                texterror.visibility = View.GONE
                val queue = Volley.newRequestQueue(applicationContext)
                val url = "http://192.168.100.13/login/timeout.php"
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
                                    texterror.visibility = View.VISIBLE
                                    texterror.text =
                                        "Timed out Successfully, Thank you for your work"
                                    texterror.setTextColor(ContextCompat.getColor(this, R.color.green))

                                    val editor = sharedPreferences.edit()
                                    editor.putString("logged", "true")
                                    editor.putBoolean("isTimeIn", true)
                                    editor.apply()

                                    timeinButton.text = "Time In"
                                    timeinButton.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
                                    isTimeIn = true
                                    isTimerFinished = true
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
            val url = "http://192.168.100.13/login/fetchweek.php"

            val stringRequest = object : StringRequest(
                Method.POST, url,
                Response.Listener { response ->
                    try {
                        val jsonResponse = JSONObject(response)
                        val status = jsonResponse.getString("status")
                        val message = jsonResponse.getString("message")
                        if (status == "success") {
                            countdown.visibility = View.INVISIBLE

                            // Clear the existing table rows
                            timeRecordsTable.removeAllViews()
                            timeRecordsTable.visibility = View.VISIBLE
                            logButton.text = "Clear History"
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


                            val fontSize = resources.getDimensionPixelSize(R.dimen.header_text_size).toFloat()
                            headerTimeIn.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
                            headerTimeOut.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)
                            headerTotalWorkingTime.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize)

                            val cellParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT)
                            cellParams.weight = 1f // This indicates equal width for all columns
                            cellParams.marginEnd = 16 // Add spacing between columns (adjust as needed)

                            headerTimeIn.layoutParams = cellParams
                            headerTimeOut.layoutParams = cellParams
                            headerTotalWorkingTime.layoutParams = cellParams

                            headerRow.addView(headerTimeIn)
                            headerRow.addView(headerTimeOut)
                            headerRow.addView(headerTotalWorkingTime)
                            timeRecordsTable.addView(headerRow)

                            // Set the font size for the data cells
                            val dfontSize = resources.getDimensionPixelSize(R.dimen.data_text_size).toFloat()


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
                                cellTotalWorkingTime.setTextSize(TypedValue.COMPLEX_UNIT_PX, dfontSize)

                                cellTimeIn.text = if (timeIn != "null") formatDate(timeIn) else ""
                                cellTimeOut.text = if (timeOut != "null") formatDate(timeOut) else ""
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
                        texterror.visibility = View.VISIBLE}
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
        }else {
                timeRecordsTable.visibility = View.INVISIBLE
                logButton.text = "See History"
                logButton.setBackgroundColor(ContextCompat.getColor(this, R.color.green))
                isFetched = true
                countdown.visibility = View.VISIBLE

            }
            }
        requestOvertimeButton.setOnClickListener {
            texterror.visibility = View.GONE
            val queue = Volley.newRequestQueue(applicationContext)
            val url = "http://192.168.100.13/login/request.php"
            val stringRequest = object : StringRequest(
                Method.POST, url,
                Response.Listener { response ->
                    try {
                        val jsonObject = JSONObject(response)
                        val status: String = jsonObject.getString("status")
                        val message: String = jsonObject.getString("message")
                        if (status == "success") {
                            texterror.visibility = View.VISIBLE
                            texterror.text = "Overtime requested successfully"
                            texterror.setTextColor(ContextCompat.getColor(this, R.color.green))
                        } else {
                            texterror.setTextColor(Color.RED)
                            texterror.text = message
                            texterror.visibility = View.VISIBLE
                        }
                    } catch (e: JSONException) { e.printStackTrace()}
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
            val checkOTRequestUrl = "http://192.168.100.13/login/check_ot_request.php"
            val checkqueue = Volley.newRequestQueue(applicationContext)
            val checkOTRequestStringRequest = object : StringRequest(
                Method.POST, checkOTRequestUrl,
                Response.Listener { response ->
                    try {
                        val jsonObject = JSONObject(response)
                        val status: String = jsonObject.getString("status")
                        val message: String = jsonObject.getString("message")

                        if (status == "success") {
                            val otRequestStatus: Int = jsonObject.getInt("ot_request_status")
                            if (otRequestStatus == 1) {
                                overtimeStatus.text = "Overtime Request Pending"
                                overtimeStatus.visibility = View.VISIBLE
                            } else {
                                overtimeStatus.text = "Overtime Approved!"
                                overtimeStatus.visibility = View.VISIBLE
                            }
                        } else {
                            texterror.setTextColor(Color.RED)
                            texterror.text = message
                            texterror.visibility = View.VISIBLE
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    } },
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
        }
    }
}


