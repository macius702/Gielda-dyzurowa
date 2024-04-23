package com.example.gieldadyzurowa


import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gieldadyzurowa.network.RetrofitClient
import com.example.gieldadyzurowa.types.AssignDutySlotRequest
import com.example.gieldadyzurowa.types.DoctorAvailability
import com.example.gieldadyzurowa.types.DutySlotActionRequest
import com.example.gieldadyzurowa.types.DutySlotStatus
import com.example.gieldadyzurowa.types.DutyVacancy
import com.example.gieldadyzurowa.types.LoginRequest
import com.example.gieldadyzurowa.types.PublishDutySlotRequest
import com.example.gieldadyzurowa.types.RegistrationRequest
import com.example.gieldadyzurowa.types.Specialty
import com.example.gieldadyzurowa.types.UserroleAndId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.*
import okio.ByteString
import retrofit2.Call
import retrofit2.Callback
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import retrofit2.Response as RetrofitResponse


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


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppContent()
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent() {
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    var isLoggedIn by remember { mutableStateOf(false) }
    var selectedNav by remember { mutableStateOf(LANDING_SCREEN) }
    var username by remember { mutableStateOf("") }
    var userrole by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf("") }
    var showRegistrationSuccessDialog by remember { mutableStateOf(false) }
    val specialtyViewModel = viewModel<SpecialtyViewModel>()
    val dutyVacanciesViewModel = DutyVacanciesViewModel(specialtyViewModel)
    val doctorAvailabilitiesViewModel = viewModel<DoctorAvailabilitiesViewModel>()

    fun fetchAndAssignUserData(user: String) {
        RetrofitClient.apiService.fetchUserData().enqueue(object : Callback<UserroleAndId> {
            override fun onResponse(
                call: Call<UserroleAndId>,
                response: RetrofitResponse<UserroleAndId>
            ) {
                if (response.isSuccessful) {
                    val userInfo = response.body()
                    if (userInfo != null) {
                        userrole = userInfo.role
                        userId = userInfo._id
                        isLoggedIn = true
                        username = user
                        selectedNav = LANDING_SCREEN
                        Log.d("fetchUserData", "Successfully fetched userrole and Id")
                    }
                } else {
                    Log.e("fetchUserData", "Failed to fetch user info: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<UserroleAndId>, t: Throwable) {
                Log.e("fetchUserData", "Failed to fetch user info", t)
            }
        })
    }


    // debug aid debugging
    performLogin("H1", "alamakota",
        onLoginSuccess = { user ->
            fetchAndAssignUserData(user)
        }
    )
    {}

    ModalNavigationDrawer(drawerState = drawerState, drawerContent = {
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
            }, isLoggedIn = isLoggedIn
        )
    }) {
        // Scaffold and your app's content go here
        Scaffold(topBar = {
            TopAppBar(title = {
                Text(
                    text = if (isLoggedIn && username.isNotEmpty()) "Logged in as $username" else "Not Logged In"
                )
            }, navigationIcon = {
                if (drawerState.isClosed) {
                    IconButton(onClick = {
                        coroutineScope.launch { drawerState.open() }
                    }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            })
        }) { padding ->
            Column(modifier = Modifier.padding(padding)) {
                if (showRegistrationSuccessDialog) {
                    AlertDialog(onDismissRequest = { showRegistrationSuccessDialog = false },
                        title = { Text("Registration Successful") },
                        text = { Text("You've successfully registered. Please log in.") },
                        confirmButton = {
                            TextButton(onClick = {
                                showRegistrationSuccessDialog = false
                                selectedNav = "Login"
                            }) { Text("OK") }
                        })
                }

                // The rest of your conditional content logic
                when (selectedNav) {
                    "Login" -> if (!isLoggedIn) {
                        LoginScreen(onLoginSuccess = { user ->
                            fetchAndAssignUserData(user)
                        })
                    }

                    "Register" -> if (!isLoggedIn) {
                        RegisterScreen(dutyVacanciesViewModel,
                            onRegistrationSuccess = {
                                showRegistrationSuccessDialog = true
                            })
                    }

                    "Duty Vacancies" -> DutyVacanciesScreen(
                        viewModel = dutyVacanciesViewModel,
                        userrole = userrole,
                        userId = userId
                    )

                    "Doctor Availabilities" -> DoctorAvailabilitiesScreen(viewModel = doctorAvailabilitiesViewModel)


                    "Publish Duty Vacancy" -> if (isLoggedIn) {
                        // Ensure only logged in users can access the publish screen
                        DutyVacancyPublishScreen(viewModel = dutyVacanciesViewModel,
                            onPublishSuccess = {
                                // Handle what happens after a successful publish
                                // For example, you could navigate back to the Duty Vacancies list
                                selectedNav = "Duty Vacancies"
                            })
                    }

                    "Add Doctor Availability" -> if (isLoggedIn) {
                        val viewModel: DoctorAvailabilitiesViewModel = viewModel()
                        AddDoctorAvailabilityScreen(viewModel) {
                            selectedNav = "Doctor Availabilities"
                        }

                    }

                    else -> Text("Select an option from the navigation.")

                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DrawerContent(onNavSelected: (String) -> Unit, isLoggedIn: Boolean) {
    val drawerItems = listOf("Login", "Register", "Doctor Availabilities")
    val loggedInItems =
        listOf("Add Doctor Availability", "Duty Vacancies", "Publish Duty Vacancy", "Logout")

    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background) // Set background color
    ) {
        items(count = drawerItems.size) { index ->
            ListItem(text = {
                Text(
                    drawerItems[index],
                    style = MaterialTheme.typography.titleLarge
                )
            }, // Increase text size
                modifier = Modifier.clickable { onNavSelected(drawerItems[index]) })
        }

        if (isLoggedIn) {
            items(count = loggedInItems.size) { index ->
                ListItem(text = {
                    Text(
                        loggedInItems[index],
                        style = MaterialTheme.typography.titleLarge
                    )
                }, // Increase text size
                    modifier = Modifier.clickable { onNavSelected(loggedInItems[index]) })
            }
        }
    }
}


@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }


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
                performLogin(username, password, onLoginSuccess) { error ->
                    // Show the error message in a Snackbar
                    showError = true
                    errorMessage = error
                }
            }, modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Login")
        }
        if (showError) {
            Snackbar(modifier = Modifier.padding(16.dp), action = {
                TextButton(onClick = { showError = false }) {
                    Text("Dismiss")
                }
            }) {
                Text(errorMessage)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: DutyVacanciesViewModel,
    onRegistrationSuccess: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    val selectedSpecialty = remember { mutableStateOf(Specialty("", "")) }
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = role.ifEmpty { "Select Role" },
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.menuAnchor(),
                    label = { Text("Role") },
                )
                ExposedDropdownMenu(
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
                            Text(text = selectionOption)
                        }
                    }
                }
            }
        }
        if (showSpecialtyLocalization) {

            SpecialtyDropdownMenu(viewModel.specialtiesViewModel, selectedSpecialty)

            OutlinedTextField(
                value = localization,
                onValueChange = { localization = it },
                label = { Text("Localization") },
                singleLine = true
            )
        }

        var showSnackbar by remember { mutableStateOf(false) }
        var snackbarText by remember { mutableStateOf("") }

        if (showSnackbar) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { showSnackbar = false }) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text(snackbarText)
            }
        }

        Button(
            onClick = {

                if (username.isEmpty() || password.isEmpty() || role.isEmpty()) {
                    snackbarText = "Username, password, and role are mandatory"
                    showSnackbar = true
                } else if (role == "doctor" && (selectedSpecialty.value._id.isEmpty() || localization.isEmpty())) {
                    snackbarText = "Specialty and localization are mandatory for doctors"
                    showSnackbar = true
                } else {

                    performRegistration(
                        username,
                        password,
                        role,
                        selectedSpecialty.value._id,
                        localization,
                        onRegistrationSuccess
                    )
                }

            }, modifier = Modifier.padding(top = 16.dp)
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
                Log.e(
                    "DoctorAvailViewModel",
                    "Error fetching availabilities: ${response.errorBody()?.string()}"
                )
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
                Log.e(
                    "DoctorAvailability",
                    "Error adding doctor availability: ${response.errorBody()?.string()}"
                )
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
) {
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
            onValueChange = { availableHours = it },
            label = { Text("Available Hours") },
            modifier = Modifier.fillMaxWidth()
        )

        // Placeholder for Submit Button
        Button(
            onClick = {
                viewModel.addDoctorAvailability(date, availableHours)
                onAddSuccess()

            }, modifier = Modifier.align(Alignment.End)
        ) {
            Text("Submit")
        }
    }
}

