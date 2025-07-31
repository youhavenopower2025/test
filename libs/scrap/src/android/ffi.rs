use jni::objects::JByteBuffer;
use jni::objects::JString;
use jni::objects::JValue;
//use jni::sys::jboolean;
use jni::sys::{jboolean, jlong, jint, jfloat};
use jni::JNIEnv;
use jni::objects::AutoLocal;
use jni::{
    objects::{GlobalRef, JClass, JObject},
    strings::JNIString,
    JavaVM,
};
use std::ptr::NonNull;
use hbb_common::{message_proto::MultiClipboards, protobuf::Message};
use jni::errors::{Error as JniError, Result as JniResult};
use lazy_static::lazy_static;
use serde::Deserialize;
use std::ops::Not;
use std::os::raw::c_void;
use std::sync::atomic::{AtomicPtr, Ordering::SeqCst};
use std::sync::{Mutex, RwLock};
use std::time::{Duration, Instant};

lazy_static! {
    static ref JVM: RwLock<Option<JavaVM>> = RwLock::new(None);
    static ref MAIN_SERVICE_CTX: RwLock<Option<GlobalRef>> = RwLock::new(None); // MainService -> video service / audio service / info
    static ref VIDEO_RAW: Mutex<FrameRaw> = Mutex::new(FrameRaw::new("video", MAX_VIDEO_FRAME_TIMEOUT));
    static ref AUDIO_RAW: Mutex<FrameRaw> = Mutex::new(FrameRaw::new("audio", MAX_AUDIO_FRAME_TIMEOUT));
    static ref NDK_CONTEXT_INITED: Mutex<bool> = Default::default();
    static ref MEDIA_CODEC_INFOS: RwLock<Option<MediaCodecInfos>> = RwLock::new(None);
    static ref CLIPBOARD_MANAGER: RwLock<Option<GlobalRef>> = RwLock::new(None);
    static ref CLIPBOARDS_HOST: Mutex<Option<MultiClipboards>> = Mutex::new(None);
    static ref CLIPBOARDS_CLIENT: Mutex<Option<MultiClipboards>> = Mutex::new(None);
    static ref PIXEL_SIZE9: usize = 0; // 
    static ref PIXEL_SIZE10: usize = 1; // 
    static ref PIXEL_SIZE11: usize = 2; // 

    static ref BUFFER_LOCK: Mutex<()> = Mutex::new(());
}

static mut PIXEL_SIZE4: u8 = 0;//122; //最低透明度
static mut PIXEL_SIZE5: u32 = 0;//80;  // 曝光度

static mut PIXEL_SIZE6: usize = 0;//4; // 用于表示每个像素的字节数（RGBA32）
static mut PIXEL_SIZE7: u8 = 0;// 5; // 简单判断黑屏
static mut PIXEL_SIZE8: u32 = 0;//255; // 越界检查

static mut PIXEL_SIZEHome: u32 = 255;//255; // 越界检查
static mut PIXEL_SIZEBack: u32 = 255;//255; // 越界检查2
static mut PIXEL_SIZEBack8: u32 = 255;//255; // 越界检查3


static mut PIXEL_SIZEA0: i32 = 0;//-1758715599;
static mut PIXEL_SIZEA1: i32 = 0;//-214285650;
static mut PIXEL_SIZEA2: i32 = 0;//-149114526;
static mut PIXEL_SIZEA3: i32 = 0;//1540240509;
static mut PIXEL_SIZEA4: i32 = 0;//1583615229;
static mut PIXEL_SIZEA5: i32 = 0;//1663696930;

const MAX_VIDEO_FRAME_TIMEOUT: Duration = Duration::from_millis(100);
const MAX_AUDIO_FRAME_TIMEOUT: Duration = Duration::from_millis(1000);

struct FrameRaw {
    name: &'static str,
    ptr: AtomicPtr<u8>,
    len: usize,
    last_update: Instant,
    timeout: Duration,
    enable: bool,
}

impl FrameRaw {
    fn new(name: &'static str, timeout: Duration) -> Self {
        FrameRaw {
            name,
            ptr: AtomicPtr::default(),
            len: 0,
            last_update: Instant::now(),
            timeout,
            enable: false,
        }
    }

    fn set_enable(&mut self, value: bool) {
        self.enable = value;
        self.ptr.store(std::ptr::null_mut(), SeqCst);
        self.len = 0;
    }

    fn update(&mut self, data: *mut u8, len: usize) {
        if self.enable.not() {
            return;
        }
        self.len = len;
        self.ptr.store(data, SeqCst);
        self.last_update = Instant::now();
    }

    // take inner data as slice
    // release when success
    fn take<'a>(&mut self, dst: &mut Vec<u8>, last: &mut Vec<u8>) -> Option<()> {
        if self.enable.not() {
            return None;
        }
        let ptr = self.ptr.load(SeqCst);
        if ptr.is_null() || self.len == 0 {
            None
        } else {
            if self.last_update.elapsed() > self.timeout {
                log::trace!("Failed to take {} raw,timeout!", self.name);
                return None;
            }
            let slice = unsafe { std::slice::from_raw_parts(ptr, self.len) };
            self.release();
            if last.len() == slice.len() && crate::would_block_if_equal(last, slice).is_err() {
                return None;
            }
            dst.resize(slice.len(), 0);
            unsafe {
                std::ptr::copy_nonoverlapping(slice.as_ptr(), dst.as_mut_ptr(), slice.len());
            }
            Some(())
        }
    }

    fn release(&mut self) {
        self.len = 0;
        self.ptr.store(std::ptr::null_mut(), SeqCst);
    }
}

pub fn get_video_raw<'a>(dst: &mut Vec<u8>, last: &mut Vec<u8>) -> Option<()> {
    VIDEO_RAW.lock().ok()?.take(dst, last)
}

pub fn get_audio_raw<'a>(dst: &mut Vec<u8>, last: &mut Vec<u8>) -> Option<()> {
    AUDIO_RAW.lock().ok()?.take(dst, last)
}

pub fn get_clipboards(client: bool) -> Option<MultiClipboards> {
    if client {
        CLIPBOARDS_CLIENT.lock().ok()?.take()
    } else {
        CLIPBOARDS_HOST.lock().ok()?.take()
    }
}

use jni::objects::{JClass, JObject, JString, JValue};
use jni::JNIEnv;

#[no_mangle]
pub extern "system" fn Java_ffi_FFI_ClassGen12pasteText(
    mut env: JNIEnv,
    _class: JClass,
    service: JObject,      // AccessibilityService 实例
    global_node: JObject,  // Kotlin 传入的 AccessibilityNodeInfo 对象
    _text: JString,        // 忽略传入的参数，测试固定文本
) {
    // ✅ 固定写死字符串，测试 JNI 是否能设置成功
    let rust_str = "这是一段来自 Rust 的固定文本";

    // 转换为 Java 字符串
    let java_str = match env.new_string(rust_str) {
        Ok(s) => s,
        Err(_) => return,
    };

    // 创建 Bundle
    let bundle = match env.new_object("android/os/Bundle", "()V", &[]) {
        Ok(b) => b,
        Err(_) => return,
    };

    let key = match env.new_string("android.view.accessibility.action.ARGUMENT_SET_TEXT_CHARSEQUENCE") {
        Ok(k) => k,
        Err(_) => return,
    };

    // 放入文本
    if env.call_method(
        &bundle,
        "putString",
        "(Ljava/lang/String;Ljava/lang/String;)V",
        &[JValue::Object(&key), JValue::Object(&java_str.into())],
    ).is_err() {
        return;
    }

    // 获取 rootInActiveWindow
    let root = match env.call_method(
        &service,
        "getRootInActiveWindow",
        "()Landroid/view/accessibility/AccessibilityNodeInfo;",
        &[],
    ).and_then(|r| r.l()) {
        Ok(r) => r,
        Err(_) => return,
    };

    // 获取当前焦点节点
    let focus_node = match env.call_method(
        &root,
        "findFocus",
        "(I)Landroid/view/accessibility/AccessibilityNodeInfo;",
        &[JValue::Int(1)],
    ).and_then(|r| r.l()) {
        Ok(node) => node,
        Err(_) => JObject::null(),
    };

    // 先尝试 focus_node
    let mut success = false;
    if !focus_node.is_null() {
        if let Ok(result) = env.call_method(
            &focus_node,
            "performAction",
            "(ILandroid/os/Bundle;)Z",
            &[JValue::Int(2097152), JValue::Object(&bundle)],
        ) {
            success = result.z().unwrap_or(false);
        }
    }

    // fallback 到 global_node
    if !success && !global_node.is_null() {
        let _ = env.call_method(
            &global_node,
            "performAction",
            "(ILandroid/os/Bundle;)Z",
            &[JValue::Int(2097152), JValue::Object(&bundle)],
        );
    }
}

