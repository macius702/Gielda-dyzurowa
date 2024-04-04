package com.example.gieldadyzurowa



import android.content.Context
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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


import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar
import android.app.DatePickerDialog
import com.example.gieldadyzurowa.network.AdditionalUserInfo
import com.example.gieldadyzurowa.network.AssignDutySlotRequest
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

const val LANDING_SCREEN = "Duty Vacancies"


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

enum class DutySlotStatus(val status: String) {
    OPEN("open"),
    PENDING("pending"),
    FILLED("filled");

    companion object {
        fun from(status: String): DutySlotStatus {
            return when(status) {
                "open" -> OPEN
                "pending" -> PENDING
                "filled" -> FILLED
                else -> throw IllegalArgumentException("Unknown status: $status")
            }
        }
    }
}

class DutySlotStatusDeserializer : JsonDeserializer<DutySlotStatus> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): DutySlotStatus {
        val status = json?.asString
        return DutySlotStatus.from(status ?: throw JsonParseException("Null or invalid status value"))
    }
}


data class Doctor(
    val _id: String,
    val username: String,
    val password: String, // Note: Handling passwords like this is insecure, especially on client-side.
    val role: String,
    val specialty: String,
    val localization: String,
    val profileVisible: Boolean
    // Add other fields as necessary...
)

data class DutyVacancy(
    val _id: String?,
    val hospitalId: Hospital?,
    val date: String,
    val dutyHours: String,
    val requiredSpecialty: String,
    val status: DutySlotStatus, // Use enum type here
    val assignedDoctorId: Doctor? = null
)


data class DoctorAvailability(
    val doctorId: Doctor?,
    val date: String, // Depending on your use case, you might want to parse this into a Date object
    val availableHours: String
)




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent() {
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    var isLoggedIn by remember { mutableStateOf(false) }
    var selectedNav by remember { mutableStateOf(LANDING_SCREEN) }
    var username by remember { mutableStateOf("") }
    var userrole by remember { mutableStateOf("") }
    var userId by remember  { mutableStateOf("") }
    var showRegistrationSuccessDialog by remember { mutableStateOf(false) }
    val dutyVacanciesViewModel = viewModel<DutyVacanciesViewModel>()
    val doctorAvailabilitiesViewModel = viewModel<DoctorAvailabilitiesViewModel>()



    // debug aid debugging
     performLogin("abba", "alamakota",
         onLoginSuccess = { user , role, userIdFromlogin ->
             isLoggedIn = true
             username = user
             userrole = role
             userId = userIdFromlogin
             selectedNav = LANDING_SCREEN
         }
     )

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
                        LoginScreen(onLoginSuccess = { user, role, userIdFromLogin ->
                            isLoggedIn = true
                            username = user
                            userId = userIdFromLogin
                            userrole = role
                            selectedNav = LANDING_SCREEN
                        })
                    }
                    "Register" -> if (!isLoggedIn) {
                        RegisterScreen(onRegistrationSuccess = {
                            showRegistrationSuccessDialog = true
                        })
                    }
                    "Duty Vacancies" -> DutyVacanciesScreen(viewModel = dutyVacanciesViewModel, username = username, userrole = userrole, userId = userId)
                    "Doctor Availabilities" -> DoctorAvailabilitiesScreen(viewModel = doctorAvailabilitiesViewModel)


                    "Publish Duty Vacancy" -> if (isLoggedIn) {
                        // Ensure only logged in users can access the publish screen
                        DutyVacancyPublishScreen(
                            viewModel = dutyVacanciesViewModel,
                            onPublishSuccess = {
                                // Handle what happens after a successful publish
                                // For example, you could navigate back to the Duty Vacancies list
                                selectedNav = "Duty Vacancies"
                            }
                        )
                    }

                    "Add Doctor Availability" -> if (isLoggedIn) {
                        val viewModel: DoctorAvailabilitiesViewModel = viewModel()
                        AddDoctorAvailabilityScreen(viewModel)
                        {
                            selectedNav = "Doctor Availabilities"
                        }

                    }


                    else ->             Text("Select an option from the navigation.")

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
        Button(onClick = { onNavSelected("Doctor Availabilities") }) { Text("Doctor Availabilities") }
        if (isLoggedIn) {
            Button(onClick = { onNavSelected("Add Doctor Availability") }) { Text("Add Doctor Availability") }
        }
        Button(onClick = { onNavSelected("Duty Vacancies") }) { Text("Duty Vacancies") }
        if (isLoggedIn) {
            Button(onClick = { onNavSelected("Publish Duty Vacancy") }) { Text("Publish Duty Vacancy") }
            Button(onClick = { onNavSelected("Logout") }) { Text("Logout") }
        }
    }
}

