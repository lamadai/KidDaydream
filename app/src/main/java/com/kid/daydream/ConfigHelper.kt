package com.kid.daydream

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import java.io.File

/**
 * Created by kid on 17-05-23.
 */
class ConfigHelper {
    companion object {
        private const val SMB_AUTH_CONFIG_FILE_NAME = "kid_daydream.conf"
        private const val SmbAuthConfig = "SmbAuthConfig"
        private const val SMB_ADDRESS = "SMB_ADDRESS"
        private const val SMB_USERNAME = "SMB_USERNAME"
        private const val SMB_PASSWORD = "SMB_PASSWORD"
        @JvmStatic
        public fun saveSmbConfig(context: Context,
                                 address: String,
                                 username: String?,
                                 password: String?): Boolean {
            val editor = context?.getSharedPreferences(SmbAuthConfig, MODE_PRIVATE).edit()
            editor.putString(SMB_ADDRESS, address)
            editor.putString(SMB_USERNAME, username)
            editor.putString(SMB_PASSWORD, password)
            return editor.commit()
        }

        @JvmStatic
        public fun getSmbConfig(context: Context): Array<String> {
            val address: String = context?.getSharedPreferences(SmbAuthConfig, MODE_PRIVATE).getString(SMB_ADDRESS, "smb://192.168.1.1/photo")
            val username: String = context?.getSharedPreferences(SmbAuthConfig, MODE_PRIVATE).getString(SMB_USERNAME, "kid")
            val password: String = context?.getSharedPreferences(SmbAuthConfig, MODE_PRIVATE).getString(SMB_PASSWORD, "")
            return arrayOf(if (address.endsWith(File.separatorChar)) address else address + File.separator, username, password)
        }


        @JvmStatic
        public fun readFromFile(context: Context, needDelete: Boolean, configFile: String): Boolean {
            var file: File? = File(configFile
                    + File.separator + SMB_AUTH_CONFIG_FILE_NAME)
            try {
                if (file != null
                        && file.canRead()) {
                    var lines = file!!.readLines()?.take(3)
                    return saveSmbConfig(context, lines[0], lines[1], lines[2])
                }
            } catch (e: Exception) {
                if (KidDayDreamApp.isDebug) Toast.makeText(context, e.toString(), LENGTH_LONG).show()
            }
            return false
        }
    }
}