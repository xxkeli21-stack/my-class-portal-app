package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*

class SchoolRepository(private val schoolDao: SchoolDao) {

    // School Config
    val schoolConfig: Flow<SchoolConfig?> = schoolDao.getSchoolConfig()
    suspend fun saveSchoolConfig(config: SchoolConfig) = schoolDao.insertSchoolConfig(config)

    // Users
    val allUsers: Flow<List<User>> = schoolDao.getAllUsers()
    suspend fun getUserByEmail(email: String): User? = schoolDao.getUserByEmail(email)
    suspend fun getUserById(id: Int): User? = schoolDao.getUserById(id)
    suspend fun insertUser(user: User): Long = schoolDao.insertUser(user)
    suspend fun updateUser(user: User) = schoolDao.updateUser(user)
    suspend fun deleteUser(id: Int) = schoolDao.deleteUser(id)

    // Classes
    val allClasses: Flow<List<ClassEntity>> = schoolDao.getAllClasses()
    suspend fun insertClass(classEntity: ClassEntity) = schoolDao.insertClass(classEntity)
    suspend fun deleteClass(id: Int) = schoolDao.deleteClass(id)

    // Houses
    val allHouses: Flow<List<HouseEntity>> = schoolDao.getAllHouses()
    suspend fun insertHouse(houseEntity: HouseEntity) = schoolDao.insertHouse(houseEntity)
    suspend fun deleteHouse(id: Int) = schoolDao.deleteHouse(id)

    // Attendance
    val allAttendance: Flow<List<AttendanceRecord>> = schoolDao.getAllAttendance()
    fun getAttendanceForStudent(studentId: String): Flow<List<AttendanceRecord>> = 
        schoolDao.getAttendanceForStudent(studentId)
    suspend fun insertAttendance(record: AttendanceRecord) = schoolDao.insertAttendance(record)
    suspend fun hasMarkedAttendanceToday(studentId: String): Boolean {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        return schoolDao.getAttendanceCountForDay(studentId, today) > 0
    }

    // Announcements
    val allAnnouncements: Flow<List<Announcement>> = schoolDao.getAllAnnouncements()
    suspend fun insertAnnouncement(announcement: Announcement) = schoolDao.insertAnnouncement(announcement)
    suspend fun deleteAnnouncement(id: Int) = schoolDao.deleteAnnouncement(id)

    // Resources
    val allResources: Flow<List<LearningResource>> = schoolDao.getAllResources()
    suspend fun insertResource(resource: LearningResource) = schoolDao.insertResource(resource)
    suspend fun deleteResource(id: Int) = schoolDao.deleteResource(id)

    // Assignments
    val allAssignments: Flow<List<Assignment>> = schoolDao.getAllAssignments()
    suspend fun insertAssignment(assignment: Assignment) = schoolDao.insertAssignment(assignment)
    suspend fun updateAssignment(assignment: Assignment) = schoolDao.updateAssignment(assignment)
    suspend fun deleteAssignment(id: Int) = schoolDao.deleteAssignment(id)

    // Timetable
    val allTimetable: Flow<List<TimetableRow>> = schoolDao.getAllTimetable()
    suspend fun insertTimetableRow(row: TimetableRow) = schoolDao.insertTimetableRow(row)
    suspend fun deleteTimetableRow(id: Int) = schoolDao.deleteTimetableRow(id)

    // Events
    val allEvents: Flow<List<SchoolEvent>> = schoolDao.getAllEvents()
    suspend fun insertEvent(event: SchoolEvent) = schoolDao.insertEvent(event)
    suspend fun deleteEvent(id: Int) = schoolDao.deleteEvent(id)

    // Feedback
    val allFeedback: Flow<List<Feedback>> = schoolDao.getAllFeedback()
    suspend fun insertFeedback(feedback: Feedback) = schoolDao.insertFeedback(feedback)
    suspend fun updateFeedback(feedback: Feedback) = schoolDao.updateFeedback(feedback)
    suspend fun deleteFeedback(id: Int) = schoolDao.deleteFeedback(id)

