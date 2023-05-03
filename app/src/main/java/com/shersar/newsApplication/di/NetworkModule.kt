package com.shersar.newsApplication.di

import com.shersar.newsApplication.api.NewsAPI
import com.shersar.newsApplication.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, factory: GsonConverterFactory): Retrofit {
        return Retrofit
            .Builder()
            .baseUrl(Constants.BASE_URL)
            .client(client)
            .addConverterFactory(factory)
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): NewsAPI {
        return retrofit.create(NewsAPI::class.java)
    }

    @Provides
    @Singleton
    fun provideFactory(): GsonConverterFactory {
        return GsonConverterFactory.create()
    }


}