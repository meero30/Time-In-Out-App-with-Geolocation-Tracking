package CPD2.scworker

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
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

        val goToTestPageButton: Button = findViewById(R.id.button2)
        goToTestPageButton.setOnClickListener {
            val intent = Intent(this@MainPage, TimeInTimeOutPage::class.java)
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


