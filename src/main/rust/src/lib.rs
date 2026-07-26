use jni::EnvUnowned;
use jni::objects::JClass;
use jni::sys::{jdouble, jlong};
use std::slice;
use std::str;

#[unsafe(no_mangle)]
pub extern "system" fn Java_sco3_Main_passStringRust(
    _env: EnvUnowned,
    _class: JClass,
    address: jlong,
    length: jlong,
) {
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

#[unsafe(no_mangle)]
pub extern "system" fn Java_sco3_Main_parseDoubleRust(
    _env: EnvUnowned,
    _class: JClass,
    address: jlong,
    length: jlong,
    error_address: jlong,
) -> jdouble {
    //println!("Rust");

    let d: f64;
    unsafe {
        let error_ptr = error_address as *mut jlong;

        let ptr = address as *const u8;
        let bytes = slice::from_raw_parts(ptr, length as usize);

        let s = String::from_utf8_lossy(bytes);
        //println!("Rust parse got: {}", s);
        match s.parse::<f64>() {
            Ok(v) => {
                d = v;
                *error_ptr = -1;
            }
            Err(_) => {
                //println!("Error during parsing: {:?}", s.as_bytes());
                *error_ptr = 0;
                d = 0.0;
            }
        };
    }
    d as jdouble
}
