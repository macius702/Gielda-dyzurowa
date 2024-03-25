package com.example.gieldadyzurowa

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.example.gieldadyzurowa.viewmodel.MainViewModel
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.gieldadyzurowa.network.ApiService
import com.example.gieldadyzurowa.network.RetrofitService
import com.example.gieldadyzurowa.network.RetrofitService.createService
import com.example.gieldadyzurowa.utils.SharedPreferencesManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val prefsManager = SharedPreferencesManager(this)

        findViewById<Button>(R.id.loginButton).setOnClickListener {
            val username = findViewById<EditText>(R.id.usernameEditText).text.toString()
            val password = findViewById<EditText>(R.id.passwordEditText).text.toString()

            val apiService = createService(ApiService::class.java)
            apiService.login(hashMapOf("username" to username, "password" to password))
                .enqueue(object : Callback<HashMap<String, String>> {
                    override fun onResponse(call: Call<HashMap<String, String>>, response: Response<HashMap<String, String>>) {
                        if (response.isSuccessful) {
                            response.body()?.get("token")?.let { token ->
                                prefsManager.saveToken(token)
                                Toast.makeText(applicationContext, "Login successful", Toast.LENGTH_LONG).show()
                                // Navigate to the next screen or show success message
                            }
                        } else {
                            Toast.makeText(applicationContext, "Login failed", Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<HashMap<String, String>>, t: Throwable) {
                        Toast.makeText(applicationContext, "Error: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
        }
    }
}