package com.example.mvpnnewsapp.ui.fragment

import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mvpnnewsapp.R
import com.example.mvpnnewsapp.adapter.NewsAdapter
import com.example.mvpnnewsapp.ui.MainActivity
import com.example.mvpnnewsapp.ui.NewsViewModel
import com.example.mvpnnewsapp.util.Constans
import com.example.mvpnnewsapp.util.Resource
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_breaking_news2.*
import kotlinx.android.synthetic.main.fragment_search_news.*
import kotlinx.android.synthetic.main.fragment_search_news.paginationProgressBar
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SearchNewsFragment : Fragment(R.layout.fragment_search_news) {

    lateinit var viewModel:NewsViewModel
    lateinit var newsAdapter: NewsAdapter



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity).viewModel

        setUpRecycleView()

        newsAdapter.setOnClickListener {
            val bundle = Bundle().apply {
                putSerializable("article",it)
            }

            findNavController().navigate(
                R.id.action_searchNewsFragment_to_articleFragment,bundle
            )
        }


        viewModel.searchNews.observe(viewLifecycleOwner, Observer { response ->
            when(response){
                is Resource.Succes->{
                    hideProgessBar()
                    response.data?.let {
                        newsAdapter.differ.submitList(it.articles)
                    }
                }

                is Resource.Error -> {
                    hideProgessBar()
                    response.message.let {
                        Snackbar.make(view, "An error Occured: $it", Snackbar.LENGTH_SHORT).show()
                    }
                }

                is Resource.Loading ->{
                    showProgessBar()
                }
            }
        })

        var job: Job? = null
        etSearch.addTextChangedListener { editable ->
        job?.cancel()
        job = MainScope().launch {
            delay(500L)
            editable?.let {
                if (editable.toString().isNotEmpty()){
                    viewModel.searchNews(editable.toString())
                }
            }
        }
        }

    }

    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    private val scrollingListener = object  : RecyclerView. OnScrollListener(){
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager= recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount =  layoutManager.childCount
            val totalItemCount= layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition+ visibleItemCount >= totalItemCount
            val isNotBegining= firstVisibleItemPosition >=0
            val isTotalMoreThanVisible = totalItemCount >= Constans.QUERY_PAGE_SIZE
            val shouldPaginte = isNotLoadingAndNotLastPage && isAtLastItem && isNotBegining &&
                    isTotalMoreThanVisible && isScrolling

            if (shouldPaginte){
                viewModel.searchNews(etSearch.text.toString())
                isScrolling = false
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                isScrolling = true
            }

        }
    }

    private fun showProgessBar(){
        paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    private fun hideProgessBar(){
        paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun setUpRecycleView() {
        newsAdapter= NewsAdapter()
        rvSearchNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@SearchNewsFragment.scrollingListener)
        }
    }

}