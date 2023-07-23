package CPD2.scworker


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONObject


class Nextpage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.next_page)

        val texterror : TextView = findViewById(R.id.error)
        val fetchfullname: TextView = findViewById(R.id.fetchfullname)
        val fetchphone: TextView= findViewById(R.id.fetchphone)
        val fetchresult: TextView= findViewById(R.id.fetchResult)
        val btlogout : Button = findViewById(R.id.logout)
        val btfetch : Button = findViewById(R.id.fetchProfile)

        texterror.visibility = View.GONE
        val sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        if(sharedPreferences.getString("logged","false").equals("false")){
            val intent = Intent(this@Nextpage, Login::class.java)
            startActivity(intent)
            finish()
        }
        fetchfullname.text = sharedPreferences.getString("fullname","")
        fetchphone.text = sharedPreferences.getString("phone","")

        btlogout.setOnClickListener(
            View.OnClickListener {
                texterror.visibility = View.GONE
                val queue = Volley.newRequestQueue(applicationContext)
                val url = "http://192.168.100.13/login/logout.php"
                val stringRequest: StringRequest =
                    object : StringRequest(
                        Request.Method.POST, url,
                        Response.Listener<String> { response ->
                                if(response.equals("success")) {
                                    val editor = sharedPreferences.edit()
                                    editor.putString("logged", "false")
                                    editor.putString("fullname", "")
                                    editor.putString("phone","")
                                    editor.putString("email", "")
                                    editor.putString("apiKey", "")
                                    editor.apply()

                                    val intent = Intent(this@Nextpage, Login::class.java)
                                    startActivity(intent)
                                    finish()
                                }else Toast.makeText(this@Nextpage,response,Toast.LENGTH_SHORT).show()


                        },
                        Response.ErrorListener { error->
                            error.printStackTrace()
                            texterror.text = "Error: ${error.message}"
                            texterror.visibility = View.VISIBLE
                        }) {
                        override fun getParams(): Map<String, String>? {
                            val paramV: MutableMap<String, String> = HashMap()
                            paramV["phone"] = sharedPreferences.getString("phone", "") ?: ""
                            paramV["apiKey"] = sharedPreferences.getString("apiKey", "") ?: ""
                            return paramV
                        }
                    }
                queue.add(stringRequest)

})
        btfetch.setOnClickListener(
            View.OnClickListener {
                val queue = Volley.newRequestQueue(applicationContext)
                val url = "http://192.168.100.13/login/profile.php"
                val stringRequest: StringRequest =
                    object : StringRequest(
                        Request.Method.POST, url,
                        Response.Listener<String> { response ->
                            fetchresult.setText(response)
                            fetchresult.visibility = View.VISIBLE
                        },
                        Response.ErrorListener { error->
                            error.printStackTrace()
                            texterror.text = "Error: ${error.message}"
                            texterror.visibility = View.VISIBLE
                        }) {
                        override fun getParams(): Map<String, String>? {
                            val paramV: MutableMap<String, String> = HashMap()
                            paramV["phone"] = sharedPreferences.getString("phone", "") ?: ""
                            paramV["apiKey"] = sharedPreferences.getString("apiKey", "") ?: ""
                            return paramV
                        }
                    }
                queue.add(stringRequest)

            }
        )
}}
