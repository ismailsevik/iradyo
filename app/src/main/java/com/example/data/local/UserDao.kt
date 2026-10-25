package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("UPDATE users SET totalListeningMinutes = totalListeningMinutes + :minutes WHERE id = :userId")
    suspend fun addListeningTime(userId: String, minutes: Int)

    // Favorites
    @Query("SELECT podcastId FROM favorites WHERE userId = :userId")
    fun getFavoritePodcastIds(userId: String): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE userId = :userId AND podcastId = :podcastId)")
    fun isFavorite(userId: String, podcastId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE userId = :userId AND podcastId = :podcastId")
    suspend fun removeFavorite(userId: String, podcastId: String)

    // History
    @Query("SELECT * FROM history WHERE userId = :userId ORDER BY listenedAt DESC")
    fun getListeningHistory(userId: String): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity)

    @Query("DELETE FROM history WHERE userId = :userId")
    suspend fun clearHistory(userId: String)

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: String)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    @Query("DELETE FROM favorites")
    suspend fun clearAllFavorites()

    @Query("DELETE FROM history")
    suspend fun clearAllHistory()
}
