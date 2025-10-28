package com.navigine.naviginedemocompose.domain.repository

import com.navigine.naviginedemocompose.domain.model.DebugSnapshot
import kotlinx.coroutines.flow.Flow

interface DebugRepository {
    fun observeSnapshot(): Flow<DebugSnapshot>
}