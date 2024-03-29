package com.example.gieldadyzurowa



import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions


import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ModalDrawer
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel



import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gieldadyzurowa.network.LoginRequest
import com.example.gieldadyzurowa.network.RegistrationRequest
import com.example.gieldadyzurowa.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor


import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import java.text.SimpleDateFormat
import java.util.Locale


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.Menu



fun formatDate(dateStr: String): String {
    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    val formatter = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
    return try {
        val parsedDate = parser.parse(dateStr)
        if (parsedDate != null) formatter.format(parsedDate) else "Invalid date"
    } catch (e: Exception) {
        "Invalid date"
    }
}


class MainActivity : ComponentActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContent()
        {
            AppContent()
        }
    }
}

data class Hospital(
    val _id: String,
    val username: String,
    val password: String, // Note: It's unusual and insecure to handle passwords in client-side code.
    val role: String,
    val profileVisible: Boolean
    // Add other fields as necessary...
)

data class DutySlot(
    val hospitalId: Hospital, // Assuming hospitalId is unique and can be used as such; adjust as needed
    val date: String, // Using String for simplicity; consider using a proper date type
    val dutyHours: String,
    val requiredSpecialty: String
)



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent() {
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    var isLoggedIn by remember { mutableStateOf(false) }
    var selectedNav by remember { mutableStateOf("Doctor Availabilities") }
    var username by remember { mutableStateOf("") }
    var showRegistrationSuccessDialog by remember { mutableStateOf(false) }
    val dutySlotsViewModel = viewModel<DutyOffersViewModel>()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                onNavSelected = { nav ->
                    selectedNav = nav
                    coroutineScope.launch {
                        drawerState.close()
                    }
                    if (nav == "Logout") {
                        isLoggedIn = false
                        username = ""
                        selectedNav = "Login"
                    }
                },
                isLoggedIn = isLoggedIn
            )
        }
    ) {
        // Scaffold and your app's content go here
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            text = if (isLoggedIn && username.isNotEmpty()) "Logged in as $username" else "Not Logged In"
                        )
                    },                    navigationIcon = {
                        if (drawerState.isClosed) {
                            IconButton(onClick = {
                                coroutineScope.launch { drawerState.open() }
                            }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                    }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                if (showRegistrationSuccessDialog) {
                    AlertDialog(
                        onDismissRequest = { showRegistrationSuccessDialog = false },
                        title = { Text("Registration Successful") },
                        text = { Text("You've successfully registered. Please log in.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showRegistrationSuccessDialog = false
                                    selectedNav = "Login"
                                }
                            ) { Text("OK") }
                        }
                    )
                }

                // The rest of your conditional content logic
                when (selectedNav) {
                    "Login" -> if (!isLoggedIn) {
                        LoginScreen(onLoginSuccess = { user ->
                            isLoggedIn = true
                            username = user
                            selectedNav = "Doctor Availabilities"
                        })
                    }
                    "Register" -> if (!isLoggedIn) {
                        RegisterScreen(onRegistrationSuccess = {
                            showRegistrationSuccessDialog = true
                        })
                    }
                    "Duty Offers" -> DutyOffersScreen(viewModel = dutySlotsViewModel)
                    else -> MainContent(selectedNav)
                }
            }
        }
    }
}

@Composable
fun DrawerContent(onNavSelected: (String) -> Unit, isLoggedIn: Boolean) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Populate your drawer content here
        Button(onClick = { onNavSelected("Login") }) { Text("Login") }
        Button(onClick = { onNavSelected("Register") }) { Text("Register") }
        Button(onClick = { onNavSelected("Duty Offers") }) { Text("Duty Offers") }
        Button(onClick = { onNavSelected("Doctor Availabilities") }) { Text("Doctors") }
        if (isLoggedIn) {
            Button(onClick = { onNavSelected("Logout") }) { Text("Logout") }
        }
    }
}

// Ensure you define or adapt LoginScreen, RegisterScreen, DutyOffersScreen, and MainContent for compatibility with your app's logic and Material 3 components.


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Header(isLoggedIn: Boolean, username: String) {
    TopAppBar(
        title = {
            Text(text = if (isLoggedIn) username else "Not Logged In")
        }
    )
}





@Composable
fun MainContent(selectedNav: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when (selectedNav) {
            "Doctor Availabilities" -> DoctorAvailabilitiesScreen()
            else -> Text("Select an option from the navigation.")
        }
    }
}


@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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
                performLogin(username, password, onLoginSuccess) // Call performLogin on click

            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Login")
        }
    }
}

@Composable
fun ExampleDropdownMenu() {
    var expanded by remember { mutableStateOf(false) }
    val roles = listOf("doctor", "hospital")
    var selectedRole by remember { mutableStateOf("") }

    Column {
        OutlinedTextField(
            value = selectedRole,
            onValueChange = { selectedRole = it },
            label = { Text("Role") },
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown",
                    Modifier.clickable { expanded = !expanded }
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            roles.forEach { role ->
                DropdownMenuItem(
                    onClick = {
                        selectedRole = role
                        expanded = false
                    }
                ) {
                    // Ensure the Text composable is correctly receiving a string for its 'text' parameter
                    Text(text = role.replaceFirstChar { it.uppercase() })
                }
            }
        }
    }
}


