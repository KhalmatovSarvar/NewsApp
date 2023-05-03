package com.shersar.newsApplication.repository

import com.shersar.newsApplication.api.NewsAPI
import com.shersar.newsApplication.db.ArticleDao
import com.shersar.newsApplication.models.Article
import javax.inject.Inject

class NewsRepository @Inject constructor(
    private val articleDao: ArticleDao,
    private val apiService: NewsAPI,

    ) {

    suspend fun getBreakingNews(country: String, pageNumber: Int) =
        apiService.getBreakingNews(country, pageNumber)

    suspend fun searchNews(searchQuery: String, pageNumber: Int) =
        apiService.searchForNews(searchQuery, pageNumber)

    suspend fun upsert(article: Article) = articleDao.upsert(article)

    fun getSavedNews() = articleDao.getAllArticles()

    suspend fun deleteArticle(article: Article) = articleDao.deleteArticle(article)
}