@Composable
fun Footer() {
    BottomAppBar {
        Text("Footer", modifier = Modifier.padding(16.dp))
    }
}

fun performRegistration(
    username: String,
    password: String,
    role: String,
    specialty: String?,
    localization: String?,
    onRegistrationSuccess: () -> Unit
) {
    val registrationRequest = RegistrationRequest(username = username,
        password = password,
        role = role,
        specialty = specialty?.takeIf { it.isNotEmpty() },
        localization = localization?.takeIf { it.isNotEmpty() })

    RetrofitClient.apiService.registerUser(registrationRequest).enqueue(object : Callback<Void> {
        override fun onResponse(call: Call<Void>, response: RetrofitResponse<Void>) {
            if (response.isSuccessful) {
                Log.d("RegistrationSuccess", "Successfully registered user: $username")
                onRegistrationSuccess()
            } else {
                Log.e(
                    "RegistrationError",
                    "Failed to register user: $username. Response code: ${response.code()}"
                )
                // Here you can handle different HTTP codes and give feedback to the user accordingly
            }
        }

        override fun onFailure(call: Call<Void>, t: Throwable) {
            Log.e("RegistrationFailure", "Failed to register user: $username", t)
            // Handle the failure, for example, by showing an error message to the user
        }
    })
}

fun performLogin(
    username: String,
    password: String,
    onLoginSuccess: (String) -> Unit,
    onLoginFailure: (String) -> Unit
) {
    val loginRequest = LoginRequest(username, password)
    RetrofitClient.apiService.loginUser(loginRequest).enqueue(object : Callback<Unit> {
        override fun onResponse(call: Call<Unit>, response: RetrofitResponse<Unit>) {
            if (response.isSuccessful) {
                response.body()?.let { _ ->
                    Log.d("LoginSuccess", "Successfully logged in user: $username")
                    // Proceed with login success logic, e.g., updating UI or navigating to another activity
                    onLoginSuccess(username)
                } ?: run {
                    // Handle the case where response is successful but the body is null
                    Log.e("LoginError", "Login was successful but no user info was received")
                    onLoginFailure("Login was successful but no user info was received")
                }
            } else {
                // Handle unsuccessful response, e.g., wrong credentials
                Log.e(
                    "LoginError",
                    "Failed to log in user. Response code: ${response.code()}, message: ${
                        response.errorBody()?.string() ?: "Unknown error"
                    }"
                )
                onLoginFailure("Invalid username or password.")
            }
        }

        override fun onFailure(call: Call<Unit>, t: Throwable) {
            Log.e("LoginFailure", "Failed to log in user: $username", t)
            // Handle the failure, e.g., show an error message
            onLoginFailure("Failed to log in user: $username")
        }
    })
}


