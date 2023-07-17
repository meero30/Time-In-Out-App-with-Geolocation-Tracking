package CPD2.scworker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


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
        //linkTextView.setOnClickListener {
            // Handle the click event for the "Forgot password?" link
            // Add your desired functionality here
        //}
    }
}