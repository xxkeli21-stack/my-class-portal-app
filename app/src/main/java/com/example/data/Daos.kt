package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolDao {
    // School Config
    @Query("SELECT * FROM school_config WHERE id = 1 LIMIT 1")
    fun getSchoolConfig(): Flow<SchoolConfig?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchoolConfig(config: SchoolConfig)

    // Users
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUser(id: Int)

    // Classes
    @Query("SELECT * FROM classes ORDER BY name ASC")
    fun getAllClasses(): Flow<List<ClassEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(classEntity: ClassEntity)

    @Query("DELETE FROM classes WHERE id = :id")
    suspend fun deleteClass(id: Int)

    // Houses
    @Query("SELECT * FROM houses ORDER BY name ASC")
    fun getAllHouses(): Flow<List<HouseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHouse(houseEntity: HouseEntity)

    @Query("DELETE FROM houses WHERE id = :id")
    suspend fun deleteHouse(id: Int)

    // Attendance
    @Query("SELECT * FROM attendance ORDER BY date DESC, time DESC")
    fun getAllAttendance(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY date DESC")
    fun getAttendanceForStudent(studentId: String): Flow<List<AttendanceRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(record: AttendanceRecord)

    @Query("SELECT COUNT(*) FROM attendance WHERE studentId = :studentId AND date = :date")
    suspend fun getAttendanceCountForDay(studentId: String, date: String): Int

    // Announcements
    @Query("SELECT * FROM announcements ORDER BY date DESC, id DESC")
    fun getAllAnnouncements(): Flow<List<Announcement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: Announcement)

    @Query("DELETE FROM announcements WHERE id = :id")
    suspend fun deleteAnnouncement(id: Int)

    // Resources
    @Query("SELECT * FROM learning_resources ORDER BY uploadDate DESC")
    fun getAllResources(): Flow<List<LearningResource>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResource(resource: LearningResource)

    @Query("DELETE FROM learning_resources WHERE id = :id")
    suspend fun deleteResource(id: Int)

    // Assignments
    @Query("SELECT * FROM assignments ORDER BY deadline ASC")
    fun getAllAssignments(): Flow<List<Assignment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: Assignment)

    @Update
    suspend fun updateAssignment(assignment: Assignment)

    @Query("DELETE FROM assignments WHERE id = :id")
    suspend fun deleteAssignment(id: Int)

    // Timetable
    @Query("SELECT * FROM timetable ORDER BY id ASC")
    fun getAllTimetable(): Flow<List<TimetableRow>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetableRow(row: TimetableRow)

    @Query("DELETE FROM timetable WHERE id = :id")
    suspend fun deleteTimetableRow(id: Int)

    // Events
    @Query("SELECT * FROM events ORDER BY date ASC")
    fun getAllEvents(): Flow<List<SchoolEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: SchoolEvent)

    @Query("DELETE FROM events WHERE id = :id")
    suspend fun deleteEvent(id: Int)

    // Feedback
    @Query("SELECT * FROM feedback ORDER BY timestamp DESC")
    fun getAllFeedback(): Flow<List<Feedback>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeedback(feedback: Feedback)

    @Update
    suspend fun updateFeedback(feedback: Feedback)

    @Query("DELETE FROM feedback WHERE id = :id")
    suspend fun deleteFeedback(id: Int)
}
