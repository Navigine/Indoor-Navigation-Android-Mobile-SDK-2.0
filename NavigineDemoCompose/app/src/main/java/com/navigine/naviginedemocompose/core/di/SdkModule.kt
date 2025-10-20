package com.navigine.naviginedemocompose.core.di

import android.content.Context
import com.navigine.naviginedemocompose.core.sdk.NavigineSdkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object SdkModule {
    @Provides
    @Singleton
    fun provideNavigineSdkManager(@ApplicationContext context : Context) = NavigineSdkManager(context)
}