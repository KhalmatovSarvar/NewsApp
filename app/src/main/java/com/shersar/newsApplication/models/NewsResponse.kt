package com.shersar.newsApplication.models

data class NewsResponse(
	val totalResults: Int,
	val articles: MutableList<Article>,
	val status: String
)
