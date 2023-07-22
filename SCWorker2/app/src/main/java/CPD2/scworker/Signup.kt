package CPD2.scworker


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.vishnusivadas.advanced_httpurlconnection.PutData


class Signup : AppCompatActivity() {


        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.signup_page)

            val fullNameInput: TextInputEditText  = findViewById(R.id.fullname)
            val emailInput: TextInputEditText  = findViewById(R.id.email)
            val phoneInput: TextInputEditText  = findViewById(R.id.phoneSignup)
            val passwordInput: TextInputEditText  = findViewById(R.id.passwordSignup)
            val signupButton: Button = findViewById(R.id.btSignup)
            val progress: ProgressBar = findViewById(R.id.progress)

            signupButton.setOnClickListener(
                View.OnClickListener {
                    val fullname: String = fullNameInput.text.toString()
                    val email: String = emailInput.text.toString()
                    val phone: String = phoneInput.text.toString()
                    val password: String = passwordInput.text.toString()

                    if(fullname != "" && email != "" && phone != "" && password != "") {
                        progress.visibility = View.VISIBLE
                        val handler = Handler(Looper.getMainLooper())
                        handler.post(Runnable {
                            val field = arrayOfNulls<String>(4)
                            field[0] = "fullname"
                            field[1] = "email"
                            field[2] = "phone"
                            field[3] = "password"
                            val data = arrayOfNulls<String>(4)
                            data[0] = fullname
                            data[1] = email
                            data[2] = phone
                            data[3] = password
                            val putData = PutData(
                                "http://192.168.100.13/SCworkerLogin/signup.php",
                                "POST",
                                field,
                                data
                            )
                            if (putData.startPut()) {
                                if (putData.onComplete()) {
                                    progress.visibility = View.GONE
                                    val result = putData.result
                                    if(result.equals("Sign Up Success")){
                                        Toast.makeText(applicationContext, result, Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this@Signup,Login::class.java)
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


            val linkTextView: TextView = findViewById(R.id.backToLogin)
            linkTextView.setOnClickListener {
                val intent = Intent(this@Signup ,Login::class.java)
                startActivity(intent)
            }
        }
    }
