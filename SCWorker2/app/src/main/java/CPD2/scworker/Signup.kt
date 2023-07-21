package CPD2.scworker

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


    class Signup : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.signup_page)

            val linkTextView: TextView = findViewById(R.id.backToLogin)
            linkTextView.setOnClickListener {
                val intent = Intent(this@Signup ,Login::class.java)
                startActivity(intent)
            }
        }
    }
