package com.shersar.newsApplication.di

import android.content.Context
import com.shersar.newsApplication.api.NewsAPI
import com.shersar.newsApplication.db.AppDatabase
import com.shersar.newsApplication.db.ArticleDao
import com.shersar.newsApplication.repository.NewsRepository
import com.shersar.newsApplication.utils.NetworkConnectivityObserver

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @[Singleton Provides]
    fun provideRoomDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Singleton
    @Provides
    fun provideArticleDao(appDatabase: AppDatabase): ArticleDao {
        return appDatabase.articleDao()
    }

    @Provides
    fun provideNewsRepository(articleDao: ArticleDao,api: NewsAPI): NewsRepository {
        return NewsRepository(articleDao,api)
    }


    @Provides
    fun provideNetworkConnectivityObserver(@ApplicationContext context: Context): NetworkConnectivityObserver {
        return NetworkConnectivityObserver(context)
    }


}


