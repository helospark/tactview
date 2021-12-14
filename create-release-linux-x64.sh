#!/bin/bash

skipDebian=false
skipDocker=true

set -e

releaseFolderFileName=tactview_linux64
releaseFolder=release/$releaseFolderFileName

rm -rf $releaseFolder || true
mkdir -p $releaseFolder
cd tactview-native
echo "Compiling native code..."
./build.sh
cd ..
mvn clean install #-Dmaven.test.skip

cp tactview-ui/target/tactview-ui*.jar $releaseFolder/tactview.jar

jlink --no-header-files --no-man-pages --compress=2 --strip-debug --add-modules java.base,java.xml,java.naming,java.desktop,jdk.unsupported,java.compiler,jdk.compiler,jdk.zipfs,java.sql,java.management --output $releaseFolder/java-runtime

g++ --std=c++17 -no-pie tactview-native/linux/startup.cpp -o $releaseFolder/tactview -lstdc++fs -static-libstdc++

echo "Copying dynamic library dependencies"

mkdir $releaseFolder/libs

# We can omit LSB libraries: http://refspecs.linuxfoundation.org/LSB_5.0.0/LSB-Common/LSB-Common/requirements.html#RLIBRARIES
LSB_LIBRARIES="libcrypt.so.1\|libdl.so.2\|libgcc_s.so.1\|libncurses.so.5\|libncursesw.so.5\|libnspr4.so\|libnss3.so\|libpam.so.0\|libpthread.so.0\|librt.so.1\|libssl3.so\|libstdc++.so.6\|libutil.so.1\|libz.so.1\|libGL.so.1\|libGLU.so.1\|libICE.so.6\|libQtCore.so.4\|libQtGui.so.4\|libQtNetwork.so.4\|libQtOpenGL.so.4\|libQtSql.so.4\|libQtSvg.so.4\|libQtXml.so.4\|libSM.so.6\|libX11.so.6\|libXext.so.6\|libXft.so.2\|libXi.so.6\|libXrender.so.1\|libXt.so.6\|libXtst.so.6\|libasound.so.2\|libatk-1.0.so.0\|libcairo.so.2\|libcairo-gobject.so.2\|libcairo-script-interpreter.so.2\|libfontconfig.so.1\|libfreetype.so.6\|libgdk-x11-2.0.so.0\|libgdk_pixbuf-2.0.so.0\|libgdk_pixbuf_xlib-2.0.so.0\|libgio-2.0.so.0\|libglib-2.0.so.0\|libgmodule-2.0.so.0\|libgobject-2.0.so.0\|libgthread-2.0.so.0\|libgtk-x11-2.0.so.0\|libjpeg.so.62\|libpango-1.0.so.0\|libpangocairo-1.0.so.0\|libpangoft2-1.0.so.0\|libpangoxft-1.0.so.0\|libpng12.so.0\|libtiff.so.5\|libxcb.so.1\|libcups.so.2\|libcupsimage.so.2\|libsane.so.1\|libxml2.so.2\|libxslt.so.1\|libc.so\|libm.so"

ADDITIONAL_EXCLUDES="libmount.so\|libblkid.so\|libcom_err.so\|libuuid.so\|libselinux.so\|libresolv.so"

BLACKLISTED_DEPENDENCIES="$LSB_LIBRARIES\|$ADDITIONAL_EXCLUDES"

ldd tactview-native/*.so | grep "=> /" | grep -v $BLACKLISTED_DEPENDENCIES | awk '{print $3}' | sort -u | xargs -I '{}' cp -v '{}' $releaseFolder/libs/.


echo "Moving static data"

buildconfig/prepare_static_files.sh "$(pwd)/$releaseFolder"

echo "Compressing..."

cd release

builddate=`date '+%Y%m%d_%H%M%S'`
filename="tactview_linux64_$builddate.tar.xz"

# Use all threads for the compression
export XZ_DEFAULTS="-T 0"

tar -cJf $filename $releaseFolderFileName/

cd ..

if [ "$skipDebian" = false ]
then
  echo "[DEB] Debian release not skipped, creating .deb file"
  echo "[DEB] Cleaning up old build"
  rm -rf release/debian

  version=`mvn -q -N org.codehaus.mojo:build-helper-maven-plugin:3.0.0:parse-version     org.codehaus.mojo:exec-maven-plugin:1.3.1:exec     -Dexec.executable='echo'     -Dexec.args='${parsedVersion.majorVersion}.${parsedVersion.minorVersion}'`
  buildnumber=`cat buildconfig/debian/buildnumber`
  new_buildnumber=$((buildnumber + 1))
  
  fullVersion="$version-$buildnumber"

  echo "[DEB] Build number is $fullVersion"

  echo "$new_buildnumber" > buildconfig/debian/buildnumber

  echo "[DEB] Setup control file"
  mkdir -p release/debian/tactview/DEBIAN
  cp buildconfig/debian/control release/debian/tactview/DEBIAN/control

  size=`du -s $releaseFolder | cut -f -1`

  sed -i "s/__VERSION__/$fullVersion/g" release/debian/tactview/DEBIAN/control
  sed -i "s/__SIZE__/$size/g" release/debian/tactview/DEBIAN/control

  echo "[DEB] Setup desktop entry"
  mkdir -p release/debian/tactview/usr/share/applications
  cp buildconfig/debian/tactview.desktop release/debian/tactview/usr/share/applications


  mkdir -p release/debian/tactview/usr/bin
  cp buildconfig/debian/tactview.sh release/debian/tactview/usr/bin/tactview
  chmod +x release/debian/tactview/usr/bin/tactview


  mkdir -p release/debian/tactview/usr/share/mime/packages/
  cp buildconfig/debian/mime-tactview.xml release/debian/tactview/usr/share/mime/packages/tactview.xml


  echo "[DEB] Setup icons, this requires imagemagick..."
  mkdir -p release/debian/tactview/usr/share/icons/hicolor

  sizes="16x16 32x32 64x64 128x128 256x256"
  for i in $sizes; do
    echo "Scaling icon to $i"
    mkdir -p release/debian/tactview/usr/share/icons/hicolor/$i/apps/
    convert images/icons/icon_full.png -resize $i release/debian/tactview/usr/share/icons/hicolor/$i/apps/tactview.png

    # Icon for the save file
    mkdir -p release/debian/tactview/usr/share/icons/hicolor/$i/mimetypes/
    cp release/debian/tactview/usr/share/icons/hicolor/$i/apps/tactview.png release/debian/tactview/usr/share/icons/hicolor/$i/mimetypes/application-x-tactviewsavefile.png
  done

  sed -i "s/__INSTALLLOCATION__/\/opt\/tactview/g" release/debian/tactview/usr/share/applications/tactview.desktop

  echo "[DEB] Copying executable"
  mkdir -p release/debian/tactview/opt/tactview/
  cp -r $releaseFolder/* release/debian/tactview/opt/tactview/

  echo "[DEB] Building, this could take a few minutes..."
  dpkg-deb --build release/debian/tactview

  outputFileName="release/tactview_${fullVersion}_amd64.deb"
  mv "release/debian/tactview.deb" "$outputFileName"
  echo "$outputFileName created"

fi


if [ "$skipDocker" = false ]
then
  rm release/Dockerfile || true
  cp buildconfig/linux_dockerfile release/Dockerfile
  sudo docker build -t tactview_0.0.1 release/.
fi
