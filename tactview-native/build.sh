gcc -w -shared -fPIC -Wl,-soname,libffmpegmediadecoder.so -o libffmpegmediadecoder.so ffmpegmediadecoder.c  -lavcodec -lavformat -lavutil -lswscale
cp libffmpegmediadecoder.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc `pkg-config --cflags opencv` -shared -fPIC -Wl,-soname,libopencvimagedecorder.so -o libopencvimagedecorder.so opencvimagedecorder.cpp `pkg-config --libs opencv` 
cp libopencvimagedecorder.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc `pkg-config --cflags opencv` -shared -fPIC -Wl,-soname,libopencvblur.so -o libopencvblur.so opencvblur.cpp `pkg-config --libs opencv` 
cp libopencvblur.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc `pkg-config --cflags opencv` -shared -fPIC -Wl,-soname,libopencvscale.so -o libopencvscale.so opencvscale.cpp `pkg-config --libs opencv` 
cp libopencvscale.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc `pkg-config --cflags opencv` -shared -fPIC -Wl,-soname,libopencvdenoise.so -o libopencvdenoise.so opencvdenoise.cpp `pkg-config --libs opencv` 
cp libopencvdenoise.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc `pkg-config --cflags opencv` -shared -fPIC -Wl,-soname,libopencvthreshold.so -o libopencvthreshold.so opencvthreshold.cpp `pkg-config --libs opencv` 
cp libopencvthreshold.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc `pkg-config --cflags opencv` -shared -fPIC -Wl,-soname,libopencverodedilate.so -o libopencverodedilate.so opencverodedilate.cpp `pkg-config --libs opencv`
cp libopencverodedilate.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc `pkg-config --cflags opencv` -shared -fPIC -Wl,-soname,libopencvedgedetect.so -o libopencvedgedetect.so opencvedgedetect.cpp `pkg-config --libs opencv`
cp libopencvedgedetect.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc `pkg-config --cflags opencv` -shared -fPIC -Wl,-soname,libopencvrotate.so -o libopencvrotate.so opencvrotate.cpp `pkg-config --libs opencv` 
cp libopencvrotate.so ../tactview-core/src/main/resources/linux-x86-64/.

g++ -w -shared -fPIC -Wl,-soname,libffmpegmediaencoder.so -o libffmpegmediaencoder.so ffmpegmediaencoder.cpp -lavcodec -lavformat -lavutil -lswscale
cp libffmpegmediaencoder.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc `pkg-config --cflags opencv` -shared -fPIC -Wl,-soname,libopencvgreenscreen.so -o libopencvgreenscreen.so opencvgreenscreen.cpp `pkg-config --libs opencv`
cp libopencvgreenscreen.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc `pkg-config --cflags opencv` -shared -fPIC -Wl,-soname,libopencvhistogram.so -o libopencvhistogram.so opencvhistogram.cpp `pkg-config --libs opencv`
cp libopencvhistogram.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc `pkg-config --cflags opencv` -shared -fPIC -Wl,-soname,libopencvcartoon.so -o libopencvcartoon.so opencvcartoon.cpp `pkg-config --libs opencv`
cp libopencvcartoon.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc `pkg-config --cflags opencv` -shared -fPIC -Wl,-soname,libopencvpencil.so -o libopencvpencil.so opencvpencil.cpp `pkg-config --libs opencv`
cp libopencvpencil.so ../tactview-core/src/main/resources/linux-x86-64/.
