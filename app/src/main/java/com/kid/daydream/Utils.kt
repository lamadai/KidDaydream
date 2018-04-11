package com.kid.daydream

import android.content.Context
import android.content.pm.ApplicationInfo

class Utils {
    companion object {
        @JvmStatic
        public fun isDebug(context: Context): Boolean {
            return context.applicationInfo != null && context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
        }
    }
}