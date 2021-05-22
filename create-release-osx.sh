#!/bin/bash

set -e

rm -rf release/tactview.app || true

mkdir -p release/tactview.app/Contents/MacOS
mkdir -p release/tactview.app/Contents/Resources
mkdir -p release/tactview.app/Contents/tacview

cp -r buildconfig/osx/bundle_template release/tactview.app

cd tactview-native
echo "Compiling native code..."
./buildmac.sh
cd ..
mvn clean install # -Dmaven.test.skip

cp tactview-ui/target/tactview-ui*.jar release/tactview.app/Contents/MacOS/tactview.jar

echo "Creating JRE..."

jlink --no-header-files --no-man-pages --compress=2 --strip-debug --add-modules java.base,java.xml,java.naming,java.desktop,jdk.unsupported,java.compiler,jdk.compiler,jdk.zipfs,java.sql,java.management --output release/tactview.app/Contents/MacOS/java-runtime

g++ --std=c++17  tactview-native/osx/startup.cpp -o release/tactview.app/Contents/MacOS/tactview /usr/local/lib/libboost_filesystem.a

echo "Copying dynamic library dependencies"

mkdir release/tactview.app/Contents/MacOS/libs

# Libs presents an empty OSX HighSierra and Catalina
DEFAULT_LIBS="/usr/lib/libCRFSuite.dylib\|/usr/lib/libChineseTokenizer.dylib\|/usr/lib/libDiagnosticMessagesClient.dylib\|/usr/lib/libFosl_dynamic.dylib\|/usr/lib/libapple_nghttp2.dylib\|/usr/lib/libarchive.2.dylib\|/usr/lib/libauto.dylib\|/usr/lib/libboringssl.dylib\|/usr/lib/libbsm.0.dylib\|/usr/lib/libbz2.1.0.dylib\|/usr/lib/libc++.1.dylib\|/usr/lib/libc++abi.dylib\|/usr/lib/libcmph.dylib\|/usr/lib/libcompression.dylib\|/usr/lib/libcoretls.dylib\|/usr/lib/libcoretls_cfhelpers.dylib\|/usr/lib/libcups.2.dylib\|/usr/lib/libenergytrace.dylib\|/usr/lib/libexpat.1.dylib\|/usr/lib/libheimdal-asn1.dylib\|/usr/lib/libiconv.2.dylib\|/usr/lib/libicucore.A.dylib\|/usr/lib/liblangid.dylib\|/usr/lib/liblzma.5.dylib\|/usr/lib/libmarisa.dylib\|/usr/lib/libmecabra.dylib\|/usr/lib/libnetwork.dylib\|/usr/lib/libpam.2.dylib\|/usr/lib/libpcap.A.dylib\|/usr/lib/libresolv.9.dylib\|/usr/lib/libspindump.dylib\|/usr/lib/libsqlite3.dylib\|/usr/lib/libusrtcp.dylib\|/usr/lib/libutil.dylib\|/usr/lib/libxar.1.dylib\|/usr/lib/libxml2.2.dylib\|/usr/lib/libxslt.1.dylib\|/usr/lib/libz.1.dylib\|libOpenScriptingUtil.dylib\|libobjc.A.dylib"

ADDITIONAL_EXCLUDES="/System/Library/Frameworks\|/System/Library/PrivateFrameworks\|tactview-native\|libSystem.B.dylib\|/usr/lib/system"

EXCLUDES="$ADDITIONAL_EXCLUDES\|$DEFAULT_LIBS"

LIBS_LOCATION=buildconfig/osx/tmp/libs.txt

rm -r buildconfig/osx/tmp || true
mkdir -p buildconfig/osx/tmp

for i in `ls  tactview-native/*.dylib`; do echo " - Processing $i"; python buildconfig/osx/otoolrecursive.py $(pwd)/$i >> $LIBS_LOCATION 2>/dev/null; done

cat $LIBS_LOCATION | sort -u | grep "/" | grep -v "rpath" | grep ".dylib" | grep -v "$EXCLUDES" | xargs -I % sh -c "cp -v '%' release/tactview.app/Contents/MacOS/libs/.;chmod +w release/tactview.app/Contents/MacOS/libs/*"

echo "Moving static data"

# Do when GLSL fixed
#buildconfig/prepare_static_files.sh "$(pwd)/release/tactview.app/Contents/MacOS/tactview"

echo "Creating icons"

# Based on https://stackoverflow.com/a/39678276

mkdir -p buildconfig/osx/tmp/tactview.iconset

sizes="16x16 32x32 64x64 128x128 256x256 512x512"
for i in $sizes; do
  echo " - Scaling icon to $i"
  convert images/icons/icon_full.png -density 74 -units pixelsperinch -resize $i buildconfig/osx/tmp/tactview.iconset/icon_$i.png
  convert images/icons/icon_full.png -density 144 -units pixelsperinch -resize $i buildconfig/osx/tmp/tactview.iconset/icon_$i@2x.png
done

iconutil --convert icns buildconfig/osx/tmp/tactview.iconset
mv buildconfig/osx/tmp/tactview.icns release/tactview.app/Contents/Resources/.

echo "Compressing..."

cd release

builddate=`date '+%Y%m%d_%H%M%S'`
filename="tactview_osx_$builddate.dmg"

hdiutil create -volname tactview -srcfolder tactview.app -ov -format UDZO $filename


cd ..
