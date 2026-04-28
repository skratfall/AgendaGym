package com.gym.agenda.data.database



import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gym.agenda.data.model.GymAppointment
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM gym_appointments ORDER BY dateMillis ASC, timeHour ASC")
    fun getAll(): Flow<List<GymAppointment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appointment: GymAppointment)

    @Delete
    suspend fun delete(appointment: GymAppointment)
}