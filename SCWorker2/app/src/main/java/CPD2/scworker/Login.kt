package CPD2.scworker


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.vishnusivadas.advanced_httpurlconnection.PutData


class Login : AppCompatActivity() {


        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.login_page)

            val phoneInput: TextInputEditText = findViewById(R.id.phoneLogin)
            val passwordInput: TextInputEditText = findViewById(R.id.passwordLogin)
            val loginButton: Button = findViewById(R.id.btLogin)
            val progress: ProgressBar = findViewById(R.id.progress)

            loginButton.setOnClickListener(
                View.OnClickListener {
                    val phone: String = phoneInput.text.toString()
                    val password: String = passwordInput.text.toString()

                    if(phone != "" && password != "") {
                        progress.visibility = View.VISIBLE
                        val handler = Handler(Looper.getMainLooper())
                        handler.post(Runnable {
                            val field = arrayOfNulls<String>(2)
                            field[0] = "phone"
                            field[1] = "password"


                            val data = arrayOfNulls<String>(2)
                            data[0] = phone
                            data[1] = password
                            val putData = PutData(
                                "http://192.168.100.13//SCworkerLogin//login.php",
                                "POST",
                                field,
                                data
                            )
                            if (putData.startPut()) {
                                if (putData.onComplete()) {
                                    progress.visibility = View.GONE
                                    val result = putData.result
                                    if(result.equals("Login Success")){
                                        Toast.makeText(applicationContext, result, Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this@Login,Nextpage::class.java)
                                        startActivity(intent)
                                        finish()
                                    }else{
                                        Toast.makeText(applicationContext, result, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                        )
                    }
                    else{
                        Toast.makeText(applicationContext, "All fields are required", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            val signupButtonL: Button = findViewById(R.id.btSignupL)
            signupButtonL.setOnClickListener {
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
