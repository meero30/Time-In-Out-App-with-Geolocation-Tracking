package CPD2.scworker

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices


class MainPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_page)

        val loginButton: Button = findViewById(R.id.btLogin)
        loginButton.setOnClickListener {
            val intent = Intent(this@MainPage, LoginPage::class.java)
            startActivity(intent)
        }
        val signUpButton: Button = findViewById(R.id.btSignup)
        signUpButton.setOnClickListener {
            val url = "https://www.google.com" // Replace with your desired URL
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }

    }


}

class LoginPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_page)
        val imageView: ImageView = findViewById(R.id.logo1)
        imageView.setOnClickListener {
            onBackPressed()
        }

        // Add your logic or functionality for the second activity here
        val linkTextView: TextView = findViewById(R.id.linkTextView)
        linkTextView.setOnClickListener {
            val url = "https://www.google.com" // Replace with your desired URL
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
    }
}


class TimeInTimeOutPage : AppCompatActivity() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var tvLatitude: TextView
    private lateinit var tvLongitude: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.time_in_time_out_page)


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

    }

    // TODO: Make the app monitor geofencing, if user disables permissions or location, automatic time out will execute.


    //  TODO: Complete the getCurrentLocation() Function
    private fun getCurrentLocation() {
        // Check if permissions are enabled
        if (checkPermissions()) {

            if (isLocationEnabled()) {
                // Get location here
            } else {
                // Open settings here to enable location
            }
        } else {
            // Pop Up explaining the importance of Enabling permissions
            // Request Permission
        }
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
            == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {

            return true
        }

        return false
    }


    private fun isLocationEnabled(): Boolean {
        return true
    }

}