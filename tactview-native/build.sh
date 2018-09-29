gcc -w -shared -fPIC -Wl,-soname,libffmpegmediadecoder.so -o libffmpegmediadecoder.so ffmpegmediadecoder.c  -lavcodec -lavformat -lavutil -lswscale
cp libffmpegmediadecoder.so ../tactview-core/src/main/resources/linux-x86-64/.

gcc `pkg-config --cflags opencv` -shared -fPIC -Wl,-soname,opencvimagedecorder.so -o opencvimagedecorder.so opencvimagedecorder.cpp `pkg-config --libs opencv` 
cp opencvimagedecorder.so ../tactview-core/src/main/resources/linux-x86-64/.
