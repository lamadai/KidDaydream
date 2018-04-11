package com.kid.daydream

import android.app.Application
import android.content.pm.ApplicationInfo

class KidDayDreamApp : Application() {
    companion object {
        var isDebug: Boolean = false
    }

    override fun onCreate() {
        super.onCreate()
        isDebug = applicationInfo != null
                && applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    }

}
