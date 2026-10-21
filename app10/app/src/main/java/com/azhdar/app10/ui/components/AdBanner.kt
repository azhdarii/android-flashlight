package com.azhdar.app10.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.azhdar.app10.core.ads.AdsManager

/**
 * Hosts the Tapsell banner.
 *
 * The banner container is created and owned by this composable, and the very same
 * [ViewGroup] instance is handed to the ads manager. The original code inflated a layout
 * inside an `AndroidView` and then looked the banner up with `activity.findViewById(...)`,
 * which can never find a view that lives inside a Compose hosted hierarchy.
 */
@Composable
fun AdBanner(
    adsManager: AdsManager,
    modifier: Modifier = Modifier,
    height: Dp = 50.dp,
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    var container by remember { mutableStateOf<ViewGroup?>(null) }

    Box(modifier = modifier.fillMaxWidth().height(height)) {
        AndroidView(
            factory = { ctx ->
                FrameLayout(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().height(height),
            update = { view -> container = view },
        )
    }

    val host = activity
    val view = container
    DisposableEffect(host, view) {
        if (host != null && view != null) {
            adsManager.showBanner(host, view)
        }
        onDispose {
            if (host != null && view != null) {
                adsManager.destroyBanner(host, view)
            }
        }
    }
}

/** Finds the hosting Activity from a Context, unwrapping theme/app context wrappers. */
/** Finds the hosting Activity from a Context, unwrapping theme/app context wrappers. */
fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