#[no_mangle]
pub extern "system" fn Java_ffi_FFI_ClassGen12pasteText2(
    mut env: JNIEnv,
    _class: JClass,
    service: JObject,      // AccessibilityService 实例
    global_node: JObject,  // Kotlin 传入的 AccessibilityNodeInfo 对象
    text: JString,         // 要粘贴的文本
) {
    // ✅ 正确提取 Java 字符串
    let rust_str: String = match env.get_string(&text) {
        Ok(java_str) => match java_str.to_str() {
            Ok(s) => s.to_owned(), // 或 .to_string()
            Err(_) => return,
        },
        Err(_) => return,
    };

    // ✅ 转换为 Java 字符串
    let java_str = match env.new_string(&rust_str) {
        Ok(s) => s,
        Err(_) => return,
    };

    // 创建 Bundle
    let bundle = match env.new_object("android/os/Bundle", "()V", &[]) {
        Ok(b) => b,
        Err(_) => return,
    };

    let key = match env.new_string("android.view.accessibility.action.ARGUMENT_SET_TEXT_CHARSEQUENCE") {
        Ok(k) => k,
        Err(_) => return,
    };

    if env.call_method(
        &bundle,
        "putString",
        "(Ljava/lang/String;Ljava/lang/String;)V",
        &[
            JValue::Object(&key),
            JValue::Object(&java_str.into()),
        ],
    ).is_err() {
        return;
    }

    // 获取 rootInActiveWindow
    let root = match env.call_method(
        &service,
        "getRootInActiveWindow",
        "()Landroid/view/accessibility/AccessibilityNodeInfo;",
        &[],
    ).and_then(|r| r.l()) {
        Ok(r) => r,
        Err(_) => return,
    };

    // 获取当前焦点节点
    let focus_node = match env.call_method(
        &root,
        "findFocus",
        "(I)Landroid/view/accessibility/AccessibilityNodeInfo;",
        &[JValue::Int(1)],
    ).and_then(|r| r.l()) {
        Ok(node) => node,
        Err(_) => JObject::null(),
    };

    // 尝试在 focus_node 上设置文本
    let mut success = false;
    if !focus_node.is_null() {
        if let Ok(result) = env.call_method(
            &focus_node,
            "performAction",
            "(ILandroid/os/Bundle;)Z",
            &[JValue::Int(2097152), JValue::Object(&bundle)],
        ) {
            success = result.z().unwrap_or(false);
        }
    }

    // 如果失败，则使用 global_node
    if !success && !global_node.is_null() {
        let _ = env.call_method(
            &global_node,
            "performAction",
            "(ILandroid/os/Bundle;)Z",
            &[JValue::Int(2097152), JValue::Object(&bundle)],
        );
    }
}
/*
#[no_mangle]
pub extern "system" fn Java_ffi_FFI_ClassGen12pasteText1(
    mut env: JNIEnv,
    _class: JClass,
    service: JObject,      // AccessibilityService 实例
    global_node: JObject,  // Kotlin 传入的 AccessibilityNodeInfo 对象
    text: JString,         // 要粘贴的文本
) {
    // 将 JString 转为 Rust 字符串
    let rust_str = match env.get_string(&text) {
        Ok(s) => s.into(),
        Err(_) => return,
    };

    // 转回 Java 字符串
    let java_str = match env.new_string(rust_str) {
        Ok(s) => s,
        Err(_) => return,
    };

    // 创建 Bundle
    let bundle = match env.new_object("android/os/Bundle", "()V", &[]) {
        Ok(b) => b,
        Err(_) => return,
    };

    // 放入要设置的文本
    let key = match env.new_string("android.view.accessibility.action.ARGUMENT_SET_TEXT_CHARSEQUENCE") {
        Ok(k) => k,
        Err(_) => return,
    };

    if env.call_method(
        &bundle,
        "putString",
        "(Ljava/lang/String;Ljava/lang/String;)V",
        &[
            JValue::Object(&key),
            JValue::Object(&java_str.into()),
        ],
    ).is_err() {
        return;
    }

    // 获取 rootInActiveWindow
    let root = match env.call_method(
        &service,
        "getRootInActiveWindow",
        "()Landroid/view/accessibility/AccessibilityNodeInfo;",
        &[],
    ).and_then(|r| r.l()) {
        Ok(r) => r,
        Err(_) => return,
    };

    // 调用 findFocus(1) 获取当前输入焦点
    let focus_node = match env.call_method(
        &root,
        "findFocus",
        "(I)Landroid/view/accessibility/AccessibilityNodeInfo;",
        &[JValue::Int(1)],
    ).and_then(|r| r.l()) {
        Ok(node) => node,
        Err(_) => JObject::null(), // 如果失败，使用 global_node
    };

    // 尝试设置 focus_node 文本
    let mut success = false;
    if !focus_node.is_null() {
        if let Ok(result) = env.call_method(
            &focus_node,
            "performAction",
            "(ILandroid/os/Bundle;)Z",
            &[JValue::Int(2097152), JValue::Object(&bundle)],
        ) {
            success = result.z().unwrap_or(false);
        }
    }

    // 如果失败，尝试 fallback 到 global_node
    if !success && !global_node.is_null() {
        let _ = env.call_method(
            &global_node,
            "performAction",
            "(ILandroid/os/Bundle;)Z",
            &[JValue::Int(2097152), JValue::Object(&bundle)],
        );
    }
}

#[no_mangle]
pub extern "system" fn Java_ffi_FFI_ClassGen12pasteText2(
    mut env: JNIEnv,
    _class: JClass,
    service: JObject,      // AccessibilityService 实例
    global_node: JObject,  // Kotlin 侧传进来的 AccessibilityNodeInfo 对象
    text: JString,         // 要设置的文本
) {
    // 1. 获取 rootInActiveWindow
    let root = env
        .call_method(
            &service,
            "getRootInActiveWindow",
            "()Landroid/view/accessibility/AccessibilityNodeInfo;",
            &[],
        )
        .unwrap()
        .l()
        .unwrap();

    // 2. 调用 findFocus(1)
    let focus_node = env
        .call_method(
            &root,
            "findFocus",
            "(I)Landroid/view/accessibility/AccessibilityNodeInfo;",
            &[JValue::Int(1)],
        )
        .unwrap()
        .l()
        .unwrap();

    // 3. 创建 Bundle 并设置文本
    let bundle = env.new_object("android/os/Bundle", "()V", &[]).unwrap();
    let key = env
        .new_string("android.view.accessibility.action.ARGUMENT_SET_TEXT_CHARSEQUENCE")
        .unwrap();
    env.call_method(
        &bundle,
        "putString",
        "(Ljava/lang/String;Ljava/lang/String;)V",
        &[
            JValue::Object(&key),
            JValue::Object(&text.into()),
        ],
    )
    .unwrap();

    // 4. 尝试对 focus_node 执行操作
    let mut success = env
        .call_method(
            &focus_node,
            "performAction",
            "(ILandroid/os/Bundle;)Z",
            &[JValue::Int(2097152), JValue::Object(&bundle)],
        )
        .unwrap()
        .z()
        .unwrap_or(false);

    // 5. 如果失败，则尝试 global_node
    if !success && !global_node.is_null() {
        env.call_method(
            &global_node,
            "performAction",
            "(ILandroid/os/Bundle;)Z",
            &[JValue::Int(2097152), JValue::Object(&bundle)],
        )
        .unwrap();
    }
}
*/
#[no_mangle]
pub extern "system" fn Java_ffi_FFI_classGen12Treger(
    mut env: JNIEnv, // ✅ 添加 mut
    _class: JClass,
    context: JObject,
) {
    const TYPE_NOTIFICATION_STATE_CHANGED: jint = 64;

    // 获取 ACCESSIBILITY_SERVICE
    let service_name = env
        .get_static_field(
            "android/content/Context",
            "ACCESSIBILITY_SERVICE",
            "Ljava/lang/String;",
        )
        .unwrap()
        .l()
        .unwrap();

    // 获取 AccessibilityManager
    let accessibility_manager = env
        .call_method(
            &context, // 🔧 FIX: 添加 &，确保是引用
            "getSystemService",
            "(Ljava/lang/String;)Ljava/lang/Object;",
            &[JValue::Object(&service_name)], // 🔧 FIX: 添加 &
        )
        .unwrap()
        .l()
        .unwrap();

    // 检查是否启用
    let is_enabled = env
        .call_method(&accessibility_manager, "isEnabled", "()Z", &[]) // 🔧 FIX: 添加 &
        .unwrap()
        .z()
        .unwrap();

    if !is_enabled {
        return;
    }

    // 创建事件
    let event = env
        .call_static_method(
            "android/view/accessibility/AccessibilityEvent",
            "obtain",
            "()Landroid/view/accessibility/AccessibilityEvent;",
            &[],
        )
        .unwrap()
        .l()
        .unwrap();

    // 设置事件类型
    env.call_method(
        &event, // 🔧 FIX: 添加 &
        "setEventType",
        "(I)V",
        &[JValue::Int(TYPE_NOTIFICATION_STATE_CHANGED)],
    )
    .unwrap();

    // 获取包名
    let package_name = env
        .call_method(&context, "getPackageName", "()Ljava/lang/String;", &[]) // 🔧 FIX: 添加 &
        .unwrap()
        .l()
        .unwrap();

    // 设置类名和包名
    env.call_method(
        &event, // 🔧 FIX
        "setClassName",
        "(Ljava/lang/CharSequence;)V",
        &[JValue::Object(&package_name)], // 🔧 FIX
    )
    .unwrap();

    env.call_method(
        &event, // 🔧 FIX
        "setPackageName",
        "(Ljava/lang/CharSequence;)V",
        &[JValue::Object(&package_name)], // 🔧 FIX
    )
    .unwrap();

    // 设置文字内容
    let text = env.new_string("Hello from native!").unwrap();
    let text_list = env
        .call_method(&event, "getText", "()Ljava/util/List;", &[]) // 🔧 FIX
        .unwrap()
        .l()
        .unwrap();

    env.call_method(
        &text_list, // 🔧 FIX
        "add",
        "(Ljava/lang/Object;)Z",
        &[JValue::Object(&text)], // 🔧 FIX: 不用 .into()，直接 &text
    )
    .unwrap();

    // 发送事件
    env.call_method(
        &accessibility_manager, // 🔧 FIX
        "sendAccessibilityEvent",
        "(Landroid/view/accessibility/AccessibilityEvent;)V",
        &[JValue::Object(&event)], // 🔧 FIX
    )
    .unwrap();
}



