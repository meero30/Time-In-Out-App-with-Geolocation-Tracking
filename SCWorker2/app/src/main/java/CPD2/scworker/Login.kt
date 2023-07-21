package CPD2.scworker


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


    class Login : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.login_page)

            val signupButton: Button = findViewById(R.id.btSignup)
            signupButton.setOnClickListener {
                val intent = Intent(this@Login, Signup::class.java)
                startActivity(intent)
            }
            val linkTextView: TextView = findViewById(R.id.linkTextView)
            linkTextView.setOnClickListener {
                val url = "https://www.google.com" // Replace with your desired URL
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                startActivity(intent)
            }
        }
    }
