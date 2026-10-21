package com.azhdar.app10.core.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import com.azhdar.app10.BuildConfig
import ir.tapsell.plus.AdRequestCallback
import ir.tapsell.plus.AdShowListener
import ir.tapsell.plus.TapsellPlus
import ir.tapsell.plus.TapsellPlusBannerType
import ir.tapsell.plus.TapsellPlusInitListener
import ir.tapsell.plus.model.AdNetworkError
import ir.tapsell.plus.model.AdNetworks
import ir.tapsell.plus.model.TapsellPlusAdModel
import ir.tapsell.plus.model.TapsellPlusErrorModel

/**
 * Wraps the Tapsell SDK so the rest of the app never talks to it directly.
 *
 * Design notes:
 *  - Every zone id comes from [BuildConfig]. When a zone id is missing the manager simply
 *    disables that ad format instead of firing requests with a placeholder key, which used
 *    to result in runtime errors.
 *  - The banner is rendered into a [ViewGroup] that the UI layer owns. The previous
 *    implementation called `activity.findViewById(R.id.standardBanner)` on a view that was
 *    inflated inside a Compose `AndroidView`, so the lookup always returned `null`.
 *  - Requests issued before the SDK finished initialising are queued and replayed, because
 *    the banner view exists long before the SDK is ready.
 */
class AdsManager(private val context: Context) {

    private val appKey = BuildConfig.TAPSELL_APP_KEY
    private val bannerZone = BuildConfig.TAPSELL_BANNER_ZONE
    private val interstitialZone = BuildConfig.TAPSELL_INTERSTITIAL_ZONE
    private val rewardedZone = BuildConfig.TAPSELL_REWARDED_ZONE

    /** True when the SDK can be used at all. */
    private val enabled: Boolean = appKey.isNotBlank()

    /** True when at least one ad zone has been configured for this build. */
    val hasAds: Boolean
        get() = bannerZone.isNotBlank() || interstitialZone.isNotBlank() || rewardedZone.isNotBlank()

    /**
     * True once the SDK finished initialising, or immediately when ads are disabled, so
     * callers never wait for something that will never happen.
     */
    val isReady: Boolean
        get() = initialized || !enabled

    private var initialized = false
    private var bannerResponseId: String? = null
    private var interstitialResponseId: String? = null
    private var rewardedResponseId: String? = null

    // Requests that arrived before the SDK was ready are replayed once it is.
    private var pendingBannerActivity: Activity? = null
    private var pendingBannerContainer: ViewGroup? = null

    /**
     * Initializes the Tapsell SDK. Safe to call multiple times; only the first call has an
     * effect.
     */
    fun initialize() {
        if (initialized) return
        if (!enabled) {
            Log.i(TAG, "No Tapsell app key configured, ads are disabled")
            initialized = true
            return
        }
        TapsellPlus.initialize(
            context.applicationContext,
            appKey,
            object : TapsellPlusInitListener {
                override fun onInitializeSuccess(adNetworks: AdNetworks) {
                    initialized = true
                    flushPendingBanner()
                }

                override fun onInitializeFailed(
                    adNetworks: AdNetworks,
                    adNetworkError: AdNetworkError,
                ) {
                    Log.w(TAG, "Tapsell initialization failed: ${adNetworkError.errorMessage}")
                }
            },
        )
    }

    /** Requests a banner ad and renders it into [container] as soon as it arrives. */
    fun showBanner(activity: Activity, container: ViewGroup) {
        if (!enabled || bannerZone.isBlank()) return
        if (!initialized) {
            // Remember the request; it is replayed as soon as the SDK becomes available.
            pendingBannerActivity = activity
            pendingBannerContainer = container
            return
        }
        requestBanner(activity, container)
    }

    private fun flushPendingBanner() {
        val activity = pendingBannerActivity
        val container = pendingBannerContainer
        pendingBannerActivity = null
        pendingBannerContainer = null
        if (activity != null && container != null && !activity.isDestroyed) {
            requestBanner(activity, container)
        }
    }

    private fun requestBanner(activity: Activity, container: ViewGroup) {
        TapsellPlus.requestStandardBannerAd(
            activity,
            bannerZone,
            TapsellPlusBannerType.BANNER_320x50,
            object : AdRequestCallback() {
                override fun response(tapsellPlusAdModel: TapsellPlusAdModel) {
                    super.response(tapsellPlusAdModel)
                    if (activity.isDestroyed) return
                    bannerResponseId = tapsellPlusAdModel.responseId
                    renderBanner(activity, container, tapsellPlusAdModel.responseId)
                }

                override fun error(message: String) {
                    Log.w(TAG, "Banner request failed: $message")
                }
            },
        )
    }

