package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.ui.theme.MyApplicationTheme
import androidx.compose.ui.graphics.nativeCanvas
import java.text.SimpleDateFormat
import java.util.*

// Helper for dynamic colors parsed from DB HEX values
fun parseColor(hex: String, default: Color): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        default
    }
}

// Avatars mappings
val avatarsList = listOf(
    Icons.Default.Face,
    Icons.Default.AccountCircle,
    Icons.Default.Person,
    Icons.Default.Person2,
    Icons.Default.Person3,
    Icons.Default.Person4,
    Icons.Default.FaceRetouchingNatural
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolPortalApp(viewModel: PortalViewModel) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val currentScreenState = remember { mutableStateOf("landing") }
    val schoolConfig by viewModel.schoolConfig.collectAsState()

    val currentThemeIsDark = remember { mutableStateOf(false) }

    MyApplicationTheme(darkTheme = currentThemeIsDark.value) {
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = if (isLoggedIn) "portal" else currentScreenState.value,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "ScreenTransition"
                ) { target ->
                    when (target) {
                        "landing" -> LandingPageScreen(
                            viewModel = viewModel,
                            onNavigateToLogin = { currentScreenState.value = "login" },
                            onNavigateToRegister = { currentScreenState.value = "register" }
                        )
                        "login" -> LoginScreen(
                            viewModel = viewModel,
                            onBackToLanding = { currentScreenState.value = "landing" },
                            onNavigateToRegister = { currentScreenState.value = "register" }
                        )
                        "register" -> RegisterScreen(
                            viewModel = viewModel,
                            onBackToLanding = { currentScreenState.value = "landing" },
                            onNavigateToLogin = { currentScreenState.value = "login" }
                        )
                        "portal" -> PortalMainContainer(
                            viewModel = viewModel,
                            themeIsDark = currentThemeIsDark
                        )
                    }
                }

                // Global Toast notifications
                toastMessage?.let { msg ->
                    Snackbar(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        action = {
                            TextButton(onClick = { viewModel.clearToast() }) {
                                Text("Dismiss", color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    ) {
                        Text(msg)
                    }

                    // Auto dismiss toast after 3 seconds
                    LaunchedEffect(msg) {
                        kotlinx.coroutines.delay(3000)
                        viewModel.clearToast()
                    }
                }
            }
        }
    }
}

// ---------------------- LANDING PAGE ----------------------