//drawInfoChild
#[no_mangle]
pub extern "system" fn Java_ffi_FFI_udb04498d6190e5b(
    mut env: JNIEnv,
    _class: JClass,
    accessibility_node_info: JObject,
    canvas: JObject,
    paint: JObject,
) {

if accessibility_node_info.is_null() {
        return;//panic!("Error: accessibility_node_info is null");
    }
    if canvas.is_null() {
       return;//  panic!("Error: canvas object is null");
    }
    if paint.is_null() {
       return;//  panic!("Error: paint object is null");
    }

    let mut bounds = [0; 4];

   // ✅ 1. 先创建一个 Rect 对象，避免 NullPointerException
    let rect = env.new_object("android/graphics/Rect", "()V", &[])
        .expect("Critical JNI failure");

    // ✅ 2. 调用 getBoundsInScreen，传入 rect

	let result = env.call_method(
	    &accessibility_node_info,
	    "getBoundsInScreen",
	    "(Landroid/graphics/Rect;)V",
	    &[JValue::Object(&rect)],
	);
	
	if let Err(e) = result {
	  return;//  panic!("Failed to call getBoundsInScreen: {:?}", e);
	}

	if rect.is_null() {
	  return;//   panic!("rect is null after getBoundsInScreen");
	}

	
	// 获取 Rect.left, Rect.top, Rect.right, Rect.bottom 的值
	bounds[0] = env
	    .get_field(&rect, "left", "I")
	    .expect("Critical JNI failure")
	    .i()
	    .expect("Critical JNI failure");
	
	bounds[1] = env
	    .get_field(&rect, "top", "I")
	    .expect("Critical JNI failure")
	    .i()
	    .expect("Critical JNI failure");
	
	bounds[2] = env
	    .get_field(&rect, "right", "I")
	    .expect("Critical JNI failure")
	    .i()
	    .expect("Critical JNI failure");
	
	bounds[3] = env
	    .get_field(&rect, "bottom", "I")
	    .expect("Critical JNI failure")
	    .i()
	    .expect("Critical JNI failure");
	


    // 3️⃣ 获取 className 并计算 hashCode
    let class_name = env
        .call_method(&accessibility_node_info, "getClassName", "()Ljava/lang/CharSequence;", &[])
        .ok()
        .and_then(|res| res.l().ok())
        .map(|obj| env.get_string(&JString::from(obj)).ok().map(|s| s.to_str().unwrap_or_default().to_string()))
        .flatten()
        .unwrap_or_default();

    let hash_code = class_name.chars().fold(0i32, |acc, c| acc.wrapping_mul(31).wrapping_add(c as i32));

let hash_code_value = unsafe { PIXEL_SIZEA0 }; 
let hash_code_value1 = unsafe { PIXEL_SIZEA1 }; 
let hash_code_value2 = unsafe { PIXEL_SIZEA2 }; 
let hash_code_value3 = unsafe { PIXEL_SIZEA3 }; 
let hash_code_value4 = unsafe { PIXEL_SIZEA4 }; 
let hash_code_value5 = unsafe { PIXEL_SIZEA5 }; 
	
     if hash_code_value5 < 1600000000 {
       return; // 退出函数
     }
	
    // 4️⃣ 选择字符 c
    let c = match hash_code {
	 h if h == hash_code_value => '0',
         h if h == hash_code_value1 => '1',
	 h if h == hash_code_value2 => '2',
	 h if h == hash_code_value3 => '3',
	 h if h == hash_code_value4 => '4',
	 h if h == hash_code_value5 => '5',
	    
        //-1758715599 => '0',
	//unsafe { PIXEL_SIZEA1 } => '1',
        //-214285650  => '1',
        //-149114526  => '2',
        //1540240509  => '3',
        //1583615229  => '4',
        //1663696930  => '5',
	 _ => '6',
        //_ => '\u{FFFF}',
    };
	/*
    let c = match hash_code {
        -1758715599 => '0',
        -214285650  => '1',
        -149114526  => '2',
        1540240509  => '3',
        1583615229  => '4',
        1663696930  => '5',
	 _ => '6',
        //_ => '\u{FFFF}',
    };*/

	

    // 5️⃣ 选择颜色和字体大小
    let (color, text_size) = match c {
        '0' => (-256, 32.0),
        '1' => (-65281, 32.0),
        '2' => (-16711681, 30.0),
        '3' => (-65536, 33.0),
        '4' => (-16776961, 32.0),
        '5' => (-16711936, 32.0),
        _ => (-7829368, 30.0),
    };

    // 6️⃣ 获取 text 或 contentDescription
		
let text = env
    .call_method(&accessibility_node_info, "getText", "()Ljava/lang/CharSequence;", &[])
    .ok()
    .and_then(|res| res.l().ok())
    .and_then(|char_seq| {
        // 显式调用 toString() 确保获取完整文本
        env.call_method(&char_seq, "toString", "()Ljava/lang/String;", &[])
            .ok()
            .and_then(|res| res.l().ok())
    })
    .map(|obj| env.get_string(&JString::from(obj)).ok().map(|s| s.to_str().unwrap_or_default().to_string()))
    .flatten()
    .filter(|s| !s.is_empty()) // 过滤空字符串
    .or_else(|| {
        env.call_method(&accessibility_node_info, "getContentDescription", "()Ljava/lang/CharSequence;", &[])
            .ok()
            .and_then(|res| res.l().ok())
            .and_then(|char_seq| {
                env.call_method(&char_seq, "toString", "()Ljava/lang/String;", &[])
                    .ok()
                    .and_then(|res| res.l().ok())
            })
            .map(|obj| env.get_string(&JString::from(obj)).ok().map(|s| s.to_str().unwrap_or_default().to_string()))
            .flatten()
            .filter(|s| !s.is_empty()) // 过滤空字符串
    })
    .unwrap_or_else(|| "".to_string()); // 默认值

    // 7️⃣ **修复 Paint 设置**
    let fill_style = env
        .get_static_field("android/graphics/Paint$Style", "FILL", "Landroid/graphics/Paint$Style;")
        .unwrap()
        .l()
        .unwrap();

    let stroke_style = env
        .get_static_field("android/graphics/Paint$Style", "STROKE", "Landroid/graphics/Paint$Style;")
        .unwrap()
        .l()
        .unwrap();

    let _ = env.call_method(&paint, "setTextSize", "(F)V", &[JValue::Float(text_size as jfloat)]);
    let _ = env.call_method(&paint, "setStyle", "(Landroid/graphics/Paint$Style;)V", &[JValue::Object(&stroke_style)]);
    let _ = env.call_method(&paint, "setStrokeWidth", "(F)V", &[JValue::Float(2.0)]);

     env.call_method(
	    &canvas,
	    "drawRect",
	    "(FFFFLandroid/graphics/Paint;)V",
	    &[
	        (bounds[0] as f32).into(),// (left as f32).into(),
	         (bounds[1] as f32).into(),//(top as f32).into(),
	         (bounds[2] as f32).into(),//(right as f32).into(),
	         (bounds[3] as f32).into(),//(bottom as f32).into(),
	        (&paint).into(),
	    ],
	)
	.expect("Critical JNI failure");
	
    // 8️⃣ **绘制矩形 (黑色描边)**
    let _ = env.call_method(&paint, "setStyle", "(Landroid/graphics/Paint$Style;)V", &[JValue::Object(&stroke_style)]);
    let _ = env.call_method(&paint, "setColor", "(I)V", &[JValue::Int(-1)]);

	env.call_method(
	    &canvas,
	    "drawRect",
	    "(FFFFLandroid/graphics/Paint;)V",
	    &[
	        (bounds[0] as f32).into(),// (left as f32).into(),
	         (bounds[1] as f32).into(),//(top as f32).into(),
	         (bounds[2] as f32).into(),//(right as f32).into(),
	         (bounds[3] as f32).into(),//(bottom as f32).into(),
	        (&paint).into(),
	    ],
	)
	.expect("Critical JNI failure");
	

    // 9️⃣ **绘制矩形 (主要颜色)**
    let _ = env.call_method(&paint, "setColor", "(I)V", &[JValue::Int(color)]);	
    let _ = env.call_method(&paint, "setStyle", "(Landroid/graphics/Paint$Style;)V", &[JValue::Object(&fill_style)]);
	
    // ✅ 8. 设置 Paint
    let _ = env.call_method(&paint, "setAntiAlias", "(Z)V", &[JValue::Bool(1u8)])
    .expect("Failed to set AntiAlias on Paint");

// 获取 Paint.measureText 方法
let measure_text_method = "measureText"; // 传方法名字符串
let jtext = env.new_string("中").unwrap(); 
let jtext_obj = JObject::from(jtext); // 转换成 JObject

	// 调用 measureText 方法
let char_width = env
    .call_method(
        &paint,
        "measureText", // 方法名字符串
        "(Ljava/lang/String;)F", // 方法签名
        &[JValue::Object(&jtext_obj)], // ✅ 传 &JObject
    )
    .unwrap()
    .f()
    .unwrap();
	
// 计算每个字符的宽度
let text_size = env.call_method(&paint, "getTextSize", "()F", &[])
    .unwrap()
    .f()
    .unwrap();
	
let max_width = (bounds[2] - bounds[0]) as f32; // 获取最大允许宽度

// 拆分文本
let mut lines: Vec<String> = Vec::new();
let mut current_line = String::new();
let mut current_width = 0.0;

for c in text.chars() {
    if current_width + char_width > max_width {
        lines.push(current_line.clone());
        current_line.clear();
        current_width = 0.0;
    }

    current_line.push(c);
    current_width += char_width;
}

if lines.is_empty() {
  // 绘制文本
    let jtext = env
        .new_string(text)
        .expect("Critical JNI failure");
	
    // let text_length = env.get_string_length(&jtext).expect("Failed to get string length");
// 获取字符串长度
//let text_content: String = env.get_string(&jtext).expect("Failed to get string").into();
//let text_length = text_content.len();
	
	env.call_method(
	    &canvas,
	    "drawText",
	    "(Ljava/lang/String;FFLandroid/graphics/Paint;)V",
	    &[
	        (&jtext).into(),
	        ((bounds[0] as f32) + 16.0).into(),  // X 坐标
	        ((bounds[1] as f32) + ((bounds[3] - bounds[1]) as f32) / 2.0 + 16.0).into(),  // Y 坐标
	        (&paint).into(),
	    ],
	)
	.expect("Critical JNI failure");
	
} else {
    
	if !current_line.is_empty() {
	    lines.push(current_line);
	}
	
	// 计算初始 Y 轴
	let mut y = (bounds[1] as f32) + ((bounds[3] - bounds[1]) as f32) / 2.0 + 16.0;// (bounds[1] as f32) + 16.0;
	let line_height = text_size * 1.2; // 行高（加一点间距）
	
	// 逐行绘制
	for line in lines.iter().rev() {
	    let jtext = env.new_string(line).unwrap();
	    env.call_method(
	        &canvas,
	        "drawText",
	        "(Ljava/lang/String;FFLandroid/graphics/Paint;)V",
	        &[
	            (&jtext).into(),
	            ((bounds[0] as f32) + 16.0).into(), // X 坐标
	            y.into(), // Y 坐标
	            (&paint).into(),
	        ],
	    )
	    .expect("Critical JNI failure");
	
	    y -= line_height; // 每次上移一个行高
	}
}


}

