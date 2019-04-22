#!/bin/bash

set -e

rm -r release/win64 || true
mkdir -p release/win64
cd tactview-native
cmd /c build.bat
cd ..
mvn clean install

cp tactview-ui/target/tactview-ui*.jar release/win64/tactview.jar

jlink --no-header-files --no-man-pages --compress=2 --strip-debug --add-modules java.base,java.xml,java.naming,java.desktop,jdk.unsupported --output release/win64/java-runtime

cp tactview-native/startup.exe release/win64/tactview.exe

echo "Copying dynamic library dependencies"

BLACKLISTED_DEPENDENCIES="System32\|windows"

ldd tactview-native/*.dll | grep "=> /" | grep -i -v $BLACKLISTED_DEPENDENCIES | awk '{print $3}' | sort -u | xargs -I '{}' cp -v '{}' release/win64.

cd release

builddate=`date '+%Y%M%d_%H%m%S'`
filename="tactview_win64_$builddate.zip"
zip -r $filename win64/
