use jni::EnvUnowned;
use jni::objects::JClass;
use jni::sys::{jdouble, jlong};
use std::slice;
use std::str;

#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "system" fn Java_sco3_Main_passStringRust(
    _env: EnvUnowned,
    _class: JClass,
    address: jlong,
    length: jlong,
) {
    let ptr = address as *const u8;
    let bytes = unsafe { slice::from_raw_parts(ptr, length as usize) };

    match str::from_utf8(bytes) {
        Ok(s) => println!("Rust got: {}", s),
        Err(_) => {
            let s = String::from_utf8_lossy(bytes);
            println!("Rust got lossy: {}", s);
        }
    }
}

#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "system" fn Java_sco3_Main_parseDoubleRust(
    _env: EnvUnowned,
    _class: JClass,
    address: jlong,
    length: jlong,
    error_address: jlong,
) -> jdouble {
    //println!("Rust");

    let d: f64;

    let error_ptr = error_address as *mut jlong;

    let ptr = address as *const u8;
    let bytes = unsafe { slice::from_raw_parts(ptr, length as usize) };

    let v = str::from_utf8(bytes) //
        .map_err(|_| ()) //
        .and_then(|s| s.trim().parse::<f64>().map_err(|_| ()));

    //println!("Rust parse got: {}", s);
    match v {
        Ok(v) => {
            unsafe { *error_ptr = -1 };
            d = v;
        }
        Err(_e) => {
            //println!("Error during parsing: {:?} {:?}", s.as_bytes(), _e);
            unsafe { *error_ptr = 0 };
            d = 0.0;
        }
    };

    d as jdouble
}

#[unsafe(no_mangle)]
#[allow(non_snake_case)]
pub extern "system" fn Java_sco3_Main_parseFastFloatRust(
    _env: EnvUnowned,
    _class: JClass,
    address: jlong,
    length: jlong,
    error_address: jlong,
) -> jdouble {
    //println!("Rust");

    let d: f64;

    let error_ptr = error_address as *mut jlong;

    let ptr = address as *const u8;
    let bytes = unsafe { slice::from_raw_parts(ptr, length as usize) };

    // let v = str::from_utf8(bytes) //
    //     .map_err(|_| ()) //
    //     .and_then(|s| s.trim().parse::<f64>().map_err(|_| ()));
    let v = fast_float::parse(bytes);

    //println!("Rust parse got: {}", s);
    match v {
        Ok(v) => {
            unsafe { *error_ptr = -1 };
            d = v;
        }
        Err(_e) => {
            //println!("Error during parsing: {:?} {:?}", s.as_bytes(), _e);
            unsafe { *error_ptr = 0 };
            d = 0.0;
        }
    };

    d as jdouble
}
