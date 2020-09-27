  echo "[DEB] Debian release not skipped, creating .deb file"
  echo "[DEB] Cleaning up old build"
  rm -rf release/debian

  echo "[DEB] Querying current version number"
  version=`mvn -q -N org.codehaus.mojo:build-helper-maven-plugin:3.0.0:parse-version     org.codehaus.mojo:exec-maven-plugin:1.3.1:exec     -Dexec.executable='echo'     -Dexec.args='${parsedVersion.majorVersion}.${parsedVersion.minorVersion}'`
  buildnumber=`cat buildconfig/debian/buildnumber`
  new_buildnumber=$((buildnumber + 1))

  echo "$new_buildnumber" > buildconfig/debian/buildnumber

  echo "[DEB] Build number is $version.$buildnumber"

  echo "[DEB] Setup control file"
  mkdir -p release/debian/tactview/DEBIAN
  cp buildconfig/debian/control release/debian/tactview/DEBIAN/control

  size=`du -s release/linux64 | cut -f -1`

  sed -i "s/__VERSION__/$version/g" release/debian/tactview/DEBIAN/control
  sed -i "s/__SIZE__/$size/g" release/debian/tactview/DEBIAN/control

  echo "[DEB] Setup desktop entry"
  mkdir -p release/debian/tactview/usr/share/applications
  cp buildconfig/debian/tactview.desktop release/debian/tactview/usr/share/applications

  mkdir -p release/debian/tactview/usr/bin
  cp buildconfig/debian/tactview.sh release/debian/tactview/usr/bin/tactview

  echo "[DEB] Setup icons, this requires imagemagick..."
  mkdir -p release/debian/tactview/usr/share/icons/hicolor

  sizes="16x16 32x32 64x64 128x128 256x256"
  for i in $sizes; do
    echo "Scaling icon to $i"
    mkdir -p release/debian/tactview/usr/share/icons/hicolor/$i/apps/
    convert images/icons/icon_full.png -resize $i release/debian/tactview/usr/share/icons/hicolor/$i/apps/tactview.png
  done

  sed -i "s/__INSTALLLOCATION__/\/opt\/tactview/g" release/debian/tactview/usr/share/applications/tactview.desktop

  echo "[DEB] Copying executable"
  mkdir -p release/debian/tactview/opt/tactview/
  cp -r release/linux64/* release/debian/tactview/opt/tactview/

  echo "[DEB] Building, this could take a few minutes..."
  dpkg-deb --build release/debian/tactview

  outputFileName="release/tactview_${version}-${buildnumber}_amd64.deb"
  mv "release/debian/tactview.deb" $outputFileName
  echo "$outputFileName created"
