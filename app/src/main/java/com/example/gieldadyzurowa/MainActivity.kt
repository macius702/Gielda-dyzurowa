package com.example.gieldadyzurowa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.gieldadyzurowa.ui.theme.GieldadyzurowaTheme


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.gieldadyzurowa.ui.theme.GieldadyzurowaTheme

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

import android.content.Context
import org.json.JSONObject
import java.io.IOException


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GieldadyzurowaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoginScreen()
                }
            }
        }
    }
}

@Composable
fun LoginScreen() {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current // Capture the context

    Column(modifier = Modifier.padding(PaddingValues(16.dp))) {
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { /* Handle login */ })
        )
        Button(
            onClick = {
                performLogin(username, password, context) // Call performLogin on click

            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Login")
        }
    }
}



@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GieldadyzurowaTheme {
        Greeting("Android")
    }
}




// This function can be called within the onClick listener of the login button
fun performLogin(username: String, password: String, context: Context) {
    val client = OkHttpClient()
    val jsonObject = JSONObject()
    jsonObject.put("username", username)
    jsonObject.put("password", password)

    val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
    val requestBody = jsonObject.toString().toRequestBody(mediaType)

    val request = Request.Builder()
        .url("https://powerful-sea-67789-a7c9da8bf02d.herokuapp.com/auth/login")
        //.url("http://10.0.2.2:3000/auth/login")
        .post(requestBody)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onResponse(call: Call, response: Response) {
            if (response.isSuccessful) {
                Log.d("LoginSuccess", "Successfully logged in user: $username")
                // Handle successful response, e.g., navigate to another screen
            } else {
                Log.e("LoginError", "Failed to log in user: $username. Response code: ${response.code}")
                // Handle error response
            }
        }

        override fun onFailure(call: Call, e: IOException) {
            Log.e("LoginFailure", "Failed to log in user: $username", e)

            // Handle the failure, e.g., show an error message
        }
    })
}

