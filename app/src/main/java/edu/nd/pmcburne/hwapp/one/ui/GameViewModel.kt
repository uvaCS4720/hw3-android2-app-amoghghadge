package edu.nd.pmcburne.hwapp.one.ui

import android.app.Application
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import edu.nd.pmcburne.hwapp.one.data.api.ScoreboardApi
import edu.nd.pmcburne.hwapp.one.data.db.AppDatabase
import edu.nd.pmcburne.hwapp.one.data.db.GameEntity
import edu.nd.pmcburne.hwapp.one.data.repository.GameRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class Gender(val label: String, val apiValue: String) {
    MEN("Men", "men"),
    WOMEN("Women", "women")
}

data class ScoreboardUiState(
    val games: List<GameEntity> = emptyList(),
    val isLoading: Boolean = false,
    val selectedDate: LocalDate = LocalDate.now(),
    val selectedGender: Gender = Gender.MEN,
    val errorMessage: String? = null
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository
    private val _uiState = MutableStateFlow(ScoreboardUiState())
    val uiState: StateFlow<ScoreboardUiState> = _uiState.asStateFlow()

    private var collectJob: Job? = null

    init {
        val db = AppDatabase.getInstance(application)
        val api = ScoreboardApi.create()
        repository = GameRepository(api, db.gameDao())
        loadGames()
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
        loadGames()
    }

    fun onGenderSelected(gender: Gender) {
        _uiState.value = _uiState.value.copy(selectedGender = gender)
        loadGames()
    }

    fun refresh() {
        loadGames()
    }

    private fun loadGames() {
        val state = _uiState.value
        val date = state.selectedDate
        val gender = state.selectedGender
        val dateStr = date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))
        val year = date.format(DateTimeFormatter.ofPattern("yyyy"))
        val month = date.format(DateTimeFormatter.ofPattern("MM"))
        val day = date.format(DateTimeFormatter.ofPattern("dd"))

        collectJob?.cancel()
        collectJob = viewModelScope.launch {
            repository.getGamesFromDb(dateStr, gender.apiValue).collect { games ->
                _uiState.value = _uiState.value.copy(games = games, errorMessage = null)
            }
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                if (isNetworkAvailable()) {
                    repository.refreshGames(dateStr, gender.apiValue, year, month, day)
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "No internet connection. Showing cached data."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to load scores. Showing cached data."
                )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getApplication<Application>().getSystemService(ConnectivityManager::class.java)
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
