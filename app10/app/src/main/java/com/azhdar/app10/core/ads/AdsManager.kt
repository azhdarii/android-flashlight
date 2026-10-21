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
 *  - Ad requests are issued from [onRequest] and are guarded against a destroyed activity.
 */
class AdsManager(private val context: Context) {

    private var initialized = false
    private var bannerResponseId: String? = null
    private var interstitialResponseId: String? = null
    private var rewardedResponseId: String? = null

    /** True when at least one ad zone has been configured for this build. */
    val hasAds: Boolean
        get() = bannerZone.isNotBlank() || interstitialZone.isNotBlank() || rewardedZone.isNotBlank()

    /**
     * True once the SDK finished initialising. When ads are not configured at all the
     * manager reports ready immediately, so callers never wait for something that will
     * never happen.
     */
    val isReady: Boolean
        get() = initialized || !hasAds

    private val bannerZone = BuildConfig.TAPSELL_BANNER_ZONE
    private val interstitialZone = BuildConfig.TAPSELL_INTERSTITIAL_ZONE
    private val rewardedZone = BuildConfig.TAPSELL_REWARDED_ZONE

    /**
     * Initializes the Tapsell SDK. Safe to call multiple times; only the first call has an
     * effect. [onReady] is invoked once initialization succeeded.
     */
    fun initialize(onReady: () -> Unit = {}) {
        if (initialized) {
            onReady()
            return
        }
        val appKey = BuildConfig.TAPSELL_APP_KEY
        if (appKey.isBlank()) {
            Log.i(TAG, "No Tapsell app key configured, ads are disabled")
            return
        }
        TapsellPlus.initialize(
            context.applicationContext,
            appKey,
            object : TapsellPlusInitListener {
                override fun onInitializeSuccess(adNetworks: AdNetworks) {
                    initialized = true
                    onReady()
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
        if (!initialized || bannerZone.isBlank()) return
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
        if (!initialized || interstitialZone.isBlank()) return
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
        if (!initialized || rewardedZone.isBlank()) {
            onReward()
            return
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
