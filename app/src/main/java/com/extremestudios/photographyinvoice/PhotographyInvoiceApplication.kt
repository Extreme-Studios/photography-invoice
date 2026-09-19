package com.extremestudios.photographyinvoicelite

import android.app.Application

class PhotographyInvoiceApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        App.init(this)
        AdsHelper.initialize(this)
        SecurityManager.logSecurityEvent("APP_STARTED")
    }
}
