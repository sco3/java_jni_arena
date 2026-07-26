
cd $(dirname $0)

pwd


gcc=${1:-gcc}


ver=$(gcc -dumpspecs | grep -A 1  *version: | grep -v version  )

out=native-method-$gcc-$ver.so


extra=$2

$gcc -s -g0 \
  -std=c99  \
  -I ./include -I ./include/linux \
  -fPIC  -shared \
  -o $out  $extra  \
  src/main/c/native_method.c