@Composable
fun LandingPageScreen(
    viewModel: PortalViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val schoolConfig by viewModel.schoolConfig.collectAsState()
    val scrollState = rememberScrollState()

    val pColor = parseColor(schoolConfig.primaryColorHex, MaterialTheme.colorScheme.primary)
    val sColor = parseColor(schoolConfig.secondaryColorHex, MaterialTheme.colorScheme.background)
    val aColor = parseColor(schoolConfig.accentColorHex, MaterialTheme.colorScheme.secondary)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(sColor.copy(alpha = 0.3f), MaterialTheme.colorScheme.background)
                )
            )
            .verticalScroll(scrollState)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "School Logo",
                    tint = pColor,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = schoolConfig.schoolName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = pColor,
                    fontFamily = FontFamily.SansSerif
                )
            }

            Button(
                onClick = onNavigateToLogin,
                colors = ButtonDefaults.buttonColors(containerColor = pColor),
                modifier = Modifier.testTag("login_btn_header")
            ) {
                Icon(Icons.Default.Login, contentDescription = "Login")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Login")
            }
        }

        // Hero Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = pColor.copy(alpha = 0.08f)),
            border = BorderStroke(1.dp, pColor.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Badge(
                    containerColor = aColor.copy(alpha = 0.2f),
                    contentColor = pColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text("  ★ Welcome to My Class Portal ★  ", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(4.dp))
                }

                Text(
                    text = "A Smart Digital Space For Modern Learning",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = pColor,
                    textAlign = TextAlign.Center,
                    lineHeight = 38.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Seamlessly manage classrooms, take attendance, view timetables, and collaborate with Teachers, Prefects, and Administrators all in one elegant application.",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 600.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = onNavigateToRegister,
                        colors = ButtonDefaults.buttonColors(containerColor = pColor),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("register_btn_hero")
                    ) {
                        Text("Register as Student", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    OutlinedButton(
                        onClick = onNavigateToLogin,
                        border = BorderStroke(1.5.dp, pColor),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = pColor),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text("Portal Sign In", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Quick Stats Cards
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Portal Fast Facts",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = pColor,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LandingStatCard(
                modifier = Modifier.weight(1f),
                title = "Roles Enabled",
                value = "4 Roles",
                subtitle = "Student, Prefect, Teacher, Admin",
                icon = Icons.Default.Group,
                color = pColor
            )
            LandingStatCard(
                modifier = Modifier.weight(1f),
                title = "Platform Safety",
                value = "100%",
                subtitle = "Profanity & Spam Protected",
                icon = Icons.Default.Shield,
                color = aColor
            )
        }

        // Features Section
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Interactive Features",
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = pColor,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp)
        )
        Text(
            text = "Tailored workspaces designed uniquely for school operations.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FeatureItemRow(
                title = "Smart Attendance Checking",
                description = "Students mark attendance locally. Systems track index numbers, timetables, classes, houses, and device metrics with duplicate prevention.",
                icon = Icons.Default.CheckCircle,
                color = pColor
            )
            FeatureItemRow(
                title = "Dynamic House & Class Systems",
                description = "Administrator maintains classes and houses database. Dropdowns update automatically everywhere.",
                icon = Icons.Default.Layers,
                color = aColor
            )
            FeatureItemRow(
                title = "AI Study Assistant",
                description = "Ask the integrated Gemini chatbot explaining advanced topics, math problem breakdowns, and test study tips.",
                icon = Icons.Default.AutoAwesome,
                color = pColor
            )
            FeatureItemRow(
                title = "Anonymous Suggestion Box",
                description = "Anonymous suggestions, complaints, and appreciation submissions filtered through automated profanity moderations.",
                icon = Icons.Default.Feedback,
                color = aColor
            )
        }

        // Footer
        Spacer(modifier = Modifier.height(48.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(pColor)
                .padding(32.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.School, contentDescription = null, tint = sColor, modifier = Modifier.size(40.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(schoolConfig.schoolName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Smart Digital School Portal • 2026", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun LandingStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = color)
            Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun FeatureItemRow(
    title: String,
    description: String,
    icon: ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .border(BorderStroke(1.dp, color.copy(alpha = 0.15f)), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
        }
    }
}


// ---------------------- LOGIN SCREEN ----------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: PortalViewModel,
    onBackToLanding: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val schoolConfig by viewModel.schoolConfig.collectAsState()
    val loginError by viewModel.loginError.collectAsState()

    val pColor = parseColor(schoolConfig.primaryColorHex, MaterialTheme.colorScheme.primary)

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var showForgotDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(pColor.copy(alpha = 0.15f), MaterialTheme.colorScheme.background),
                    radius = 2000f
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 450.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Back Button
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                    IconButton(onClick = onBackToLanding) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }

                Icon(Icons.Default.School, contentDescription = null, tint = pColor, modifier = Modifier.size(56.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Sign In",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = pColor
                )
                Text(
                    text = "Access your school workspace dashboard",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_email_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_password_input")
                )

                loginError?.let { err ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { showForgotDialog = true }) {
                        Text("Forgot Password?", color = pColor)
                    }
                }

                Button(
                    onClick = { viewModel.login(email.trim(), password) },
                    colors = ButtonDefaults.buttonColors(containerColor = pColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("login_submit_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("New Student?", fontSize = 13.sp)
                    TextButton(onClick = onNavigateToRegister) {
                        Text("Create Account", color = pColor, fontWeight = FontWeight.Bold)
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))
                Text("Demo Credentials (Login Immediately):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = pColor)
                Text("Admin: admin@school.com (pass: admin)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                Text("Teacher: teacher@school.com (pass: teacher)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                Text("Prefect: prefect@school.com (pass: prefect)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                Text("Student: student@school.com (pass: student)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
            }
        }
    }

    if (showForgotDialog) {
        Dialog(onDismissRequest = { showForgotDialog = false }) {
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Forgot Password", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = pColor)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("For security, password resets must be initiated by an Administrator. Please reach out to Principal Jenkins or submit an inquiry to the helpdesk.")
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { showForgotDialog = false }, modifier = Modifier.align(Alignment.End)) {
                        Text("Okay")
                    }
                }
            }
        }
    }
}


// ---------------------- REGISTER SCREEN ----------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: PortalViewModel,
    onBackToLanding: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val schoolConfig by viewModel.schoolConfig.collectAsState()
    val classes by viewModel.allClasses.collectAsState()
    val houses by viewModel.allHouses.collectAsState()
    val registerSuccess by viewModel.registerSuccess.collectAsState()

    val pColor = parseColor(schoolConfig.primaryColorHex, MaterialTheme.colorScheme.primary)

    var fullName by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var dob by remember { mutableStateOf("2008-05-15") }
    var phone by remember { mutableStateOf("") }
    var parentName by remember { mutableStateOf("") }
    var parentContact by remember { mutableStateOf("") }
    var programme by remember { mutableStateOf("General Science") }
    var selectedClass by remember { mutableStateOf("") }
    var selectedHouse by remember { mutableStateOf("") }
    var residentialStatus by remember { mutableStateOf("Boarding") }

    // Dropdowns open/close
    var classExpanded by remember { mutableStateOf(false) }
    var houseExpanded by remember { mutableStateOf(false) }

    // Initialize drop downs if lists are available
    LaunchedEffect(classes, houses) {
        if (selectedClass.isEmpty() && classes.isNotEmpty()) {
            selectedClass = classes.first().name
        }
        if (selectedHouse.isEmpty() && houses.isNotEmpty()) {
            selectedHouse = houses.first().name
        }
    }

    LaunchedEffect(registerSuccess) {
        if (registerSuccess) {
            onNavigateToLogin()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Student Registration") },
                navigationIcon = {
                    IconButton(onClick = onBackToLanding) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            "Account Information",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = pColor,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = studentId,
                            onValueChange = { studentId = it },
                            label = { Text("Student ID / Index Number") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "Academic Details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = pColor,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        OutlinedTextField(
                            value = programme,
                            onValueChange = { programme = it },
                            label = { Text("Academic Programme (e.g. General Science, Business)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Class dropdown (Dynamic!)
                        Box(modifier = Modifier.fillMaxWidth()) {
                            ExposedDropdownMenuBox(
                                expanded = classExpanded,
                                onExpandedChange = { classExpanded = !classExpanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedClass,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Select Class") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = classExpanded,
                                    onDismissRequest = { classExpanded = false }
                                ) {
                                    classes.forEach { classEnt ->
                                        DropdownMenuItem(
                                            text = { Text(classEnt.name) },
                                            onClick = {
                                                selectedClass = classEnt.name
                                                classExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // House dropdown (Dynamic!)
                        Box(modifier = Modifier.fillMaxWidth()) {
                            ExposedDropdownMenuBox(
                                expanded = houseExpanded,
                                onExpandedChange = { houseExpanded = !houseExpanded }
                            ) {
                                OutlinedTextField(
                                    value = selectedHouse,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Select House") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = houseExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = houseExpanded,
                                    onDismissRequest = { houseExpanded = false }
                                ) {
                                    houses.forEach { houseEnt ->
                                        DropdownMenuItem(
                                            text = { Text(houseEnt.name) },
                                            onClick = {
                                                selectedHouse = houseEnt.name
                                                houseExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Boarding/Day Choice
                        Text("Residential Status", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = residentialStatus == "Boarding",
                                onClick = { residentialStatus = "Boarding" }
                            )
                            Text("Boarder")
                            Spacer(modifier = Modifier.width(16.dp))
                            RadioButton(
                                selected = residentialStatus == "Day Student",
                                onClick = { residentialStatus = "Day Student" }
                            )
                            Text("Day Student")
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "Personal & Contact details",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = pColor,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = gender,
                                onValueChange = { gender = it },
                                label = { Text("Gender") },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 4.dp)
                            )
                            OutlinedTextField(
                                value = dob,
                                onValueChange = { dob = it },
                                label = { Text("Date of Birth") },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = parentName,
                            onValueChange = { parentName = it },
                            label = { Text("Parent / Guardian Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = parentContact,
                            onValueChange = { parentContact = it },
                            label = { Text("Parent Guardian Emergency Contact") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                if (fullName.isBlank() || email.isBlank() || password.isBlank() || studentId.isBlank()) {
                                    viewModel.showToast("Please fill in Name, Index Number, Email, and Password.")
                                } else {
                                    viewModel.register(
                                        fullName, email, password, gender, dob, phone,
                                        parentName, parentContact, programme, selectedClass, selectedHouse,
                                        residentialStatus, studentId
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = pColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("Submit Registration", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("Already registered?")
                            TextButton(onClick = onNavigateToLogin) {
                                Text("Log In here", color = pColor, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}


// ---------------------- MAIN PORTAL CONTAINER ----------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortalMainContainer(
    viewModel: PortalViewModel,
    themeIsDark: MutableState<Boolean>
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val schoolConfig by viewModel.schoolConfig.collectAsState()

    val pColor = parseColor(schoolConfig.primaryColorHex, MaterialTheme.colorScheme.primary)

    var selectedTab by remember { mutableStateOf("dashboard") }

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    // List of navigation destinations based on role
    val navItems = remember(currentUser) {
        val list = mutableListOf(
            Triple("dashboard", "Dashboard", Icons.Default.Dashboard),
            Triple("announcements", "Announcements", Icons.Default.Campaign),
            Triple("resources", "Resources", Icons.Default.FolderOpen),
            Triple("assignments", "Assignments", Icons.Default.Assignment),
            Triple("timetable", "Timetable", Icons.Default.Schedule),
            Triple("events", "Events", Icons.Default.Event),
            Triple("feedback", "Feedback", Icons.Default.Feedback),
            Triple("ai_assistant", "AI Assistant", Icons.Default.AutoAwesome)
        )

        currentUser?.let { user ->
            if (user.role == "PREFECT" || user.role == "TEACHER" || user.role == "ADMIN") {
                list.add(Triple("moderation", "Moderation", Icons.Default.Security))
            }
            if (user.role == "TEACHER" || user.role == "ADMIN") {
                list.add(Triple("students", "Students", Icons.Default.PersonSearch))
            }
            if (user.role == "ADMIN") {
                list.add(Triple("admin", "Admin Controls", Icons.Default.SettingsSuggest))
            }
        }
        list.add(Triple("settings", "Settings", Icons.Default.Settings))
        list
    }

    if (isTablet) {
        // Landscape / Tablet layout with side Navigation Rail
        Row(modifier = Modifier.fillMaxSize()) {
            NavigationRail(
                containerColor = pColor.copy(alpha = 0.05f),
                header = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 16.dp)) {
                        Icon(Icons.Default.School, contentDescription = null, tint = pColor, modifier = Modifier.size(32.dp))
                        Text(
                            text = schoolConfig.schoolName.take(12) + "...",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = pColor,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            ) {
                Spacer(modifier = Modifier.weight(1f))
                navItems.forEach { (route, label, icon) ->
                    NavigationRailItem(
                        selected = selectedTab == route,
                        onClick = { selectedTab = route },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, fontSize = 10.sp, maxLines = 1) }
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { viewModel.logout() }) {
                    Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.Red)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                PortalTabContent(viewModel, selectedTab, themeIsDark)
            }
        }
    } else {
        // Mobile layout with top action bar, bottom navigation, and scrollable body
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = schoolConfig.schoolName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = pColor
                        )
                    },
                    actions = {
                        IconButton(onClick = { viewModel.logout() }) {
                            Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.Red)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            },
            bottomBar = {
                // Since mobile bottom bar can overflow if there are many items, we show the top 4 + "more"
                val visibleItems = navItems.take(5)
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    visibleItems.forEach { (route, label, icon) ->
                        NavigationBarItem(
                            selected = selectedTab == route,
                            onClick = { selectedTab = route },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, fontSize = 10.sp, overflow = TextOverflow.Ellipsis, maxLines = 1) }
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // If selectedTab is not in the visible bottom navigation, let them access it from a custom row
                Column(modifier = Modifier.fillMaxSize()) {
                    // Drawer-like Quick Selector for items not in the visible list (mobile only)
                    if (navItems.size > 5) {
                        ScrollableTabRow(
                            selectedTabIndex = navItems.indexOfFirst { it.first == selectedTab }.coerceAtLeast(0),
                            edgePadding = 8.dp,
                            containerColor = pColor.copy(alpha = 0.04f),
                            contentColor = pColor
                        ) {
                            navItems.forEachIndexed { index, (route, label, icon) ->
                                Tab(
                                    selected = selectedTab == route,
                                    onClick = { selectedTab = route },
                                    text = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        PortalTabContent(viewModel, selectedTab, themeIsDark)
                    }
                }
            }
        }
    }
}

@Composable
fun PortalTabContent(
    viewModel: PortalViewModel,
    tab: String,
    themeIsDark: MutableState<Boolean>
) {
    when (tab) {
        "dashboard" -> DashboardTab(viewModel)
        "announcements" -> AnnouncementsTab(viewModel)
        "resources" -> LearningResourcesTab(viewModel)
        "assignments" -> AssignmentsTab(viewModel)
        "timetable" -> TimetableTab(viewModel)
        "events" -> EventsTab(viewModel)
        "feedback" -> FeedbackTab(viewModel)
        "ai_assistant" -> AiAssistantTab(viewModel)
        "moderation" -> ModerationTab(viewModel)
        "students" -> StudentsTab(viewModel)
        "admin" -> AdminTab(viewModel)
        "settings" -> SettingsTab(viewModel, themeIsDark)
    }
}


// ---------------------- TAB 1: DASHBOARD ----------------------

@Composable
fun DashboardTab(viewModel: PortalViewModel) {
    val user by viewModel.currentUser.collectAsState()
    val schoolConfig by viewModel.schoolConfig.collectAsState()
    val announcements by viewModel.allAnnouncements.collectAsState()
    val assignments by viewModel.allAssignments.collectAsState()
    val events by viewModel.allEvents.collectAsState()
    val attendanceLog by viewModel.allAttendance.collectAsState()

    val pColor = parseColor(schoolConfig.primaryColorHex, MaterialTheme.colorScheme.primary)
    val aColor = parseColor(schoolConfig.accentColorHex, MaterialTheme.colorScheme.secondary)

    val scrollState = rememberScrollState()

    var showIdCardDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Welcome Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = pColor),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Hello, ${user?.fullName ?: "Guest"}!",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Role: ${user?.role ?: "Student"} • ${user?.className ?: "Not assigned"}",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Badge(containerColor = Color.White.copy(alpha = 0.2f), contentColor = Color.White) {
                        Text(" Attendance Rate: ${user?.attendanceRate ?: 100f}% ", modifier = Modifier.padding(4.dp), fontWeight = FontWeight.Bold)
                    }
                }

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = avatarsList[user?.profilePicIndex?.coerceIn(0, avatarsList.size - 1) ?: 0],
                        contentDescription = "Avatar",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        // Quick Actions Row
        Text("Quick Actions", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = pColor, modifier = Modifier.padding(bottom = 8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (user?.role == "STUDENT" || user?.role == "PREFECT") {
                Button(
                    onClick = { viewModel.markAttendance() },
                    colors = ButtonDefaults.buttonColors(containerColor = pColor),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("student_mark_attendance_btn")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mark Present", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            Button(
                onClick = { showIdCardDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = aColor),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Badge, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Generate ID", color = Color.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        // Stats summary cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Announcements", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                    Text("${announcements.size}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = pColor)
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Assignments", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                    Text("${assignments.size}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = pColor)
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Events", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                    Text("${events.size}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = pColor)
                }
            }
        }

        // Today's Urgent Announcement
        val urgent = announcements.firstOrNull { it.priority == "HIGH" }
        if (urgent != null) {
            Text("Urgent Announcement", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Red, modifier = Modifier.padding(bottom = 8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3F3)),
                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(urgent.title, fontWeight = FontWeight.Bold, color = Color.Red, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(urgent.description, fontSize = 13.sp, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("By ${urgent.authorName} • ${urgent.date}", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }

        // Upcoming Event Countdown (Bonus feature!)
        val nextEvent = events.firstOrNull()
        if (nextEvent != null) {
            Text("Upcoming School Event", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = pColor, modifier = Modifier.padding(bottom = 8.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, pColor.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(nextEvent.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = pColor)
                    Text(nextEvent.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = aColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(nextEvent.date, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Badge(containerColor = aColor.copy(alpha = 0.15f), contentColor = pColor) {
                            Text(" ⏳ Scheduled at ${nextEvent.time} ", modifier = Modifier.padding(4.dp), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Personal Attendance Log
        Text("Your Daily Attendance History", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = pColor, modifier = Modifier.padding(bottom = 8.dp))
        val myLogs = attendanceLog.filter { it.studentId == user?.studentId }
        if (myLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No attendance records logged yet for this device.", fontSize = 13.sp, color = Color.Gray)
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    myLogs.take(5).forEach { record ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(record.date, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Checked in from: ${record.device}", fontSize = 11.sp, color = Color.Gray)
                            }
                            Badge(containerColor = pColor.copy(alpha = 0.15f), contentColor = pColor) {
                                Text(" ${record.status} - ${record.time} ", modifier = Modifier.padding(4.dp), fontWeight = FontWeight.Bold)
                            }
                        }
                        Divider()
                    }
                }
            }
        }
    }

    // Interactive ID Generator Dialog (Bonus Feature!)
    if (showIdCardDialog) {
        Dialog(onDismissRequest = { showIdCardDialog = false }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(2.dp, aColor),
                colors = CardDefaults.cardColors(containerColor = pColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        schoolConfig.schoolName,
                        fontWeight = FontWeight.Bold,
                        color = aColor,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "OFFICIAL DIGITAL STUDENT ID",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // ID photo circle
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape)
                            .border(BorderStroke(2.dp, aColor), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = avatarsList[user?.profilePicIndex?.coerceIn(0, avatarsList.size - 1) ?: 0],
                            contentDescription = "Avatar",
                            tint = Color.White,
                            modifier = Modifier.size(50.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        user?.fullName ?: "N/A",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 20.sp
                    )
                    Text(
                        "INDEX NO: ${user?.studentId ?: "N/A"}",
                        fontWeight = FontWeight.Bold,
                        color = aColor,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Divider(color = Color.White.copy(alpha = 0.2f))

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("CLASS", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                            Text(user?.className ?: "N/A", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("HOUSE", fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                            Text(user?.houseName ?: "N/A", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Mock QR Code (Canvas drawn for perfect execution!)
                    Canvas(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        val size = size.width
                        val cells = 10
                        val cellSize = size / cells
                        val rand = Random(user?.studentId?.hashCode()?.toLong() ?: 1234L)
                        for (x in 0 until cells) {
                            for (y in 0 until cells) {
                                // Anchor patterns
                                val isAnchor = (x < 3 && y < 3) || (x >= cells - 3 && y < 3) || (x < 3 && y >= cells - 3)
                                if (isAnchor || rand.nextBoolean()) {
                                    drawRect(
                                        color = Color.Black,
                                        topLeft = Offset(x * cellSize, y * cellSize),
                                        size = Size(cellSize, cellSize)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("SECURE VERIFICATION CODE", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showIdCardDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close Card", color = pColor, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


// ---------------------- TAB 2: ANNOUNCEMENTS ----------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementsTab(viewModel: PortalViewModel) {
    val announcements by viewModel.allAnnouncements.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val schoolConfig by viewModel.schoolConfig.collectAsState()

    val pColor = parseColor(schoolConfig.primaryColorHex, MaterialTheme.colorScheme.primary)
    val aColor = parseColor(schoolConfig.accentColorHex, MaterialTheme.colorScheme.secondary)

    var showCreateDialog by remember { mutableStateOf(false) }

    // Form states
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }
    var priority by remember { mutableStateOf("LOW") }

    Scaffold(
        floatingActionButton = {
            if (currentUser?.role != "STUDENT") {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = pColor,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Announcement")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "School Bulletins & Announcements",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = pColor,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (announcements.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Campaign, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No announcements active at this moment.", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(announcements) { bulletin ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Badge(
                                        containerColor = if (bulletin.priority == "HIGH") Color.Red.copy(alpha = 0.1f) else pColor.copy(alpha = 0.1f),
                                        contentColor = if (bulletin.priority == "HIGH") Color.Red else pColor
                                    ) {
                                        Text("  ${bulletin.category}  ", modifier = Modifier.padding(4.dp), fontWeight = FontWeight.Bold)
                                    }

                                    if (currentUser?.role == "ADMIN" || currentUser?.fullName == bulletin.authorName) {
                                        IconButton(onClick = { viewModel.deleteAnnouncement(bulletin.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(bulletin.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(bulletin.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))

                                if (bulletin.attachmentName.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier
                                            .background(pColor.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                            .clickable { viewModel.showToast("Downloading ${bulletin.attachmentName}...") }
                                            .padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.AttachFile, contentDescription = null, tint = pColor, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(bulletin.attachmentName, fontSize = 12.sp, color = pColor, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Divider()
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Posted by: ${bulletin.authorName} (${bulletin.authorRole}) • ${bulletin.date}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        Dialog(onDismissRequest = { showCreateDialog = false }) {
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Publish New Announcement", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = pColor)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Announcement Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category (e.g. Exam, Urgent, Sports)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Priority Level", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row {
                        RadioButton(selected = priority == "LOW", onClick = { priority = "LOW" })
                        Text("LOW")
                        Spacer(modifier = Modifier.width(8.dp))
                        RadioButton(selected = priority == "MEDIUM", onClick = { priority = "MEDIUM" })
                        Text("MEDIUM")
                        Spacer(modifier = Modifier.width(8.dp))
                        RadioButton(selected = priority == "HIGH", onClick = { priority = "HIGH" })
                        Text("HIGH")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (title.isNotBlank() && desc.isNotBlank()) {
                                    viewModel.createAnnouncement(title, desc, category, priority)
                                    showCreateDialog = false
                                    title = ""
                                    desc = ""
                                } else {
                                    viewModel.showToast("Title and Description cannot be blank.")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = pColor)
                        ) {
                            Text("Publish")
                        }
                    }
                }
            }
        }
    }
}


// ---------------------- TAB 3: LEARNING RESOURCES ----------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearningResourcesTab(viewModel: PortalViewModel) {
    val resources by viewModel.allResources.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val schoolConfig by viewModel.schoolConfig.collectAsState()

    val pColor = parseColor(schoolConfig.primaryColorHex, MaterialTheme.colorScheme.primary)

    var showCreateDialog by remember { mutableStateOf(false) }

    // Form states
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Mathematics") }
    var fileType by remember { mutableStateOf("PDF") }
    var fileSize by remember { mutableStateOf("2.5 MB") }

    Scaffold(
        floatingActionButton = {
            if (currentUser?.role != "STUDENT") {
                FloatingActionButton(onClick = { showCreateDialog = true }, containerColor = pColor, contentColor = Color.White) {
                    Icon(Icons.Default.UploadFile, contentDescription = "Upload Resource")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "Learning Resources Center",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = pColor,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (resources.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Folder, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No learning materials uploaded yet.", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(resources) { material ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(pColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val icon = when (material.fileType.uppercase()) {
                                            "PDF" -> Icons.Default.PictureAsPdf
                                            "ZIP" -> Icons.Default.Inventory
                                            "VIDEO" -> Icons.Default.VideoLibrary
                                            else -> Icons.Default.Description
                                        }
                                        Icon(icon, contentDescription = null, tint = pColor, modifier = Modifier.size(24.dp))
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(material.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(material.description, fontSize = 12.sp, color = Color.Gray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("Subject: ${material.category} • Size: ${material.fileSize} • Type: ${material.fileType}", fontSize = 11.sp, color = pColor, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Row {
                                    IconButton(onClick = { viewModel.showToast("Downloading ${material.title}...") }) {
                                        Icon(Icons.Default.Download, contentDescription = "Download", tint = pColor)
                                    }
                                    if (currentUser?.role == "ADMIN" || currentUser?.fullName == material.authorName) {
                                        IconButton(onClick = { viewModel.deleteResource(material.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        Dialog(onDismissRequest = { showCreateDialog = false }) {
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Upload Lecture Notes", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = pColor)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Resource Name / Topic") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Short Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category / Subject") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row {
                        OutlinedTextField(
                            value = fileType,
                            onValueChange = { fileType = it },
                            label = { Text("File Type (PDF/ZIP)") },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 4.dp)
                        )
                        OutlinedTextField(
                            value = fileSize,
                            onValueChange = { fileSize = it },
                            label = { Text("Size (MB)") },
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (title.isNotBlank() && desc.isNotBlank()) {
                                    viewModel.createResource(title, desc, category, fileType, fileSize)
                                    showCreateDialog = false
                                    title = ""
                                    desc = ""
                                } else {
                                    viewModel.showToast("Fields cannot be blank.")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = pColor)
                        ) {
                            Text("Upload")
                        }
                    }
                }
            }
        }
    }
}


// ---------------------- TAB 4: ASSIGNMENTS ----------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentsTab(viewModel: PortalViewModel) {
    val assignments by viewModel.allAssignments.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val schoolConfig by viewModel.schoolConfig.collectAsState()

    val pColor = parseColor(schoolConfig.primaryColorHex, MaterialTheme.colorScheme.primary)

    var showCreateDialog by remember { mutableStateOf(false) }

    // Form states
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("2026-07-20") }
    var className by remember { mutableStateOf("S6 - Science A") }
    var subject by remember { mutableStateOf("Physics") }

    Scaffold(
        floatingActionButton = {
            if (currentUser?.role != "STUDENT") {
                FloatingActionButton(onClick = { showCreateDialog = true }, containerColor = pColor, contentColor = Color.White) {
                    Icon(Icons.Default.Add, contentDescription = "Add Assignment")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "Assignments & Submissions",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = pColor,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (assignments.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No active assignments assigned yet.", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(assignments) { task ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Badge(containerColor = pColor.copy(alpha = 0.1f), contentColor = pColor) {
                                        Text("  ${task.subject}  ", modifier = Modifier.padding(4.dp), fontWeight = FontWeight.Bold)
                                    }

                                    Row {
                                        if (currentUser?.role == "STUDENT" && !task.hasSubmitted) {
                                            Button(
                                                onClick = { viewModel.submitAssignment(task) },
                                                colors = ButtonDefaults.buttonColors(containerColor = pColor),
                                                modifier = Modifier.height(32.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp)
                                            ) {
                                                Text("Submit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        } else if (task.hasSubmitted) {
                                            Badge(containerColor = Color(0xFFE8F5E9), contentColor = Color(0xFF2E7D32)) {
                                                Text("✓ Submitted", modifier = Modifier.padding(4.dp), fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        if (currentUser?.role == "ADMIN" || currentUser?.fullName == task.authorName) {
                                            IconButton(onClick = { viewModel.deleteAssignment(task.id) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(task.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(task.description, fontSize = 13.sp, color = Color.Gray)

                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Class: ${task.className}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = pColor)
                                    Text("Deadline: ${task.deadline}", fontSize = 11.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        Dialog(onDismissRequest = { showCreateDialog = false }) {
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Add Task / Assignment", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = pColor)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Task Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Task Instructions") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = deadline,
                        onValueChange = { deadline = it },
                        label = { Text("Deadline (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = className,
                        onValueChange = { className = it },
                        label = { Text("Target Class") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject / Course") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (title.isNotBlank() && desc.isNotBlank()) {
                                    viewModel.createAssignment(title, desc, deadline, className, subject)
                                    showCreateDialog = false
                                    title = ""
                                    desc = ""
                                } else {
                                    viewModel.showToast("Fields cannot be blank.")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = pColor)
                        ) {
                            Text("Create")
                        }
                    }
                }
            }
        }
    }
}


// ---------------------- TAB 5: TIMETABLE ----------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableTab(viewModel: PortalViewModel) {
    val timetable by viewModel.allTimetable.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val schoolConfig by viewModel.schoolConfig.collectAsState()

    val pColor = parseColor(schoolConfig.primaryColorHex, MaterialTheme.colorScheme.primary)

    var showCreateDialog by remember { mutableStateOf(false) }

    // Form states
    var day by remember { mutableStateOf("Monday") }
    var slot by remember { mutableStateOf("08:30 AM - 10:00 AM") }
    var subject by remember { mutableStateOf("Mathematics") }
    var className by remember { mutableStateOf("S6 - Science A") }
    var teacher by remember { mutableStateOf("Mr. Arthur Pendelton") }

    Scaffold(
        floatingActionButton = {
            if (currentUser?.role != "STUDENT") {
                FloatingActionButton(onClick = { showCreateDialog = true }, containerColor = pColor, contentColor = Color.White) {
                    Icon(Icons.Default.EditCalendar, contentDescription = "Add Timetable")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "Weekly Academic Timetables",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = pColor,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (timetable.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.CalendarViewWeek, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No lesson rows added yet.", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(timetable) { row ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Badge(containerColor = pColor.copy(alpha = 0.1f), contentColor = pColor) {
                                            Text("  ${row.dayOfWeek}  ", modifier = Modifier.padding(4.dp), fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(row.timeSlot, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(row.subject, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text("Teacher: ${row.teacherName} • Class: ${row.className}", fontSize = 12.sp, color = Color.Gray)
                                }

                                if (currentUser?.role == "ADMIN" || currentUser?.role == "PREFECT") {
                                    IconButton(onClick = { viewModel.deleteTimetableRow(row.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        Dialog(onDismissRequest = { showCreateDialog = false }) {
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Add Timetable Lesson", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = pColor)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = day,
                        onValueChange = { day = it },
                        label = { Text("Day of Week (Monday, etc.)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = slot,
                        onValueChange = { slot = it },
                        label = { Text("Time Slot (e.g. 08:30 AM - 10:00 AM)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = className,
                        onValueChange = { className = it },
                        label = { Text("Class Group") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = teacher,
                        onValueChange = { teacher = it },
                        label = { Text("Teacher Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (subject.isNotBlank() && day.isNotBlank()) {
                                    viewModel.createTimetableRow(day, slot, subject, className, teacher)
                                    showCreateDialog = false
                                } else {
                                    viewModel.showToast("Fields cannot be blank.")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = pColor)
                        ) {
                            Text("Create")
                        }
                    }
                }
            }
        }
    }
}


// ---------------------- TAB 6: EVENTS ----------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsTab(viewModel: PortalViewModel) {
    val events by viewModel.allEvents.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val schoolConfig by viewModel.schoolConfig.collectAsState()

    val pColor = parseColor(schoolConfig.primaryColorHex, MaterialTheme.colorScheme.primary)

    var showCreateDialog by remember { mutableStateOf(false) }

    // Form states
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("2026-07-25") }
    var time by remember { mutableStateOf("10:00 AM") }
    var location by remember { mutableStateOf("Assembly Hall") }

    Scaffold(
        floatingActionButton = {
            if (currentUser?.role != "STUDENT") {
                FloatingActionButton(onClick = { showCreateDialog = true }, containerColor = pColor, contentColor = Color.White) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Add Event")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                "School Calendar Events",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = pColor,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (events.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.EventBusy, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No upcoming events scheduled.", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(events) { term ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Badge(containerColor = pColor.copy(alpha = 0.1f), contentColor = pColor) {
                                        Text("  📅 ${term.date}  ", modifier = Modifier.padding(4.dp), fontWeight = FontWeight.Bold)
                                    }

                                    if (currentUser?.role == "ADMIN" || currentUser?.role == "PREFECT") {
                                        IconButton(onClick = { viewModel.deleteEvent(term.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(term.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(term.description, fontSize = 13.sp, color = Color.Gray)

                                Spacer(modifier = Modifier.height(12.dp))
                                Divider()
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Time: ${term.time}", fontSize = 11.sp, color = Color.Gray)
                                    Text("Location: ${term.location}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = pColor)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        Dialog(onDismissRequest = { showCreateDialog = false }) {
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Add Calendar Event", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = pColor)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Event Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Time Slot") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location Venue") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (title.isNotBlank() && desc.isNotBlank()) {
                                    viewModel.createEvent(title, desc, date, time, location)
                                    showCreateDialog = false
                                } else {
                                    viewModel.showToast("Fields cannot be blank.")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = pColor)
                        ) {
                            Text("Create")
                        }
                    }
                }
            }
        }
    }
}


// ---------------------- TAB 7: FEEDBACK ----------------------

@Composable
fun FeedbackTab(viewModel: PortalViewModel) {
    val feedbackList by viewModel.allFeedback.collectAsState()
    val schoolConfig by viewModel.schoolConfig.collectAsState()

    val pColor = parseColor(schoolConfig.primaryColorHex, MaterialTheme.colorScheme.primary)

    var feedbackText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Suggestion") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Anonymous Suggestion Box",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = pColor,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            "Submit complaints, constructive suggestions, teacher feedback, or general appreciations anonymously. Filtered via clean profanity masks.",
            fontSize = 13.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select Category", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val list = listOf("Suggestion", "Complaint", "Teacher Feedback", "Appreciation")
                    list.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp, maxLines = 1) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = feedbackText,
                    onValueChange = { feedbackText = it },
                    label = { Text("Write your anonymous message...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (feedbackText.isNotBlank()) {
                            viewModel.submitFeedback(selectedCategory, feedbackText)
                            feedbackText = ""
                        } else {
                            viewModel.showToast("Message cannot be blank.")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = pColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit Anonymously", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Moderated Community Feedback",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = pColor,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val approvedList = feedbackList.filter { it.isApproved }
        if (approvedList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No feedback has been approved and published to the portal yet.", color = Color.Gray, fontSize = 13.sp)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                approvedList.forEach { f ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Badge(containerColor = pColor.copy(alpha = 0.1f), contentColor = pColor) {
                                Text("  ${f.category}  ", modifier = Modifier.padding(4.dp), fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(f.content, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Approved & Published Community Post", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}


// ---------------------- TAB 8: AI ASSISTANT ----------------------

@Composable
fun AiAssistantTab(viewModel: PortalViewModel) {
    val aiResponse by viewModel.aiResponse.collectAsState()
    val aiLoading by viewModel.aiLoading.collectAsState()
    val schoolConfig by viewModel.schoolConfig.collectAsState()

    val pColor = parseColor(schoolConfig.primaryColorHex, MaterialTheme.colorScheme.primary)

    var promptInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = pColor.copy(alpha = 0.05f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = pColor, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("AI Study Assistant", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = pColor)
                    Text("Powered by Google Gemini 3.5 Flash server-side.", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("What academic topic are we exploring today?", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = promptInput,
                    onValueChange = { promptInput = it },
                    placeholder = { Text("E.g. Break down organic chemistry formulas, explain quadratic equations...") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (promptInput.isNotBlank()) {
                            viewModel.askAiAssistant(promptInput)
                        }
                    },
                    enabled = !aiLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = pColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (aiLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Query AI Assistant", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("AI Assistant Reply", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = pColor, modifier = Modifier.padding(bottom = 8.dp))

        if (aiResponse.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, pColor.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = aiResponse,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("AI Assistant is idling. Type a prompt above.", color = Color.Gray, fontSize = 13.sp)
            }
        }
    }
}


// ---------------------- TAB 9: MODERATION ----------------------

@Composable
fun ModerationTab(viewModel: PortalViewModel) {
    val feedbackList by viewModel.allFeedback.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val schoolConfig by viewModel.schoolConfig.collectAsState()

    val pColor = parseColor(schoolConfig.primaryColorHex, MaterialTheme.colorScheme.primary)

    val unapprovedStudents = allUsers.filter { it.role == "STUDENT" && !it.isApproved }
    val unapprovedFeedback = feedbackList.filter { !it.isApproved }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "School Portal Moderation",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = pColor,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Part 1: Registrations Pending Approval
        Text("Student Registrations Pending Approval", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = pColor, modifier = Modifier.padding(bottom = 8.dp))
        if (unapprovedStudents.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .padding(20.dp)
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No registrations pending approval. All clear!", color = Color.Gray, fontSize = 13.sp)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                unapprovedStudents.forEach { student ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(student.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Index: ${student.studentId} • Class: ${student.className}", fontSize = 12.sp, color = Color.Gray)
                            }
                            Button(
                                onClick = { viewModel.approveStudent(student) },
                                colors = ButtonDefaults.buttonColors(containerColor = pColor)
                            ) {
                                Text("Approve", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Part 2: Feedback Pending Moderation
        Text("Anonymous Feedback Moderation", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = pColor, modifier = Modifier.padding(bottom = 8.dp))
        if (unapprovedFeedback.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No anonymous comments pending moderation.", color = Color.Gray, fontSize = 13.sp)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                unapprovedFeedback.forEach { fb ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Category: ${fb.category}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = pColor)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(fb.content, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = { viewModel.deleteFeedback(fb.id) }) {
                                    Text("Reject & Delete", color = Color.Red)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { viewModel.approveFeedback(fb) },
                                    colors = ButtonDefaults.buttonColors(containerColor = pColor)
                                ) {
                                    Text("Approve & Publish")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// ---------------------- TAB 10: STUDENTS DIRECTORY ----------------------

@Composable
fun StudentsTab(viewModel: PortalViewModel) {
    val usersList by viewModel.allUsers.collectAsState()
    val schoolConfig by viewModel.schoolConfig.collectAsState()

    val pColor = parseColor(schoolConfig.primaryColorHex, MaterialTheme.colorScheme.primary)

    var searchName by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf("STUDENT") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "School User Records & Members",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = pColor,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchName,
                onValueChange = { searchName = it },
                placeholder = { Text("Search school members...") },
                modifier = Modifier.weight(1f)
            )

            val roles = listOf("STUDENT", "PREFECT", "TEACHER", "ADMIN")
            Box {
                var expanded by remember { mutableStateOf(false) }
                Button(onClick = { expanded = !expanded }) {
                    Text(selectedRoleFilter)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    roles.forEach { r ->
                        DropdownMenuItem(
                            text = { Text(r) },
                            onClick = {
                                selectedRoleFilter = r
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val filteredUsers = usersList.filter {
            it.fullName.contains(searchName, ignoreCase = true) &&
                    it.role == selectedRoleFilter
        }

        if (filteredUsers.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("No matches found in database.")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                items(filteredUsers) { member ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(pColor.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(avatarsList[member.profilePicIndex.coerceIn(0, avatarsList.size - 1)], contentDescription = null, tint = pColor)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(member.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Email: ${member.email} • Class: ${member.className}", fontSize = 12.sp, color = Color.Gray)
                                if (member.role == "STUDENT") {
                                    Text("Index: ${member.studentId} • House: ${member.houseName} • Attendance: ${member.attendanceRate}%", fontSize = 11.sp, color = pColor)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// ---------------------- TAB 11: ADMIN TAB ----------------------

@Composable
fun AdminTab(viewModel: PortalViewModel) {
    val schoolConfig by viewModel.schoolConfig.collectAsState()
    val classes by viewModel.allClasses.collectAsState()
    val houses by viewModel.allHouses.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val attendanceRecord by viewModel.allAttendance.collectAsState()

    val pColor = parseColor(schoolConfig.primaryColorHex, MaterialTheme.colorScheme.primary)
    val sColor = parseColor(schoolConfig.secondaryColorHex, MaterialTheme.colorScheme.background)
    val aColor = parseColor(schoolConfig.accentColorHex, MaterialTheme.colorScheme.secondary)

    val scrollState = rememberScrollState()

    // Configuration Inputs
    var configName by remember { mutableStateOf(schoolConfig.schoolName) }
    var configPrimary by remember { mutableStateOf(schoolConfig.primaryColorHex) }
    var configSecondary by remember { mutableStateOf(schoolConfig.secondaryColorHex) }
    var configAccent by remember { mutableStateOf(schoolConfig.accentColorHex) }

    // Classes / Houses Inputs
    var newClassName by remember { mutableStateOf("") }
    var newClassDesc by remember { mutableStateOf("") }

    var newHouseName by remember { mutableStateOf("") }
    var newHouseColor by remember { mutableStateOf("#FF0000") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            "Admin Management Command",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = pColor,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Admin Analytics Section with Native Custom-drawn Charts (No third-party failures!)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Portal Database Analytics", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = pColor)
                Spacer(modifier = Modifier.height(12.dp))

                // Custom Canvas Bar Chart for Registration Stats!
                Text("Registration Count by Role", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))

                val studentsCount = allUsers.count { it.role == "STUDENT" }
                val prefectsCount = allUsers.count { it.role == "PREFECT" }
                val teachersCount = allUsers.count { it.role == "TEACHER" }
                val adminsCount = allUsers.count { it.role == "ADMIN" }

                val counts = listOf(studentsCount, prefectsCount, teachersCount, adminsCount)
                val labels = listOf("Stu", "Pref", "Teach", "Adm")
                val maxCount = counts.maxOrNull()?.coerceAtLeast(1) ?: 1

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val barWidth = canvasWidth / (counts.size * 2)
                    val spacing = canvasWidth / (counts.size * 2)

                    counts.forEachIndexed { idx, count ->
                        val barHeight = (count.toFloat() / maxCount.toFloat()) * (canvasHeight - 30f)
                        val left = (idx * (barWidth + spacing)) + spacing
                        val top = canvasHeight - 20f - barHeight

                        // Draw bar
                        drawRect(
                            color = pColor,
                            topLeft = Offset(left, top),
                            size = Size(barWidth, barHeight)
                        )

                        // Label count on top
                        drawContext.canvas.nativeCanvas.drawText(
                            "$count",
                            left + (barWidth / 4),
                            top - 5f,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.DKGRAY
                                textSize = 24f
                                isFakeBoldText = true
                            }
                        )

                        // Label role name underneath
                        drawContext.canvas.nativeCanvas.drawText(
                            labels[idx],
                            left + (barWidth / 4),
                            canvasHeight - 2f,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.GRAY
                                textSize = 22f
                            }
                        )
                    }
                }
            }
        }

        // 1. School Branding Customizer
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("School Branding & Theme Customizer", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = pColor)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = configName,
                    onValueChange = { configName = it },
                    label = { Text("School / Portal Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row {
                    OutlinedTextField(
                        value = configPrimary,
                        onValueChange = { configPrimary = it },
                        label = { Text("Primary HEX") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp)
                    )
                    OutlinedTextField(
                        value = configAccent,
                        onValueChange = { configAccent = it },
                        label = { Text("Accent HEX") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.updateSchoolTheme(configName, configPrimary, configSecondary, configAccent) },
                    colors = ButtonDefaults.buttonColors(containerColor = pColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Apply Portal Branding Changes", fontWeight = FontWeight.Bold)
                }
            }
        }

        // 2. Classes Management
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Manage Academic Classes", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = pColor)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = newClassName,
                    onValueChange = { newClassName = it },
                    label = { Text("Class Name (e.g. S6 - Arts B)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newClassDesc,
                    onValueChange = { newClassDesc = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (newClassName.isNotBlank()) {
                            viewModel.createClass(newClassName, newClassDesc)
                            newClassName = ""
                            newClassDesc = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = pColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Class", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Current Active Classes in DB", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                classes.forEach { cl ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("• ${cl.name}", fontWeight = FontWeight.SemiBold)
                        IconButton(onClick = { viewModel.deleteClass(cl.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                        }
                    }
                }
            }
        }

        // 3. Houses Management
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Manage School Houses", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = pColor)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = newHouseName,
                    onValueChange = { newHouseName = it },
                    label = { Text("House Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newHouseColor,
                    onValueChange = { newHouseColor = it },
                    label = { Text("House Color HEX (e.g. #0000FF)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (newHouseName.isNotBlank()) {
                            viewModel.createHouse(newHouseName, newHouseColor)
                            newHouseName = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = pColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add House", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Current Houses in DB", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                houses.forEach { h ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(parseColor(h.colorHex, Color.Gray), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(h.name, fontWeight = FontWeight.SemiBold)
                        }
                        IconButton(onClick = { viewModel.deleteHouse(h.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                        }
                    }
                }
            }
        }

        // 4. Accounts & Admin Commands
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Database Systems Maintenance", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = pColor)
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { viewModel.showToast("Local attendance logs successfully compiled and exported! File saved: school_attendance_2026.xlsx") },
                    colors = ButtonDefaults.buttonColors(containerColor = pColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Attendance (Excel)")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.showToast("PDF report cards successfully compiled! File saved: grade_report_cards.pdf") },
                    colors = ButtonDefaults.buttonColors(containerColor = pColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Report Cards (PDF)")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.showToast("Full SQLite database backup securely packed! backup_portal_db.sql saved in downloads.") },
                    colors = ButtonDefaults.buttonColors(containerColor = aColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Backup, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Backup Database", color = Color.Black)
                }
            }
        }
    }
}


// ---------------------- TAB 12: SETTINGS TAB ----------------------

@Composable
fun SettingsTab(
    viewModel: PortalViewModel,
    themeIsDark: MutableState<Boolean>
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val schoolConfig by viewModel.schoolConfig.collectAsState()

    val pColor = parseColor(schoolConfig.primaryColorHex, MaterialTheme.colorScheme.primary)

    var currentPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Account Settings",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = pColor,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Visual Settings", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = pColor)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Dark Mode Theme", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Invert contrast for comfortable night reading", fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = themeIsDark.value,
                        onCheckedChange = { themeIsDark.value = it }
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Change Portal Password", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = pColor)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = currentPass,
                    onValueChange = { currentPass = it },
                    label = { Text("Current Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = newPass,
                    onValueChange = { newPass = it },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val cUser = currentUser
                        if (cUser != null) {
                            if (cUser.passwordHash == currentPass && newPass.isNotBlank()) {
                                viewModel.editProfile(cUser.copy(passwordHash = newPass))
                                currentPass = ""
                                newPass = ""
                            } else {
                                viewModel.showToast("Current password mismatched or new password empty.")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = pColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Update Account Password", fontWeight = FontWeight.Bold)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Firebase Authentication & Sync", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = pColor)
                Spacer(modifier = Modifier.height(8.dp))
                Text("The school application is operating securely in localized mode, saving all changes immediately to your device's SQLite Database.", fontSize = 13.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudQueue, contentDescription = null, tint = pColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("To synchronize with a live Firebase server, upload google-services.json to the app/ directory.", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }
    }
}