@Composable
fun DutyVacanciesScreen(
    viewModel: DutyVacanciesViewModel,
    userrole: String,
    userId: String
) {
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
                    DutyVacancyCard(dutyVacancy = dutyVacancy,
                        userrole = userrole,
                        userId = userId,
                        onAssign = {
                            viewModel.assignDutySlot(
                                dutySlotId = dutyVacancy._id,
                                doctorId = userId
                            )
                        },
                        onGiveConsent = { viewModel.giveConsent(dutyVacancy._id) },
                        onRevoke = { viewModel.revokeAssignment(dutyVacancy._id) },
                        onRemoveSlot = { viewModel.removeDutySlot(dutyVacancy._id) })

                })
            }
        }
    }
}


class DutyVacanciesViewModel(val specialtiesViewModel: SpecialtyViewModel) : ViewModel() {
    private val _dutyVacancies = MutableStateFlow<List<DutyVacancy>>(emptyList())
    val dutyVacancies: StateFlow<List<DutyVacancy>> = _dutyVacancies
    private lateinit var webSocket: WebSocket

    init {
        fetchDutyVacancies()
        initWebSocket()
    }

    private fun initWebSocket() {
        val client = OkHttpClient()
        val request = Request.Builder().url("ws://10.0.2.2:8080").build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                // Assume the server sends a JSON string that can be directly converted to a DutyVacancy object or list
                // Update your duty vacancies list here based on the message content
                // This is a simplified example; you'll need to parse the JSON and update the list appropriately
                Log.d("WebSocket", "Message received: $text")
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                val jsonString = bytes.utf8()
                Log.d("WebSocket", "JSON message received: $jsonString")
                fetchDutyVacancies()
            }
        })
    }

    override fun onCleared() {
        super.onCleared()
        webSocket.close(1000, null)
    }

    fun fetchDutyVacancies() = viewModelScope.launch {
        try {
            val response = RetrofitClient.apiService.fetchDutyVacancies()
            if (response.isSuccessful) {
                _dutyVacancies.value = response.body() ?: emptyList()
            } else {
                // Log error or handle error state
                Log.e(
                    "DutyVacanciesViewModel",
                    "Error fetching duty vacancies: ${response.errorBody()?.string()}"
                )
            }
        } catch (e: Exception) {
            // Handle exceptions from network call
            Log.e("DutyVacanciesViewModel", "Exception when fetching duty vacancies", e)
        }
    }

    fun publishDutyVacancy(
        startDate: String,
        startTime: String,
        endDate: String,
        endTime: String,
        date: String,
        dutyHours: String,
        requiredSpecialty: MutableState<Specialty>,
        onPublishSuccess: () -> Unit
    ) = viewModelScope.launch {
        try {
            //val requiredSpecialtyId = specialtiesViewModel.specialties.value.find { it.name == requiredSpecialty }?._id ?: ""

            val dutyVacancy = PublishDutySlotRequest(
                date = date,
                dutyHours = dutyHours,
                requiredSpecialty = requiredSpecialty.value,
                startDate = startDate,
                startTime = startTime,
                endDate = endDate,
                endTime = endTime
            )

            val response = RetrofitClient.apiService.publishDutyVacancy(dutyVacancy)
            if (response.isSuccessful) {
                // Handle successful publish
                Log.d("DutyVacanciesViewModel", "Duty vacancy published successfully")
                onPublishSuccess()
                // Optionally, refresh the list of vacancies or navigate the user
            } else {
                // Log error or handle error state
                Log.e(
                    "DutyVacanciesViewModel",
                    "Error publishing duty vacancy: ${response.errorBody()?.string()}"
                )
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

    fun assignDutySlot(
        dutySlotId: String,
        doctorId: String
    ) = viewModelScope.launch {
        val data = AssignDutySlotRequest(
            _id = dutySlotId,
            sendingDoctorId = doctorId,
        )
        try {
            Log.d(
                "AssignDutySlotRequest",
                "Created with slotId: $dutySlotId, sendingDoctorId: $doctorId"
            )

            val response = RetrofitClient.apiService.assignDutySlot(data)
            if (response.isSuccessful) {
                Log.d("DutyVacanciesViewModel", "Duty slot assigned successfully")

                updateDutyVacancyStatus(dutySlotId, DutySlotStatus.PENDING)

            } else {
                Log.e(
                    "DutyVacanciesViewModel",
                    "Error assigning duty slot: ${response.errorBody()?.string()}"
                )
            }
        } catch (e: Exception) {
            Log.e("DutyVacanciesViewModel", "Exception when assigning duty slot", e)
        }
    }

    fun giveConsent(dutySlotId: String) = viewModelScope.launch {
        val request = DutySlotActionRequest(_id = dutySlotId)
        try {
            val response = RetrofitClient.apiService.giveConsent(request)
            if (response.isSuccessful) {
                Log.d("DutyVacanciesViewModel", "Consent given successfully")
                updateDutyVacancyStatus(dutySlotId, DutySlotStatus.FILLED)
            } else {
                Log.e(
                    "DutyVacanciesViewModel",
                    "Error giving consent: ${response.errorBody()?.string()}"
                )
            }
        } catch (e: Exception) {
            Log.e("DutyVacanciesViewModel", "Exception when giving consent", e)
        }
    }

    fun revokeAssignment(dutySlotId: String) = viewModelScope.launch {
        val request = DutySlotActionRequest(_id = dutySlotId)
        try {
            val response = RetrofitClient.apiService.revokeAssignment(request)
            if (response.isSuccessful) {
                Log.d("DutyVacanciesViewModel", "Assignment revoked successfully")
                updateDutyVacancyStatus(dutySlotId, DutySlotStatus.OPEN)
            } else {
                Log.e(
                    "DutyVacanciesViewModel",
                    "Error revoking assignment: ${response.errorBody()?.string()}"
                )
            }
        } catch (e: Exception) {
            Log.e("DutyVacanciesViewModel", "Exception when revoking assignment", e)
        }
    }

    fun removeDutySlot(dutySlotId: String) = viewModelScope.launch {
        val request = DutySlotActionRequest(_id = dutySlotId)
        try {
            val response = RetrofitClient.apiService.removeDutySlot(request)
            if (response.isSuccessful) {
                Log.d("DutyVacanciesViewModel", "Duty slot removed successfully")
                _dutyVacancies.value = _dutyVacancies.value.filter { it._id != dutySlotId }
            } else {
                Log.e(
                    "DutyVacanciesViewModel",
                    "Error removing duty slot: ${response.errorBody()?.string()}"
                )
            }
        } catch (e: Exception) {
            Log.e("DutyVacanciesViewModel", "Exception when removing duty slot", e)
        }
    }
}


@Composable
fun DutyVacancyCard(
    dutyVacancy: DutyVacancy,
    userrole: String,
    userId: String,
    onAssign: () -> Unit,
    onGiveConsent: () -> Unit,
    onRevoke: () -> Unit,
    onRemoveSlot: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Hospital: ${dutyVacancy.hospitalId.username}",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                "Date: ${formatDate(dutyVacancy.date)}",
                style = MaterialTheme.typography.bodyLarge
            )
            Text("Duty Hours: ${dutyVacancy.dutyHours}", style = MaterialTheme.typography.bodyLarge)
            Text(
                "Required Specialty: ${dutyVacancy.requiredSpecialty.name}",
                style = MaterialTheme.typography.bodyLarge
            )
            Text("Status: ${dutyVacancy.status}", style = MaterialTheme.typography.bodyLarge)

            when (userrole) {
                "doctor" -> {
                    when (dutyVacancy.status) {
                        DutySlotStatus.OPEN -> Button(onClick = onAssign) { Text("Assign") }
                        DutySlotStatus.PENDING -> Button(
                            onClick = {},
                            enabled = false
                        ) { Text("Waiting for Consent") }

                        DutySlotStatus.FILLED -> Button(onClick = onRevoke) { Text("Revoke") }


                    }
                }

                "hospital" -> {
                    when (dutyVacancy.status) {
                        DutySlotStatus.OPEN -> {
                            Button(onClick = {}, enabled = false) {
                                Text("Waiting")
                            }

                            // Add a Remove button if the user is the hospital owner of the slot
                            if (userId == dutyVacancy.hospitalId._id) {
                                Button(onClick = onRemoveSlot) {
                                    Text("Remove")
                                }
                            }
                        }

                        DutySlotStatus.PENDING -> Button(onClick = onGiveConsent) { Text("Consent") }
                        DutySlotStatus.FILLED -> Button(
                            onClick = {},
                            enabled = false
                        ) { Text("Filled") }


                    }
                }

                else -> {} // Handle unexpected
            }


        }
    }
}