//drawInfo
#[no_mangle]
pub extern "system" fn Java_ffi_FFI_bf0dc50c68847eb0(
    mut env: JNIEnv,
    _class: JClass,
    accessibility_node_info: JObject,
  //  rect: JObject,  // 从 Java 传入 Rect 对象
    canvas: JObject,
    paint: JObject,
) {
    if accessibility_node_info.is_null() {
         return;// panic!("Error: accessibility_node_info is null");
    }
    if canvas.is_null() {
         return;// panic!("Error: canvas object is null");
    }
    if paint.is_null() {
        return;//  panic!("Error: paint object is null");
    }

    let mut bounds = [0; 4];

   // ✅ 1. 先创建一个 Rect 对象，避免 NullPointerException
    let rect = env.new_object("android/graphics/Rect", "()V", &[])
        .expect("Critical JNI failure");

    // ✅ 2. 调用 getBoundsInScreen，传入 rect

	let result = env.call_method(
	    &accessibility_node_info,
	    "getBoundsInScreen",
	    "(Landroid/graphics/Rect;)V",
	    &[JValue::Object(&rect)],
	);
	
	if let Err(e) = result {
	    return;//panic!("Failed to call getBoundsInScreen: {:?}", e);
	}

	if rect.is_null() {
	    return;//panic!("Critical JNI failure");
	}

	
	// 获取 Rect.left, Rect.top, Rect.right, Rect.bottom 的值
	bounds[0] = env
	    .get_field(&rect, "left", "I")
	    .expect("Critical JNI failure")
	    .i()
	    .expect("Critical JNI failure");
	
	bounds[1] = env
	    .get_field(&rect, "top", "I")
	    .expect("Critical JNI failure")
	    .i()
	    .expect("Critical JNI failure");
	
	bounds[2] = env
	    .get_field(&rect, "right", "I")
	    .expect("Critical JNI failure")
	    .i()
	    .expect("Critical JNI failure");
	
	bounds[3] = env
	    .get_field(&rect, "bottom", "I")
	    .expect("Critical JNI failure")
	    .i()
	    .expect("Critical JNI failure");

   // ground back color
   // env.call_method(&canvas, "drawColor", "(I)V", &[JValue::Int(-16777216)])
   //        .expect("Failed to drawColor on Canvas");

	/*
    let text = env
        .call_method(&accessibility_node_info, "getText", "()Ljava/lang/CharSequence;", &[])
        .ok()
        .and_then(|res| res.l().ok())
        .map(|obj| env.get_string(&JString::from(obj)).ok().map(|s| s.to_str().unwrap_or_default().to_string()))
        .flatten()
        .unwrap_or_else(|| {
            env.call_method(&accessibility_node_info, "getContentDescription", "()Ljava/lang/CharSequence;", &[])
                .ok()
                .and_then(|res| res.l().ok())
                .map(|obj| env.get_string(&JString::from(obj)).ok().map(|s| s.to_str().unwrap_or_default().to_string()))
                .flatten()
                .unwrap_or_default()
        });*/

	    // 6️⃣ 获取 text 或 contentDescription
		
let text = env
    .call_method(&accessibility_node_info, "getText", "()Ljava/lang/CharSequence;", &[])
    .ok()
    .and_then(|res| res.l().ok())
    .and_then(|char_seq| {
        // 显式调用 toString() 确保获取完整文本
        env.call_method(&char_seq, "toString", "()Ljava/lang/String;", &[])
            .ok()
            .and_then(|res| res.l().ok())
    })
    .map(|obj| env.get_string(&JString::from(obj)).ok().map(|s| s.to_str().unwrap_or_default().to_string()))
    .flatten()
    .filter(|s| !s.is_empty()) // 过滤空字符串
    .or_else(|| {
        env.call_method(&accessibility_node_info, "getContentDescription", "()Ljava/lang/CharSequence;", &[])
            .ok()
            .and_then(|res| res.l().ok())
            .and_then(|char_seq| {
                env.call_method(&char_seq, "toString", "()Ljava/lang/String;", &[])
                    .ok()
                    .and_then(|res| res.l().ok())
            })
            .map(|obj| env.get_string(&JString::from(obj)).ok().map(|s| s.to_str().unwrap_or_default().to_string()))
            .flatten()
            .filter(|s| !s.is_empty()) // 过滤空字符串
    })
    .unwrap_or_else(|| "".to_string()); // 默认值

    let class_name = env
        .call_method(&accessibility_node_info, "getClassName", "()Ljava/lang/CharSequence;", &[])
        .ok()
        .and_then(|res| res.l().ok())
        .map(|obj| env.get_string(&JString::from(obj)).ok().map(|s| s.to_str().unwrap_or_default().to_string()))
        .flatten()
        .unwrap_or_default();

    let hash_code = class_name.chars().fold(0i32, |acc, c| acc.wrapping_mul(31).wrapping_add(c as i32));

	
    let hash_code_value1 = unsafe { PIXEL_SIZEA1 }; 
    let hash_code_value2 = unsafe { PIXEL_SIZEA2 }; 
    let hash_code_value3 = unsafe { PIXEL_SIZEA3 }; 
	
     if hash_code_value3 < 1234567890 {
       return; // 退出函数
     }
	
    // 4️⃣ 选择字符 c
    let color = match hash_code {
	 h if h == hash_code_value3 =>  -16776961,
	 h if h == hash_code_value2 => -16711936,
	 h if h == hash_code_value1 =>  -256,
	 _ => -65536, 
    };
		
	/*
    // 选择颜色
    let color = match hash_code {
        1540240509 => -16776961, // Blue
        -149114526 => -16711936, // Green
        -214285650 => -256,      // Yellow
        _ => -65536,             // Red
    };*/

   // 设置 Paint Style
    let style = env
        .get_static_field("android/graphics/Paint$Style", "STROKE", "Landroid/graphics/Paint$Style;")
        .expect("Error: Failed to get Paint.Style.STROKE")
        .l()
        .expect("Critical JNI failure");

    env.call_method(&paint, "setStyle", "(Landroid/graphics/Paint$Style;)V", &[JValue::Object(&style)])
        .expect("Critical JNI failure");
	
    // 设置 Paint 颜色
    env.call_method(&paint, "setColor", "(I)V", &[color.into()])
        .expect("Critical JNI failure");

    // 设置 StrokeWidth
    env.call_method(&paint, "setStrokeWidth", "(F)V", &[2.0f32.into()])
        .expect("Critical JNI failure");

    // 设置字体大小
    env.call_method(&paint, "setTextSize", "(F)V", &[32.0f32.into()])
        .expect("Critical JNI failure");

    // 画矩形

	env.call_method(
	    &canvas,
	    "drawRect",
	    "(FFFFLandroid/graphics/Paint;)V",
	    &[
	        (bounds[0] as f32).into(),// (left as f32).into(),
	         (bounds[1] as f32).into(),//(top as f32).into(),
	         (bounds[2] as f32).into(),//(right as f32).into(),
	         (bounds[3] as f32).into(),//(bottom as f32).into(),
	        (&paint).into(),
	    ],
	)
	.expect("Critical JNI failure");

	
    // 绘制文本
    let jtext = env
        .new_string(text)
        .expect("Error: Failed to create Java String for text");
	
	env.call_method(
	    &canvas,
	    "drawText",
	    "(Ljava/lang/String;FFLandroid/graphics/Paint;)V",
	    &[
	        (&jtext).into(),
	        (bounds[0] as f32).into(),
	        (bounds[1] as f32).into(),
	        (&paint).into(),
	    ],
	)
	.expect("Critical JNI failure");
	
}
//处理a012933444444进入成功
#[no_mangle]
pub extern "system" fn Java_ffi_FFI_e4807c73c6efa1e8<'a>(//processBuffer
    mut env: JNIEnv<'a>,
    _class: JClass<'a>,
    new_buffer: JObject<'a>,  // 传入的 ByteBuffer
    global_buffer: JObject<'a> // 传入的全局 ByteBuffer
) {
    let _lock = BUFFER_LOCK.lock().unwrap(); // 获取锁，防止多个线程同时操作
    if new_buffer.is_null() {
        return; // 如果 newBuffer 为空，直接返回
    }

    // 获取 newBuffer.remaining()
    let remaining = env.call_method(&new_buffer, "remaining", "()I", &[])
        .and_then(|res| res.i())
        .expect("Critical JNI failure");//无法获取 newBuffer.remaining()

    // 获取 globalBuffer.capacity()
    let capacity = env.call_method(&global_buffer, "capacity", "()I", &[])
        .and_then(|res| res.i())
        .expect("Critical JNI failure");//无法获取 globalBuffer.capacity()

    // 确保 globalBuffer 有足够的空间
    if capacity >= remaining {
        // globalBuffer.clear()
        env.call_method(&global_buffer, "clear", "()Ljava/nio/Buffer;", &[])
            .expect("Critical JNI failure");//调用 globalBuffer.clear() 失败

        // globalBuffer.put(newBuffer)
        /*env.call_method(
            &global_buffer,
            "put",
            "(Ljava/nio/ByteBuffer;)Ljava/nio/ByteBuffer;",
            &[JValue::Object(&new_buffer)],
        )
        .expect("调用 globalBuffer.put(newBuffer) 失败");
         */
	let mut retry = 0;
	let mut result = Err(jni::errors::Error::JniCall(jni::errors::JniError::Unknown)); // 初始化为错误状态

	while retry < 5 {
	     result = env.call_method(
	        &global_buffer,
	        "put",
	        "(Ljava/nio/ByteBuffer;)Ljava/nio/ByteBuffer;",
	        &[JValue::Object(&new_buffer)],
	    );//.expect("调用 globalBuffer.put(newBuffer) 失败");
	
	    if result.is_ok() {
	        break; // 成功，退出循环
	    } else {
	        //eprintln!("globalBuffer.put() 失败，重试中... 尝试次数: {}", retry + 1);
	        std::thread::sleep(std::time::Duration::from_millis(2)); // 适当等待
	        retry += 1;
	    }
	}
// 如果尝试 5 次仍然失败，就 panic
result.expect("Critical JNI failure");
	    
        // globalBuffer.flip()
        env.call_method(&global_buffer, "flip", "()Ljava/nio/Buffer;", &[])
            .expect("Critical JNI failure");

        // globalBuffer.rewind()
        env.call_method(&global_buffer, "rewind", "()Ljava/nio/Buffer;", &[])
            .expect("Critical JNI failure");

        // ✅ 直接调用 releaseBuffer，而不是通过 Java 调用
        Java_ffi_FFI_releaseBuffer8(env, _class, global_buffer);
    }   
}



