package com.shersar.newsApplication.di

import com.shersar.newsApplication.repository.NewsRepository
import com.shersar.newsApplication.repository.NewsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {
    @[Binds Singleton]
    fun bindRepository(impl: NewsRepositoryImpl) : NewsRepository
}