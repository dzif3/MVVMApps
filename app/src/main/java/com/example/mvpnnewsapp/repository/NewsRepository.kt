package com.example.mvpnnewsapp.repository

import com.example.mvpnnewsapp.database.ArticleDatabase
import com.example.mvpnnewsapp.model.Article
import com.example.mvpnnewsapp.network.RetrofitInstance

class NewsRepository (val db: ArticleDatabase){

    //get semua data di tampilkan di fragment breakingNews
    suspend fun getBreakingNews(countryCode: String, pageNumber: Int)=
        RetrofitInstance.api.getBreakingNews(countryCode, pageNumber)

    suspend fun searchNews(searchQuery: String, pageNumber: Int)=
        RetrofitInstance.api.searchNews(searchQuery, pageNumber)

    //membuat database baru di local database
    suspend fun upsert(article : Article)= db.getArticleDao().upsert(article)

    //untuk get semua data yang sudah di bookmark
    fun getSavedNews() = db.getArticleDao().getAllArticle()

    //delete database local
    suspend fun deleteArticle(article: Article)=db.getArticleDao().deleteArticle(article)
}