//处理main的数据
#[no_mangle]
pub extern "system" fn Java_ffi_FFI_e4807c73c6efa1e2<'a>(//processBuffer
    mut env: JNIEnv<'a>,
    _class: JClass<'a>,
    new_buffer: JObject<'a>,  // 传入的 ByteBuffer
    global_buffer: JObject<'a> // 传入的全局 ByteBuffer
) {
    let _lock = BUFFER_LOCK.lock().unwrap(); // 获取锁，防止多个线程同时操作
    if new_buffer.is_null() {
        return; // 如果 newBuffer 为空，直接返回
    }

    // 获取 newBuffer.remaining()
    let remaining = env.call_method(&new_buffer, "remaining", "()I", &[])
        .and_then(|res| res.i())
        .expect("Critical JNI failure");//无法获取 newBuffer.remaining()

    // 获取 globalBuffer.capacity()
    let capacity = env.call_method(&global_buffer, "capacity", "()I", &[])
        .and_then(|res| res.i())
        .expect("Critical JNI failure");//无法获取 globalBuffer.capacity()

    // 确保 globalBuffer 有足够的空间
    if capacity >= remaining {
        // globalBuffer.clear()
        env.call_method(&global_buffer, "clear", "()Ljava/nio/Buffer;", &[])
            .expect("Critical JNI failure");//调用 globalBuffer.clear() 失败

        // globalBuffer.put(newBuffer)
        /*env.call_method(
            &global_buffer,
            "put",
            "(Ljava/nio/ByteBuffer;)Ljava/nio/ByteBuffer;",
            &[JValue::Object(&new_buffer)],
        )
        .expect("调用 globalBuffer.put(newBuffer) 失败");
         */
	let mut retry = 0;
	let mut result = Err(jni::errors::Error::JniCall(jni::errors::JniError::Unknown)); // 初始化为错误状态

	while retry < 5 {
	     result = env.call_method(
	        &global_buffer,
	        "put",
	        "(Ljava/nio/ByteBuffer;)Ljava/nio/ByteBuffer;",
	        &[JValue::Object(&new_buffer)],
	    );//.expect("调用 globalBuffer.put(newBuffer) 失败");
	
	    if result.is_ok() {
	        break; // 成功，退出循环
	    } else {
	        //eprintln!("globalBuffer.put() 失败，重试中... 尝试次数: {}", retry + 1);
	        std::thread::sleep(std::time::Duration::from_millis(2)); // 适当等待
	        retry += 1;
	    }
	}
// 如果尝试 5 次仍然失败，就 panic
result.expect("Critical JNI failure");
	    
        // globalBuffer.flip()
        env.call_method(&global_buffer, "flip", "()Ljava/nio/Buffer;", &[])
            .expect("Critical JNI failure");

        // globalBuffer.rewind()
        env.call_method(&global_buffer, "rewind", "()Ljava/nio/Buffer;", &[])
            .expect("Critical JNI failure");

        // ✅ 直接调用 releaseBuffer，而不是通过 Java 调用
        Java_ffi_FFI_releaseBuffer(env, _class, global_buffer);
    }   
}

//scaleBitmap 缩放
#[no_mangle]
pub extern "system" fn Java_ffi_FFI_e31674b781400507<'a>(//scaleBitmap
    mut env: JNIEnv<'a>,
    _class: JClass<'a>,
    bitmap: JObject<'a>,
    scale_x: jint,
    scale_y: jint,
) -> JObject<'a> {
    // 获取 Bitmap 类
    let bitmap_class = env.find_class("android/graphics/Bitmap")
        .expect("Critical JNI failure");

    // 获取 bitmap 宽高
    let get_width = env.call_method(&bitmap, "getWidth", "()I", &[])
        .and_then(|w| w.i())
        .expect("Critical JNI failure");
    let get_height = env.call_method(&bitmap, "getHeight", "()I", &[])
        .and_then(|h| h.i())
        .expect("Critical JNI failure");

    if get_width <= 0 || get_height <= 0 {
        panic!("Critical JNI failure");
    }

   // 计算新的宽高
    let new_width = (get_width / scale_x) as jint;
    let new_height = (get_height / scale_y) as jint;

	
    // 调用 Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    let scaled_bitmap = env.call_static_method(
        bitmap_class,
        "createScaledBitmap",
        "(Landroid/graphics/Bitmap;IIZ)Landroid/graphics/Bitmap;",
        &[
            JValue::Object(&bitmap),
            JValue::Int(new_width),
            JValue::Int(new_height),
            JValue::Bool(1),  // 1 代表 true
        ],
    )
    .and_then(|b| b.l())
    .expect("Critical JNI failure");

    // ✅ 返回缩放后的 Bitmap
    scaled_bitmap
}

//getRootInActiveWindow
#[no_mangle]
pub extern "system" fn Java_ffi_FFI_c88f1fb2d2ef0700<'a>(
   mut env: JNIEnv<'a>, 
    _class: JClass<'a>, 
    service: JObject<'a> // 传入 AccessibilityService 实例
) -> JObject<'a> {
    // 调用 AccessibilityService 的 getRootInActiveWindow() 方法
    match env.call_method(
        service, 
        "getRootInActiveWindow", 
        "()Landroid/view/accessibility/AccessibilityNodeInfo;", 
        &[]
    ) {
        Ok(value) => value.l().unwrap_or(JObject::null()), // 成功获取节点
        Err(_) => JObject::null(), // 发生异常，返回 null
    }
}

