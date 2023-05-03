package com.shersar.newsApplication.ui.fragments

import android.os.Bundle
import android.view.View
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.shersar.newsApplication.R
import com.shersar.newsApplication.databinding.FragmentArticleBinding
import com.shersar.newsApplication.ui.NewsViewmodel
import dagger.hilt.android.AndroidEntryPoint
import viewBinding

@AndroidEntryPoint
class ArticleFragment : Fragment(R.layout.fragment_article) {
    private val viewmodel: NewsViewmodel by viewModels()
    private val binding by viewBinding { FragmentArticleBinding.bind(it) }


    val args: ArticleFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val article = args.article

        binding.apply {
            webView.apply {
                webViewClient = WebViewClient()
                article.url?.let { loadUrl(it) }
            }

            fab.setOnClickListener {
                viewmodel.saveArticle(article)
                Snackbar.make(view, "Article saved successfully", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

}

