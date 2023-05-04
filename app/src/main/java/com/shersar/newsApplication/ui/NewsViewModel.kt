package com.shersar.newsApplication.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_ETHERNET
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.shersar.newsApplication.NewsApp
import com.shersar.newsApplication.models.Article
import com.shersar.newsApplication.models.NewsResponse
import com.shersar.newsApplication.repository.NewsRepository
import com.shersar.newsApplication.repository.NewsRepositoryImpl
import com.shersar.newsApplication.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    app: Application,
    private val newsRepository: NewsRepositoryImpl,
    private val newsRepository2: NewsRepository
) : AndroidViewModel(app), NewsFragmentViewModel {


    override val breakingNewsLiveData = MutableLiveData<Resource<NewsResponse>>()

    var breakingNewsPage = 1;
    var breakingNewsResponse: NewsResponse? = null

    val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNewsPage = 1;
    var searchNewsResponse: NewsResponse? = null

    init {
        getBreakingNews("us")
    }

    fun getBreakingNews(countryCode: String) = viewModelScope.launch {
        safeBreakingNewsCall(countryCode)
    }


    fun searchNews(searchQuery: String) = viewModelScope.launch {
        safeSearchNewsCall(searchQuery)
    }


    private fun handleBreakingNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                breakingNewsPage++
                if (breakingNewsResponse == null) {
                    breakingNewsResponse = resultResponse
                } else {
                    val oldArticles = breakingNewsResponse?.articles
                    val newArticles = resultResponse.articles
//                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(breakingNewsResponse ?: resultResponse)
            }
        }

        return Resource.Error(response.message())

    }

    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                searchNewsPage++
                if (searchNewsResponse == null) {
                    searchNewsResponse = resultResponse
                } else {
                    val oldArticles = searchNewsResponse?.articles
                    val newArticles = resultResponse.articles
//                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(searchNewsResponse ?: resultResponse)
            }
        }

        return Resource.Error(response.message())

    }

    fun saveArticle(article: Article) = viewModelScope.launch {
        newsRepository.upsert(article)
    }

    fun getSavedNews() = newsRepository.getSavedNews()

    fun deleteArticle(article: Article) = viewModelScope.launch {
        newsRepository.deleteArticle(article)
    }

    suspend fun safeBreakingNewsCall(countryCode: String) {
        newsRepository
            .getBreakingNews(countryCode)
            .onStart {
                breakingNewsLiveData.postValue(Resource.Loading())
            }
            .onEach {
                it.fold(
                    {
                        breakingNewsLiveData.postValue(Resource.Success(it))
                    },
                    {
                        breakingNewsLiveData.postValue(Resource.Error(it.message.toString()))
                    }
                )
            }
            .catch {
                breakingNewsLiveData.postValue(Resource.Error(it.message.toString()))
            }.launchIn(viewModelScope)
    }

    suspend fun safeSearchNewsCall(searchQuery: String) {
        try {
            if (hasInternetConnection()) {
                val response = newsRepository.searchNews(searchQuery, searchNewsPage)
                searchNews.postValue(handleSearchNewsResponse(response))
            } else {
                searchNews.postValue(Resource.Error("No Internet connection"))
            }

        } catch (t: Throwable) {
            when (t) {
                is IOException -> {
                    searchNews.postValue(Resource.Error("Network failure"))
                }

                else -> searchNews.postValue(Resource.Error("Conversion error"))
            }

        }
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<NewsApp>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return when {
            capabilities.hasTransport(TRANSPORT_WIFI) -> true
            capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
            else -> false
        }
        return false
    }

}