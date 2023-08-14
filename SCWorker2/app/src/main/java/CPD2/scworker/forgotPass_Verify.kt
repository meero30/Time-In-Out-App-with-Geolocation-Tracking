package CPD2.scworker

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONException
import org.json.JSONObject

class forgotPass_Verify : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.forgot_password_verify)

        val phoneInput: TextInputEditText = findViewById(R.id.phone)
        val verifyButton: Button = findViewById(R.id.btverify)
        val texterror : TextView = findViewById(R.id.error)
        val progress: ProgressBar = findViewById(R.id.progress)
        texterror.visibility = View.GONE

        verifyButton.setOnClickListener {
            texterror.visibility = View.GONE
            progress.visibility = View.VISIBLE
            val phone: String = phoneInput.text.toString()
            val queue = Volley.newRequestQueue(applicationContext)
            val url = "http://192.168.254.131/login/VerifyPhone.php"

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
                                texterror.text = message
                                texterror.setTextColor(ContextCompat.getColor(this, R.color.green))

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
                        texterror.setTextColor(Color.RED)
                        texterror.text = "Error: ${error.message}"
                        texterror.visibility = View.VISIBLE
                    }) {
                    override fun getParams(): Map<String, String>? {
                        val paramV: MutableMap<String, String> = HashMap()
                        paramV["phone"] = phone
                        return paramV
                    }
                }
            queue.add(stringRequest)
        }

        val btnext: Button = findViewById(R.id.btnext)
        btnext.setOnClickListener {
            val intent = Intent(this@forgotPass_Verify ,forgotPass_Reset::class.java)
            startActivity(intent)
            finish()
        }
        val linkTextView: TextView = findViewById(R.id.backToLogin)
        linkTextView.setOnClickListener {
            val intent = Intent(this@forgotPass_Verify ,Login::class.java)
            startActivity(intent)
            finish()
        }
    }
}
