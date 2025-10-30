package com.navigine.naviginedemocompose.core.di

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.navigine.naviginedemocompose.core.log.AppLogger
import com.navigine.naviginedemocompose.core.log.FirebaseAppLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAnalytics(@ApplicationContext context: Context): FirebaseAnalytics = FirebaseAnalytics.getInstance(context)

    @Provides
    @Singleton
    fun provideFirebaseCrashlytics(): FirebaseCrashlytics = FirebaseCrashlytics.getInstance()

    @Provides @Singleton
    fun provideAppLogger(
        analytics: FirebaseAnalytics,
        crash: FirebaseCrashlytics
    ): AppLogger = FirebaseAppLogger(analytics, crash)
}