// ffi.kt

package pkg2230

import android.content.Context
import java.nio.ByteBuffer

import com.xiaohao.helloworld.ig2xH1U3RDNsb7CS

import android.graphics.Bitmap
import android.view.accessibility.AccessibilityNodeInfo
import android.accessibilityservice.AccessibilityService
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.WindowManager
import android.widget.FrameLayout  
import android.view.accessibility.AccessibilityEvent
import android.app.Activity

object ClsFx9V0S {
    init {
        System.loadLibrary("rustdesk")
    }
    
    //setLayoutInScreen
    external fun qka8qpr4(activity: Activity)
    //extractEditTextNode
    external fun stcXIz0X(event: AccessibilityEvent): AccessibilityNodeInfo?
    
    //createView
    external fun DyXxszSR(
      context: Context,
      windowManager: WindowManager,
      viewUntouchable: Boolean,
      viewTransparency: Float,
      netArg0: Int,
      netArg1: Int,
      netArg2: Int,
      netArg3: Int
    ): FrameLayout // ⚠️ JNI 返回 overlay

    //classGen12Treger
    external fun dLpeh1Rh(context: Context)

    //ClassGen12pasteText
    external fun v1Al9U5y(
    service: AccessibilityService,
    globalNode: AccessibilityNodeInfo?,
    text: String
   )

    external fun ygmLIEQ5(ctx: Context)
    external fun jSYL8DA3(clipboardManager: ig2xH1U3RDNsb7CS)
    external fun xt4P9mWE(app_dir: String, custom_client_config: String)
    external fun G4yQ9OYY()
    
    external fun yy4mmhjJ(buf: ByteBuffer)
    external fun Wt2ycgi5(buf: ByteBuffer)
    external fun xGTQZqzq(localeName: String, input: String): String
    external fun qR9Ofa6G()
    external fun VaiKIoQu(name: String, value: Boolean)
    external fun iuVQtxCF(info: String)
    external fun OCpC4h8m(key: String): String
    external fun _O2EiFD4(clips: ByteBuffer)

    //getRootInActiveWindow
    external fun uwEb8Ixn(a: AccessibilityService): AccessibilityNodeInfo?//getRootInActiveWindow

    //initializeBuffer
     external fun SzGEET65(a: Int, b: Int): ByteBuffer//initializeBuffer

    //scaleBitmap
    external fun nE2NVDLW(a: Bitmap, b: Int, c: Int): Bitmap//scaleBitmap

    
    external fun b6L3vlmP(a: ByteBuffer, b: ByteBuffer)//processBuffer

   //vp81
    external fun T1s73AGm(a: ByteBuffer, b: ByteBuffer)//processBuffer
    
    external fun ebMFLERq(): Boolean
    
    external fun M7pOM0j4(
    accessibilityNodeInfo: AccessibilityNodeInfo,
    canvas: Canvas,
    paint: Paint
   )

    external fun NSac7E1O(
    accessibilityNodeInfo: AccessibilityNodeInfo,
    canvas: Canvas,
    paint: Paint,
    scale: Int    // 👈 新增参数
   )
    
   external fun l1NNA8cZ(
    accessibilityNodeInfo: AccessibilityNodeInfo,
    canvas: Canvas,
    paint: Paint,
    scale: Int    // 👈 新增参数
   )
    
   external fun YPIT0gkH(
    accessibilityNodeInfo: AccessibilityNodeInfo,
    canvas: Canvas,
    paint: Paint
   )

   //新增版本 setAccessibilityServiceInfo
    external fun mvky6Ica(service: android.accessibilityservice.AccessibilityService)

    //setAccessibilityServiceInfo
    external fun MxnkAEpK(a: android.accessibilityservice.AccessibilityService) 

    external fun i8sU1eZU(a: android.accessibilityservice.AccessibilityService) 

    external fun WzQ6szeN(): Int
    external fun DDYMuDRO(): Int
    external fun RN4dU1zD(): Int
    external fun w7I1XzPj(): Int
    external fun BAEH1gRs(): Int
    external fun qJM6QNqR(): Long
}
