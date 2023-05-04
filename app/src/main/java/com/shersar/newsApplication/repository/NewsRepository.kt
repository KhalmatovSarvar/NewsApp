package com.shersar.newsApplication.repository

import com.shersar.newsApplication.models.Article
import com.shersar.newsApplication.models.NewsResponse
import kotlinx.coroutines.flow.Flow

interface NewsRepository {
    suspend fun getBreakingNews(country: String) : Flow<Result<NewsResponse>>
}