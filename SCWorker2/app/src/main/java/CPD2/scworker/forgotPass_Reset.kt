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


class forgotPass_Reset : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.forgot_password_reset)

        val phoneInput: TextInputEditText = findViewById(R.id.phone)
        val otpInput: TextInputEditText = findViewById(R.id.otp)
        val newpassInput: TextInputEditText = findViewById(R.id.new_password)
        val resetButton: Button = findViewById(R.id.btReset)
        val texterror : TextView = findViewById(R.id.error)
        val progress: ProgressBar = findViewById(R.id.progress)
        texterror.visibility = View.GONE

        resetButton.setOnClickListener {
            texterror.visibility = View.GONE
            progress.visibility = View.VISIBLE
            val phone: String = phoneInput.text.toString()
            val otp: String = otpInput.text.toString()
            val newpass: String = newpassInput.text.toString()
            val queue = Volley.newRequestQueue(applicationContext)
            val url = "http://192.168.254.131/login/ResetPassword.php"

            val stringRequest: StringRequest =
                object : StringRequest(
                    Method.POST, url,
                    Response.Listener{ response ->
                        progress.visibility = View.GONE
                        if (response.equals("success")) {
                            Toast.makeText(
                                applicationContext,
                                "Password Changed Successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(this@forgotPass_Reset ,Login::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            texterror.text = response;
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
                        paramV["otp"] = otp
                        paramV["phone"] = phone
                        paramV["new-password"] = newpass
                        return paramV
                    }
                }
            queue.add(stringRequest)
        }


        val backButton: Button = findViewById(R.id.btGoback)
        backButton.setOnClickListener {
            val intent = Intent(this,forgotPass_Verify::class.java)
            startActivity(intent)
            finish();
        }
    }
}
