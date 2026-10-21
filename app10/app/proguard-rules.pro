##---------------Begin: app specific ProGuard rules ----------

# Keep line numbers for readable crash reports while still stripping the source file name.
-keepattributes SourceFile,LineNumber
-renamesourcefileattribute SourceFile

##---------------End: app specific ProGuard rules ----------


##---------------Begin: Tapsell Plus SDK ----------
# The Tapsell SDK is a thin dispatcher over several optional mediation SDKs (AdColony,
# Chartboost, the Google IMA SDK, ...). Those SDKs are not part of this app, so R8 reports
# them as missing classes. They are only ever touched on code paths that are never reached
# without the mediation dependency being present.
-dontwarn com.adcolony.sdk.**
-dontwarn com.chartboost.sdk.**
-dontwarn com.google.ads.interactivemedia.v3.api.**
-dontwarn com.google.ads.interactivemedia.v3.api.player.**
-dontwarn com.google.ads.interactivemedia.v3.api.AdEvent$AdEventListener
-dontwarn com.google.ads.interactivemedia.v3.api.AdEvent$AdEventType
-dontwarn com.google.ads.interactivemedia.v3.api.AdsLoader$AdsLoadedListener
-dontwarn com.google.ads.interactivemedia.v3.api.AdsManagerLoadedEvent
-dontwarn com.google.android.gms.ads.**
# Guava is only pulled in by the optional IMA preroll integration.
-dontwarn com.google.common.collect.BiMap
-dontwarn com.google.common.collect.**

# The remaining entries are the other optional mediation networks the Tapsell SDK is able
# to dispatch to (Unity Ads, AppLovin, Vungle, Mbridge, Facebook Audience Network, ...).
# None of them are part of this build, so R8 must not fail on their absence.
-dontwarn com.unity3d.ads.**
-dontwarn com.unity3d.services.**
-dontwarn com.applovin.**
-dontwarn com.vungle.**
-dontwarn com.mbridge.**
-dontwarn com.facebook.ads.**
-dontwarn com.facebook.**
-dontwarn com.inmobi.**
-dontwarn com.mopub.**
-dontwarn com.verizon.**
-dontwarn com.smaato.**
-dontwarn com.fyber.**
-dontwarn com.ironsource.**
-dontwarn com.mobvista.**
-dontwarn com.pangle.**
-dontwarn com.bytedance.**
-dontwarn com.anythink.**
-dontwarn com.baidu.**
-dontwarn com.sigmob.**


##---------------End: Tapsell Plus SDK ----------


##---------------Begin: Jetpack Compose ----------
# Compose relies on the generic signatures of its runtime for the compiler plugin and for
# the recomposer to work correctly.
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-dontwarn androidx.compose.**

##---------------End: Jetpack Compose ----------


##---------------Begin: Kotlin ----------
-keepclassmembers class kotlin.Metadata { *; }
-dontwarn kotlin.**

##---------------End: Kotlin ----------
