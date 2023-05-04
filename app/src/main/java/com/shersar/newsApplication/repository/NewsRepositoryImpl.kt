package com.shersar.newsApplication.repository

import com.shersar.newsApplication.api.NewsAPI
import com.shersar.newsApplication.db.ArticleDao
import com.shersar.newsApplication.models.Article
import com.shersar.newsApplication.models.NewsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(
    private val articleDao: ArticleDao,
    private val apiService: NewsAPI,
) : NewsRepository {

    private var currentNewsPage = 1
    private var receivedNewsCount = 0
    private var totalNewsCount = 0
    private var receivedAtLeastOnce = false


    override suspend fun getBreakingNews(country: String) = flow<Result<NewsResponse>> {
        if (receivedAtLeastOnce && receivedNewsCount == totalNewsCount) {
            emit(Result.failure(Error("There is no news more")))
        } else {
            val response = apiService.getBreakingNews(country, currentNewsPage++)
            if (response.isSuccessful) {
                response.body()?.let {
                    if (!receivedAtLeastOnce) {
                        totalNewsCount = it.totalResults
                    }
                    receivedNewsCount += it.articles.size
                    emit(Result.success(it))
                } ?: emit(Result.failure(Error("Null body")))
            } else {
                emit(Result.failure(Error("Something went wrong")))
            }
        }
    }.flowOn(Dispatchers.IO)

    suspend fun searchNews(searchQuery: String, pageNumber: Int) =
        apiService.searchForNews(searchQuery, pageNumber)

    suspend fun upsert(article: Article) = articleDao.upsert(article)

    fun getSavedNews() = articleDao.getAllArticles()

    suspend fun deleteArticle(article: Article) = articleDao.deleteArticle(article)
}