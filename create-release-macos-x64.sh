#!/bin/bash

set -e

rm -rf release/macos || true
mkdir -p release/macos
cd tactview-native
echo "Compiling native code..."
./buildmac.sh
cd ..
mvn clean install

cp tactview-ui/target/tactview-ui*.jar release/macos/tactview.jar

jlink --no-header-files --no-man-pages --compress=2 --strip-debug --add-modules java.base,java.xml,java.naming,java.desktop,jdk.unsupported,java.compiler,jdk.compiler,jdk.zipfs,java.sql,java.management --output release/macos/java-runtime

g++ tactview-native/startup.cpp -o release/macos/tactview

echo "Copying dynamic library dependencies"

mkdir release/macos/libs

BLACKLISTED_DEPENDENCIES="libSystem.B.dylib"

otool -L tactview-native/*.dylib | awk '{print $1}' | sed 's/@rpath/\/usr\/local\/lib/g' | grep  "^/" | sort -u | xargs -I '{}' cp -v '{}' release/macos/libs/.

cd release

builddate=`date '+%Y%m%d_%H%M%S'`
filename="tactview_macos_$builddate.zip"
zip -r $filename macos/