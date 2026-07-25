package com.seacliff.pos.di

import com.seacliff.pos.BuildConfig
import com.seacliff.pos.data.remote.api.ApiService
import com.seacliff.pos.data.remote.api.AuthInterceptor
import com.seacliff.pos.data.remote.api.TokenProvider
import com.seacliff.pos.data.local.prefs.PreferencesManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import timber.log.Timber
import java.net.InetAddress
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .setLenient()
            .create()
    }

    @Provides
    @Singleton
    fun provideTokenProvider(preferencesManager: PreferencesManager): TokenProvider {
        return preferencesManager
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenProvider: TokenProvider): AuthInterceptor {
        return AuthInterceptor(tokenProvider)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        // Custom DNS resolver with logging and IPv4 preference
        val dns = object : Dns {
            override fun lookup(hostname: String): List<InetAddress> {
                Timber.d("DNS lookup for: $hostname")
                return try {
                    val addresses = InetAddress.getAllByName(hostname).toList()
                    // Prefer IPv4 addresses
                    val sortedAddresses = addresses.sortedBy {
                        if (it.address.size == 4) 0 else 1
                    }
                    Timber.d("DNS resolved $hostname to: ${sortedAddresses.map { it.hostAddress }}")
                    if (sortedAddresses.isEmpty()) {
                        throw java.net.UnknownHostException("No addresses found for $hostname")
                    }
                    sortedAddresses
                } catch (e: Exception) {
                    Timber.e(e, "DNS lookup failed for $hostname")
                    throw e
                }
            }
        }

        return OkHttpClient.Builder()
            .dns(dns)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(BuildConfig.API_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .readTimeout(BuildConfig.API_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .writeTimeout(BuildConfig.API_TIMEOUT.toLong(), TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}