@Composable
fun RegisterScreen(onRegistrationSuccess: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("") }
    var localization by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val showSpecialtyLocalization by remember { derivedStateOf { role == "doctor" } }

    val roles = listOf("doctor", "hospital") // Your roles

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            singleLine = true
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Box {
            OutlinedTextField(
                readOnly = true,
                value = role.ifEmpty { "Select Role" },
                onValueChange = {},
                label = { Text("Role") },
                trailingIcon = {
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        Modifier.clickable { expanded = !expanded }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                roles.forEach { selectionOption ->
                    DropdownMenuItem(
                        onClick = {
                            role = selectionOption

                            expanded = false
                        }
                    ) {
                        Text(text = selectionOption.replaceFirstChar { it.uppercase() })
                    }
                }
            }
        }

        if (showSpecialtyLocalization) {
            OutlinedTextField(
                value = specialty,
                onValueChange = { specialty = it },
                label = { Text("Specialty (Doctors only)") },
                singleLine = true
            )
            OutlinedTextField(
                value = localization,
                onValueChange = { localization = it },
                label = { Text("Localization (Doctors only)") },
                singleLine = true
            )
        }

        Button(
            onClick = {

                performRegistration(username,
                    password,
                    role,
                    specialty,
                    localization,
                    onRegistrationSuccess)

            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Register")
        }
    }
}


@Composable
fun DoctorAvailabilitiesScreen() {
    // Implementation of the Doctor Availabilities Screen
    Text("Doctor Availabilities Screen")
}

@Composable
fun Footer()
{
    BottomAppBar {
        Text("Footer", modifier = Modifier.padding(16.dp))
    }
}


object OkHttpClientInstance {
    val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(object : CookieJar {
                private val cookieStore = mutableMapOf<String, List<Cookie>>()

                override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
                    cookieStore[url.toString()] = cookies
                }

                override fun loadForRequest(url: HttpUrl): List<Cookie> {
                    return cookieStore[url.toString()] ?: listOf()
                }
            })
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }
}

fun performRegistration(
    username: String,
    password: String,
    role: String,
    specialty: String?,
    localization: String?,
    onRegistrationSuccess: () -> Unit
)
{
    val registrationRequest = RegistrationRequest(
        username = username,
        password = password,
        role = role,
        specialty = specialty?.takeIf { it.isNotEmpty() },
        localization = localization?.takeIf { it.isNotEmpty() }
    )


    RetrofitClient.apiService.registerUser(registrationRequest).enqueue(object : Callback<Void>
    {
        override fun onResponse(call: Call<Void>, response: Response<Void>)
        {
            if (response.isSuccessful)
            {
                Log.d("RegistrationSuccess", "Successfully registered user: $username")
                onRegistrationSuccess()
            }
            else
            {
                Log.e("RegistrationError", "Failed to register user: $username. Response code: ${response.code()}")
                // Here you can handle different HTTP codes and give feedback to the user accordingly
            }
        }

        override fun onFailure(call: Call<Void>, t: Throwable)
        {
            Log.e("RegistrationFailure", "Failed to register user: $username", t)
            // Handle the failure, for example, by showing an error message to the user
        }
    })
}

// This function can be called within the onClick listener of the login button
fun performLogin(username: String, password: String, onLoginSuccess: (String) -> Unit) {
    val loginRequest = LoginRequest(username, password)
    RetrofitClient.apiService.loginUser(loginRequest).enqueue(object : Callback<Void> {
        override fun onResponse(call: Call<Void>, response: Response<Void>) {
            if (response.isSuccessful) {
                Log.d("LoginSuccess", "Successfully logged in user: $username")
                onLoginSuccess(username)
            } else {
                Log.e(
                    "LoginError",
                    "Failed to log in user: $username. Response code: ${response.code()}"
                )
                // Handle error response
            }
        }

        override fun onFailure(call: Call<Void>, t: Throwable) {
            Log.e("LoginFailure", "Failed to log in user: $username", t)
            // Handle the failure, e.g., show an error message
        }
    })
}


@Composable
fun DutyOffersScreen(viewModel: DutyOffersViewModel) {
    viewModel.fetchDutySlots()
    val dutySlots = viewModel.dutySlots.collectAsState().value
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Duty Slots", style = MaterialTheme.typography.headlineMedium)
        if (dutySlots.isEmpty()) {
            Text("No duty slots found.", modifier = Modifier.padding(top = 16.dp))
        } else {
            LazyColumn {
                items(count = dutySlots.size, itemContent = { index ->
                    DutySlotCard(slot = dutySlots[index])
                })
            }}
    }
}

class DutyOffersViewModel : ViewModel() {
    private val _dutySlots = MutableStateFlow<List<DutySlot>>(emptyList())
    val dutySlots: StateFlow<List<DutySlot>> = _dutySlots

    init {
        fetchDutySlots()
    }

     fun fetchDutySlots() = viewModelScope.launch {
        try {
            val response = RetrofitClient.apiService.fetchDutySlots()
            if (response.isSuccessful) {
                _dutySlots.value = response.body() ?: emptyList()
            } else {
                // Log error or handle error state
                Log.e("DutyOffersViewModel", "Error fetching duty slots: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            // Handle exceptions from network call
            Log.e("DutyOffersViewModel", "Exception when fetching duty slots", e)
        }
    }
}



@Composable
fun DutySlotCard(slot: DutySlot) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Hospital: ${slot.hospitalId.username}", style = MaterialTheme.typography.bodyLarge)
            Text("Date: ${formatDate(slot.date)}", style = MaterialTheme.typography.bodyLarge)
            Text("Duty Hours: ${slot.dutyHours}", style = MaterialTheme.typography.bodyLarge)
            Text("Required Specialty: ${slot.requiredSpecialty}", style = MaterialTheme.typography.bodyLarge)
        }
    }
}



