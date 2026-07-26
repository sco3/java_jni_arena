#!/usr/bin/env -S bash

set -xueo pipefail

cd src/main/rust
cargo build --release 
cp target/release/libnative_method_rust.so ../../../
cd -
