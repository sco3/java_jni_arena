# Java JNI Arena

A demonstration project for passing data between Java and native code (C and Rust) via JNI, using Java 25's Foreign Function & Memory API (`java.lang.foreign`).

## Overview

This project shows how to allocate native memory from Java, populate it with data, and pass the raw pointer and length to native functions — avoiding the traditional JNI `GetStringUTFChars` / `NewStringUTF` approach entirely.

Native functions in this project are implemented in both **C** and **Rust**.

**What happens at runtime:**

1. Java allocates a `MemorySegment` containing a UTF-8 string using `Arena.ofConfined()`
2. The native address and byte length are passed as `long` / `int` (or `jlong`) to C or Rust functions
3. The native function reads and processes the string directly from that memory pointer

## Native Implementations

The native functions are implemented across two languages:

- **C** (`src/main/c/native_method.c` → `native-method.so`):
  - `passString`: Reads raw memory pointer and prints UTF-8 characters directly.
  - `parseDouble`: Parses double floating point numbers using `strtod` and reports parse error positions via pointer.
- **Rust** (`src/main/rust/src/lib.rs` → `libnative_method_rust.so`):
  - `passStringRust`: Constructs a Rust slice `&[u8]` from raw pointer address and length, validating UTF-8 and printing output.
  - `parseDoubleRust`: Constructs a Rust slice `&[u8]` from raw pointer and parses `f64`.

## Project Structure

```
.
├── build.gradle                  # Gradle build (Java 25, auto-compiles C native code)
├── build-native.sh               # Standalone C native build script
├── build-native-rust.sh          # Standalone Rust native build script
├── include/                      # Local JNI headers (for manual builds)
│   ├── jni.h
│   └── linux/
│       ├── jni_md.h
│       └── jawt_md.h
└── src/main/
    ├── c/native_method.c         # JNI native C implementation
    ├── java/sco3/Main.java       # Java entry point (loads C & Rust native libs)
    └── rust/                     # JNI native Rust implementation
        ├── Cargo.toml
        └── src/lib.rs
```

## Requirements

- **JDK 25** (or later) — required for `java.lang.foreign` stable API
- **GCC** — to compile the C native library
- **Rust & Cargo** — to compile the Rust native library
- **Gradle** — for building and running

## Build & Run

### 1. Build Native Libraries

**C native library:** Automatically compiled by Gradle during `gradle run` / `./gradlew run`, or manually via:

```bash
bash build-native.sh
```

**Rust native library:** Compiled using Cargo via the provided script:

```bash
bash build-native-rust.sh
```

### 2. Run Application

```bash
gradle run
Reusing configuration cache.

> Task :run
C got: 3.1415926\0
Rust got: 3.1415926

C double: 3.1415926                       260.26 ns
C double: n/a                             264.66 ns
Java double: 3.1415926                    142.61 ns
Java double: n/a                         1049.53 ns
Rust double: 3.1415926                    199.30 ns
Rust double: n/a                          195.48 ns
Rust fast float: 3.1415926                175.22 ns
Rust fast float: n/a                      171.56 ns

BUILD SUCCESSFUL in 1s

```

This runs `sco3.Main` with `--enable-native-access=ALL-UNNAMED`, invoking both C and Rust native implementations alongside standard Java parsing.

## Notes

- The `java.lang.foreign` API is in use.
- The application loads `native-method.so` (C implementation) and `libnative_method_rust.so` (Rust implementation) from the current working directory at runtime.
- The `build-native.sh` script encodes the GCC version in the output filename (e.g., `native-method-gcc-16.1.1.so`) to allow testing across multiple compiler versions.
