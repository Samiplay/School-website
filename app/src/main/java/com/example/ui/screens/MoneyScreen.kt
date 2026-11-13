package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.HubAltButton
import com.example.ui.components.HubCard
import com.example.ui.components.HubDatePickerButton
import com.example.ui.components.HubDeleteButton
import com.example.ui.components.HubDropdownField
import com.example.ui.components.HubPrimaryButton
import com.example.ui.components.HubProgressBar
import com.example.ui.components.HubRowDivider
import com.example.ui.components.HubSectionHeader
import com.example.ui.components.HubSegmentedTabs
import com.example.ui.components.HubTextField
import com.example.ui.components.SummaryPillData
import com.example.ui.components.SummaryPillRow
import com.example.ui.theme.LocalHubPalette
import com.example.ui.theme.SoraFamily
import com.example.ui.viewmodel.HubFormatters
import com.example.ui.viewmodel.HubUiState
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun MoneyScreen(
    state: HubUiState,
    onSelectTab: (String) -> Unit,
    onAddTransaction: (String, String, String, String, String) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    onToggleShowAll: () -> Unit,
    onSetBudget: (String) -> Unit,
    onSetGoal: (String, String) -> Unit,
    onAddSavings: (String) -> Unit,
    onRemoveGoal: () -> Unit,
    onSetCurrency: (String) -> Unit,
    onEraseAll: () -> Unit
) {
    val palette = LocalHubPalette.current
    val budget = state.monthlyBudget
    val spent = state.spentThisMonth
    val budgetLeftText = if (budget > 0.0) {
        val rem = budget - spent
        if (rem >= 0.0) {
            "${HubFormatters.formatMoney(state.currency, rem)} left of your ${HubFormatters.formatMoney(state.currency, budget)} budget"
        } else {
            "Over budget by ${HubFormatters.formatMoney(state.currency, -rem)}"
        }
    } else {
        "Set a monthly budget to see what is left."
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Money Hero
        Column(modifier = Modifier.padding(top = 4.dp)) {
            Text(
                text = "Spent this month",
                style = MaterialTheme.typography.bodyLarge,
                color = palette.muted
            )
            Text(
                text = HubFormatters.formatMoney(state.currency, spent),
                fontFamily = SoraFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 42.sp,
                lineHeight = 46.sp,
                letterSpacing = (-1.6).sp,
                color = palette.spend,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Text(
                text = budgetLeftText,
                style = MaterialTheme.typography.bodyLarge,
                color = palette.muted
            )
        }

        // Summary Pills
        SummaryPillRow(
            pills = listOf(
                SummaryPillData(HubFormatters.formatMoney(state.currency, state.spentToday), "Today"),
                SummaryPillData(HubFormatters.formatMoney(state.currency, state.spentLast7Days), "Last 7 days"),
                SummaryPillData(HubFormatters.formatMoney(state.currency, state.earnedThisMonth), "Earned this month")
            )
        )

        // Sub-Tabs
        HubSegmentedTabs(
            tabs = listOf(
                "log" to "Log & History",
                "budget" to "Budget & Goals",
                "settings" to "Settings"
            ),
            selectedKey = state.moneyTab,
            onSelect = onSelectTab,
            testTagPrefix = "money_tab"
        )

        when (state.moneyTab) {
            "log" -> LogAndHistoryTabContent(
                state = state,
                onAddTransaction = onAddTransaction,
                onDeleteTransaction = onDeleteTransaction,
                onToggleShowAll = onToggleShowAll
            )
            "budget" -> BudgetAndGoalsTabContent(
                state = state,
                onSetBudget = onSetBudget,
                onSetGoal = onSetGoal,
                onAddSavings = onAddSavings,
                onRemoveGoal = onRemoveGoal
            )
            else -> SettingsTabContent(
                state = state,
                onSetCurrency = onSetCurrency,
                onEraseAll = onEraseAll
            )
        }
    }
}

@Composable
private fun LogAndHistoryTabContent(
    state: HubUiState,
    onAddTransaction: (String, String, String, String, String) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    onToggleShowAll: () -> Unit
) {
    val palette = LocalHubPalette.current
    val todayKey = remember(state.todayDate) {
        state.todayDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    var txTypeLabel by remember { mutableStateOf("Spent") }
    var txAmount by remember { mutableStateOf("") }
    var txCategory by remember { mutableStateOf(HubFormatters.MONEY_CATEGORIES.first()) }
    var txNote by remember { mutableStateOf("") }
    var txDate by remember(todayKey) { mutableStateOf(todayKey) }

    // Add Transaction Card
    HubCard {
        HubSectionHeader(title = "Add money in or out")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HubDropdownField(
                    label = "Type",
                    options = listOf("Spent", "Earned"),
                    selectedOption = txTypeLabel,
                    onSelect = { txTypeLabel = it },
                    modifier = Modifier.weight(0.9f)
                )
                HubTextField(
                    value = txAmount,
                    onValueChange = { txAmount = it },
                    placeholder = "Amount",
                    maxLength = 14,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1.1f)
                        .testTag("tx_amount_input")
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HubDropdownField(
                    label = "Category",
                    options = HubFormatters.MONEY_CATEGORIES,
                    selectedOption = txCategory,
                    onSelect = { txCategory = it },
                    modifier = Modifier.weight(1f)
                )
                HubDatePickerButton(
                    dateKey = txDate,
                    placeholder = "Date",
                    onDateSelected = { txDate = it },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HubTextField(
                    value = txNote,
                    onValueChange = { txNote = it },
                    placeholder = "What was it for?",
                    maxLength = 60,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tx_note_input")
                )
                HubPrimaryButton(
                    text = "Save",
                    onClick = {
                        val amt = txAmount.toDoubleOrNull()
                        if (amt != null && amt > 0.0) {
                            onAddTransaction(
                                txAmount,
                                if (txTypeLabel == "Earned") "e" else "s",
                                txCategory,
                                txNote,
                                txDate
                            )
                            txAmount = ""
                            txNote = ""
                        }
                    },
                    modifier = Modifier.testTag("tx_save_button")
                )
            }
        }
    }

    // History Card
    HubCard {
        HubSectionHeader(title = "History", subtitle = "Recent entries")
        if (state.transactions.isEmpty()) {
            Text(
                text = "No entries yet. Add your first one above.",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.muted
            )
        } else {
            val visible = if (state.showAllTransactions) state.transactions else state.transactions.take(12)
            visible.forEachIndexed { idx, tx ->
                if (idx > 0) HubRowDivider()
                val isSpend = tx.type == "s"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tx.note.ifBlank { tx.cat },
                            style = MaterialTheme.typography.bodyLarge,
                            color = palette.ink
                        )
                        Text(
                            text = "${tx.cat}, ${HubFormatters.formatShortDate(tx.date)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.muted
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = (if (isSpend) "-" else "+") + HubFormatters.formatMoney(state.currency, tx.amt),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isSpend) palette.spend else palette.earn
                    )
                    HubDeleteButton(
                        onClick = { onDeleteTransaction(tx.id) },
                        contentDesc = "Delete entry"
                    )
                }
            }
            if (state.transactions.size > 12) {
                Spacer(Modifier.height(10.dp))
                HubAltButton(
                    text = if (state.showAllTransactions) "Show fewer" else "Show all ${state.transactions.size}",
                    onClick = onToggleShowAll
                )
            }
        }
    }
}

