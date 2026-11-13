package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.hubDataStore: DataStore<Preferences> by preferencesDataStore(name = "hub_prefs")

data class HubPrefsState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val activeSection: String = "school",
    val schoolTab: String = "tomorrow",
    val moneyTab: String = "log",
    val monthlyBudget: Double = 0.0,
    val currency: String = "$",
    val goalName: String = "",
    val goalTarget: Double = 0.0,
    val goalSaved: Double = 0.0
)

class HubPreferences(private val context: Context) {
    private object Keys {
        val THEME = stringPreferencesKey("hub_theme")
        val APP = stringPreferencesKey("hub_app")
        val SP_TAB = stringPreferencesKey("sp_tab")
        val MM_TAB = stringPreferencesKey("mm_tab")
        val MM_BUDGET = doublePreferencesKey("mm_budget")
        val MM_CUR = stringPreferencesKey("mm_cur")
        val MM_GOAL_NAME = stringPreferencesKey("mm_goal_name")
        val MM_GOAL_TARGET = doublePreferencesKey("mm_goal_target")
        val MM_GOAL_SAVED = doublePreferencesKey("mm_goal_saved")
    }

    val prefsFlow: Flow<HubPrefsState> = context.hubDataStore.data.map { prefs ->
        HubPrefsState(
            themeMode = ThemeMode.fromKey(prefs[Keys.THEME] ?: "system"),
            activeSection = prefs[Keys.APP] ?: "school",
            schoolTab = prefs[Keys.SP_TAB] ?: "tomorrow",
            moneyTab = prefs[Keys.MM_TAB] ?: "log",
            monthlyBudget = prefs[Keys.MM_BUDGET] ?: 0.0,
            currency = prefs[Keys.MM_CUR] ?: "$",
            goalName = prefs[Keys.MM_GOAL_NAME] ?: "",
            goalTarget = prefs[Keys.MM_GOAL_TARGET] ?: 0.0,
            goalSaved = prefs[Keys.MM_GOAL_SAVED] ?: 0.0
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.hubDataStore.edit { it[Keys.THEME] = mode.key }
    }

    suspend fun setActiveSection(section: String) {
        context.hubDataStore.edit { it[Keys.APP] = section }
    }

    suspend fun setSchoolTab(tab: String) {
        context.hubDataStore.edit { it[Keys.SP_TAB] = tab }
    }

    suspend fun setMoneyTab(tab: String) {
        context.hubDataStore.edit { it[Keys.MM_TAB] = tab }
    }

    suspend fun setMonthlyBudget(amount: Double) {
        context.hubDataStore.edit { it[Keys.MM_BUDGET] = amount }
    }

    suspend fun setCurrency(symbol: String) {
        context.hubDataStore.edit { it[Keys.MM_CUR] = symbol }
    }

    suspend fun setSavingsGoal(name: String, target: Double, saved: Double = 0.0) {
        context.hubDataStore.edit {
            it[Keys.MM_GOAL_NAME] = name
            it[Keys.MM_GOAL_TARGET] = target
            it[Keys.MM_GOAL_SAVED] = saved
        }
    }

    suspend fun updateGoalSaved(saved: Double) {
        context.hubDataStore.edit { it[Keys.MM_GOAL_SAVED] = saved }
    }

    suspend fun clearGoal() {
        context.hubDataStore.edit {
            it.remove(Keys.MM_GOAL_NAME)
            it.remove(Keys.MM_GOAL_TARGET)
            it.remove(Keys.MM_GOAL_SAVED)
        }
    }

    suspend fun clearMoneySettings() {
        context.hubDataStore.edit {
            it[Keys.MM_BUDGET] = 0.0
            it.remove(Keys.MM_GOAL_NAME)
            it.remove(Keys.MM_GOAL_TARGET)
            it.remove(Keys.MM_GOAL_SAVED)
        }
    }
}
