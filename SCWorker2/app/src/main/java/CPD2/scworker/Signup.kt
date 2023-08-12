package CPD2.scworker


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputEditText


class Signup : AppCompatActivity() {



        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.signup_page)

            val fullNameInput: TextInputEditText  = findViewById(R.id.fullname)
            val emailInput: TextInputEditText  = findViewById(R.id.email)
            val phoneInput: TextInputEditText  = findViewById(R.id.phone)
            val passwordInput: TextInputEditText  = findViewById(R.id.password)
            val signupButton: Button = findViewById(R.id.btSignup)
            val texterror : TextView = findViewById(R.id.error)
            val progress: ProgressBar = findViewById(R.id.progress)
            texterror.visibility = View.GONE

            signupButton.setOnClickListener {
                texterror.visibility = View.GONE
                progress.visibility = View.VISIBLE
                val fullname: String = fullNameInput.text.toString()
                val email: String = emailInput.text.toString()
                val phone: String = phoneInput.text.toString()
                val password: String = passwordInput.text.toString()
                val queue = Volley.newRequestQueue(applicationContext)
                val url = "http://192.168.100.13/login/signup.php"

                val stringRequest: StringRequest =
                    object : StringRequest(
                        Method.POST, url,
                        Response.Listener{ response ->
                            progress.visibility = View.GONE
                            if (response.equals("success")) {
                                Toast.makeText(
                                    applicationContext,
                                    "Sign up successful",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val intent = Intent(this@Signup, Login::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                texterror.text = response
                                texterror.visibility = View.VISIBLE
                            }
                        },
                        Response.ErrorListener { error ->
                            progress.visibility = View.GONE
                            texterror.text = "Error: ${error.message}"
                            texterror.visibility = View.VISIBLE
                        }) {
                        override fun getParams(): Map<String, String>? {
                            val paramV: MutableMap<String, String> = HashMap()
                            paramV["fullname"] = fullname
                            paramV["email"] = email
                            paramV["phone"] = phone
                            paramV["password"] = password
                            return paramV
                        }
                    }
                queue.add(stringRequest)
            }
            val linkTextView: TextView = findViewById(R.id.backToLogin)
            linkTextView.setOnClickListener {
                val intent = Intent(this@Signup ,Login::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
