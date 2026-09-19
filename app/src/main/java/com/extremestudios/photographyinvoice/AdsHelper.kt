package com.extremestudios.photographyinvoicelite

import android.app.Activity
import android.content.Context
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdsHelper {
    const val BANNER_AD_UNIT_ID = "ca-app-pub-8451272966172172/8350122710"
    private const val INTERSTITIAL_AD_UNIT_ID = ""
    private const val APP_OPEN_AD_UNIT_ID = ""

    private var interstitialAd: InterstitialAd? = null
    private var appOpenAd: AppOpenAd? = null
    private var initialized = false
    private var interstitialLoading = false
    private var appOpenLoading = false

    fun initialize(context: Context) {
        if (initialized) return
        initialized = true
        if (BANNER_AD_UNIT_ID.isBlank() && INTERSTITIAL_AD_UNIT_ID.isBlank() && APP_OPEN_AD_UNIT_ID.isBlank()) return
        MobileAds.initialize(context.applicationContext)
        loadInterstitial(context.applicationContext)
        loadAppOpen(context.applicationContext)
    }

    private fun loadInterstitial(context: Context) {
        if (INTERSTITIAL_AD_UNIT_ID.isBlank() || interstitialLoading || interstitialAd != null) return
        interstitialLoading = true
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    interstitialLoading = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    interstitialAd = null
                    interstitialLoading = false
                }
            }
        )
    }

    private fun loadAppOpen(context: Context) {
        if (APP_OPEN_AD_UNIT_ID.isBlank() || appOpenLoading || appOpenAd != null) return
        appOpenLoading = true
        AppOpenAd.load(
            context,
            APP_OPEN_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    appOpenLoading = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    appOpenAd = null
                    appOpenLoading = false
                }
            }
        )
    }

    fun showInterstitial(activity: Activity, onDismiss: () -> Unit) {
        val ad = interstitialAd
        if (ad == null) {
            loadInterstitial(activity.applicationContext)
            onDismiss()
            return
        }

        interstitialAd = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                loadInterstitial(activity.applicationContext)
                onDismiss()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                loadInterstitial(activity.applicationContext)
                onDismiss()
            }
        }
        ad.show(activity)
    }

    fun showAppOpen(activity: Activity, onDismiss: () -> Unit) {
        val ad = appOpenAd
        if (ad == null) {
            loadAppOpen(activity.applicationContext)
            onDismiss()
            return
        }

        appOpenAd = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                loadAppOpen(activity.applicationContext)
                onDismiss()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                loadAppOpen(activity.applicationContext)
                onDismiss()
            }
        }
        ad.show(activity)
    }
}

@Composable
fun BannerAdView(modifier: Modifier = Modifier) {
    if (AdsHelper.BANNER_AD_UNIT_ID.isBlank()) {
        Box(modifier = modifier)
        return
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = AdsHelper.BANNER_AD_UNIT_ID
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
