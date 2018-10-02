gcc -w -shared -fPIC -Wl,-soname,libffmpegmediadecoder.so -o libffmpegmediadecoder.so ffmpegmediadecoder.c  -lavcodec -lavformat -lavutil -lswscale
cp libffmpegmediadecoder.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc `pkg-config --cflags opencv` -shared -fPIC -Wl,-soname,libopencvimagedecorder.so -o libopencvimagedecorder.so opencvimagedecorder.cpp `pkg-config --libs opencv` 
cp libopencvimagedecorder.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc `pkg-config --cflags opencv` -shared -fPIC -Wl,-soname,libopencvblur.so -o libopencvblur.so opencvblur.cpp `pkg-config --libs opencv` 
cp libopencvblur.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc `pkg-config --cflags opencv` -shared -fPIC -Wl,-soname,libopencvscale.so -o libopencvscale.so opencvscale.cpp `pkg-config --libs opencv` 
cp libopencvscale.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc `pkg-config --cflags opencv` -shared -fPIC -Wl,-soname,libopencvrotate.so -o libopencvrotate.so opencvrotate.cpp `pkg-config --libs opencv` 
cp libopencvrotate.so ../tactview-core/src/main/resources/linux-x86-64/.
