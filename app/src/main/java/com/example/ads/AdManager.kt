package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AdManager {
    private const val TAG = "FitPulseAdManager"

    // AdMob App ID and Rewarded Ad Unit ID requested by user
    const val ADMOB_APP_ID = "ca-app-pub-7750958842878506~6720324116"
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-7750958842878506/6350105673"

    // Observable states for UI feedback
    private val _isRewardedAdLoaded = MutableStateFlow(false)
    val isRewardedAdLoaded: StateFlow<Boolean> = _isRewardedAdLoaded.asStateFlow()

    private val _rewardCount = MutableStateFlow(0)
    val rewardCount: StateFlow<Int> = _rewardCount.asStateFlow()

    private val _lastAdMessage = MutableStateFlow("AdMob ready")
    val lastAdMessage: StateFlow<String> = _lastAdMessage.asStateFlow()

    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading: Boolean = false

    fun initialize(context: Context) {
        try {
            // Configure emulator test device to allow safe testing without policy violations
            val requestConfig = RequestConfiguration.Builder()
                .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                .build()
            MobileAds.setRequestConfiguration(requestConfig)

            MobileAds.initialize(context) { initializationStatus ->
                Log.d(TAG, "MobileAds initialized: $initializationStatus")
                _lastAdMessage.value = "MobileAds initialized"
                loadRewardedAd(context)
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error during MobileAds initialization", e)
            _lastAdMessage.value = "Init issue: ${e.localizedMessage ?: "Unknown error"}"
        }
    }

    // ==========================================
    // REWARDED AD
    // ==========================================

    fun loadRewardedAd(context: Context) {
        if (isRewardedLoading || rewardedAd != null) {
            return
        }

        try {
            isRewardedLoading = true
            _lastAdMessage.value = "Loading Rewarded Ad..."
            val request = AdRequest.Builder().build()

            RewardedAd.load(
                context,
                REWARDED_AD_UNIT_ID,
                request,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        isRewardedLoading = false
                        _isRewardedAdLoaded.value = true
                        _lastAdMessage.value = "Rewarded Ad loaded & ready"
                        Log.d(TAG, "Rewarded Ad loaded successfully")
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        rewardedAd = null
                        isRewardedLoading = false
                        _isRewardedAdLoaded.value = false
                        _lastAdMessage.value = "Rewarded Ad load failed: ${loadAdError.message}"
                        Log.w(TAG, "Rewarded Ad failed to load: ${loadAdError.message}")
                    }
                }
            )
        } catch (e: Throwable) {
            rewardedAd = null
            isRewardedLoading = false
            _isRewardedAdLoaded.value = false
            _lastAdMessage.value = "Rewarded Ad load error: ${e.localizedMessage ?: "Error"}"
            Log.e(TAG, "Exception loading rewarded ad", e)
        }
    }

    fun showRewardedAd(
        activity: Activity,
        onUserEarnedReward: (RewardItem) -> Unit,
        onAdClosedOrFailed: () -> Unit = {}
    ) {
        try {
            val currentAd = rewardedAd
            if (currentAd == null) {
                Log.d(TAG, "Rewarded Ad not ready yet.")
                _lastAdMessage.value = "Rewarded Ad is loading, please try in a moment"
                onAdClosedOrFailed()
                loadRewardedAd(activity.applicationContext)
                return
            }

            var rewardGranted = false

            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    _lastAdMessage.value = "Showing Rewarded Ad"
                    Log.d(TAG, "Rewarded Ad showed")
                }

                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    _isRewardedAdLoaded.value = false
                    _lastAdMessage.value = if (rewardGranted) "Reward collected!" else "Rewarded Ad closed"
                    Log.d(TAG, "Rewarded Ad dismissed. Granted: $rewardGranted")
                    onAdClosedOrFailed()
                    loadRewardedAd(activity.applicationContext)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedAd = null
                    _isRewardedAdLoaded.value = false
                    _lastAdMessage.value = "Rewarded Ad failed to show: ${adError.message}"
                    Log.w(TAG, "Rewarded Ad failed to show: ${adError.message}")
                    onAdClosedOrFailed()
                    loadRewardedAd(activity.applicationContext)
                }
            }

            currentAd.show(activity) { rewardItem ->
                rewardGranted = true
                _rewardCount.value += 1
                _lastAdMessage.value = "Earned ${rewardItem.amount} ${rewardItem.type}!"
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                onUserEarnedReward(rewardItem)
            }
        } catch (e: Throwable) {
            rewardedAd = null
            isRewardedLoading = false
            _isRewardedAdLoaded.value = false
            _lastAdMessage.value = "Show ad error: ${e.localizedMessage ?: "Error"}"
            Log.e(TAG, "Exception showing rewarded ad", e)
            onAdClosedOrFailed()
        }
    }
}
