package edu.nd.pmcburne.hwapp.one.data.api

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

data class ScoreboardResponse(
    val games: List<GameWrapper>? = null
)

data class GameWrapper(
    val game: GameData
)

data class GameData(
    val gameID: String,
    val away: TeamData,
    val home: TeamData,
    val gameState: String,
    val currentPeriod: String?,
    val contestClock: String?,
    val startTime: String?,
    val startDate: String?,
    val finalMessage: String?,
    val network: String?
)

data class TeamData(
    val score: String?,
    val names: TeamNames,
    val winner: Boolean,
    val seed: String?,
    val rank: String?
)

data class TeamNames(
    val char6: String?,
    val short: String?,
    val seo: String?,
    val full: String?
)

interface ScoreboardApi {

    @GET("scoreboard/basketball-{gender}/d1/{year}/{month}/{day}")
    suspend fun getScoreboard(
        @Path("gender") gender: String,
        @Path("year") year: String,
        @Path("month") month: String,
        @Path("day") day: String
    ): ScoreboardResponse

    @GET("scoreboard/basketball-{gender}/d1")
    suspend fun getScoreboardToday(
        @Path("gender") gender: String
    ): ScoreboardResponse

    companion object {
        private const val BASE_URL = "https://ncaa-api.henrygd.me/"

        fun create(): ScoreboardApi {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ScoreboardApi::class.java)
        }
    }
}
