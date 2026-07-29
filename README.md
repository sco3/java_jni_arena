# Java JNI Arena

A demonstration project for passing data between Java and native code (C and Rust) via JNI and Java 25's Foreign Function & Memory (FFM) API (`java.lang.foreign`).

## Overview

This project demonstrates two approaches for native interop using Java 25's Foreign Function & Memory API (`java.lang.foreign`):

1. **JNI with Raw Memory Pointers**: Allocating native memory in Java via `Arena` and passing raw pointer addresses (`long`/`jlong`) into traditional JNI native methods — avoiding `GetStringUTFChars` / `NewStringUTF` allocations.
2. **FFM Downcalls (`Linker.downcallHandle`)**: Invoking native C-ABI functions directly from Java using downcall method handles, eliminating JNI headers (`jni.h`), `JNIEnv` parameters, and JNI wrapper boilerplate entirely.

Native functions in this project are implemented in both **C** and **Rust**.

**What happens at runtime:**

1. Java allocates a `MemorySegment` containing a UTF-8 string or native buffer using `Arena.ofConfined()`
2. **For JNI calls**: The native address and byte length are passed as `long` / `int` (or `jlong`) to C or Rust functions
3. **For Foreign Function Downcalls**: Java looks up C-ABI symbols in the native library using `SymbolLookup` and executes them directly via a downcall `MethodHandle` created with `Linker.nativeLinker().downcallHandle(...)`
4. The native function reads and processes the data directly from that memory pointer

## Native Implementations

The native functions are implemented across two languages:

- **C** (`src/main/c/native_method.c` → `native-method.so`):
  - `passString` (JNI): Reads raw memory pointer and prints UTF-8 characters directly.
  - `parseDouble` (JNI): Parses double floating point numbers using `strtod` and reports parse error positions via pointer.
- **Rust** (`src/main/rust/src/lib.rs` → `libnative_method_rust.so`):
  - `passStringRust` (JNI): Constructs a Rust slice `&[u8]` from raw pointer address and length, validating UTF-8 and printing output.
  - `parseDoubleRust` (JNI): Constructs a Rust slice `&[u8]` from raw pointer and parses `f64`.
  - `parseFastFloatRust` (JNI): Parses `f64` from raw pointer using the `fast_float` crate.
  - `process_string` (FFM Downcall): Exported C-ABI function (`extern "C"`) called directly from Java via `Linker.downcallHandle` without JNI bindings.
  - `parse_fast_float_rust` (FFM Downcall): Exported native function called directly via FFM downcall `MethodHandle` passing `MemorySegment` pointers for input and error tracking.

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
    ├── java/sco3/Main.java       # Java entry point (JNI & FFM downcall invocations)
    └── rust/                     # Native Rust implementation (JNI & FFM downcall exports)
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

> Task :run
C got: 3.1415926\0
Rust got: 3.1415926
Rust downcall got: 3.1415926

Run 1000000 tests

C double: 3.1415926                                 154.52 ns
C double: n/a                                       156.82 ns
Java double: 3.1415926                               82.98 ns
Java double: n/a                                    712.33 ns
Rust double: 3.1415926                               92.78 ns
Rust double: n/a                                     96.75 ns
Rust fast float downcall: 3.1415926                  79.30 ns
Rust fast float downcall: n/a                        77.22 ns
Rust fast float: 3.1415926                           72.98 ns
Rust fast float: n/a                                 70.84 ns

BUILD SUCCESSFUL in 7s
```

This runs `sco3.Main` with `--enable-native-access=ALL-UNNAMED`, invoking C and Rust native implementations via both JNI and FFM downcalls alongside standard Java parsing.

## Foreign Function Call Downcalls

Java 25's FFM API (`java.lang.foreign`) provides `Linker` and `SymbolLookup` to create downcall method handles directly to C-ABI native functions:

1. **Symbol Lookup**:
   ```java
   SymbolLookup rustLib = SymbolLookup.libraryLookup(RUST_PATH, Arena.global());
   MemorySegment funcAddr = rustLib.find("parse_fast_float_rust").orElseThrow();
   ```

2. **Function Descriptor & Downcall Handle**:
   ```java
   FunctionDescriptor descriptor = FunctionDescriptor.of(
       ValueLayout.JAVA_DOUBLE, // Return type
       ValueLayout.ADDRESS,     // Native buffer address
       ValueLayout.JAVA_LONG,   // Byte length
       ValueLayout.ADDRESS      // Error pointer address
   );

   MethodHandle parseDoubleMH = Linker.nativeLinker().downcallHandle(
       funcAddr,
       descriptor,
       Linker.Option.critical(false)
   );
   ```

3. **Direct Invocation**:
   ```java
   double result = (double) parseDoubleMH.invoke(nativeData, len, errorSeg);
   ```

This approach allows calling native code directly from Java without declaring `native` methods, writing JNI header files (`jni.h`), or passing `JNIEnv` / `jclass` parameters.

## Notes

- The `java.lang.foreign` API is in use.
- The application loads `native-method.so` (C implementation) and `libnative_method_rust.so` (Rust implementation) from the current working directory at runtime.
- The `build-native.sh` script encodes the GCC version in the output filename (e.g., `native-method-gcc-16.1.1.so`) to allow testing across multiple compiler versions.