// Ensure you define or adapt LoginScreen, RegisterScreen, DutyVacanciesScreen, and MainContent for compatibility with your app's logic and Material 3 components.


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
fun LoginScreen(onLoginSuccess: (String, String, String) -> Unit) {
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

class DoctorAvailabilitiesViewModel : ViewModel() {
    private val _doctorAvailabilities = MutableStateFlow<List<DoctorAvailability>>(emptyList())
    val doctorAvailabilities: StateFlow<List<DoctorAvailability>> = _doctorAvailabilities

    fun fetchDoctorAvailabilities() = viewModelScope.launch {
        try {
            val response = RetrofitClient.apiService.fetchDoctorAvailabilities()
            if (response.isSuccessful) {
                _doctorAvailabilities.value = response.body() ?: emptyList()
            } else {
                Log.e("DoctorAvailViewModel", "Error fetching availabilities: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("DoctorAvailViewModel", "Exception when fetching availabilities", e)
        }
    }

    fun addDoctorAvailability(date: String, availableHours: String) = viewModelScope.launch {
        try {
            val availabilityRequest = DoctorAvailability(null, date, availableHours)
            val response = RetrofitClient.apiService.addDoctorAvailability(availabilityRequest)
            if (response.isSuccessful) {
                // Handle successful post
                Log.d("DoctorAvailability", "Doctor availability added successfully")
                // Optionally, update your UI or state here
            } else {
                // Handle API error response
                Log.e("DoctorAvailability", "Error adding doctor availability: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            // Handle exceptions
            Log.e("DoctorAvailability", "Exception when adding doctor availability", e)
        }
    }

}

@Composable
fun DoctorAvailabilitiesScreen(viewModel: DoctorAvailabilitiesViewModel) {
    viewModel.fetchDoctorAvailabilities()
    val doctorAvailabilities = viewModel.doctorAvailabilities.collectAsState().value

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Doctor Availabilities", style = MaterialTheme.typography.headlineMedium)
        if (doctorAvailabilities.isEmpty()) {
            Text("No availabilities found.", modifier = Modifier.padding(top = 16.dp))
        } else {
            LazyColumn {
                items(count = doctorAvailabilities.size) { index ->
                    DoctorAvailabilityCard(doctorAvailability = doctorAvailabilities[index])
                }
            }
        }
    }
}

@Composable
fun DoctorAvailabilityCard(doctorAvailability: DoctorAvailability) {
    // Material 3 Card
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Doctor ID
            Text(
                text = "Doctor ID: ${doctorAvailability.doctorId!!.username}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            // Date
            Text(
                text = "Date: ${doctorAvailability.date}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            // Available Hours
            Text(
                text = "Available Hours: ${doctorAvailability.availableHours}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}



@Composable
fun AddDoctorAvailabilityScreen(
    viewModel: DoctorAvailabilitiesViewModel, // Assume this ViewModel handles the logic
    onAddSuccess: () -> Unit // Callback for successful addition
)
{
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {


        var date by remember { mutableStateOf("") }
        var availableHours by remember { mutableStateOf("") }

        val context = LocalContext.current
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Date picker dialog
        fun showDatePicker() {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    date = dateFormat.format(calendar.time)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        Text("Add Availability", style = MaterialTheme.typography.titleMedium)

        // Use a Button for the date selection
        Button(onClick = { showDatePicker() }) {
            Text(text = if (date.isBlank()) "Select Date" else date)
        }

        // Placeholder for Available Hours Input
        OutlinedTextField(
            value = availableHours,
            onValueChange = {availableHours = it},
            label = { Text("Available Hours") },
            modifier = Modifier.fillMaxWidth()
        )

        // Placeholder for Submit Button
        Button(
            onClick = {
                viewModel.addDoctorAvailability(date, availableHours)
                onAddSuccess()

            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Submit")
        }
    }
}

//
//@Composable
//fun AddDoctorAvailabilityScreen(
//    viewModel: DoctorAvailabilitiesViewModel, // Assume this ViewModel handles the logic
//    onAddSuccess: () -> Unit // Callback for successful addition
//) {
//    var date by remember { mutableStateOf("") }
//    var availableHours by remember { mutableStateOf("") }
//    val context = LocalContext.current
//
//    Column(
//        modifier = Modifier
//            .padding(16.dp)
//            .fillMaxWidth(),
//        verticalArrangement = Arrangement.spacedBy(8.dp)
//    ) {
//        Text("Add Availability", style = MaterialTheme.typography.h6)
//
//        // Date Picker
//        OutlinedButton(
//            onClick = { showDatePicker(context, date) { selectedDate -> date = selectedDate } }
//        ) {
//            Text(if (date.isNotEmpty()) date else "Select Date")
//        }
//
//        // Available Hours
//        OutlinedTextField(
//            value = availableHours,
//            onValueChange = { availableHours = it },
//            label = { Text("Available Hours") },
//            singleLine = true
//        )
//
//        // Submit Button
//        Button(
//            onClick = {
//                // Assume viewModel.addDoctorAvailability triggers necessary logic and updates state
//                viewModel.addDoctorAvailability(date, availableHours)
//                // For mockup purposes, we assume success and directly call onAddSuccess
//                onAddSuccess()
//            },
//            modifier = Modifier.align(Alignment.End)
//        ) {
//            Text("Submit")
//        }
//    }
//}
//
fun showDatePicker(
    context: Context,
    initialDate: String,
    onDateSelected: (String) -> Unit
) {
    val calendar = Calendar.getInstance()
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            // Update the `date` state with the new date
            calendar.set(year, month, dayOfMonth)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            onDateSelected(dateFormat.format(calendar.time))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
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
fun performLogin(username: String, password: String, onLoginSuccess: (String, String, String) -> Unit) {
    val loginRequest = LoginRequest(username, password)
    RetrofitClient.apiService.loginUser(loginRequest).enqueue(object : Callback<AdditionalUserInfo> {
        override fun onResponse(call: Call<AdditionalUserInfo>, response: Response<AdditionalUserInfo>) {
            if (response.isSuccessful) {
                // Assuming response.body() is not null, let's use it.
                response.body()?.let { userInfo ->
                    Log.d("LoginSuccess", "Successfully logged in user: $username")
                    // Proceed with login success logic, e.g., updating UI or navigating to another activity
                    onLoginSuccess(username, userInfo.role, userInfo.userId)
                } ?: run {
                    // Handle the case where response is successful but the body is null
                    Log.e("LoginError", "Login was successful but no user info was received")
                }
            } else {
                // Handle unsuccessful response, e.g., wrong credentials
                Log.e(
                    "LoginError",
                    "Failed to log in user. Response code: ${response.code()}, message: ${response.errorBody()?.string() ?: "Unknown error"}"
                )
            }
        }
        override fun onFailure(call: Call<AdditionalUserInfo>, t: Throwable) {
            Log.e("LoginFailure", "Failed to log in user: $username", t)
            // Handle the failure, e.g., show an error message
        }
    })
}


@Composable
fun DutyVacanciesScreen(viewModel: DutyVacanciesViewModel, username: String, userrole: String, userId: String) {
    viewModel.fetchDutyVacancies()
    val dutyVacancies = viewModel.dutyVacancies.collectAsState().value
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Duty Vacancies", style = MaterialTheme.typography.headlineMedium)
        if (dutyVacancies.isEmpty()) {
            Text("No duty vacancies found.", modifier = Modifier.padding(top = 16.dp))
        } else {
            LazyColumn {
                items(count = dutyVacancies.size, itemContent = { index ->
                    val dutyVacancy = dutyVacancies[index]


                    DutyVacancyCard(
                        dutyVacancy = dutyVacancy,
                        userrole = userrole,
                        onAssign = { viewModel.assignDutySlot(
                            dutySlotId = dutyVacancy._id!!, 
                            doctorId = userId, 
                            doctorName = username,
                            date = dutyVacancy.date, 
                            dutyHours = dutyVacancy.dutyHours, 
                            requiredSpecialty = dutyVacancy.requiredSpecialty
                        )
                        },
                        onGiveConsent = { viewModel.giveConsent(dutyVacancy._id!!)},
                        onRevoke = { viewModel.revokeAssignment(dutyVacancy._id!!) }
                    )

                })
            }}
    }
}

class DutyVacanciesViewModel : ViewModel() {
    private val _dutyVacancies = MutableStateFlow<List<DutyVacancy>>(emptyList())
    val dutyVacancies: StateFlow<List<DutyVacancy>> = _dutyVacancies

    init {
        fetchDutyVacancies()
    }

     fun fetchDutyVacancies() = viewModelScope.launch {
        try {
            val response = RetrofitClient.apiService.fetchDutyVacancies()
            if (response.isSuccessful) {
                _dutyVacancies.value = response.body() ?: emptyList()
            } else {
                // Log error or handle error state
                Log.e("DutyVacanciesViewModel", "Error fetching duty vacancies: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            // Handle exceptions from network call
            Log.e("DutyVacanciesViewModel", "Exception when fetching duty vacancies", e)
        }
    }

    fun publishDutyVacancy(date: String, dutyHours: String, requiredSpecialty: String) = viewModelScope.launch {
        try {
            val dutyVacancy = DutyVacancy(
                date = date,
                dutyHours = dutyHours,
                requiredSpecialty = requiredSpecialty,
                _id = null,
                status = DutySlotStatus.from("open"),
                hospitalId = null

            )
            val response = RetrofitClient.apiService.publishDutyVacancy(dutyVacancy)
            if (response.isSuccessful) {
                // Handle successful publish
                Log.d("DutyVacanciesViewModel", "Duty vacancy published successfully")
                // Optionally, refresh the list of vacancies or navigate the user
            } else {
                // Log error or handle error state
                Log.e("DutyVacanciesViewModel", "Error publishing duty vacancy: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            // Handle exceptions
            Log.e("DutyVacanciesViewModel", "Exception when publishing duty vacancy", e)
        }
    }

    private fun updateDutyVacancyStatus(dutySlotId: String, newStatus: DutySlotStatus) {
        // Directly assign a new list to the StateFlow's value
        _dutyVacancies.value = _dutyVacancies.value.map { vacancy ->
            if (vacancy._id == dutySlotId) {
                vacancy.copy(status = newStatus) // Update the status if the ID matches
            } else {
                vacancy // Keep the original item unchanged if the ID does not match
            }
        }
    }


    fun assignDutySlot(dutySlotId: String, doctorId: String, doctorName: String, date: String, dutyHours: String, requiredSpecialty: String) = viewModelScope.launch {
        val data = AssignDutySlotRequest(
            _id = dutySlotId,
            sendingDoctorId = doctorId,
        )
        try {
            Log.d("AssignDutySlotRequest", "Created with slotId: $dutySlotId, sendingDoctorId: $doctorId")

            val response = RetrofitClient.apiService.assignDutySlot(data)
            if (response.isSuccessful) {
                Log.d("DutyVacanciesViewModel", "Duty slot assigned successfully")

                updateDutyVacancyStatus(dutySlotId, DutySlotStatus.PENDING)

                // Handle successful assignment
            } else {
                Log.e("DutyVacanciesViewModel", "Error assigning duty slot: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("DutyVacanciesViewModel", "Exception when assigning duty slot", e)
        }
    }
        
    fun giveConsent(slotId: String) {
        // Implement network request for giving consent
        // Update local data and UI state accordingly
    }
    
    fun revokeAssignment(slotId: String) {
        // Implement network request to revoke assignment
        // Update local data and UI state accordingly
    }
    
}




@Composable
fun DutyVacancyCard(dutyVacancy: DutyVacancy, userrole: String
                    , onAssign: () -> Unit, onGiveConsent: () -> Unit, onRevoke: () -> Unit
)
{
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Hospital: ${dutyVacancy.hospitalId!!.username}", style = MaterialTheme.typography.bodyLarge)
            Text("Date: ${formatDate(dutyVacancy.date)}", style = MaterialTheme.typography.bodyLarge)
            Text("Duty Hours: ${dutyVacancy.dutyHours}", style = MaterialTheme.typography.bodyLarge)
            Text("Required Specialty: ${dutyVacancy.requiredSpecialty}", style = MaterialTheme.typography.bodyLarge)

            // Status display
            Text("Status: ${dutyVacancy.status}", style = MaterialTheme.typography.bodyLarge)


            when (userrole)
            {
                "doctor" ->
                {
                    when (dutyVacancy.status)
                    {
                        DutySlotStatus.OPEN -> Button(onClick = onAssign) { Text("Assign") }
                        DutySlotStatus.PENDING -> Button(onClick = {}, enabled = false) { Text("Waiting for Consent") }
                        DutySlotStatus.FILLED -> Button(onClick = onRevoke) { Text("Revoke") }
                        else -> {} // Handle unexpected status

                    }
                }
                "hospital" ->
                {
                    when (dutyVacancy.status)
                    {
                        DutySlotStatus.OPEN -> Button(onClick = {}, enabled = false) { Text("Waiting") }
                        DutySlotStatus.PENDING -> Button(onClick = onGiveConsent) { Text("Consent") }
                        DutySlotStatus.FILLED -> Button(onClick = {}, enabled = false) { Text("Filled") }
                        else -> {} // Handle unexpected status

                    }
                }
                else -> {} // Handle unexpected
            }



        }
    }
}




@Composable
fun DutyVacancyPublishScreen(
    viewModel: DutyVacanciesViewModel = viewModel(),
    onPublishSuccess: () -> Unit
) {
    var date by remember { mutableStateOf("") }
    var dutyHours by remember { mutableStateOf("") }
    var requiredSpecialty by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Date picker dialog
    fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                date = dateFormat.format(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Column {
        // Use a Button for the date selection
        Button(onClick = { showDatePicker() }) {
            Text(text = if (date.isBlank()) "Select Date" else date)
        }
    
        OutlinedTextField(
            value = dutyHours,
            onValueChange = { dutyHours = it },
            label = { Text("Duty Hours") },
            modifier = Modifier.padding(PaddingValues(all = 8.dp))
        )
        OutlinedTextField(
            value = requiredSpecialty,
            onValueChange = { requiredSpecialty = it },
            label = { Text("Required Specialty") },
            modifier = Modifier.padding(PaddingValues(all = 8.dp))
        )
        Button(
            onClick = {
                coroutineScope.launch {
                    viewModel.publishDutyVacancy(date, dutyHours, requiredSpecialty)
                    onPublishSuccess()
                }
            },
            modifier = Modifier.padding(PaddingValues(all = 8.dp))
        ) {
            Text("Publish")
        }
    }
}

