package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PortalViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = SchoolRepository(db.schoolDao())

    // UI States
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _registerSuccess = MutableStateFlow(false)
    val registerSuccess: StateFlow<Boolean> = _registerSuccess.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Database flows
    val schoolConfig: StateFlow<SchoolConfig> = repository.schoolConfig
        .map { it ?: SchoolConfig() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SchoolConfig())

    val allUsers: StateFlow<List<User>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allClasses: StateFlow<List<ClassEntity>> = repository.allClasses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHouses: StateFlow<List<HouseEntity>> = repository.allHouses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAttendance: StateFlow<List<AttendanceRecord>> = repository.allAttendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAnnouncements: StateFlow<List<Announcement>> = repository.allAnnouncements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allResources: StateFlow<List<LearningResource>> = repository.allResources
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAssignments: StateFlow<List<Assignment>> = repository.allAssignments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTimetable: StateFlow<List<TimetableRow>> = repository.allTimetable
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEvents: StateFlow<List<SchoolEvent>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFeedback: StateFlow<List<Feedback>> = repository.allFeedback
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // AI Study Assistant state
    private val _aiResponse = MutableStateFlow("")
    val aiResponse: StateFlow<String> = _aiResponse.asStateFlow()

    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    // Global Search State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
        }
    }

    // Toast utility
    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // AUTH ACTIONS
    fun login(email: String, pWord: String) {
        viewModelScope.launch {
            _loginError.value = null
            val user = repository.getUserByEmail(email)
            if (user == null) {
                _loginError.value = "User account not found."
                return@launch
            }
            if (user.isSuspended) {
                _loginError.value = "Your account has been suspended by the Admin."
                return@launch
            }
            if (!user.isApproved) {
                _loginError.value = "Your registration is pending approval by the Class Prefect or Admin."
                return@launch
            }
            if (user.passwordHash == pWord) {
                _currentUser.value = user
                _isLoggedIn.value = true
                repository.updateUser(user.copy(recentActivity = "Logged into the system"))
                showToast("Welcome back, ${user.fullName}!")
            } else {
                _loginError.value = "Incorrect password. Please try again."
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _currentUser.value?.let {
                repository.updateUser(it.copy(recentActivity = "Logged out of system"))
            }
            _currentUser.value = null
            _isLoggedIn.value = false
            _registerSuccess.value = false
            showToast("Logged out successfully.")
        }
    }

    fun register(
        fullName: String,
        email: String,
        pWord: String,
        gender: String,
        dob: String,
        phone: String,
        parentName: String,
        parentContact: String,
        programme: String,
        className: String,
        houseName: String,
        residentialStatus: String,
        studentId: String
    ) {
        viewModelScope.launch {
            _registerSuccess.value = false
            val existing = repository.getUserByEmail(email)
            if (existing != null) {
                showToast("An account with this email already exists.")
                return@launch
            }

            val isStudent = true // This registration form is for students
            val newUser = User(
                email = email,
                passwordHash = pWord,
                fullName = fullName,
                studentId = studentId,
                role = "STUDENT",
                gender = gender,
                dob = dob,
                phone = phone,
                parentName = parentName,
                parentContact = parentContact,
                programme = programme,
                className = className,
                houseName = houseName,
                residentialStatus = residentialStatus,
                profilePicIndex = (1..6).random(),
                isApproved = false, // Students need Prefect or Admin approval
                recentActivity = "Registered account. Pending approval."
            )

            repository.insertUser(newUser)
            _registerSuccess.value = true
            showToast("Registration successful! Pending Class Prefect approval.")
        }
    }

    fun editProfile(updatedUser: User) {
        viewModelScope.launch {
            repository.updateUser(updatedUser.copy(recentActivity = "Updated personal profile details"))
            _currentUser.value = updatedUser
            showToast("Profile updated successfully.")
        }
    }

    // ATTENDANCE ACTIONS
    fun markAttendance() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val hasMarked = repository.hasMarkedAttendanceToday(user.studentId)
            if (hasMarked) {
                showToast("You have already marked your attendance for today.")
                return@launch
            }

            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val todayTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

            val record = AttendanceRecord(
                studentName = user.fullName,
                studentId = user.studentId,
                className = user.className,
                houseName = user.houseName,
                date = todayDate,
                time = todayTime,
                device = "Android Device (${android.os.Build.MODEL})",
                status = "PRESENT"
            )

            repository.insertAttendance(record)
            showToast("Attendance marked successfully!")
        }
    }

    fun takeAttendanceManually(
        student: User,
        status: String
    ) {
        viewModelScope.launch {
            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val todayTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

            val record = AttendanceRecord(
                studentName = student.fullName,
                studentId = student.studentId,
                className = student.className,
                houseName = student.houseName,
                date = todayDate,
                time = todayTime,
                device = "Manual Entry by ${currentUser.value?.fullName}",
                status = status
            )

            repository.insertAttendance(record)
            showToast("Recorded $status for ${student.fullName}.")
        }
    }

    // PREFECT / TEACHER / ADMIN ACTIONS
    fun approveStudent(student: User) {
        viewModelScope.launch {
            repository.updateUser(student.copy(isApproved = true, recentActivity = "Approved by Prefect/Admin"))
            showToast("Approved registration for ${student.fullName}.")
        }
    }

    fun suspendUser(user: User, suspend: Boolean) {
        viewModelScope.launch {
            repository.updateUser(user.copy(isSuspended = suspend, recentActivity = if (suspend) "Account suspended" else "Suspension lifted"))
            showToast(if (suspend) "Suspended ${user.fullName}." else "Restored ${user.fullName}.")
        }
    }

    fun resetPassword(user: User, newPWord: String) {
        viewModelScope.launch {
            repository.updateUser(user.copy(passwordHash = newPWord, recentActivity = "Password reset by Admin"))
            showToast("Password for ${user.fullName} has been reset.")
        }
    }

    fun createAnnouncement(title: String, desc: String, cat: String, priority: String, attachment: String = "") {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val announcement = Announcement(
                title = title,
                description = desc,
                category = cat,
                priority = priority,
                date = today,
                authorName = user.fullName,
                authorRole = user.role,
                attachmentName = attachment
            )
            repository.insertAnnouncement(announcement)
            showToast("Announcement published successfully.")
        }
    }

    fun deleteAnnouncement(id: Int) {
        viewModelScope.launch {
            repository.deleteAnnouncement(id)
            showToast("Announcement deleted.")
        }
    }

    fun createResource(title: String, desc: String, category: String, fileType: String, fileSize: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val res = LearningResource(
                title = title,
                description = desc,
                category = category,
                fileType = fileType,
                fileSize = fileSize,
                authorName = user.fullName,
                uploadDate = today
            )
            repository.insertResource(res)
            showToast("Resource uploaded successfully.")
        }
    }

    fun deleteResource(id: Int) {
        viewModelScope.launch {
            repository.deleteResource(id)
            showToast("Resource deleted.")
        }
    }

    fun createAssignment(title: String, desc: String, deadline: String, className: String, subject: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val assignment = Assignment(
                title = title,
                description = desc,
                deadline = deadline,
                className = className,
                subject = subject,
                authorName = user.fullName
            )
            repository.insertAssignment(assignment)
            showToast("Assignment published.")
        }
    }

    fun submitAssignment(assignment: Assignment) {
        viewModelScope.launch {
            repository.updateAssignment(assignment.copy(hasSubmitted = true))
            showToast("Assignment submitted successfully.")
        }
    }

    fun deleteAssignment(id: Int) {
        viewModelScope.launch {
            repository.deleteAssignment(id)
            showToast("Assignment deleted.")
        }
    }

    fun createTimetableRow(day: String, slot: String, subject: String, className: String, teacher: String) {
        viewModelScope.launch {
            val row = TimetableRow(
                dayOfWeek = day,
                timeSlot = slot,
                subject = subject,
                className = className,
                teacherName = teacher
            )
            repository.insertTimetableRow(row)
            showToast("Timetable row added.")
        }
    }

    fun deleteTimetableRow(id: Int) {
        viewModelScope.launch {
            repository.deleteTimetableRow(id)
            showToast("Timetable item deleted.")
        }
    }

    fun createEvent(title: String, desc: String, date: String, time: String, location: String) {
        viewModelScope.launch {
            val event = SchoolEvent(
                title = title,
                description = desc,
                date = date,
                time = time,
                location = location
            )
            repository.insertEvent(event)
            showToast("School event scheduled.")
        }
    }

    fun deleteEvent(id: Int) {
        viewModelScope.launch {
            repository.deleteEvent(id)
            showToast("Event deleted.")
        }
    }

    // ANONYMOUS FEEDBACK ACTIONS
    fun submitFeedback(category: String, content: String) {
        viewModelScope.launch {
            // Apply a simple profanity filter as requested
            val filteredContent = filterProfanity(content)
            val feedback = Feedback(
                category = category,
                content = filteredContent,
                timestamp = System.currentTimeMillis(),
                isApproved = false // Pending Prefect / Teacher / Admin moderation
            )
            repository.insertFeedback(feedback)
            showToast("Anonymous feedback submitted and pending moderation.")
        }
    }

    fun approveFeedback(feedback: Feedback) {
        viewModelScope.launch {
            repository.updateFeedback(feedback.copy(isApproved = true))
            showToast("Feedback approved and published to dashboard.")
        }
    }

    fun deleteFeedback(id: Int) {
        viewModelScope.launch {
            repository.deleteFeedback(id)
            showToast("Feedback deleted.")
        }
    }

    private fun filterProfanity(text: String): String {
        val badWords = listOf("swearword", "badword", "idiot", "jerk", "fool")
        var filtered = text
        for (word in badWords) {
            val stars = "*".repeat(word.length)
            filtered = filtered.replace(word, stars, ignoreCase = true)
        }
        return filtered
    }

    // SCHOOL / CLASS / HOUSE MANAGEMENT (ADMIN ONLY)
    fun createClass(name: String, desc: String) {
        viewModelScope.launch {
            repository.insertClass(ClassEntity(name = name, description = desc))
            showToast("Class '$name' created.")
        }
    }

    fun deleteClass(id: Int) {
        viewModelScope.launch {
            repository.deleteClass(id)
            showToast("Class deleted.")
        }
    }

    fun createHouse(name: String, colorHex: String) {
        viewModelScope.launch {
            repository.insertHouse(HouseEntity(name = name, colorHex = colorHex))
            showToast("House '$name' created.")
        }
    }

    fun deleteHouse(id: Int) {
        viewModelScope.launch {
            repository.deleteHouse(id)
            showToast("House deleted.")
        }
    }

    fun updateSchoolTheme(name: String, primaryColor: String, secondaryColor: String, accentColor: String) {
        viewModelScope.launch {
            val current = schoolConfig.value
            repository.saveSchoolConfig(
                current.copy(
                    schoolName = name,
                    primaryColorHex = primaryColor,
                    secondaryColorHex = secondaryColor,
                    accentColorHex = accentColor
                )
            )
            showToast("School branding customized successfully!")
        }
    }

    // AI STUDY ASSISTANT ACTION
    fun askAiAssistant(question: String) {
        if (question.isBlank()) return
        viewModelScope.launch {
            _aiLoading.value = true
            _aiResponse.value = ""
            val sysPrompt = "You are an expert AI Study Assistant inside 'My Class Portal', a school platform. " +
                    "Help the student with their school subjects (Science, Math, Chemistry, English, etc.). " +
                    "Explain concepts clearly, in a friendly, encouraging manner. Do not exceed 3-4 paragraphs. " +
                    "Always refer to the student warmly."
            val response = GeminiService.generateResponse(question, sysPrompt)
            _aiResponse.value = response
            _aiLoading.value = false
        }
    }

    fun clearAiResponse() {
        _aiResponse.value = ""
    }
}
