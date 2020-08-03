package com.tian.myapplication

import android.app.Application
import android.content.Context

/**
 *  create by txm  on 2020/8/2
 *  desc
 */
class AppApplication1 :Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)

        val hotFix = HotFix(this)
        hotFix.loadLocalDex()
    }
}