package com.navigine.naviginedemocompose.core.di.network

import com.navigine.naviginedemocompose.BuildConfig
import com.navigine.naviginedemocompose.data.local.HostUrlStore
import com.navigine.naviginedemocompose.data.network.auth.AuthApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideLogging(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    @Provides
    @Singleton
    fun provideDynamicBaseUrlInterceptor(store: HostUrlStore): Interceptor = Interceptor { chain ->
        val req = chain.request()
        val savedBase = runBlocking { store.serverUrlFlow.first() } // small, once per app start typically
        val base = (savedBase.ifBlank { BuildConfig.DEFAULT_SERVER_URL }).trim()
        val baseUrl = base.toHttpUrl()
        val newUrl = req.url.newBuilder()
            .scheme(baseUrl.scheme)
            .host(baseUrl.host)
            .port(baseUrl.port)
            .build()
        chain.proceed(req.newBuilder().url(newUrl).build())
    }

    @Provides
    @Singleton
    fun provideOkHttp(logging: HttpLoggingInterceptor, dynBase: Interceptor): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(dynBase)
            .addInterceptor(logging)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, moshi: Moshi): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.DEFAULT_SERVER_URL) // real host swapped by interceptor
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    // Factory for ad-hoc hosts (used by server validation)
    @Provides
    @Singleton
    fun provideRetrofitFactory(moshi: Moshi): RetrofitFactory = RetrofitFactory { baseUrl ->
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(OkHttpClient.Builder() // lightweight client without dynamic base swap
                .addInterceptor(provideLogging())
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)
}

fun interface RetrofitFactory {
    fun create(baseUrl: String): Retrofit
}