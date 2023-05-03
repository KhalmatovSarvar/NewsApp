package com.shersar.newsApplication.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_ETHERNET
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.shersar.newsApplication.NewsApp
import com.shersar.newsApplication.models.Article
import com.shersar.newsApplication.models.NewsResponse
import com.shersar.newsApplication.repository.NewsRepository
import com.shersar.newsApplication.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class NewsViewmodel @Inject constructor(
    app:Application,
    private val newsRepository: NewsRepository
) : AndroidViewModel(app) {


    val breakingNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
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
                if (breakingNewsResponse == null){
                    breakingNewsResponse = resultResponse
                }else{
                    val oldArticles = breakingNewsResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(breakingNewsResponse?:resultResponse)
            }
        }

        return Resource.Error(response.message())

    }

    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                searchNewsPage++
                if (searchNewsResponse == null){
                    searchNewsResponse = resultResponse
                }else{
                    val oldArticles = searchNewsResponse?.articles
                    val newArticles = resultResponse.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(searchNewsResponse?:resultResponse)
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

    suspend fun safeBreakingNewsCall(countryCode: String){
        breakingNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()){
                val response = newsRepository.getBreakingNews(countryCode, breakingNewsPage)
                breakingNews.postValue(handleBreakingNewsResponse(response))
            }else{
                breakingNews.postValue(Resource.Error("No Internet connection"))
            }

        }catch (t:Throwable){
            when(t){
                is IOException->{
                 breakingNews.postValue(Resource.Error("Network failure"))
                }
                else-> breakingNews.postValue(Resource.Error("Conversion error"))
            }

        }
    }
    suspend fun safeSearchNewsCall(searchQuery: String){
        try {
            if (hasInternetConnection()){
                val response = newsRepository.searchNews(searchQuery,searchNewsPage)
                searchNews.postValue(handleSearchNewsResponse(response))
            }else{
                searchNews.postValue(Resource.Error("No Internet connection"))
            }

        }catch (t:Throwable){
            when(t){
                is IOException->{
                    searchNews.postValue(Resource.Error("Network failure"))
                }
                else-> searchNews.postValue(Resource.Error("Conversion error"))
            }

        }
    }

    private fun hasInternetConnection():Boolean{
        val connectivityManager = getApplication<NewsApp>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager

        val activeNetwork =connectivityManager.activeNetwork?:return false
        val capabilities =connectivityManager.getNetworkCapabilities(activeNetwork)?:return false
        return when{
            capabilities.hasTransport(TRANSPORT_WIFI)-> true
            capabilities.hasTransport(TRANSPORT_CELLULAR)-> true
            capabilities.hasTransport(TRANSPORT_ETHERNET)-> true
            else->false
        }
        return false
    }

}