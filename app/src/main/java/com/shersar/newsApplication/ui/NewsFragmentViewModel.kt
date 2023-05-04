package com.shersar.newsApplication.ui

import androidx.lifecycle.LiveData
import com.shersar.newsApplication.models.NewsResponse
import com.shersar.newsApplication.utils.Resource

interface NewsFragmentViewModel {
    val breakingNewsLiveData: LiveData<Resource<NewsResponse>>
}