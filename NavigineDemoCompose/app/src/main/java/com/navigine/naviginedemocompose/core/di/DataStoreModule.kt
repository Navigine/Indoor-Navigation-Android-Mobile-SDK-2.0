package com.navigine.naviginedemocompose.core.di

import android.content.Context
import com.navigine.naviginedemocompose.data.local.HostUrlStore
import com.navigine.naviginedemocompose.data.local.UserStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideHostUrlStore(@ApplicationContext ctx: Context): HostUrlStore = HostUrlStore(ctx)

    @Provides
    @Singleton
    fun provideUserStore(@ApplicationContext ctx: Context) : UserStore = UserStore(ctx)
}