package com.shersar.newsApplication.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.shersar.newsApplication.R
import com.shersar.newsApplication.adapters.NewsAdapter
import com.shersar.newsApplication.databinding.FragmentSearchNewsBinding
import com.shersar.newsApplication.models.Article
import com.shersar.newsApplication.ui.NewsViewModel
import com.shersar.newsApplication.utils.Constants
import com.shersar.newsApplication.utils.Constants.Companion.SEARCH_NEWS_TIME_DELAY
import com.shersar.newsApplication.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import viewBinding

@AndroidEntryPoint
class SearchNewsFragment : Fragment(R.layout.fragment_search_news) {
    private val binding by viewBinding { FragmentSearchNewsBinding.bind(it) }
    private val viewmodel: NewsViewModel by viewModels()
    val TAG = "SearchNewsFragment"
    lateinit var newsAdapter: NewsAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        delaySearch()

        newsAdapter.onClick = {
            val bundle = Bundle().apply {
                putSerializable("article",it)
            }

            findNavController().navigate(
                R.id.action_searchNewsFragment_to_articleFragment,
                bundle
            )
        }
    }

    private fun delaySearch() {

        var job: Job? = null

        binding.etSearch.addTextChangedListener { editable ->
            job?.cancel()

            job = MainScope().launch {
              delay(SEARCH_NEWS_TIME_DELAY)
                editable?.let {
                    if (editable.toString().isNotEmpty()){
                        viewmodel.searchNewsPage = 1
                        viewmodel.searchNewsResponse = null
                        viewmodel.searchNews(editable.toString())
                    }
                }
            }
        }

    }


    private fun setupObservers() {
        viewmodel.searchNews.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles as List<Article>)
                        val totalPages = newsResponse.totalResults / Constants.QUERY_PAGE_SIZE + 2
                        isLastPage = viewmodel.searchNewsPage == totalPages

                        if (isLastPage){
                            binding.rvSearchNews.setPadding(0,0,0,0)
                        }
                    }
                }

                is Resource.Error -> {
                    hideProgressBar ()
                    response.message?.let {message->
                        Toast.makeText(activity,"Error occured : $message" , Toast.LENGTH_LONG).show()
                        Log.d(TAG, "An error occured: $message")
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })
    }

    private fun hideProgressBar() {
        binding.paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar() {
        binding.paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    var isLoading = false
    var isScrolling = false
    var isLastPage = false

    val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)


            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {//check if we are  scrolling
                isScrolling = true
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLAstPage = !isLoading && !isLastPage
            val isLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= Constants.QUERY_PAGE_SIZE

            val shouldPaginate =
                isNotLoadingAndNotLAstPage && isLastItem && isNotAtBeginning && isTotalMoreThanVisible && isScrolling

            if (shouldPaginate) {
                viewmodel.searchNews(binding.etSearch.text.toString())
                isScrolling = false
            }

        }
    }


    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter()
        binding.rvSearchNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@SearchNewsFragment.scrollListener)
        }
    }
}