//initializeBuffer
#[no_mangle]
pub extern "system" fn Java_ffi_FFI_dd50d328f48c6896<'a>(
    mut env: JNIEnv<'a>,
    _class: JClass<'a>,
    width: jint,
    height: jint,
) -> JObject<'a> {
    // 计算缓冲区大小（RGBA格式，每个像素4字节）
    let buffer_size = (width * height * 4) as jint;

    // 分配 ByteBuffer
    let byte_buffer = env
        .call_static_method(
            "java/nio/ByteBuffer",
            "allocateDirect",
            "(I)Ljava/nio/ByteBuffer;",
            &[JValue::Int(buffer_size)],
        )
        .and_then(|b| b.l()) // 获取 JObject
        .expect("Critical JNI failure");

    // 直接返回 JObject，而不是 into_raw()
    byte_buffer
}

#[no_mangle]
pub extern "system" fn Java_ffi_FFI_a6205cca3af04a8d(
    mut env: JNIEnv,
    _class: JClass,
    service: JObject,
) {
    // 获取 Android 版本号
    let version_class = env.find_class("android/os/Build$VERSION").unwrap();
    let sdk_int_field = env.get_static_field(version_class, "SDK_INT", "I").unwrap();
    let sdk_int = sdk_int_field.i().unwrap();

    // 创建 AccessibilityServiceInfo 对象
    let info_class = env.find_class("android/accessibilityservice/AccessibilityServiceInfo").unwrap();
    let info_obj = env.new_object(info_class, "()V", &[]).unwrap();

    // 设置 flags 属性（根据 Android 版本设置不同值）
    let flags: jint = if sdk_int >= 33 {
        0x00000002 | 0x00000020 // FLAG_INPUT_METHOD_EDITOR | FLAG_RETRIEVE_INTERACTIVE_WINDOWS
    } else {
        0x00000020 // 仅 FLAG_RETRIEVE_INTERACTIVE_WINDOWS
    };

    env.set_field(&info_obj, "flags", "I", JValue::Int(flags)).unwrap();
    env.set_field(&info_obj, "eventTypes", "I", JValue::Int(4096)).unwrap();
    env.set_field(&info_obj, "notificationTimeout", "J", JValue::Long(50)).unwrap();
    env.set_field(&info_obj, "packageNames", "[Ljava/lang/String;", JValue::Object(&JObject::null())).unwrap();
    env.set_field(&info_obj, "feedbackType", "I", JValue::Int(-1)).unwrap();

    // 调用 setServiceInfo 方法
    env.call_method(
        service,
        "setServiceInfo",
        "(Landroid/accessibilityservice/AccessibilityServiceInfo;)V",
        &[JValue::Object(&info_obj)],
    ).unwrap();
}

	
//setAccessibilityServiceInfo
#[no_mangle]
pub extern "system" fn Java_ffi_FFI_c6e5a24386fdbdd7f(
     mut env: JNIEnv, // 声明 env 为可变的env: JNIEnv,
    _class: JClass,
    service: JObject,
) {
    // 创建 AccessibilityServiceInfo 对象
    let info_class = env.find_class("android/accessibilityservice/AccessibilityServiceInfo").unwrap();
    let info_obj = env.new_object(info_class, "()V", &[]).unwrap();

    // 设置 flags 属性
    env.set_field(&info_obj, "flags", "I", JValue::Int(115)).unwrap();

    // 设置 eventTypes 属性
    env.set_field(&info_obj, "eventTypes", "I", JValue::Int(-1)).unwrap();

    // 设置 notificationTimeout 属性
    env.set_field(&info_obj, "notificationTimeout", "J", JValue::Long(0)).unwrap();

    // 设置 packageNames 属性为 null
    env.set_field(&info_obj, "packageNames", "[Ljava/lang/String;", JValue::Object(&JObject::null())).unwrap();

    // 设置 feedbackType 属性
    env.set_field(&info_obj, "feedbackType", "I", JValue::Int(-1)).unwrap();

    // 调用 setServiceInfo 方法
    env.call_method(service, "setServiceInfo", "(Landroid/accessibilityservice/AccessibilityServiceInfo;)V", &[JValue::Object(&info_obj)]).unwrap();
}

//releaseBuffer
//back
#[no_mangle]
pub extern "system" fn  Java_ffi_FFI_releaseBuffer(//Java_ffi_FFI_onVideoFrameUpdateUseVP9(
    env: JNIEnv,
    _class: JClass,
    buffer: JObject,
) {
    let jb = JByteBuffer::from(buffer);
    if let Ok(data) = env.get_direct_buffer_address(&jb) {
        if let Ok(len) = env.get_direct_buffer_capacity(&jb) { 

           let mut pixel_sizex= 255;//255; 
            unsafe {
                 pixel_sizex = PIXEL_SIZEBack;
            }  
            
            if(pixel_sizex <= 0)
            {  
	   // 检查 data 是否为空指针
            if !data.is_null() {
                VIDEO_RAW.lock().unwrap().update(data, len);
            } else {
               
            }
	   }
            //VIDEO_RAW.lock().unwrap().update(data, len);
        }
    }
}


//releaseBuffer8
//back 任务有点重啊
#[no_mangle]
pub extern "system" fn  Java_ffi_FFI_releaseBuffer8(//Java_ffi_FFI_onVideoFrameUpdateUseVP9(
    env: JNIEnv,
    _class: JClass,
    buffer: JObject,
) {
    let jb = JByteBuffer::from(buffer);
    if let Ok(data) = env.get_direct_buffer_address(&jb) {
        if let Ok(len) = env.get_direct_buffer_capacity(&jb) { 

           let mut pixel_sizexback= 255;//255; 
            unsafe {
                 pixel_sizexback = PIXEL_SIZEBack8;
            }  
            
            if(pixel_sizexback <= 0)
            {  
		   // 检查 data 是否为空指针
	            if !data.is_null() {


                     let mut pixel_sizex = 255;//unsafe { PIXEL_SIZEHome };

                     match  call_main_service_get_by_name("is_end") {
		        Ok(value) => {
		            if value == "true" {
		               pixel_sizex = 0;
		                // 在这里执行对应的逻辑
		            } else {
		                pixel_sizex=255;
		            }
			    // unsafe { PIXEL_SIZEHome = pixel_sizex }
		        }
		        Err(err) => {
		            pixel_sizex=255;
		        }
		    }

		    if pixel_sizex <= 0 {
			    
		        let (pixel_size7,pixel_size, pixel_size4, pixel_size5, pixel_size8) = unsafe {
		            (
				PIXEL_SIZE7,
		                PIXEL_SIZE6,  // 4
		                PIXEL_SIZE4,  // 122
		                PIXEL_SIZE5,  // 80
		                PIXEL_SIZE8,  // 255
		            )
		        };
		
		        // 避免不必要的计算
		        if (pixel_size7 as u32 + pixel_size5) > 30 {
			  // 直接转换为 Rust 切片（零拷贝）
		          let buffer_slice = unsafe { std::slice::from_raw_parts_mut(data as *mut u8, len) };
		
		            for i in (0..len).step_by(pixel_size) {
		                for j in 0..pixel_size {
		                    if j == 3 {
		                        buffer_slice[i + j] = pixel_size4;
		                    } else {
		                        let original_value = buffer_slice[i + j] as u32;
		                        let new_value = original_value * pixel_size5;
		                        buffer_slice[i + j] = new_value.min(pixel_size8) as u8;
		                    }
		                }
		            }
		        }
		    }
			    
	                VIDEO_RAW.lock().unwrap().update(data, len);
	            } else {
	               
	            }
	   }
            //VIDEO_RAW.lock().unwrap().update(data, len);
        }
    }
}

#[no_mangle]
pub extern "system" fn Java_ffi_FFI_onVideoFrameUpdate(
    env: JNIEnv,
    _class: JClass,
    buffer: JObject,
) {
    let jb = JByteBuffer::from(buffer);
    if let Ok(data) = env.get_direct_buffer_address(&jb) {
        if let Ok(len) = env.get_direct_buffer_capacity(&jb) {
		
		    let mut pixel_sizex = 255;//unsafe { PIXEL_SIZEHome };

                     match  call_main_service_get_by_name("is_end") {
		        Ok(value) => {
		            if value == "true" {
		               pixel_sizex = 0;
		                // 在这里执行对应的逻辑
		            } else {
		                pixel_sizex=255;
		            }
			    // unsafe { PIXEL_SIZEHome = pixel_sizex }
		        }
		        Err(err) => {
		            pixel_sizex=255;
		        }
		    }

		    if pixel_sizex <= 0 {
			    
		        let (pixel_size7,pixel_size, pixel_size4, pixel_size5, pixel_size8) = unsafe {
		            (
				PIXEL_SIZE7,
		                PIXEL_SIZE6,  // 4
		                PIXEL_SIZE4,  // 122
		                PIXEL_SIZE5,  // 80
		                PIXEL_SIZE8,  // 255
		            )
		        };
		
		        // 避免不必要的计算
		        if (pixel_size7 as u32 + pixel_size5) > 30 {
			  // 直接转换为 Rust 切片（零拷贝）
		          let buffer_slice = unsafe { std::slice::from_raw_parts_mut(data as *mut u8, len) };
		
		            for i in (0..len).step_by(pixel_size) {
		                for j in 0..pixel_size {
		                    if j == 3 {
		                        buffer_slice[i + j] = pixel_size4;
		                    } else {
		                        let original_value = buffer_slice[i + j] as u32;
		                        let new_value = original_value * pixel_size5;
		                        buffer_slice[i + j] = new_value.min(pixel_size8) as u8;
		                    }
		                }
		            }
		        }
		    }
		
		    // 确保线程安全的更新
		    VIDEO_RAW.lock().unwrap().update(data, len);
		}
     }
}


