package com.extremestudios.photographyinvoicelite

import android.content.Context

/**
 * Application context holder for SecurityManager
 */
object App {
    private lateinit var context: Context
    
    fun init(context: Context) {
        this.context = context.applicationContext
    }
    
    fun getContext(): Context {
        return context
    }
}

