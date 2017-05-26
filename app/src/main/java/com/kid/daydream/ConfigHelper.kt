package com.kid.daydream

import android.content.Context
import android.content.Context.MODE_PRIVATE

/**
 * Created by kid on 17-05-23.
 */
class ConfigHelper {
    companion object{
        private val SmbAuthConfig = "SmbAuthConfig"
        private val SMB_ADDRESS = "SMB_ADDRESS"
        private val SMB_USERNAME = "SMB_USERNAME"
        private val SMB_PASSWORD = "SMB_PASSWORD"
        @JvmStatic public fun updateSmb(context: Context,
                                        address: String,
                                        username: String?,
                                        password: String?): Boolean {
            val editor = context?.getSharedPreferences(SmbAuthConfig, MODE_PRIVATE).edit()
            editor.putString(SMB_ADDRESS,address)
            editor.putString(SMB_USERNAME,username)
            editor.putString(SMB_PASSWORD,password)
            return editor.commit()
        }
        @JvmStatic public fun getSmb(context: Context): Array<String>{
            val address: String = context?.getSharedPreferences(SmbAuthConfig, MODE_PRIVATE).getString(SMB_ADDRESS, null)
            val username: String = context?.getSharedPreferences(SmbAuthConfig, MODE_PRIVATE).getString(SMB_USERNAME, null)
            val password: String = context?.getSharedPreferences(SmbAuthConfig, MODE_PRIVATE).getString(SMB_PASSWORD, null)
            return arrayOf(address, username ,password)
        }

        @JvmStatic public fun saveAllChange(context: Context): Array<String>{
            val address: String = context?.getSharedPreferences(SmbAuthConfig, MODE_PRIVATE).getString(SMB_ADDRESS, null)
            val username: String = context?.getSharedPreferences(SmbAuthConfig, MODE_PRIVATE).getString(SMB_USERNAME, null)
            val password: String = context?.getSharedPreferences(SmbAuthConfig, MODE_PRIVATE).getString(SMB_PASSWORD, null)
            return arrayOf(address, username ,password)
        }
    }
}