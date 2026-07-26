#!/usr/bin/env -S bash

set -xueo pipefail

cargo build --release 
cp target/release/libnative_method_rust.so ../../../
