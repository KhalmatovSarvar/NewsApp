package com.shersar.newsApplication.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.shersar.newsApplication.R
import com.shersar.newsApplication.adapters.NewsAdapter
import com.shersar.newsApplication.databinding.FragmentSavedNewsBinding
import com.shersar.newsApplication.ui.NewsViewmodel
import dagger.hilt.android.AndroidEntryPoint
import viewBinding

@AndroidEntryPoint
class SavedNewsFragment:Fragment(R.layout.fragment_saved_news) {
    private val viewmodel: NewsViewmodel by viewModels()
    lateinit var newsAdapter: NewsAdapter
    private val binding by viewBinding { FragmentSavedNewsBinding.bind(it) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setupRecyclerView()

        viewmodel.getSavedNews()
        setupObservers()
        ItemTouchHelper(itemTouchHelperCallback).apply {
            attachToRecyclerView(binding.rvSavedNews)
        }

        newsAdapter.onClick ={
            val bundle = Bundle().apply {
                putSerializable("article",it)
            }

            findNavController().navigate(
                R.id.action_savedNewsFragment_to_articleFragment,
                bundle
            )
        }
    }

    private fun setupObservers() {
        viewmodel.getSavedNews().observe(viewLifecycleOwner) { articles ->
            newsAdapter.differ.submitList(articles)
        }
    }
    private val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN,
        ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
    ){
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            val article = newsAdapter.differ.currentList[position]
            viewmodel.deleteArticle(article)
            Snackbar.make(view!!,"Successfully deleted article", Snackbar.LENGTH_LONG).apply {
                setAction("Undo"){
                    viewmodel.saveArticle(article)
                }
            }.show()
        }
    }





    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter()
        binding.rvSavedNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }
}