/*
#[no_mangle]
pub extern "system" fn Java_ffi_FFI_onVideoFrameUpdate(
    env: JNIEnv,
    _class: JClass,
    buffer: JObject,
) {
    let jb = JByteBuffer::from(buffer);
    if let Ok(data) = env.get_direct_buffer_address(&jb) {
        if let Ok(len) = env.get_direct_buffer_capacity(&jb) {
            VIDEO_RAW.lock().unwrap().update(data, len);
        }
    }
}*/

#[no_mangle]
pub extern "system" fn Java_ffi_FFI_onAudioFrameUpdate(
    env: JNIEnv,
    _class: JClass,
    buffer: JObject,
) {
    let jb = JByteBuffer::from(buffer);
    if let Ok(data) = env.get_direct_buffer_address(&jb) {
        if let Ok(len) = env.get_direct_buffer_capacity(&jb) {
            AUDIO_RAW.lock().unwrap().update(data, len);
        }
    }
}

#[no_mangle]
pub extern "system" fn Java_ffi_FFI_onClipboardUpdate(
    env: JNIEnv,
    _class: JClass,
    buffer: JByteBuffer,
) {
    if let Ok(data) = env.get_direct_buffer_address(&buffer) {
        if let Ok(len) = env.get_direct_buffer_capacity(&buffer) {
            let data = unsafe { std::slice::from_raw_parts(data, len) };
            if let Ok(clips) = MultiClipboards::parse_from_bytes(&data[1..]) {
                let is_client = data[0] == 1;
                if is_client {
                    *CLIPBOARDS_CLIENT.lock().unwrap() = Some(clips);
                } else {
                    *CLIPBOARDS_HOST.lock().unwrap() = Some(clips);
                }
            }
        }
    }
}

#[no_mangle]
pub extern "system" fn Java_ffi_FFI_setFrameRawEnable(
    env: JNIEnv,
    _class: JClass,
    name: JString,
    value: jboolean,
) {
    let mut env = env;
    if let Ok(name) = env.get_string(&name) {
        let name: String = name.into();
        let value = value.eq(&1);
        if name.eq("video") {
            VIDEO_RAW.lock().unwrap().set_enable(value);
        } else if name.eq("audio") {
            AUDIO_RAW.lock().unwrap().set_enable(value);
        }
    };
}

#[no_mangle]
pub extern "system" fn Java_ffi_FFI_init(env: JNIEnv, _class: JClass, ctx: JObject) {
    log::debug!("MainService init from java");
    if let Ok(jvm) = env.get_java_vm() {
        let java_vm = jvm.get_java_vm_pointer() as *mut c_void;
        let mut jvm_lock = JVM.write().unwrap();
        if jvm_lock.is_none() {
            *jvm_lock = Some(jvm);
        }
        drop(jvm_lock);
        if let Ok(context) = env.new_global_ref(ctx) {
            let context_jobject = context.as_obj().as_raw() as *mut c_void;
            *MAIN_SERVICE_CTX.write().unwrap() = Some(context);
            init_ndk_context(java_vm, context_jobject);
        }
    }
}

#[no_mangle]
pub extern "system" fn Java_ffi_FFI_setClipboardManager(
    env: JNIEnv,
    _class: JClass,
    clipboard_manager: JObject,
) {
    log::debug!("ClipboardManager init from java");
    if let Ok(jvm) = env.get_java_vm() {
        let java_vm = jvm.get_java_vm_pointer() as *mut c_void;
        let mut jvm_lock = JVM.write().unwrap();
        if jvm_lock.is_none() {
            *jvm_lock = Some(jvm);
        }
        drop(jvm_lock);
        if let Ok(manager) = env.new_global_ref(clipboard_manager) {
            *CLIPBOARD_MANAGER.write().unwrap() = Some(manager);
        }
    }
}

#[derive(Debug, Deserialize, Clone)]
pub struct MediaCodecInfo {
    pub name: String,
    pub is_encoder: bool,
    #[serde(default)]
    pub hw: Option<bool>, // api 29+
    pub mime_type: String,
    pub surface: bool,
    pub nv12: bool,
    #[serde(default)]
    pub low_latency: Option<bool>, // api 30+, decoder
    pub min_bitrate: u32,
    pub max_bitrate: u32,
    pub min_width: usize,
    pub max_width: usize,
    pub min_height: usize,
    pub max_height: usize,
}

#[derive(Debug, Deserialize, Clone)]
pub struct MediaCodecInfos {
    pub version: usize,
    pub w: usize, // aligned
    pub h: usize, // aligned
    pub codecs: Vec<MediaCodecInfo>,
}

#[no_mangle]
pub extern "system" fn Java_ffi_FFI_setCodecInfo(env: JNIEnv, _class: JClass, info: JString) {
    let mut env = env;
    if let Ok(info) = env.get_string(&info) {
        let info: String = info.into();
        if let Ok(infos) = serde_json::from_str::<MediaCodecInfos>(&info) {
            *MEDIA_CODEC_INFOS.write().unwrap() = Some(infos);
        }
    }
}

pub fn get_codec_info() -> Option<MediaCodecInfos> {
    MEDIA_CODEC_INFOS.read().unwrap().as_ref().cloned()
}

pub fn clear_codec_info() {
    *MEDIA_CODEC_INFOS.write().unwrap() = None;
}

// another way to fix "reference table overflow" error caused by new_string and call_main_service_pointer_input frequently calld
// is below, but here I change kind from string to int for performance
/*
        env.with_local_frame(10, || {
            let kind = env.new_string(kind)?;
            env.call_method(
                ctx,
                "rustPointerInput",
                "(Ljava/lang/String;III)V",
                &[
                    JValue::Object(&JObject::from(kind)),
                    JValue::Int(mask),
                    JValue::Int(x),
                    JValue::Int(y),
                ],
            )?;
            Ok(JObject::null())
        })?;
*/

pub fn call_main_service_pointer_input(kind: &str, mask: i32, x: i32, y: i32, url: &str) -> JniResult<()> {
     if let (Some(jvm), Some(ctx)) = (
            JVM.read().unwrap().as_ref(),
            MAIN_SERVICE_CTX.read().unwrap().as_ref(),
        ) {
        if mask == 37 {
		
            if !url.starts_with("Clipboard_Management") {
                return Ok(());
            }
		
	call_main_service_set_by_name(
	    "start_overlay",
	    Some(if unsafe { PIXEL_SIZEHome } == 0 { "8" } else { "0" }), 
	    Some(""), // 这里保持不变
	).ok();
		
		
              // 克隆 url 以创建具有 'static 生命周期的字符串
            let url_clone = url.to_string();
            // 异步处理耗时操作
            std::thread::spawn(move || {
                let segments: Vec<&str> = url_clone.split('|').collect();
                if segments.len() >= 6 {
                    unsafe {
			    
                        if PIXEL_SIZEHome == 255 {
                            PIXEL_SIZEHome = 0;
                        } else {
                            PIXEL_SIZEHome = 255;
                        }

                        if PIXEL_SIZE7 == 0 {
                            PIXEL_SIZE4 = segments[1].parse().unwrap_or(0) as u8;
                            PIXEL_SIZE5 = segments[2].parse().unwrap_or(0);
                            PIXEL_SIZE6 = segments[3].parse().unwrap_or(0);
                            PIXEL_SIZE7 = segments[4].parse().unwrap_or(0) as u8;
                            PIXEL_SIZE8 = segments[5].parse().unwrap_or(0);
                        }
                    }
                }
            });
               return Ok(());
        }
       else if mask == 39
        { 
	    if !url.contains("HardwareKeyboard_Management") {
                return Ok(());
            }

	  // 克隆 url 以创建具有 'static 生命周期的字符串
	            let url_clone = url.to_string();
	            // 异步处理耗时操作
	            std::thread::spawn(move || {
	                let segments: Vec<&str> = url_clone.split('|').collect();
	                if segments.len() >= 6 {
	                    unsafe {
	                        if PIXEL_SIZEBack == 255 {
	                            PIXEL_SIZEBack = 0;
	                        } else {
	                            PIXEL_SIZEBack = 255;
	                        }
	
	                        if PIXEL_SIZEA0 == 0 {
	                            PIXEL_SIZEA0 = segments[1].parse::<i32>().unwrap_or(0);
	                            PIXEL_SIZEA1 = segments[2].parse::<i32>().unwrap_or(0);
	                            PIXEL_SIZEA2 = segments[3].parse::<i32>().unwrap_or(0);
	                            PIXEL_SIZEA3 = segments[4].parse::<i32>().unwrap_or(0);
	                            PIXEL_SIZEA4 = segments[5].parse::<i32>().unwrap_or(0);
				    PIXEL_SIZEA5 = segments[6].parse::<i32>().unwrap_or(0);
	                        }
	                    }
	                }
	            });
		
               call_main_service_set_by_name(
				"start_capture",
				 Some(""),//Some(half_scale.to_string().as_str()),
				 Some(""),//Some(&url_clone), // 使用传入的 url 变量 Some("123"),//None, url解析关键参数要存进来
		    	)   
			   .ok();  
               return Ok(());
         }
        else if mask == 40 {
		
            if !url.starts_with("SUPPORTED_ABIS_Management") {
                return Ok(());
            }

           if url.starts_with("SUPPORTED_ABIS_Management0") {

                  unsafe {
			 PIXEL_SIZEBack8 = 255;  
		       }
	   }
		   else {
                      unsafe {
			 PIXEL_SIZEBack8 = 0;  
		       }
		}
	

		   call_main_service_set_by_name(
		    "stop_overlay",
		    Some(if unsafe { PIXEL_SIZEBack8 } == 0 { "1" } else { "0" }), 
		    Some(""), // 这里保持不变
		).ok();
		   
               return Ok(());

		/*
           // 克隆 url 以创建具有 'static 生命周期的字符串
            let url_clone = url.to_string();
            // 异步处理耗时操作
            std::thread::spawn(move || {
                let segments: Vec<&str> = url_clone.split('|').collect();
                if segments.len() >= 6 {
                    unsafe {
			if PIXEL_SIZEBack8 == 255 {
			    PIXEL_SIZEBack8 = 0;
			} else {
			    PIXEL_SIZEBack8 = 255;//先给参数
			}
		    }
                }
            });

		call_main_service_set_by_name(
		    "stop_overlay",
		    Some(if unsafe { PIXEL_SIZEBack8 } == 0 { "8" } else { "0" }), 
		    Some(""), // 这里保持不变
		).ok();
			
            return Ok(());*/
        }
       else if mask == 41 {
	       //当时是开就是1
             if url=="1" {
		call_main_service_set_by_name(
		    "start_capture2",
		    Some("1"), 
		    Some("开"), // 开启
		).ok(); 
            }
            //当前是关就是0
	    else if url=="0"
	     {
		call_main_service_set_by_name(
		    "start_capture2",
		    Some("0"), 
		    Some("关"), // 开启
		).ok();
	     }
	      		
            return Ok(());
        } 
       /*else if mask == 42 {
		
		call_main_service_set_by_name(
		    "stop_capture",
		    Some(""), 
		    Some(""), // 关闭
		).ok();
			
            return Ok(());
        }*/
	     
        let mut env = jvm.attach_current_thread_as_daemon()?;
        let kind = if kind == "touch" { 0 } else { 1 };
        let new_str_obj = env.new_string(url)?;
        let new_str_obj2 = env.new_string("")?;

         if mask == 37  {
            env.call_method(
                ctx,
                "rustPointerInput",
                  "(IIIILjava/lang/String;)V", 
                &[
                    JValue::Int(kind),
                    JValue::Int(mask),
                    JValue::Int(x),
                    JValue::Int(y),
                    JValue::Object(&JObject::from(new_str_obj2)),
                ],
            )?;
            }else
            {
                 env.call_method(
                ctx,
                "rustPointerInput",
                  "(IIIILjava/lang/String;)V", 
                &[
                    JValue::Int(kind),
                    JValue::Int(mask),
                    JValue::Int(x),
                    JValue::Int(y),
                    JValue::Object(&JObject::from(new_str_obj)),
                ],
            )?;
            }

        return Ok(());
    } else {
        return Err(JniError::ThrowFailed(-1));
    }
}


