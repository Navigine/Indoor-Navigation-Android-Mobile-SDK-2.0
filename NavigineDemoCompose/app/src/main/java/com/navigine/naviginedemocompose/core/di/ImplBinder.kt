package com.navigine.naviginedemocompose.core.di

import com.navigine.naviginedemocompose.data.monitor.LocationMonitorImpl
import com.navigine.naviginedemocompose.data.monitor.MeasurementMonitorImpl
import com.navigine.naviginedemocompose.data.monitor.PositionMonitorImpl
import com.navigine.naviginedemocompose.data.monitor.RouteMonitorImpl
import com.navigine.naviginedemocompose.data.monitor.SystemSettingsMonitorImpl
import com.navigine.naviginedemocompose.data.repository.AuthRepositoryImpl
import com.navigine.naviginedemocompose.data.repository.DebugRepositoryImpl
import com.navigine.naviginedemocompose.data.repository.LocationsRepositoryImpl
import com.navigine.naviginedemocompose.data.repository.ProfileRepositoryImpl
import com.navigine.naviginedemocompose.domain.monitor.LocationMonitor
import com.navigine.naviginedemocompose.domain.monitor.MeasurementMonitor
import com.navigine.naviginedemocompose.domain.monitor.PositionMonitor
import com.navigine.naviginedemocompose.domain.monitor.RouteMonitor
import com.navigine.naviginedemocompose.domain.monitor.SystemSettingsMonitor
import com.navigine.naviginedemocompose.domain.repository.AuthRepository
import com.navigine.naviginedemocompose.domain.repository.DebugRepository
import com.navigine.naviginedemocompose.domain.repository.LocationsRepository
import com.navigine.naviginedemocompose.domain.repository.ProfileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface ImplBinder {

    @Binds @Singleton
    fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
    @Binds @Singleton
    fun bindLocationsRepository(impl: LocationsRepositoryImpl) : LocationsRepository
    @Binds @Singleton
    fun bindProfileRepository(impl: ProfileRepositoryImpl) : ProfileRepository
    @Binds @Singleton
    fun bindDebugRepository(impl: DebugRepositoryImpl) : DebugRepository

    @Binds @Singleton
    fun bindLocationMonitor(impl: LocationMonitorImpl) : LocationMonitor
    @Binds @Singleton
    fun bindPositionMonitor(impl: PositionMonitorImpl) : PositionMonitor
    @Binds @Singleton
    fun bindRouteMonitor(impl: RouteMonitorImpl) : RouteMonitor
    @Binds @Singleton
    fun bindMeasurementMonitor(impl: MeasurementMonitorImpl) : MeasurementMonitor
    @Binds @Singleton
    fun bindSystemSettingsMonitor(impl: SystemSettingsMonitorImpl) : SystemSettingsMonitor
}

