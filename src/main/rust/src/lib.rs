use jni::EnvUnowned;
use jni::objects::JClass;
use jni::sys::jlong;
use std::slice;
use std::str;

#[unsafe(no_mangle)]
pub extern "system" fn Java_sco3_Main_passStringRust(
    _env: EnvUnowned,
    _class: JClass,
    address: jlong,
    length: jlong,
) {
    println!("Rust");

    unsafe {
        let ptr = address as *const u8;
        let bytes = slice::from_raw_parts(ptr, length as usize);

        match str::from_utf8(bytes) {
            Ok(s) => println!("Rust got: {}", s),
            Err(_) => {
                let s = String::from_utf8_lossy(bytes);
                println!("Rust got lossy: {}", s);
            }
        }
    }
}
