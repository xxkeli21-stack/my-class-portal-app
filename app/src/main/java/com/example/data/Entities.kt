package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "school_config")
data class SchoolConfig(
    @PrimaryKey val id: Int = 1,
    val schoolName: String = "MY CLASS PORTAL",
    val primaryColorHex: String = "#2E7D32", // Green
    val secondaryColorHex: String = "#F5F5DC", // Cream
    val accentColorHex: String = "#D4AF37", // Gold
    val logoAsset: String = "ic_school"
)

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val passwordHash: String, // Plain-text for simpler local demo auth
    val fullName: String,
    val studentId: String = "", // Index number
    val role: String, // STUDENT, PREFECT, TEACHER, ADMIN
    val gender: String = "",
    val dob: String = "",
    val phone: String = "",
    val parentName: String = "",
    val parentContact: String = "",
    val programme: String = "",
    val className: String = "", // Reference to ClassEntity.name
    val houseName: String = "", // Reference to HouseEntity.name
    val residentialStatus: String = "", // BOARDER, DAY_STUDENT
    val profilePicIndex: Int = 0, // Profile picture avatar index
    val isApproved: Boolean = true, // Default true for teachers/admins, false for students
    val isSuspended: Boolean = false,
    val recentActivity: String = "Account created",
    val attendanceRate: Float = 100f
)

@Entity(tableName = "classes")
data class ClassEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String = ""
)

@Entity(tableName = "houses")
data class HouseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val colorHex: String = "#FFD700"
)

@Entity(tableName = "attendance")
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentName: String,
    val studentId: String,
    val className: String,
    val houseName: String,
    val date: String, // YYYY-MM-DD
    val time: String, // HH:MM
    val device: String,
    val status: String // PRESENT, LATE, ABSENT
)

@Entity(tableName = "announcements")
data class Announcement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String, // General, Exam, Sports, Urgent
    val priority: String, // LOW, MEDIUM, HIGH
    val date: String, // YYYY-MM-DD
    val authorName: String,
    val authorRole: String,
    val attachmentName: String = ""
)

@Entity(tableName = "learning_resources")
data class LearningResource(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String, // Customizable, e.g. Mathematics, Science
    val fileType: String, // PDF, DOCX, PPT, Video, ZIP
    val fileSize: String, // e.g. "4.2 MB"
    val authorName: String,
    val uploadDate: String
)

@Entity(tableName = "assignments")
data class Assignment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val deadline: String, // YYYY-MM-DD
    val className: String,
    val subject: String,
    val authorName: String,
    val hasSubmitted: Boolean = false
)

@Entity(tableName = "timetable")
data class TimetableRow(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dayOfWeek: String, // Monday, Tuesday, etc.
    val timeSlot: String, // 08:30 AM - 10:00 AM
    val subject: String,
    val className: String,
    val teacherName: String
)

@Entity(tableName = "events")
data class SchoolEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val date: String, // YYYY-MM-DD
    val time: String, // HH:MM
    val location: String = "School Grounds"
)

@Entity(tableName = "feedback")
data class Feedback(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String, // Suggestion, Complaint, Appreciation, etc.
    val content: String,
    val timestamp: Long,
    val isApproved: Boolean = false // Must be moderated by Prefect/Teacher/Admin
)