    // Prepopulate Data helper
    suspend fun prepopulateIfEmpty() {
        val existingConfig = schoolDao.getSchoolConfig().firstOrNull()
        if (existingConfig == null) {
            // 1. Initial Config
            schoolDao.insertSchoolConfig(SchoolConfig())

            // 2. Default Classes
            schoolDao.insertClass(ClassEntity(name = "S6 - Science A", description = "Final year physics and chemistry class"))
            schoolDao.insertClass(ClassEntity(name = "S6 - Arts C", description = "Final year English and History class"))
            schoolDao.insertClass(ClassEntity(name = "S5 - Business B", description = "Second year Economics and Accounting"))
            schoolDao.insertClass(ClassEntity(name = "S4 - Computing D", description = "First year IT and Computer Science"))

            // 3. Default Houses
            schoolDao.insertHouse(HouseEntity(name = "Red House", colorHex = "#E53935"))
            schoolDao.insertHouse(HouseEntity(name = "Gold House", colorHex = "#FFB300"))
            schoolDao.insertHouse(HouseEntity(name = "Blue House", colorHex = "#1E88E5"))
            schoolDao.insertHouse(HouseEntity(name = "Green House", colorHex = "#43A047"))

            // 4. Core Users
            schoolDao.insertUser(User(
                email = "admin@school.com",
                passwordHash = "admin",
                fullName = "Principal Sarah Jenkins",
                role = "ADMIN",
                isApproved = true,
                profilePicIndex = 1,
                recentActivity = "Configured school theme and color settings"
            ))

            schoolDao.insertUser(User(
                email = "teacher@school.com",
                passwordHash = "teacher",
                fullName = "Mr. Arthur Pendelton",
                role = "TEACHER",
                isApproved = true,
                profilePicIndex = 2,
                recentActivity = "Graded organic chemistry mid-terms"
            ))

            schoolDao.insertUser(User(
                email = "prefect@school.com",
                passwordHash = "prefect",
                fullName = "Judith Amoah",
                role = "PREFECT",
                studentId = "IDX-4091",
                isApproved = true,
                className = "S6 - Science A",
                houseName = "Red House",
                profilePicIndex = 3,
                recentActivity = "Approved registrations for three new students"
            ))

            schoolDao.insertUser(User(
                email = "student@school.com",
                passwordHash = "student",
                fullName = "Daniel Boateng",
                role = "STUDENT",
                studentId = "IDX-8092",
                isApproved = true,
                className = "S6 - Science A",
                houseName = "Red House",
                profilePicIndex = 4,
                attendanceRate = 96.5f,
                recentActivity = "Submitted Newton's Laws assignment"
            ))

            schoolDao.insertUser(User(
                email = "newstudent@school.com",
                passwordHash = "student",
                fullName = "Clara Mensah",
                role = "STUDENT",
                studentId = "IDX-9111",
                isApproved = false,
                className = "S5 - Business B",
                houseName = "Gold House",
                profilePicIndex = 5,
                recentActivity = "Registered and pending Prefect approval"
            ))

            // 5. Default Announcements
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            schoolDao.insertAnnouncement(Announcement(
                title = "Mid-Term Examinations Timetable",
                description = "The mid-term exams begin on July 22nd. Ensure all tuition balances are cleared and physical ID cards are ready for examination hall inspections.",
                category = "Exam",
                priority = "HIGH",
                date = today,
                authorName = "Principal Sarah Jenkins",
                authorRole = "ADMIN",
                attachmentName = "Mid_Term_Timetable.pdf"
            ))

            schoolDao.insertAnnouncement(Announcement(
                title = "Inter-House Athletics Competition",
                description = "Annual sporting feast takes place this Friday! Support your house. Refreshments and athletics tracks open at 8:00 AM.",
                category = "Sports",
                priority = "MEDIUM",
                date = today,
                authorName = "Judith Amoah",
                authorRole = "PREFECT",
                attachmentName = "Event_Program.pdf"
            ))

            schoolDao.insertAnnouncement(Announcement(
                title = "Study Groups in Library",
                description = "The school library will extend opening hours to 9 PM every evening starting next week to support mid-term study groups.",
                category = "General",
                priority = "LOW",
                date = today,
                authorName = "Mr. Arthur Pendelton",
                authorRole = "TEACHER"
            ))

            // 6. Default Resources
            schoolDao.insertResource(LearningResource(
                title = "Calculus I - Integration Reference Sheets",
                description = "Quick references, rules, and solved examples of integrals and derivatives.",
                category = "Mathematics",
                fileType = "PDF",
                fileSize = "2.3 MB",
                authorName = "Mr. Arthur Pendelton",
                uploadDate = today
            ))

            schoolDao.insertResource(LearningResource(
                title = "Organic Chemistry Synthesis Handbook",
                description = "Alkanes, alkenes, benzene rings, and synthesis maps.",
                category = "Chemistry",
                fileType = "PPT",
                fileSize = "5.1 MB",
                authorName = "Mr. Arthur Pendelton",
                uploadDate = today
            ))

            schoolDao.insertResource(LearningResource(
                title = "Hamlet Textual Critical Analyses",
                description = "Exhaustive bundle with key character quotes and thematic writeups.",
                category = "English",
                fileType = "ZIP",
                fileSize = "11.4 MB",
                authorName = "Principal Sarah Jenkins",
                uploadDate = today
            ))

            // 7. Default Assignments
            schoolDao.insertAssignment(Assignment(
                title = "Organic Compounds Laboratory Lab Writeup",
                description = "Detail observations of the esterification experiments. Limit your report to five pages maximum.",
                deadline = "2026-07-18",
                className = "S6 - Science A",
                subject = "Chemistry",
                authorName = "Mr. Arthur Pendelton"
            ))

            schoolDao.insertAssignment(Assignment(
                title = "Shakespeare Hamlet Act III Analysis Essay",
                description = "Explain the significance of the play-within-a-play in Act III. MLA referencing format required.",
                deadline = "2026-07-20",
                className = "S6 - Arts C",
                subject = "English Literature",
                authorName = "Mr. Arthur Pendelton"
            ))

            // 8. Timetable
            schoolDao.insertTimetableRow(TimetableRow(dayOfWeek = "Monday", timeSlot = "08:30 AM - 10:00 AM", subject = "Physics", className = "S6 - Science A", teacherName = "Mr. Arthur Pendelton"))
            schoolDao.insertTimetableRow(TimetableRow(dayOfWeek = "Monday", timeSlot = "10:30 AM - 12:00 PM", subject = "Mathematics", className = "S6 - Science A", teacherName = "Mr. Arthur Pendelton"))
            schoolDao.insertTimetableRow(TimetableRow(dayOfWeek = "Tuesday", timeSlot = "08:30 AM - 10:00 AM", subject = "Chemistry", className = "S6 - Science A", teacherName = "Mr. Arthur Pendelton"))
            schoolDao.insertTimetableRow(TimetableRow(dayOfWeek = "Wednesday", timeSlot = "01:00 PM - 02:30 PM", subject = "English Literature", className = "S6 - Arts C", teacherName = "Mr. Arthur Pendelton"))

            // 9. Events
            schoolDao.insertEvent(SchoolEvent(
                title = "Inter-House Athletics Carnival",
                description = "A massive competition for track, relays, and long jump.",
                date = "2026-07-17",
                time = "08:00 AM",
                location = "Main Sports Oval"
            ))
            schoolDao.insertEvent(SchoolEvent(
                title = "Mid-Term Examination Kick-off",
                description = "The official start date for standard curriculum examinations.",
                date = "2026-07-22",
                time = "09:00 AM",
                location = "Assembly Hall"
            ))
            schoolDao.insertEvent(SchoolEvent(
                title = "Parents and Teachers Association (PTA)",
                description = "Discuss general infrastructure upgrades and terminal progress cards.",
                date = "2026-07-29",
                time = "02:00 PM",
                location = "Interactive Multimedia Lab"
            ))

            // 10. Feedback
            schoolDao.insertFeedback(Feedback(
                category = "Suggestion",
                content = "Could we extend library opening hours during exam weeks?",
                timestamp = System.currentTimeMillis() - 86400000L, // 1 day ago
                isApproved = false
            ))
            schoolDao.insertFeedback(Feedback(
                category = "Complaint",
                content = "Water dispenser on the 2nd floor is not cold.",
                timestamp = System.currentTimeMillis() - 43200000L, // 12 hrs ago
                isApproved = true
            ))
            schoolDao.insertFeedback(Feedback(
                category = "Appreciation",
                content = "Big thanks to Prefect Judith for helping organize the chemistry study group!",
                timestamp = System.currentTimeMillis() - 10800000L, // 3 hrs ago
                isApproved = true
            ))

            // 11. Mock Attendance Log
            schoolDao.insertAttendance(AttendanceRecord(
                studentName = "Daniel Boateng",
                studentId = "IDX-8092",
                className = "S6 - Science A",
                houseName = "Red House",
                date = today,
                time = "08:14 AM",
                device = "Android Phone (Google Pixel 8 Pro)",
                status = "PRESENT"
            ))
        }
    }
}
