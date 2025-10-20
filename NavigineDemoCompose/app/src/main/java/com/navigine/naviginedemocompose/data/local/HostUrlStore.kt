package com.navigine.naviginedemocompose.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.navigine.naviginedemocompose.BuildConfig
import com.navigine.naviginedemocompose.core.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(Constants.DATA_STORE_NAME_APP)

class HostUrlStore(private val context: Context) {

    companion object { private val KEY_HOST = stringPreferencesKey(Constants.DATA_STORE_SERVER_URL) }

    val serverUrlFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_HOST] ?: BuildConfig.DEFAULT_SERVER_URL
    }

    suspend fun setServerUrl(url: String) {
        context.dataStore.edit { it[KEY_HOST] = url }
    }
}