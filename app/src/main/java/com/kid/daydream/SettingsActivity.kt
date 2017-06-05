package com.kid.daydream

import android.app.Activity
import android.os.Bundle
import kotlinx.android.synthetic.main.setting_activity.*

/**
 * Created by kid on 17-05-19.
 */
class SettingsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setting_activity)
        initView();
        Jni.dcow
    }

    private fun initView() {
        smb_address.setText("123")
        user_name.setText("123")
        password.setText("123")
    }
    companion object{
        init{
            System.loadLibrary("libsupportjni");   //defaultConfig.ndk.moduleName
        }
    }
}