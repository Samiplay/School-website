package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "my_hub_prefs")

data class UserPrefs(
    val themeMode: String = "system", // "system", "light", "dark"
    val activeSection: String = "school", // "school", "money"
    val schoolTab: String = "tomorrow", // "tomorrow", "work", "exams"
    val moneyTab: String = "log", // "log", "budget", "settings"
    val currencySymbol: String = "$",
    val monthlyBudget: Double = 0.0,
    val savingsGoalName: String? = null,
    val savingsGoalTarget: Double = 0.0,
    val savingsGoalSaved: Double = 0.0
)

class HubPreferences(private val context: Context) {
    private object Keys {
        val THEME_MODE = stringPreferencesKey("hub_theme")
        val ACTIVE_SECTION = stringPreferencesKey("hub_app")
        val SCHOOL_TAB = stringPreferencesKey("sp_tab")
        val MONEY_TAB = stringPreferencesKey("mm_tab")
        val CURRENCY = stringPreferencesKey("mm_cur")
        val BUDGET = doublePreferencesKey("mm_budget")
        val GOAL_NAME = stringPreferencesKey("mm_goal_name")
        val GOAL_TARGET = doublePreferencesKey("mm_goal_target")
        val GOAL_SAVED = doublePreferencesKey("mm_goal_saved")
    }

    val prefsFlow: Flow<UserPrefs> = context.dataStore.data.map { prefs ->
        UserPrefs(
            themeMode = prefs[Keys.THEME_MODE] ?: "system",
            activeSection = prefs[Keys.ACTIVE_SECTION] ?: "school",
            schoolTab = prefs[Keys.SCHOOL_TAB] ?: "tomorrow",
            moneyTab = prefs[Keys.MONEY_TAB] ?: "log",
            currencySymbol = prefs[Keys.CURRENCY] ?: "$",
            monthlyBudget = prefs[Keys.BUDGET] ?: 0.0,
            savingsGoalName = prefs[Keys.GOAL_NAME]?.takeIf { it.isNotBlank() },
            savingsGoalTarget = prefs[Keys.GOAL_TARGET] ?: 0.0,
            savingsGoalSaved = prefs[Keys.GOAL_SAVED] ?: 0.0
        )
    }

    suspend fun getSnapshot(): UserPrefs = prefsFlow.first()

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode }
    }

    suspend fun setActiveSection(section: String) {
        context.dataStore.edit { it[Keys.ACTIVE_SECTION] = section }
    }

    suspend fun setSchoolTab(tab: String) {
        context.dataStore.edit { it[Keys.SCHOOL_TAB] = tab }
    }

    suspend fun setMoneyTab(tab: String) {
        context.dataStore.edit { it[Keys.MONEY_TAB] = tab }
    }

    suspend fun setCurrency(symbol: String) {
        context.dataStore.edit { it[Keys.CURRENCY] = symbol }
    }

    suspend fun setMonthlyBudget(amount: Double) {
        context.dataStore.edit { it[Keys.BUDGET] = amount }
    }

    suspend fun setSavingsGoal(name: String, target: Double, saved: Double = 0.0) {
        context.dataStore.edit {
            it[Keys.GOAL_NAME] = name
            it[Keys.GOAL_TARGET] = target
            it[Keys.GOAL_SAVED] = saved
        }
    }

    suspend fun addSavings(delta: Double) {
        context.dataStore.edit {
            val current = it[Keys.GOAL_SAVED] ?: 0.0
            it[Keys.GOAL_SAVED] = kotlin.math.round((current + delta) * 100.0) / 100.0
        }
    }

    suspend fun clearSavingsGoal() {
        context.dataStore.edit {
            it.remove(Keys.GOAL_NAME)
            it.remove(Keys.GOAL_TARGET)
            it.remove(Keys.GOAL_SAVED)
        }
    }

    suspend fun eraseMoneySettings() {
        context.dataStore.edit {
            it[Keys.BUDGET] = 0.0
            it.remove(Keys.GOAL_NAME)
            it.remove(Keys.GOAL_TARGET)
            it.remove(Keys.GOAL_SAVED)
        }
    }
}
