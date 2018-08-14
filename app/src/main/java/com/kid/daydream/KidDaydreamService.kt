package com.kid.daydream

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.KeyEvent
import android.widget.ImageView
import android.widget.Toast
import jcifs.smb.NtlmPasswordAuthentication
import jcifs.smb.SmbFile
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.util.concurrent.Executors
import java.util.regex.Pattern


class KidDaydreamService : android.service.dreams.DreamService() {
    /**
     * Called when the dream's window has been created and is visible and animation may now begin.
     */
    var bitmapPre: Bitmap? = null
    var bitmapNext: Bitmap? = null
    var bitmapCurrent: Bitmap? = null
    private var mDreamingStopped: Boolean = false
    val executor = Executors.newScheduledThreadPool(1)!!

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        setContentView(mImageView);
        mDreamingStopped = true
    }

    private val TIMER_MESSAGE = 123

    private fun loadFiles() {
        Thread(Runnable {
            Looper.prepare()
            var lines = ConfigHelper.getSmbConfig(baseContext).asList()
            var address: String = lines?.get(0)?.trim()
            if (address == null) {
                false
            }
            var username: String? = lines?.get(1)?.trim()
            var password: String? = lines?.get(2)?.trim()
            val auth = NtlmPasswordAuthentication(null, username, password)
            val smbfile = SmbFile(address, auth)
            if (smbfile.canRead()
                    && smbfile.isDirectory) {
                mSmbImages = smbfile.listFiles().asList().dropWhile {
                    !it.name.toLowerCase().endsWith(".jpg")
                            && !it.name.toLowerCase().endsWith(".jpeg")
                            && !it.name.toLowerCase().endsWith(".bmp")
                            && !it.name.toLowerCase().endsWith(".png")
                            && !it.name.toLowerCase().endsWith(".webp")
                }.toTypedArray()
            }
            if (mSmbImages == null
                    || mSmbImages!!.isEmpty()) {
                if (KidDayDreamApp.isDebug) Toast.makeText(this, "Pictures not found --|", Toast.LENGTH_SHORT).show()
                finish()
                return@Runnable
            }
            bitmapPre = downloadImage((index - 1) % mSmbImages!!.size)
            bitmapCurrent = downloadImage(index)
            bitmapNext = downloadImage((index + 1) % mSmbImages!!.size)
            mHandler?.post {
                mImageView!!.setImageBitmap(bitmapCurrent)

            }
            mNetHandler = object : Handler() {
                override fun dispatchMessage(msg: Message) {
                    if (msg.arg1 == TIMER_MESSAGE) {
                        nextImage();
                    }
                }
            }
            mNetHandler!!.sendEmptyMessageDelayed(TIMER_MESSAGE, 5000);
            Looper.loop()
        }).start()
    }


    fun downloadImage(curIndex: Int): Bitmap? {
        return BitmapFactory.decodeStream(
                mSmbImages?.get(curIndex)!!.inputStream)
    }

    private var mImageView: ImageView? = null;

    private var task: AsyncTask<Integer, Integer, Integer>? = null

    private var mNetHandler: Handler? = null

    private var mHandler: Handler? = null

    private var index = 0;

    /** {@inheritDoc}  */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mHandler = Handler()
        loadFiles()
        mImageView = ImageView(baseContext);
        isInteractive = false;
        // Hide system UI
        isFullscreen = true;
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
        @JvmStatic
        private fun getURLEncode(encodeValue: String): Boolean? {
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
        @JvmStatic
        private fun getUrlDecode(urlInfo: String?, encodeStr: String): String {
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

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event == null
                || mImageView == null) {
            return false;
        }
        when (event.keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER -> {
                if (mImageView!!.scaleType == ImageView.ScaleType.CENTER_CROP) {
                    mImageView!!.scaleType = ImageView.ScaleType.CENTER_INSIDE
                } else {
                    mImageView!!.scaleType = ImageView.ScaleType.CENTER_CROP
                }
                return true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                if (!lockInput) {
                    preImage()
                }
                return true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                if (!lockInput) {
                    nextImage()
                }
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private var lockInput: Boolean = false

    private fun nextImage() {
        index = (index + 1) % mSmbImages!!.size
        mHandler?.post {
            mImageView?.setImageBitmap(bitmapNext)
            bitmapPre!!.recycle()
            bitmapPre = bitmapCurrent
            bitmapCurrent = bitmapNext
            mNetHandler!!.removeCallbacksAndMessages(TIMER_MESSAGE);
            mImageView!!.setImageBitmap(bitmapCurrent)
            mNetHandler!!.sendEmptyMessageDelayed(TIMER_MESSAGE, 5000);
            if (bitmapNext != null && mSmbImages!!.size < 3) {
                return@post
            }
            lockInput = true;
            bitmapNext != null
            mNetHandler?.post {
                bitmapNext = downloadImage((index + 1) % mSmbImages!!.size)
                lockInput = false
            }
        }
    }

    private fun preImage() {
        index = (index - 1) % mSmbImages!!.size
        mHandler?.post {
            mImageView?.setImageBitmap(bitmapNext)
            bitmapNext!!.recycle()
            bitmapNext = bitmapCurrent
            bitmapCurrent = bitmapPre
            mNetHandler!!.removeCallbacksAndMessages(TIMER_MESSAGE);
            mImageView!!.setImageBitmap(bitmapCurrent)
            mNetHandler!!.sendEmptyMessageDelayed(TIMER_MESSAGE, 5000);
            if (bitmapNext != null && mSmbImages!!.size < 3) {
                return@post
            }
            lockInput = true;
            bitmapPre = null
            mNetHandler!!.post {
                bitmapPre = downloadImage((index - 1) % mSmbImages!!.size)
                lockInput = false
            }
        }
    }
}
