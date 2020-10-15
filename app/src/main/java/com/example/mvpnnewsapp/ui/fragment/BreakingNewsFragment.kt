package com.example.mvpnnewsapp.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mvpnnewsapp.R
import com.example.mvpnnewsapp.adapter.NewsAdapter
import com.example.mvpnnewsapp.ui.MainActivity
import com.example.mvpnnewsapp.ui.NewsViewModel
import com.example.mvpnnewsapp.util.Constans.Companion.QUERY_PAGE_SIZE
import com.example.mvpnnewsapp.util.Resource
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_breaking_news2.*


class BreakingNewsFragment : Fragment(R.layout.fragment_breaking_news2) {

    lateinit var viewModel : NewsViewModel
    lateinit var newsAdapter :NewsAdapter



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity). viewModel



        setupRecycleView()

        newsAdapter.setOnClickListener {
            val bundle = Bundle().apply {
                putSerializable("article",it)
            }

            findNavController().navigate(
                R.id.action_breakingNewsFragment_to_articleFragment,bundle
            )
        }

        viewModel.breakingNews.observe(viewLifecycleOwner, Observer { response ->
            when(response){
                is Resource.Succes -> {
                    hideProgessBar()
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPage= newsResponse.totalResults / QUERY_PAGE_SIZE + 2
                        isLastPage = viewModel.breakingNewsPage == totalPage
                        if (isLastPage){
                            rvBreakingNews.setPadding(0,0,0,0)
                        }

                    }
                }

                is Resource.Error ->{
                    hideProgessBar()
                    response.message.let { message ->
                        Snackbar.make(view, "An error Occured: $message", Snackbar.LENGTH_SHORT).show()

                    }
                }
                is Resource.Loading->{
                    showProgessBar()
                }
            }
        })
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
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE
            val shouldPaginte = isNotLoadingAndNotLastPage && isAtLastItem && isNotBegining &&
                    isTotalMoreThanVisible && isScrolling

            if (shouldPaginte){
                viewModel.getBreakingNews("id")
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

    private fun setupRecycleView() {
        newsAdapter = NewsAdapter()
        rvBreakingNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@BreakingNewsFragment.scrollingListener)
        }
    }

}