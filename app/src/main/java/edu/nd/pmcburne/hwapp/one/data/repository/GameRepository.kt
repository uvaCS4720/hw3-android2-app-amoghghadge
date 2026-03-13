package edu.nd.pmcburne.hwapp.one.data.repository

import edu.nd.pmcburne.hwapp.one.data.api.GameData
import edu.nd.pmcburne.hwapp.one.data.api.ScoreboardApi
import edu.nd.pmcburne.hwapp.one.data.db.GameDao
import edu.nd.pmcburne.hwapp.one.data.db.GameEntity
import kotlinx.coroutines.flow.Flow

class GameRepository(
    private val api: ScoreboardApi,
    private val dao: GameDao
) {
    fun getGamesFromDb(date: String, gender: String): Flow<List<GameEntity>> {
        return dao.getGames(date, gender)
    }

    suspend fun refreshGames(date: String, gender: String, year: String, month: String, day: String) {
        val response = api.getScoreboard(gender, year, month, day)
        val entities = mapToEntities(response.games, date, gender)
        dao.deleteGames(date, gender)
        dao.insertGames(entities)
    }

    private fun mapToEntities(
        games: List<edu.nd.pmcburne.hwapp.one.data.api.GameWrapper>?,
        date: String,
        gender: String
    ): List<GameEntity> {
        return games?.map { wrapper ->
            val g = wrapper.game
            GameEntity(
                gameID = g.gameID,
                date = date,
                gender = gender,
                awayTeamName = g.away.names.short ?: g.away.names.char6 ?: "Unknown",
                awayScore = g.away.score ?: "",
                awayWinner = g.away.winner,
                awaySeed = g.away.seed ?: "",
                awayRank = g.away.rank ?: "",
                homeTeamName = g.home.names.short ?: g.home.names.char6 ?: "Unknown",
                homeScore = g.home.score ?: "",
                homeWinner = g.home.winner,
                homeSeed = g.home.seed ?: "",
                homeRank = g.home.rank ?: "",
                gameState = g.gameState,
                currentPeriod = g.currentPeriod ?: "",
                contestClock = g.contestClock ?: "",
                startTime = g.startTime ?: "",
                finalMessage = g.finalMessage ?: "",
                network = g.network ?: ""
            )
        } ?: emptyList()
    }
}
