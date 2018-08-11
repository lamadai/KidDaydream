package com.kid.daydream

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.storage.StorageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.CompoundButton
import android.widget.Toast
import com.kid.daydream.ConfigHelper.Companion.getSmbConfig
import kotlinx.android.synthetic.main.setting_activity.*


/**
 * Created by kid on 17-05-19.
 */
class SettingsActivity : Activity() {
    private val PERMISSIONS_STORAGE = arrayOf(
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE")

    private val REQUEST_EXTERNAL_STORAGE: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.setting_activity)
        initViews()
        readConfigFile()
        updateConfigViews();
        btn_save.setOnClickListener {
            ConfigHelper.saveSmbConfig(baseContext,
                    "" + smb_address.text,
                    "" + user_name.text,
                    "" + password.text)
        }
    }

    private fun initViews() {
        conf_first.setOnCheckedChangeListener(
                CompoundButton.OnCheckedChangeListener { compoundButton: CompoundButton, readConfigFileFirst: Boolean ->
                    if (readConfigFileFirst) readConfigFile()
                }
        )
    }

    private fun checkPermission(): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                ask4permission()
            }
            else -> true
        }
    }

    private fun ask4permission(): Boolean {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                try {
                    ActivityCompat.requestPermissions(this,
                            PERMISSIONS_STORAGE,
                            REQUEST_EXTERNAL_STORAGE)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
            return false
        } else {
            return true
        }
    }

    private fun updateConfigViews() {
        try {
            var conf = getSmbConfig(this) as Array<String>
            smb_address.setText(conf[0])
            user_name.setText(conf[1])
            password.setText(conf[2])
        } catch (e: Exception) {
        }
    }

    @SuppressLint("WrongConstant")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK) {
            return
        }
        when (requestCode) {
            REQUEST_EXTERNAL_STORAGE -> {
                if (readConfigFile()) {
                    updateConfigViews()
                } else {
                    showToastInDebug("config file not found")
                }
            }
        }
    }

    private fun readConfigFile(): Boolean {
        if (checkPermission()) {
            if (ConfigHelper.readFromFile(this, true, Environment.getExternalStorageDirectory().toString())) {
                //读内置存储空间
                return true
            } else {
                //找USB路径
                try {
                    val storageManager = getSystemService(Context.STORAGE_SERVICE) as StorageManager
                    val method = storageManager.javaClass.getMethod("getVolumePaths")
                    if (null != method) {
                        method.isAccessible = true
                        var volumePaths = method.invoke(storageManager) as Array<String>
                        if (volumePaths != null) {
                            for (sdcardPath in volumePaths) {
                                if (ConfigHelper.readFromFile(this, true, sdcardPath))
                                    return true
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return false
    }

    private fun showToastInDebug(text: String) {
        if (KidDayDreamApp.isDebug) Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }
}