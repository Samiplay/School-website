package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ui.screens.LockScreen
import com.example.ui.screens.MoneyScreen
import com.example.ui.screens.SchoolScreen
import com.example.ui.theme.BricolageGrotesqueFamily
import com.example.ui.theme.LocalHubPalette
import com.example.ui.theme.MyHubTheme
import com.example.ui.viewmodel.HubUiState
import com.example.ui.viewmodel.HubViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: HubViewModel = viewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            MyHubTheme(
                themeMode = state.themeMode,
                isMoneyMode = state.activeSection == "money"
            ) {
                val palette = LocalHubPalette.current
                val isDark = palette.isDark
                SideEffect {
                    enableEdgeToEdge(
                        statusBarStyle = if (isDark) {
                            SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                        } else {
                            SystemBarStyle.light(
                                android.graphics.Color.TRANSPARENT,
                                android.graphics.Color.TRANSPARENT
                            )
                        },
                        navigationBarStyle = if (isDark) {
                            SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                        } else {
                            SystemBarStyle.light(
                                android.graphics.Color.TRANSPARENT,
                                android.graphics.Color.TRANSPARENT
                            )
                        }
                    )
                }

                if (!state.isUnlocked) {
                    LockScreen(
                        errorText = state.lockError,
                        shakeCount = state.lockShakeCount,
                        onUnlockAttempt = viewModel::submitPassword
                    )
                } else {
                    HubMainShell(
                        state = state,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun HubMainShell(
    state: HubUiState,
    viewModel: HubViewModel
) {
    val palette = LocalHubPalette.current
    val scrollState = rememberScrollState()

    BackHandler(enabled = state.activeSection == "money") {
        viewModel.selectSection("school")
    }

    Scaffold(
        containerColor = palette.bg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(palette.bg)
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                HubTopBar(
                    activeSection = state.activeSection,
                    onSelectSection = viewModel::selectSection,
                    onToggleTheme = { viewModel.toggleTheme(palette.isDark) },
                    onLock = viewModel::lockApp
                )
                Spacer(Modifier.height(18.dp))

                if (state.activeSection == "school") {
                    SchoolScreen(
                        state = state,
                        onSelectTab = viewModel::selectSchoolTab,
                        onAddClass = viewModel::addClass,
                        onDeleteClass = viewModel::deleteClass,
                        onAddPackItem = viewModel::addPackItem,
                        onTogglePackItem = viewModel::togglePackItem,
                        onDeletePackItem = viewModel::deletePackItem,
                        onAddHomework = viewModel::addHomework,
                        onToggleHomework = viewModel::toggleHomework,
                        onDeleteHomework = viewModel::deleteHomework,
                        onAddExam = viewModel::addExam,
                        onToggleExamTopic = viewModel::toggleExamTopic,
                        onDeleteExam = viewModel::deleteExam,
                        onAddStudyTopic = viewModel::addStudyTopic,
                        onToggleStudyTopic = viewModel::toggleStudyTopic,
                        onDeleteStudyTopic = viewModel::deleteStudyTopic,
                        onToggleTimer = viewModel::toggleTimer,
                        onResetTimer = viewModel::resetTimer,
                        onSwapTimerMode = viewModel::swapTimerMode
                    )
                } else {
                    MoneyScreen(
                        state = state,
                        onSelectTab = viewModel::selectMoneyTab,
                        onAddTransaction = viewModel::addTransaction,
                        onDeleteTransaction = viewModel::deleteTransaction,
                        onToggleShowAll = viewModel::toggleShowAllTransactions,
                        onSetBudget = viewModel::setMonthlyBudget,
                        onSetGoal = viewModel::setSavingsGoal,
                        onAddSavings = viewModel::addSavings,
                        onRemoveGoal = viewModel::removeSavingsGoal,
                        onSetCurrency = viewModel::setCurrency,
                        onEraseAll = viewModel::eraseAllMoneyData
                    )
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun HubTopBar(
    activeSection: String,
    onSelectSection: (String) -> Unit,
    onToggleTheme: () -> Unit,
    onLock: () -> Unit
) {
    val palette = LocalHubPalette.current
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = palette.surface,
        border = BorderStroke(1.dp, palette.line),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.app_name),
                fontFamily = BricolageGrotesqueFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp,
                color = palette.ink,
                modifier = Modifier.padding(horizontal = 6.dp)
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(palette.bg)
                    .border(1.dp, palette.line, RoundedCornerShape(10.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TopNavTab(
                    label = stringResource(R.string.nav_school),
                    selected = activeSection == "school",
                    onClick = { onSelectSection("school") },
                    modifier = Modifier.testTag("nav_school")
                )
                TopNavTab(
                    label = stringResource(R.string.nav_money),
                    selected = activeSection == "money",
                    onClick = { onSelectSection("money") },
                    modifier = Modifier.testTag("nav_money")
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                TopActionLink(
                    label = stringResource(R.string.action_theme),
                    onClick = onToggleTheme,
                    modifier = Modifier.testTag("theme_button")
                )
                TopActionLink(
                    label = stringResource(R.string.action_lock),
                    onClick = onLock,
                    modifier = Modifier.testTag("lock_button")
                )
            }
        }
    }
}

@Composable
private fun TopNavTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalHubPalette.current
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) palette.accent else Color.Transparent)
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontSize = 14.sp,
            color = if (selected) palette.accentInk else palette.muted
        )
    }
}

@Composable
private fun TopActionLink(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalHubPalette.current
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 40.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontSize = 13.sp,
            color = palette.muted
        )
    }
}