    private fun renderBanner(activity: Activity, container: ViewGroup, responseId: String) {
        try {
            TapsellPlus.showStandardBannerAd(
                activity,
                responseId,
                container,
                object : AdShowListener() {
                    override fun onOpened(tapsellPlusAdModel: TapsellPlusAdModel) {
                        super.onOpened(tapsellPlusAdModel)
                    }

                    override fun onClosed(tapsellPlusAdModel: TapsellPlusAdModel) {
                        super.onClosed(tapsellPlusAdModel)
                    }

                    override fun onError(tapsellPlusErrorModel: TapsellPlusErrorModel) {
                        super.onError(tapsellPlusErrorModel)
                        Log.w(TAG, "Banner show failed")
                    }
                },
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unable to show banner", e)
        }
    }

    /** Releases the currently rendered banner, if any. */
    fun destroyBanner(activity: Activity, container: ViewGroup) {
        val id = bannerResponseId ?: return
        runCatching { TapsellPlus.destroyStandardBanner(activity, id, container) }
        bannerResponseId = null
    }

    /**
     * Shows an interstitial ad. When no ad is cached the request is fired and the ad is
     * displayed as soon as it becomes available.
     */
    fun showInterstitial(activity: Activity) {
        if (!enabled || interstitialZone.isBlank()) return
        if (!initialized) {
            initialize()
            if (!initialized) return
        }
        val cached = interstitialResponseId
        if (cached != null) {
            interstitialResponseId = null
            presentInterstitial(activity, cached)
        } else {
            requestInterstitial(activity)
        }
    }

    private fun requestInterstitial(activity: Activity) {
        TapsellPlus.requestInterstitialAd(
            activity,
            interstitialZone,
            object : AdRequestCallback() {
                override fun response(tapsellPlusAdModel: TapsellPlusAdModel) {
                    super.response(tapsellPlusAdModel)
                    if (activity.isDestroyed) return
                    interstitialResponseId = tapsellPlusAdModel.responseId
                }

                override fun error(message: String) {
                    Log.w(TAG, "Interstitial request failed: $message")
                }
            },
        )
    }

    private fun presentInterstitial(activity: Activity, responseId: String) {
        try {
            TapsellPlus.showInterstitialAd(
                activity,
                responseId,
                object : AdShowListener() {
                    override fun onOpened(tapsellPlusAdModel: TapsellPlusAdModel) {
                        super.onOpened(tapsellPlusAdModel)
                    }

                    override fun onClosed(tapsellPlusAdModel: TapsellPlusAdModel) {
                        super.onClosed(tapsellPlusAdModel)
                    }

                    override fun onError(tapsellPlusErrorModel: TapsellPlusErrorModel) {
                        super.onError(tapsellPlusErrorModel)
                    }
                },
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unable to show interstitial", e)
        }
    }

    /**
     * Shows a rewarded video ad. [onReward] is invoked when the user earned the reward.
     */
    fun showRewarded(activity: Activity, onReward: () -> Unit) {
        if (!enabled || rewardedZone.isBlank()) {
            onReward()
            return
        }
        if (!initialized) {
            initialize()
            if (!initialized) {
                onReward()
                return
            }
        }
        TapsellPlus.requestRewardedVideoAd(
            activity,
            rewardedZone,
            object : AdRequestCallback() {
                override fun response(tapsellPlusAdModel: TapsellPlusAdModel) {
                    super.response(tapsellPlusAdModel)
                    if (activity.isDestroyed) return
                    rewardedResponseId = tapsellPlusAdModel.responseId
                    presentRewarded(activity, onReward)
                }

                override fun error(message: String) {
                    Log.w(TAG, "Rewarded request failed: $message")
                    onReward()
                }
            },
        )
    }

    private fun presentRewarded(activity: Activity, onReward: () -> Unit) {
        val responseId = rewardedResponseId ?: return
        rewardedResponseId = null
        try {
            TapsellPlus.showRewardedVideoAd(
                activity,
                responseId,
                object : AdShowListener() {
                    override fun onOpened(tapsellPlusAdModel: TapsellPlusAdModel) {
                        super.onOpened(tapsellPlusAdModel)
                    }

                    override fun onClosed(tapsellPlusAdModel: TapsellPlusAdModel) {
                        super.onClosed(tapsellPlusAdModel)
                    }

                    override fun onRewarded(tapsellPlusAdModel: TapsellPlusAdModel) {
                        super.onRewarded(tapsellPlusAdModel)
                        onReward()
                    }

                    override fun onError(tapsellPlusErrorModel: TapsellPlusErrorModel) {
                        super.onError(tapsellPlusErrorModel)
                        onReward()
                    }
                },
            )
        } catch (e: Exception) {
            Log.e(TAG, "Unable to show rewarded ad", e)
            onReward()
        }
    }

    companion object {
        private const val TAG = "AdsManager"
    }
}
