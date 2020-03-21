package com.gelostech.dankmemes.data.wrappers

import com.google.android.gms.ads.formats.UnifiedNativeAd

data class NativeAdWrapper(
        override val id: String,
        val ad: UnifiedNativeAd
): ItemViewModel