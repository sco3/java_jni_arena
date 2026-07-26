# Java JNI Arena

A demonstration project for passing data between Java and C via JNI, using Java 25's Foreign Function & Memory API (`java.lang.foreign`).

## Overview

This project shows how to allocate native memory from Java, populate it with data, and pass the raw pointer and length to a C native method — avoiding the traditional JNI `GetStringUTFChars` / `NewStringUTF` approach entirely.

**What happens at runtime:**

1. Java allocates a `MemorySegment` containing a UTF-8 string using `Arena.ofConfined()`
2. The native address and byte length are passed as `long` / `int` to a C function
3. The C function reads and prints the string directly from that pointer

## Project Structure

```
.
├── build.gradle                  # Gradle build (Java 25, auto-compiles native code)
├── build-native.sh               # Standalone native build script (optional)
├── include/                      # Local JNI headers (for manual builds)
│   ├── jni.h
│   └── linux/
│       ├── jni_md.h
│       └── jawt_md.h
└── src/main/
    ├── java/sco3/Main.java       # Java entry point
    └── c/native_method.c         # JNI native implementation
```

## Requirements

- **JDK 25** (or later) — required for `java.lang.foreign` stable API
- **GCC** — to compile the native shared library
- **Gradle** (via wrapper) — for building and running

## Build & Run

Using Gradle (recommended):

```bash
./gradlew run
```

This automatically:
1. Compiles `native_method.c` into `native-method.so` (via the `compileNative` task)
2. Runs `sco3.Main` with `--enable-native-access=ALL-UNNAMED`

## Manual Native Build

You can also compile the native library independently:

```bash
# Using the provided script (auto-detects GCC version)
bash build-native.sh

# Or manually
gcc -std=c99 -fPIC -shared \
  -I ./include -I ./include/linux \
  -o native-method.so \
  src/main/c/native_method.c
```

## Run

```bash
./gradlew run

Reusing configuration cache.

> Task :run
Double: 3.1415926 error code: -1
Double: 3.14 error code: 4
Got: 3.1415926\0

BUILD SUCCESSFUL in 732ms
3 actionable tasks: 3 executed
Configuration cache entry reused.


```

Tests are configured with JUnit 5. Tests tagged `integration` are excluded from the default run.

## Notes

- The `java.lang.foreign` API was finalized in JDK 22 (JEP 454). This project targets JDK 25 for the latest improvements.
- The project loads `native-method.so` from the current working directory at runtime. Make sure the `.so` file is in the working directory before launching.
- The `build-native.sh` script encodes the GCC version in the output filename (e.g., `native-method-gcc-14.2.0.so`) to allow testing across multiple compiler versions.
