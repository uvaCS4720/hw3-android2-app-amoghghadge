package edu.nd.pmcburne.hwapp.one.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.nd.pmcburne.hwapp.one.data.db.GameEntity
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreboardScreen(viewModel: GameViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Basketball Scores") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Date picker row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = "Select date",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = uiState.selectedDate.format(
                        DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy")
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            // Gender toggle
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Gender.entries.forEachIndexed { index, gender ->
                    SegmentedButton(
                        selected = uiState.selectedGender == gender,
                        onClick = { viewModel.onGenderSelected(gender) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = Gender.entries.size
                        )
                    ) {
                        Text(gender.label)
                    }
                }
            }

            // Error message
            uiState.errorMessage?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Games list with pull-to-refresh
            Box(modifier = Modifier.weight(1f)) {
                PullToRefreshBox(
                    isRefreshing = uiState.isLoading,
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (uiState.games.isEmpty() && !uiState.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No games found for this date.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = 16.dp, vertical = 8.dp
                            )
                        ) {
                            items(uiState.games, key = { it.gameID }) { game ->
                                GameCard(game = game, gender = uiState.selectedGender)
                            }
                        }
                    }
                }
            }
        }

        // Date picker dialog
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = uiState.selectedDate
                    .atStartOfDay(ZoneOffset.UTC)
                    .toInstant()
                    .toEpochMilli()
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                            viewModel.onDateSelected(date)
                        }
                        showDatePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}

@Composable
fun GameCard(game: GameEntity, gender: Gender) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Status badge + network
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(game)
                if (game.network.isNotBlank()) {
                    Text(
                        text = game.network,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Away team row
            TeamRow(
                label = "AWAY",
                teamName = game.awayTeamName,
                score = game.awayScore,
                isWinner = game.awayWinner,
                seed = game.awaySeed,
                rank = game.awayRank,
                gameState = game.gameState
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Home team row
            TeamRow(
                label = "HOME",
                teamName = game.homeTeamName,
                score = game.homeScore,
                isWinner = game.homeWinner,
                seed = game.homeSeed,
                rank = game.homeRank,
                gameState = game.gameState
            )

            // Period/time info
            if (game.gameState == "live") {
                Spacer(modifier = Modifier.height(8.dp))
                val periodLabel = formatPeriod(game.currentPeriod, gender)
                Text(
                    text = "$periodLabel - ${game.contestClock}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFD32F2F),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun StatusBadge(game: GameEntity) {
    val (text, bgColor) = when (game.gameState) {
        "final" -> "Final" to MaterialTheme.colorScheme.surfaceVariant
        "live" -> "Live" to Color(0xFFD32F2F)
        "pre" -> game.startTime to MaterialTheme.colorScheme.primaryContainer
        else -> game.gameState to MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = when (game.gameState) {
        "live" -> Color.White
        "pre" -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = textColor,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

@Composable
fun TeamRow(
    label: String,
    teamName: String,
    score: String,
    isWinner: Boolean,
    seed: String,
    rank: String,
    gameState: String
) {
    val fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal
    val textColor = if (isWinner) MaterialTheme.colorScheme.onSurface
    else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Team label (HOME/AWAY)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(40.dp)
        )

        // Seed
        if (seed.isNotBlank()) {
            Text(
                text = "($seed)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 4.dp)
            )
        }

        // Rank
        if (rank.isNotBlank()) {
            Text(
                text = "#$rank",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 4.dp)
            )
        }

        // Team name
        Text(
            text = teamName,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = fontWeight,
            color = textColor,
            modifier = Modifier.weight(1f)
        )

        // Winner indicator
        if (isWinner && gameState == "final") {
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50))
            )
        }

        // Score
        if (gameState != "pre" && score.isNotBlank()) {
            Text(
                text = score,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = fontWeight,
                color = textColor
            )
        }
    }
}

fun formatPeriod(period: String, gender: Gender): String {
    return when {
        period.equals("FINAL", ignoreCase = true) -> "Final"
        gender == Gender.MEN -> {
            when (period.lowercase()) {
                "1st" -> "1st Half"
                "2nd" -> "2nd Half"
                else -> period
            }
        }
        gender == Gender.WOMEN -> {
            when (period.lowercase()) {
                "1st" -> "1st Quarter"
                "2nd" -> "2nd Quarter"
                "3rd" -> "3rd Quarter"
                "4th" -> "4th Quarter"
                else -> period
            }
        }
        else -> period
    }
}
