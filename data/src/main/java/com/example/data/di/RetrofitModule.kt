package com.example.data.di

import android.util.Log
import com.example.data.BuildConfig
import com.example.data.manager.PreferenceManager
import com.example.data.util.NetworkInterceptor
import com.example.data.util.SSLHelper
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher.SECRET_KEY
import javax.inject.Qualifier
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * 2023-02-02
 * pureum
 */
@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    private var gson: Gson = GsonBuilder().setLenient().create()

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class Login

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class Api

    @Singleton
    @Provides
    @Login
    fun provideLoginRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    //네트워크 통신 과정을 보기 위한 클라이언트
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor {
            val request = it.request()
                .newBuilder()
                .build()
            val response = it.proceed(request)
            response
        }
        .sslSocketFactory(SSLHelper.sslSocketFactory, SSLHelper.trustAllCerts[0] as X509TrustManager)
        .hostnameVerifier{_, _ -> true}
        .connectTimeout(300L, TimeUnit.MILLISECONDS)
        .readTimeout(300L, TimeUnit.MILLISECONDS)
        .writeTimeout(300L, TimeUnit.MILLISECONDS)
        .build()







    @Singleton
    @Provides
    fun provideInterceptor(preferenceManager: PreferenceManager): NetworkInterceptor =
        NetworkInterceptor(preferenceManager)

    @Singleton
    @Provides
    fun provideOkhttpApi(networkInterceptor: NetworkInterceptor): OkHttpClient =
        OkHttpClient().newBuilder()
            .addInterceptor(networkInterceptor)
            .sslSocketFactory(SSLHelper.sslSocketFactory, SSLHelper.trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .build()

    @Singleton
    @Provides
    @Api
    fun provideApiRetrofit(interceptorClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(interceptorClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
}