@Composable
private fun BudgetAndGoalsTabContent(
    state: HubUiState,
    onSetBudget: (String) -> Unit,
    onSetGoal: (String, String) -> Unit,
    onAddSavings: (String) -> Unit,
    onRemoveGoal: () -> Unit
) {
    val palette = LocalHubPalette.current
    var budgetInput by remember { mutableStateOf("") }
    var goalNameInput by remember { mutableStateOf("") }
    var goalTargetInput by remember { mutableStateOf("") }
    var addSavingsInput by remember { mutableStateOf("") }

    // Monthly Budget Card
    HubCard {
        HubSectionHeader(title = "Monthly budget")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HubTextField(
                value = budgetInput,
                onValueChange = { budgetInput = it },
                placeholder = "How much can you spend this month?",
                maxLength = 12,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .weight(1f)
                    .testTag("budget_input")
            )
            HubPrimaryButton(
                text = "Set budget",
                onClick = {
                    val v = budgetInput.toDoubleOrNull()
                    if (v != null && v >= 0.0) {
                        onSetBudget(budgetInput)
                        budgetInput = ""
                    }
                },
                modifier = Modifier.testTag("set_budget_button")
            )
        }

        val budget = state.monthlyBudget
        val spent = state.spentThisMonth
        val pct = if (budget > 0.0) (spent / budget).toFloat() else 0f
        val barColor = when {
            budget > 0.0 && spent > budget -> palette.spend
            budget > 0.0 && pct >= 0.8f -> palette.warn
            else -> palette.accent
        }

        HubProgressBar(
            progress = pct.coerceIn(0f, 1f),
            barColor = barColor,
            heightDp = 10,
            modifier = Modifier.padding(vertical = 6.dp)
        )

        val note = if (budget > 0.0) {
            val usedPct = (spent / budget * 100.0).roundToInt()
            val rem = budget - spent
            val dailyNote = if (rem > 0.0) {
                val daysLeft = HubFormatters.remainingDaysInMonth(state.todayDate)
                " About ${HubFormatters.formatMoney(state.currency, rem / daysLeft)} a day to stay on track."
            } else ""
            "$usedPct% of your budget used.$dailyNote"
        } else {
            "No budget set yet."
        }
        Text(
            text = note,
            style = MaterialTheme.typography.bodyMedium,
            color = palette.muted
        )
    }

    // Where It Went Card
    HubCard {
        HubSectionHeader(title = "Where it went", subtitle = "This month")
        if (state.categorySpends.isEmpty()) {
            Text(
                text = "Nothing spent this month yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.muted
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.categorySpends.forEach { cat ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = cat.category,
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.width(86.dp)
                        )
                        HubProgressBar(
                            progress = cat.ratio,
                            barColor = palette.accent,
                            heightDp = 10,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = HubFormatters.formatMoney(state.currency, cat.amount),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = palette.ink
                        )
                    }
                }
            }
        }
    }

    // Savings Goal Card
    HubCard {
        HubSectionHeader(title = "Savings goal")
        Column(
            modifier = Modifier.padding(bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HubTextField(
                value = goalNameInput,
                onValueChange = { goalNameInput = it },
                placeholder = "Saving for, like Headphones",
                maxLength = 40,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("goal_name_input")
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HubTextField(
                    value = goalTargetInput,
                    onValueChange = { goalTargetInput = it },
                    placeholder = "Target",
                    maxLength = 12,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("goal_target_input")
                )
                HubPrimaryButton(
                    text = "Set goal",
                    onClick = {
                        val t = goalTargetInput.toDoubleOrNull()
                        if (goalNameInput.isNotBlank() && t != null && t > 0.0) {
                            onSetGoal(goalNameInput, goalTargetInput)
                            goalNameInput = ""
                            goalTargetInput = ""
                        }
                    },
                    modifier = Modifier.testTag("set_goal_button")
                )
            }
        }

        if (state.goalName.isBlank() || state.goalTarget <= 0.0) {
            Text(
                text = "Pick something to save for and track how close you are.",
                style = MaterialTheme.typography.bodyMedium,
                color = palette.muted
            )
        } else {
            val pct = (state.goalSaved / state.goalTarget).toFloat().coerceIn(0f, 1f)
            Text(
                text = state.goalName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = palette.ink
            )
            HubProgressBar(
                progress = pct,
                barColor = palette.accent,
                heightDp = 10,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            val statusMsg = buildString {
                append(HubFormatters.formatMoney(state.currency, state.goalSaved))
                append(" of ")
                append(HubFormatters.formatMoney(state.currency, state.goalTarget))
                if (state.goalSaved >= state.goalTarget) {
                    append(". Goal reached!")
                } else {
                    append(". ")
                    append(HubFormatters.formatMoney(state.currency, state.goalTarget - state.goalSaved))
                    append(" to go.")
                }
            }
            Text(
                text = statusMsg,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.muted,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HubTextField(
                    value = addSavingsInput,
                    onValueChange = { addSavingsInput = it },
                    placeholder = "Add to savings",
                    maxLength = 12,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                HubPrimaryButton(
                    text = "Add savings",
                    onClick = {
                        val v = addSavingsInput.toDoubleOrNull()
                        if (v != null && v > 0.0) {
                            onAddSavings(addSavingsInput)
                            addSavingsInput = ""
                        }
                    }
                )
            }
            Spacer(Modifier.height(8.dp))
            HubAltButton(
                text = "Remove goal",
                onClick = onRemoveGoal
            )
        }
    }
}

@Composable
private fun SettingsTabContent(
    state: HubUiState,
    onSetCurrency: (String) -> Unit,
    onEraseAll: () -> Unit
) {
    val palette = LocalHubPalette.current
    var showErase by remember { mutableStateOf(false) }
    var wipeArmed by remember { mutableStateOf(false) }

    HubCard {
        HubSectionHeader(title = "Money settings")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Currency symbol",
                style = MaterialTheme.typography.bodyLarge,
                color = palette.muted
            )
            HubDropdownField(
                label = "Currency",
                options = HubFormatters.CURRENCY_SYMBOLS,
                selectedOption = state.currency,
                onSelect = onSetCurrency,
                modifier = Modifier.width(100.dp)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                    showErase = !showErase
                    if (!showErase) wipeArmed = false
                }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (showErase) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = palette.accent
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "Erase everything",
                style = MaterialTheme.typography.labelLarge,
                color = palette.accent
            )
        }

        AnimatedVisibility(visible = showErase) {
            Column(
                modifier = Modifier.padding(top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "This deletes every money entry, your budget, and your savings goal on this device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.muted
                )
                HubAltButton(
                    text = if (wipeArmed) "Tap again to erase everything" else "Erase all data",
                    onClick = {
                        if (!wipeArmed) {
                            wipeArmed = true
                        } else {
                            onEraseAll()
                            wipeArmed = false
                        }
                    },
                    modifier = Modifier.testTag("erase_all_button")
                )
            }
        }
    }
}