@Composable
fun DutyVacancyPublishScreen(
    viewModel: DutyVacanciesViewModel = viewModel(), onPublishSuccess: () -> Unit
) {
    var date by remember { mutableStateOf("2024-04-30") }
    var dutyHours by remember { mutableStateOf("16-08") }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val selectedSpecialty = remember { mutableStateOf(Specialty("", "")) }

    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_YEAR, 1) // Tomorrow
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val startDate = remember { mutableStateOf(dateFormat.format(calendar.time)) }
    val startTime = remember { mutableStateOf( "") }
    val endDate = remember { mutableStateOf("") }
    val endTime = remember { mutableStateOf("") }

    // React to startDate change
    LaunchedEffect(key1 = startDate.value) {
        Log.d("DutyVacancyPublishScreen", "LaunchedEffect block entered with startDateString: ${startDate.value}")

        val f = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val startDate = f.parse(startDate.value)
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1 (Sunday) to 7 (Saturday)

        // If it's Saturday or Sunday, start at 08:00, otherwise start at 16:00
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            // If it's Saturday or Sunday
            startTime.value = "08:00"
        } else {
            // If it's a weekday
            startTime.value = "16:00"
        }

        calendar.add(Calendar.DAY_OF_YEAR, 1) // Add 1 day to get a day after tomorrow's date
        endDate.value = f.format(calendar.time)
        endTime.value = "08:00"
    }

    Column {
        SpecialtyDropdownMenu(viewModel.specialtiesViewModel, selectedSpecialty)


        DateTimeInputField(context, "Start", startDate, startTime)
        DateTimeInputField(context, "End", endDate, endTime)
        
        Button(
            onClick = {
                coroutineScope.launch {
                    viewModel.publishDutyVacancy(
                        startDate.value,
                        startTime.value,
                        endDate.value,
                        endTime.value,
                        date,
                        dutyHours,
                        selectedSpecialty,
                        onPublishSuccess
                    )
                }
            }, modifier = Modifier.padding(PaddingValues(all = 8.dp))
        ) {
            Text("Publish")
        }
    }
}

