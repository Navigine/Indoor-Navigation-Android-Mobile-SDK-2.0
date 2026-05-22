package com.navigine.naviginedemocompose.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.navigine.naviginedemocompose.core.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.authDataStore by preferencesDataStore(Constants.DATA_STORE_NAME_USER)

class UserStore(private val context: Context) {

    private val KEY_USER_HASH = stringPreferencesKey(Constants.DATA_STORE_USER_HASH)
    private val KEY_USER_ID = stringPreferencesKey(Constants.DATA_STORE_USER_ID)
    private val KEY_USER_NAME = stringPreferencesKey(Constants.DATA_STORE_USER_NAME)
    private val KEY_AVATAR_URL = stringPreferencesKey(Constants.DATA_STORE_AVATAR_URL)
    private val KEY_USER_COMPANY = stringPreferencesKey(Constants.DATA_STORE_USER_COMPANY)
    private val KEY_USER_EMAIL = stringPreferencesKey(Constants.DATA_STORE_USER_EMAIL)
    private val KEY_SAVED_LOCATION_ID = intPreferencesKey(Constants.DATA_STORE_SAVED_LOCATION_ID)


    val userHashFlow: Flow<String> = context.authDataStore.data.map { it[KEY_USER_HASH] ?: "" }
    val userIdFlow: Flow<String> = context.authDataStore.data.map { it[KEY_USER_ID] ?: "" }
    val userNameFlow: Flow<String> = context.authDataStore.data.map { it[KEY_USER_NAME] ?: "" }
    val userEmailFlow: Flow<String> = context.authDataStore.data.map { it[KEY_USER_EMAIL] ?: "" }
    val userCompanyFlow: Flow<String> = context.authDataStore.data.map { it[KEY_USER_COMPANY] ?: "" }
    val userAvatarFlow: Flow<String> = context.authDataStore.data.map { it[KEY_AVATAR_URL] ?: "" }
    val savedLocationIdFlow: Flow<Int> = context.authDataStore.data.map { it[KEY_SAVED_LOCATION_ID] ?: 0 }

    suspend fun setLoggedIn(
        hash: String,
        name: String?,
        avatar: String?,
        company: String?,
        email: String?,
        id: String
    ) {
        context.authDataStore.edit {
            it[KEY_USER_HASH] = hash
            it[KEY_USER_ID] = id
            email?.let { e -> it[KEY_USER_EMAIL] = e }
            name?.let { n -> it[KEY_USER_NAME] = n }
            avatar?.let { a -> it[KEY_AVATAR_URL] = a }
            company?.let { c -> it[KEY_USER_COMPANY] = c }
        }
    }

    suspend fun saveLocationId(
        id: Int
    ) {
        context.authDataStore.edit {
            it[KEY_SAVED_LOCATION_ID] = id
        }
    }

    suspend fun logout() {
        context.authDataStore.edit { it.clear() }
    }
}