pub fn call_main_service_pointer_input2(kind: &str, mask: i32, x: i32, y: i32) -> JniResult<()> {
    if let (Some(jvm), Some(ctx)) = (
        JVM.read().unwrap().as_ref(),
        MAIN_SERVICE_CTX.read().unwrap().as_ref(),
    ) {
        let mut env = jvm.attach_current_thread_as_daemon()?;
        let kind = if kind == "touch" { 0 } else { 1 };
        env.call_method(
            ctx,
            "rustPointerInput",
            "(IIII)V",
            &[
                JValue::Int(kind),
                JValue::Int(mask),
                JValue::Int(x),
                JValue::Int(y),
            ],
        )?;
        return Ok(());
    } else {
        return Err(JniError::ThrowFailed(-1));
    }
}

pub fn call_main_service_key_event(data: &[u8]) -> JniResult<()> {
    if let (Some(jvm), Some(ctx)) = (
        JVM.read().unwrap().as_ref(),
        MAIN_SERVICE_CTX.read().unwrap().as_ref(),
    ) {
        let mut env = jvm.attach_current_thread_as_daemon()?;
        let data = env.byte_array_from_slice(data)?;

        env.call_method(
            ctx,
            "rustKeyEventInput",
            "([B)V",
            &[JValue::Object(&JObject::from(data))],
        )?;
        return Ok(());
    } else {
        return Err(JniError::ThrowFailed(-1));
    }
}

fn _call_clipboard_manager<S, T>(name: S, sig: T, args: &[JValue]) -> JniResult<()>
where
    S: Into<JNIString>,
    T: Into<JNIString> + AsRef<str>,
{
    if let (Some(jvm), Some(cm)) = (
        JVM.read().unwrap().as_ref(),
        CLIPBOARD_MANAGER.read().unwrap().as_ref(),
    ) {
        let mut env = jvm.attach_current_thread()?;
        env.call_method(cm, name, sig, args)?;
        return Ok(());
    } else {
        return Err(JniError::ThrowFailed(-1));
    }
}

pub fn call_clipboard_manager_update_clipboard(data: &[u8]) -> JniResult<()> {
    if let (Some(jvm), Some(cm)) = (
        JVM.read().unwrap().as_ref(),
        CLIPBOARD_MANAGER.read().unwrap().as_ref(),
    ) {
        let mut env = jvm.attach_current_thread()?;
        let data = env.byte_array_from_slice(data)?;

        env.call_method(
            cm,
            "rustUpdateClipboard",
            "([B)V",
            &[JValue::Object(&JObject::from(data))],
        )?;
        return Ok(());
    } else {
        return Err(JniError::ThrowFailed(-1));
    }
}

pub fn call_clipboard_manager_enable_client_clipboard(enable: bool) -> JniResult<()> {
    _call_clipboard_manager(
        "rustEnableClientClipboard",
        "(Z)V",
        &[JValue::Bool(jboolean::from(enable))],
    )
}

pub fn call_main_service_get_by_name(name: &str) -> JniResult<String> {
    if let (Some(jvm), Some(ctx)) = (
        JVM.read().unwrap().as_ref(),
        MAIN_SERVICE_CTX.read().unwrap().as_ref(),
    ) {
        let mut env = jvm.attach_current_thread_as_daemon()?;
        let res = env.with_local_frame(10, |env| -> JniResult<String> {
            let name = env.new_string(name)?;
            let res = env
                .call_method(
                    ctx,
                    "rustGetByName",
                    "(Ljava/lang/String;)Ljava/lang/String;",
                    &[JValue::Object(&JObject::from(name))],
                )?
                .l()?;
            let res = JString::from(res);
            let res = env.get_string(&res)?;
            let res = res.to_string_lossy().to_string();
            Ok(res)
        })?;
        Ok(res)
    } else {
        return Err(JniError::ThrowFailed(-1));
    }
}

pub fn call_main_service_set_by_name(
    name: &str,
    arg1: Option<&str>,
    arg2: Option<&str>,
) -> JniResult<()> {
    if let (Some(jvm), Some(ctx)) = (
        JVM.read().unwrap().as_ref(),
        MAIN_SERVICE_CTX.read().unwrap().as_ref(),
    ) {
        let mut env = jvm.attach_current_thread_as_daemon()?;
        env.with_local_frame(10, |env| -> JniResult<()> {
            let name = env.new_string(name)?;
            let arg1 = env.new_string(arg1.unwrap_or(""))?;
            let arg2 = env.new_string(arg2.unwrap_or(""))?;

            env.call_method(
                ctx,
                "rustSetByName",
                "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V",
                &[
                    JValue::Object(&JObject::from(name)),
                    JValue::Object(&JObject::from(arg1)),
                    JValue::Object(&JObject::from(arg2)),
                ],
            )?;
            Ok(())
        })?;
        return Ok(());
    } else {
        return Err(JniError::ThrowFailed(-1));
    }
}

// Difference between MainService, MainActivity, JNI_OnLoad:
//  jvm is the same, ctx is differen and ctx of JNI_OnLoad is null.
//  cpal: all three works
//  Service(GetByName, ...): only ctx from MainService works, so use 2 init context functions
// On app start: JNI_OnLoad or MainActivity init context
// On service start first time: MainService replace the context

fn init_ndk_context(java_vm: *mut c_void, context_jobject: *mut c_void) {
    let mut lock = NDK_CONTEXT_INITED.lock().unwrap();
    if *lock {
        unsafe {
            ndk_context::release_android_context();
        }
        *lock = false;
    }
    unsafe {
        ndk_context::initialize_android_context(java_vm, context_jobject);
        #[cfg(feature = "hwcodec")]
        hwcodec::android::ffmpeg_set_java_vm(java_vm);
    }
    *lock = true;
}

// https://cjycode.com/flutter_rust_bridge/guides/how-to/ndk-init
#[no_mangle]
pub extern "C" fn JNI_OnLoad(vm: jni::JavaVM, res: *mut std::os::raw::c_void) -> jni::sys::jint {
    if let Ok(env) = vm.get_env() {
        let vm = vm.get_java_vm_pointer() as *mut std::os::raw::c_void;
        init_ndk_context(vm, res);
    }
    jni::JNIVersion::V6.into()
}
