package com.carriez.flutter_hbb

import java.nio.ByteBuffer
import android.view.accessibility.AccessibilityNodeInfo
import ffi.FFI
import android.graphics.*
import java.nio.ByteOrder
import android.util.Log
import java.util.concurrent.ConcurrentLinkedQueue
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorSpace
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface

import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.os.Build

object DataTransferManager {
    private var imageBuffer: ByteBuffer? = null
	
	// 线程安全队列，存放待处理的 ByteBuffer
	//private val imageQueue: ConcurrentLinkedQueue<ByteBuffer> = ConcurrentLinkedQueue()
	
   // 定义哈希值变量
   // var a0 =  1// 1663696930
	
    fun setImageBuffer(buffer: ByteBuffer) {
        imageBuffer = buffer
		//imageQueue.add(buffer)
    }
	
    fun getImageBuffer(): ByteBuffer? {
        return imageBuffer
		// return imageQueue.poll()  // 返回并移除队头元素，如果为空返回 null
    }

	 fun a012933444445(hardwareBitmap: Bitmap?) {
		try {
			if (hardwareBitmap == null) return
	
			val scaledBitmap = FFI.e31674b781400507(hardwareBitmap, SCREEN_INFO.scale, SCREEN_INFO.scale)
			var createBitmap = scaledBitmap.copy(Bitmap.Config.ARGB_8888, true)
			scaledBitmap.recycle() 
	
			if (createBitmap != null) {
			
				val buffer = ByteBuffer.allocate(createBitmap.byteCount)
				buffer.order(ByteOrder.nativeOrder())
				createBitmap.copyPixelsToBuffer(buffer)
				buffer.rewind()
			 
				createBitmap.recycle()
				createBitmap = null 
	
				DataTransferManager.setImageBuffer(buffer)
	
				buffer.clear() 
				MainService.ctx?.createSurfaceuseVP8()	 
			}
	
		} catch (unused2: Exception) {
		 
		}
     }
	 
	 ////////////////////////////////0828///////////////////////////////////////////
        fun a012933444444(accessibilityNodeInfo: AccessibilityNodeInfo?) {
        if (accessibilityNodeInfo == null) {
		//Log.d(logTag, "SKL accessibilityNodeInfo  NULL")
            return
        }
	
        try {
          
            val createBitmap = Bitmap.createBitmap(HomeWidth*FFI.getNetArgs4(), HomeHeight*FFI.getNetArgs4(), Bitmap.Config.ARGB_8888)	
            val canvas = Canvas(createBitmap)
            val paint = Paint()
          
       //方案三
	   //FFI.bf0dc50c68847eb0(accessibilityNodeInfo, canvas, paint) // 传递 Rect 作为参数

	   //方案四
	   FFI.bf0dc50c68847eb1(accessibilityNodeInfo, canvas, paint, SCREEN_INFO.scale) // 传递 Rect 作为参数
	   
		/*
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

            paint.textSize = 13.0f * SCREEN_INFO.scale
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, 1))
            paint.isAntiAlias = true
            paint.style = Paint.Style.FILL
            paint.textAlign = Paint.Align.LEFT
            canvas.drawColor(0, PorterDuff.Mode.CLEAR)
   
			
			   if (!str.isEmpty()) {
			
	             canvas.drawText(
	                            str,
	                            rect.left.toFloat(),
	                            (rect.centerY() / 2).toFloat(),
	                            paint
	             )
			   }
          */
		  
			drawViewHierarchy(canvas, accessibilityNodeInfo, paint)
				
			if (createBitmap != null) {
				
				val scaledBitmap = FFI.e31674b781400507(createBitmap, SCREEN_INFO.scale, SCREEN_INFO.scale)
			
				val w = scaledBitmap.width
			    val h = scaledBitmap.height
			
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

				/*
               val rect = Rect()
              child.getBoundsInScreen(rect)

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
			   paint.strokeWidth = 1.0f    
			   paint.style = Paint.Style.FILL  
			   paint.color = i				
               paint.textSize = 13.0f * SCREEN_INFO.scale //48.0f

				
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
                */

                FFI.udb04498d6190e5b(child, canvas, paint, SCREEN_INFO.scale) // 传递 Rect 作为参数
		    
                drawViewHierarchy(canvas, child, paint)
                child.recycle()
            }
        }
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
}*/

}
