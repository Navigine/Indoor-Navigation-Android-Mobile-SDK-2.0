package com.navigine.naviginedemocompose.core.di

import com.navigine.naviginedemocompose.data.monitor.LocationMonitorImpl
import com.navigine.naviginedemocompose.data.monitor.PositionMonitorImpl
import com.navigine.naviginedemocompose.data.monitor.RouteMonitorImpl
import com.navigine.naviginedemocompose.data.repository.AuthRepositoryImpl
import com.navigine.naviginedemocompose.data.repository.LocationsRepositoryImpl
import com.navigine.naviginedemocompose.domain.monitor.LocationMonitor
import com.navigine.naviginedemocompose.domain.monitor.PositionMonitor
import com.navigine.naviginedemocompose.domain.monitor.RouteMonitor
import com.navigine.naviginedemocompose.domain.repository.AuthRepository
import com.navigine.naviginedemocompose.domain.repository.LocationsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryBinder {

    @Binds
    @Singleton
    fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
    @Binds @Singleton
    fun bindLocationsRepository(impl: LocationsRepositoryImpl) : LocationsRepository
    @Binds @Singleton
    fun bindLocationMonitor(impl: LocationMonitorImpl) : LocationMonitor
    @Binds @Singleton
    fun bindPositionMonitor(impl: PositionMonitorImpl) : PositionMonitor
    @Binds @Singleton
    fun bindRouteMonitor(impl: RouteMonitorImpl) : RouteMonitor
}

