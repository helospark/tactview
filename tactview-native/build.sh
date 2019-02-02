mkdir -p ../tactview-core/src/main/resources/linux-x86-64
rm -r ../tactview-core/src/main/resources/linux-x86-64/*

gcc `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libopencvsharpen.so -o libopencvsharpen.so opencvsharpen.cpp `pkg-config --libs opencv4`
cp libopencvsharpen.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libopencvlensdistort.so -o libopencvlensdistort.so opencvlensdistort.cpp `pkg-config --libs opencv4`
cp libopencvlensdistort.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libgenericconvolutionmatrix.so -o libgenericconvolutionmatrix.so genericconvolutionmatrix.cpp `pkg-config --libs opencv4`
cp libgenericconvolutionmatrix.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc -w -shared -fPIC -Wl,-soname,libffmpegmediadecoder.so -o libffmpegmediadecoder.so ffmpegmediadecoder.cpp  -lavcodec -lavformat -lavutil -lswscale
cp libffmpegmediadecoder.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc -w -shared -fPIC -Wl,-soname,libavcodecaudiodecoder.so -o libavcodecaudiodecoder.so avcodecaudiodecoder.cpp  -lavcodec -lavformat -lavutil -lswscale
cp libavcodecaudiodecoder.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libopencvimagedecorder.so -o libopencvimagedecorder.so opencvimagedecorder.cpp `pkg-config --libs opencv4` 
cp libopencvimagedecorder.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libopencvblur.so -o libopencvblur.so opencvblur.cpp `pkg-config --libs opencv4` 
cp libopencvblur.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libopencvscale.so -o libopencvscale.so opencvscale.cpp `pkg-config --libs opencv4` 
cp libopencvscale.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libopencvdenoise.so -o libopencvdenoise.so opencvdenoise.cpp `pkg-config --libs opencv4` 
cp libopencvdenoise.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libopencvthreshold.so -o libopencvthreshold.so opencvthreshold.cpp `pkg-config --libs opencv4` 
cp libopencvthreshold.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libopencverodedilate.so -o libopencverodedilate.so opencverodedilate.cpp `pkg-config --libs opencv4`
cp libopencverodedilate.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libopencvedgedetect.so -o libopencvedgedetect.so opencvedgedetect.cpp `pkg-config --libs opencv4`
cp libopencvedgedetect.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libopencvrotate.so -o libopencvrotate.so opencvrotate.cpp `pkg-config --libs opencv4` 
cp libopencvrotate.so ../tactview-core/src/main/resources/linux-x86-64/.

g++ -w -shared -fPIC -Wl,-soname,libffmpegmediaencoder.so -o libffmpegmediaencoder.so ffmpegmediaencoder.cpp -lavcodec -lavformat -lavutil -lswscale
cp libffmpegmediaencoder.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libopencvgreenscreen.so -o libopencvgreenscreen.so opencvgreenscreen.cpp `pkg-config --libs opencv4`
cp libopencvgreenscreen.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libopencvhistogram.so -o libopencvhistogram.so opencvhistogram.cpp `pkg-config --libs opencv4`
cp libopencvhistogram.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libopencvcartoon.so -o libopencvcartoon.so opencvcartoon.cpp `pkg-config --libs opencv4`
cp libopencvcartoon.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libopencvpencil.so -o libopencvpencil.so opencvpencil.cpp `pkg-config --libs opencv4`
cp libopencvpencil.so ../tactview-core/src/main/resources/linux-x86-64/.

echo "Copying dynamic library dependencies"

# We can omit LSB libraries: http://refspecs.linuxfoundation.org/LSB_5.0.0/LSB-Common/LSB-Common/requirements.html#RLIBRARIES
LSB_LIBRARIES="libcrypt.so.1\|libdl.so.2\|libgcc_s.so.1\|libncurses.so.5\|libncursesw.so.5\|libnspr4.so\|libnss3.so\|libpam.so.0\|libpthread.so.0\|librt.so.1\|libssl3.so\|libstdc++.so.6\|libutil.so.1\|libz.so.1\|libGL.so.1\|libGLU.so.1\|libICE.so.6\|libQtCore.so.4\|libQtGui.so.4\|libQtNetwork.so.4\|libQtOpenGL.so.4\|libQtSql.so.4\|libQtSvg.so.4\|libQtXml.so.4\|libSM.so.6\|libX11.so.6\|libXext.so.6\|libXft.so.2\|libXi.so.6\|libXrender.so.1\|libXt.so.6\|libXtst.so.6\|libasound.so.2\|libatk-1.0.so.0\|libcairo.so.2\|libcairo-gobject.so.2\|libcairo-script-interpreter.so.2\|libfontconfig.so.1\|libfreetype.so.6\|libgdk-x11-2.0.so.0\|libgdk_pixbuf-2.0.so.0\|libgdk_pixbuf_xlib-2.0.so.0\|libgio-2.0.so.0\|libglib-2.0.so.0\|libgmodule-2.0.so.0\|libgobject-2.0.so.0\|libgthread-2.0.so.0\|libgtk-x11-2.0.so.0\|libjpeg.so.62\|libpango-1.0.so.0\|libpangocairo-1.0.so.0\|libpangoft2-1.0.so.0\|libpangoxft-1.0.so.0\|libpng12.so.0\|libtiff.so.5\|libxcb.so.1\|libcups.so.2\|libcupsimage.so.2\|libsane.so.1\|libxml2.so.2\|libxslt.so.1\|libc.so\|libm.so"

ADDITIONAL_EXCLUDES="libmount.so\|libblkid.so\|libcom_err.so\|libuuid.so\|libselinux.so\|libresolv.so"

BLACKLISTED_DEPENDENCIES="$LSB_LIBRARIES\|$ADDITIONAL_EXCLUDES"

ldd *.so | grep "=> /" | grep -v $BLACKLISTED_DEPENDENCIES | awk '{print $3}' | sort -u | xargs -I '{}' cp -v '{}' ../tactview-core/src/main/resources/linux-x86-64/.
