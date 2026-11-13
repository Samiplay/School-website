package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "hub_preferences")

enum class ThemePreference(val key: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromKey(key: String?): ThemePreference =
            entries.firstOrNull { it.key == key } ?: SYSTEM
    }
}

enum class MainSection(val key: String) {
    SCHOOL("school"),
    MONEY("money");

    companion object {
        fun fromKey(key: String?): MainSection =
            entries.firstOrNull { it.key == key } ?: SCHOOL
    }
}

data class HubPreferences(
    val theme: ThemePreference = ThemePreference.SYSTEM,
    val activeSection: MainSection = MainSection.SCHOOL,
    val schoolTab: String = "tomorrow",
    val moneyTab: String = "log",
    val monthlyBudget: Double = 0.0,
    val currencySymbol: String = "$"
)

class PreferencesManager(private val context: Context) {

    private object Keys {
        val THEME = stringPreferencesKey("hub_theme")
        val ACTIVE_SECTION = stringPreferencesKey("hub_app")
        val SCHOOL_TAB = stringPreferencesKey("sp_tab")
        val MONEY_TAB = stringPreferencesKey("mm_tab")
        val MONTHLY_BUDGET = doublePreferencesKey("mm_budget")
        val CURRENCY_SYMBOL = stringPreferencesKey("mm_cur")
    }

    val preferencesFlow: Flow<HubPreferences> = context.dataStore.data.map { prefs ->
        HubPreferences(
            theme = ThemePreference.fromKey(prefs[Keys.THEME]),
            activeSection = MainSection.fromKey(prefs[Keys.ACTIVE_SECTION]),
            schoolTab = prefs[Keys.SCHOOL_TAB] ?: "tomorrow",
            moneyTab = prefs[Keys.MONEY_TAB] ?: "log",
            monthlyBudget = prefs[Keys.MONTHLY_BUDGET] ?: 0.0,
            currencySymbol = prefs[Keys.CURRENCY_SYMBOL] ?: "$"
        )
    }

    suspend fun setTheme(theme: ThemePreference) {
        context.dataStore.edit { it[Keys.THEME] = theme.key }
    }

    suspend fun setActiveSection(section: MainSection) {
        context.dataStore.edit { it[Keys.ACTIVE_SECTION] = section.key }
    }

    suspend fun setSchoolTab(tab: String) {
        context.dataStore.edit { it[Keys.SCHOOL_TAB] = tab }
    }

    suspend fun setMoneyTab(tab: String) {
        context.dataStore.edit { it[Keys.MONEY_TAB] = tab }
    }

    suspend fun setMonthlyBudget(budget: Double) {
        context.dataStore.edit { it[Keys.MONTHLY_BUDGET] = budget }
    }

    suspend fun setCurrencySymbol(symbol: String) {
        context.dataStore.edit { it[Keys.CURRENCY_SYMBOL] = symbol }
    }

    suspend fun resetMoneyPreferences() {
        context.dataStore.edit {
            it[Keys.MONTHLY_BUDGET] = 0.0
        }
    }
}
