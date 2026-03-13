package edu.nd.pmcburne.hwapp.one.data.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "games", primaryKeys = ["gameID"])
data class GameEntity(
    val gameID: String,
    val date: String,
    val gender: String,
    val awayTeamName: String,
    val awayScore: String,
    val awayWinner: Boolean,
    val awaySeed: String,
    val awayRank: String,
    val homeTeamName: String,
    val homeScore: String,
    val homeWinner: Boolean,
    val homeSeed: String,
    val homeRank: String,
    val gameState: String,
    val currentPeriod: String,
    val contestClock: String,
    val startTime: String,
    val finalMessage: String,
    val network: String
)

@Dao
interface GameDao {
    @Query("SELECT * FROM games WHERE date = :date AND gender = :gender")
    fun getGames(date: String, gender: String): Flow<List<GameEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGames(games: List<GameEntity>)

    @Query("DELETE FROM games WHERE date = :date AND gender = :gender")
    suspend fun deleteGames(date: String, gender: String)
}

@Database(entities = [GameEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "basketball_scores.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
