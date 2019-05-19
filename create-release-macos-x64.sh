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

jlink --no-header-files --no-man-pages --compress=2 --strip-debug --add-modules java.base,java.xml,java.naming,java.desktop,jdk.unsupported,java.compiler,jdk.compiler,jdk.zipfs --output release/macos/java-runtime

g++ tactview-native/startup.cpp -o release/macos/tactview

echo "Copying dynamic library dependencies"

mkdir release/macos/libs

# We can omit LSB libraries: http://refspecs.linuxfoundation.org/LSB_5.0.0/LSB-Common/LSB-Common/requirements.html#RLIBRARIES
LSB_LIBRARIES="libcrypt.so.1\|libdl.so.2\|libgcc_s.so.1\|libncurses.so.5\|libncursesw.so.5\|libnspr4.so\|libnss3.so\|libpam.so.0\|libpthread.so.0\|librt.so.1\|libssl3.so\|libstdc++.so.6\|libutil.so.1\|libz.so.1\|libGL.so.1\|libGLU.so.1\|libICE.so.6\|libQtCore.so.4\|libQtGui.so.4\|libQtNetwork.so.4\|libQtOpenGL.so.4\|libQtSql.so.4\|libQtSvg.so.4\|libQtXml.so.4\|libSM.so.6\|libX11.so.6\|libXext.so.6\|libXft.so.2\|libXi.so.6\|libXrender.so.1\|libXt.so.6\|libXtst.so.6\|libasound.so.2\|libatk-1.0.so.0\|libcairo.so.2\|libcairo-gobject.so.2\|libcairo-script-interpreter.so.2\|libfontconfig.so.1\|libfreetype.so.6\|libgdk-x11-2.0.so.0\|libgdk_pixbuf-2.0.so.0\|libgdk_pixbuf_xlib-2.0.so.0\|libgio-2.0.so.0\|libglib-2.0.so.0\|libgmodule-2.0.so.0\|libgobject-2.0.so.0\|libgthread-2.0.so.0\|libgtk-x11-2.0.so.0\|libjpeg.so.62\|libpango-1.0.so.0\|libpangocairo-1.0.so.0\|libpangoft2-1.0.so.0\|libpangoxft-1.0.so.0\|libpng12.so.0\|libtiff.so.5\|libxcb.so.1\|libcups.so.2\|libcupsimage.so.2\|libsane.so.1\|libxml2.so.2\|libxslt.so.1\|libc.so\|libm.so"

ADDITIONAL_EXCLUDES="libmount.so\|libblkid.so\|libcom_err.so\|libuuid.so\|libselinux.so\|libresolv.so"

BLACKLISTED_DEPENDENCIES="$LSB_LIBRARIES\|$ADDITIONAL_EXCLUDES"

ldd tactview-native/*.so | grep "=> /" | grep -v $BLACKLISTED_DEPENDENCIES | awk '{print $3}' | sort -u | xargs -I '{}' cp -v '{}' release/macos/libs/.

cd release

builddate=`date '+%Y%M%d_%H%m%S'`
filename="tactview_macos_$builddate.zip"
zip -r $filename macos/