@Composable
fun DateTimeInputField(context: Context, label: String, date: MutableState<String>, time: MutableState<String>) {
    val calendar = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    TextField(
        value = date.value,
        onValueChange = { date.value = it },
        label = { Text("$label Date") },
        modifier = Modifier.padding(PaddingValues(all = 8.dp))
    )
    Button(onClick = {
        val currentSelectedDate = dateFormat.parse(date.value)
        calendar.time = currentSelectedDate ?: Date()

        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                date.value = dateFormat.format(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }) {
        Text(text = "Select $label Date")
    }

    TextField(
        value = time.value,
        onValueChange = { time.value = it },
        label = { Text("$label Time") },
        modifier = Modifier.padding(PaddingValues(all = 8.dp))
    )
    Button(onClick = {
        val currentSelectedTime = timeFormat.parse(time.value)
        calendar.time = currentSelectedTime ?: Date()

        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                time.value = timeFormat.format(calendar.time)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }) {
        Text(text = "Select $label Time")
    }
}

// https://alexzh.com/jetpack-compose-dropdownmenu/ for ExposedDropdownMenuBox
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpecialtyDropdownMenu(
    specialtiesViewModel: SpecialtyViewModel,
    selectedSpecialty: MutableState<Specialty>
) {
    var expanded by remember { mutableStateOf(false) }
    val specialties by specialtiesViewModel.specialties.collectAsState()
    val context = LocalContext.current
    var selectedText by remember { mutableStateOf("") }


    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            }
        ) {
            OutlinedTextField(
                value = selectedText,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(),
                placeholder = { if (selectedText.isEmpty()) Text("Enter specialty") },
                label = { Text("Specialty") },
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                specialties.forEach { specialty ->
                    DropdownMenuItem(
                        text = { Text(text = specialty.name) },
                        onClick = {
                            selectedSpecialty.value = specialty
                            selectedText = specialty.name
                            expanded = false
                            Toast.makeText(context, specialty.name, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}
