EXTRA_ARGS=""

if [[ "$1" == "-d" ]]
then
  EXTRA_ARGS="$EXTRA_ARGS -g -O3"
  echo "Debug build"
fi

mkdir -p ../tactview-core/src/main/resources/linux-x86-64
rm -r ../tactview-core/src/main/resources/linux-x86-64/*

gcc `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libopencvsharpen.so -o libopencvsharpen.so opencvsharpen.cpp `pkg-config --libs opencv4`
cp libopencvsharpen.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libopencvlensdistort.so -o libopencvlensdistort.so opencvlensdistort.cpp `pkg-config --libs opencv4`
cp libopencvlensdistort.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libgenericconvolutionmatrix.so -o libgenericconvolutionmatrix.so genericconvolutionmatrix.cpp `pkg-config --libs opencv4`
cp libgenericconvolutionmatrix.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc $EXTRA_ARGS -w -shared -fPIC -Wl,-soname,libffmpegmediadecoder.so -o libffmpegmediadecoder.so ffmpegmediadecoder.cpp  `pkg-config --libs libavcodec libavformat libavutil libswscale`
cp libffmpegmediadecoder.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc -w -shared -fPIC -Wl,-soname,libffmpegconinousimagequeryservice.so -o libffmpegconinousimagequeryservice.so ffmpegconinousimagequeryservice.cpp  `pkg-config --libs libavcodec libavformat libavutil libswscale`
cp libffmpegconinousimagequeryservice.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc -w -shared -fPIC -Wl,-soname,libavcodecaudiodecoder.so -o libavcodecaudiodecoder.so avcodecaudiodecoder.cpp  `pkg-config --libs libavcodec libavformat libavutil libswscale`
cp libavcodecaudiodecoder.so ../tactview-core/src/main/resources/linux-x86-64/.

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

g++ -w -shared -fPIC -Wl,-soname,libffmpegmediaencoder.so -o libffmpegmediaencoder.so ffmpegmediaencoder.cpp `pkg-config --libs libavcodec libavformat libavutil libswscale`
cp libffmpegmediaencoder.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libopencvgreenscreen.so -o libopencvgreenscreen.so opencvgreenscreen.cpp `pkg-config --libs opencv4`
cp libopencvgreenscreen.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libopencvhistogram.so -o libopencvhistogram.so opencvhistogram.cpp `pkg-config --libs opencv4`
cp libopencvhistogram.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libopencvcartoon.so -o libopencvcartoon.so opencvcartoon.cpp `pkg-config --libs opencv4`
cp libopencvcartoon.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libopencvpencil.so -o libopencvpencil.so opencvpencil.cpp `pkg-config --libs opencv4`
cp libopencvpencil.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libopencvvideostab.so -o libopencvvideostab.so linux/opencvvideostab.cpp `pkg-config --libs opencv4` -lopencv_videostab
cp libopencvvideostab.so ../tactview-core/src/main/resources/linux-x86-64/.

# Possibly an option needed whether realtime module enabled
gcc `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libv4l2loopback.so -o libv4l2loopback.so linux/v4l2loopback.cpp `pkg-config --libs opencv4`
cp libv4l2loopback.so ../tactview-core/src/main/resources/linux-x86-64/.
