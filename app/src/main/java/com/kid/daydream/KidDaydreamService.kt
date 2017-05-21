package com.kid.daydream

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.*
import android.widget.ImageView
import jcifs.smb.NtlmPasswordAuthentication
import jcifs.smb.SmbFile
import java.io.File
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.util.*
import java.util.concurrent.Executors
import java.util.regex.Pattern


class KidDaydreamService : android.service.dreams.DreamService() {
    private var mConfigFile: File? = File(Environment.getExternalStorageDirectory().toString()
            + File.separator + "kid_daydream.conf");

    /**
     * Called when the dream's window has been created and is visible and animation may now begin.
     */
    var bitmapNext: Bitmap? = null
    var bitmapCurrent: Bitmap? = null
    private var mDreamingStopped: Boolean = false
    val executor = Executors.newScheduledThreadPool(1)

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        setContentView(mImageView);
        mDreamingStopped = true
        downloadImage()
    }

    fun changeImage() {
        mHandler?.post {
            mImageView?.setImageBitmap(bitmapNext)
            bitmapCurrent?.recycle()
            bitmapCurrent = bitmapNext
            bitmapNext = null
            downloadImage()
        }
    }

    fun downloadImage() {
        for (i in 1..200) {
            if (mNetHandler != null) {
                break
            }
            SystemClock.sleep(100)
        }
        mNetHandler!!.post {
            if (bitmapNext == null
                    || bitmapNext!!.isRecycled) {
                bitmapNext = BitmapFactory.decodeStream(
                        mSmbImages?.get(Random(SystemClock.currentThreadTimeMillis()).nextInt(mSmbImages?.size!!))!!.inputStream)
            }
            if (bitmapCurrent != null
                    && !bitmapCurrent!!.isRecycled) {
                SystemClock.sleep(5000)
            }
            changeImage()
        }
    }

    private var mImageView: ImageView? = null;

    private var task: AsyncTask<Integer, Integer, Integer>? = null

    private var mNetHandler: Handler? = null

    private var mHandler: Handler? = null

    /** {@inheritDoc}  */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mHandler = Handler()
        getParams()
        mImageView = ImageView(baseContext);
        setInteractive(false);
        // Hide system UI
        setFullscreen(true);
    }

    private var address: String? = null
    private var username: String? = null
    private var password: String? = null

    private fun getParams() {
        Thread(Runnable {
            Looper.prepare()
            if (mConfigFile == null
                    || !mConfigFile!!.canRead()) {
                false
            }
            val lines: List<String>? = mConfigFile!!.readLines()?.take(3)
            address = lines?.get(0)?.trim()
            username = lines?.get(1)?.trim()
            password = lines?.get(2)?.trim()
            if (address == null) {
                false
            }
            val auth = NtlmPasswordAuthentication(null, username, password)
            val smbfile = SmbFile(address, auth)
            if (smbfile.isDirectory) {
                mSmbImages = smbfile.listFiles()
            }
            mNetHandler = Handler()
            Looper.loop()
        }).start()

    }

    private var mSmbImages: Array<out SmbFile>? = null


    /**
     * Called when this Dream is stopped, either by external request or by calling finish(),
     * before the window has been removed.
     */
    override fun onDreamingStopped() {
        super.onDreamingStopped()
        mDreamingStopped = true
        mNetHandler?.looper?.quit()
    }

    /** {@inheritDoc}  */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mDreamingStopped = true
    }

    companion object {

        /**
         * 正则方式判断字符编码，默认为UTF-8
         * @param encodeValue
         * *
         * @return
         */
        @JvmStatic private fun getURLEncode(encodeValue: String): Boolean? {
            //utf8编码 字符集
            val utf8Pattern = Pattern.compile("^([\\x00-\\x7f]|[\\xc0-\\xdf][\\x80-\\xbf]|[\\xe0-\\xef][\\x80-\\xbf]{2}|[\\xf0-\\xf7][\\x80-\\xbf]{3}|[\\xf8-\\xfb][\\x80-\\xbf]{4}|[\\xfc-\\xfd][\\x80-\\xbf]{5})+$")
            //通用字符集(utf-8和GBK)
            val publicPattern = Pattern.compile("^([\\x01-\\x7f]|[\\xc0-\\xdf][\\x80-\\xbf])+$")
            /**
             * 通用字符集判断
             */
            val publicMatcher = publicPattern.matcher(encodeValue)
            if (publicMatcher.matches()) {
                return false
            }

            val matcher = utf8Pattern.matcher(encodeValue)
            return matcher.matches()
        }

        /**
         * 解码
         * @param urlInfo
         * *
         * @param encodeStr
         * *
         * @return
         */
        @JvmStatic private fun getUrlDecode(urlInfo: String?, encodeStr: String): String {
            var result = ""
            if (null == urlInfo) {
                return result
            }
            try {
                result = URLDecoder.decode(urlInfo, encodeStr)
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
            return result
        }
    }
}
