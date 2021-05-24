mkdir -p ../tactview-core/src/main/resources/darwin
rm -r ../tactview-core/src/main/resources/darwin/*

g++ -std=c++11 -I/usr/local/Cellar/opencv/4.5.0_4/include/opencv4/ -shared -fPIC -Wl,-install_name,libopencvsharpen.dylib -o libopencvsharpen.dylib opencvsharpen.cpp -lopencv_core -lopencv_imgproc
cp libopencvsharpen.dylib ../tactview-core/src/main/resources/darwin/.

g++ -std=c++11 -I/usr/local/Cellar/opencv/4.5.0_4/include/opencv4/ -shared -fPIC -Wl,-install_name,libopencvlensdistort.dylib -o libopencvlensdistort.dylib opencvlensdistort.cpp -lopencv_core -lopencv_imgproc -lopencv_calib3d
cp libopencvlensdistort.dylib ../tactview-core/src/main/resources/darwin/.

g++ -std=c++11 -I/usr/local/Cellar/opencv/4.5.0_4/include/opencv4/ -shared -fPIC -Wl,-install_name,libgenericconvolutionmatrix.dylib -o libgenericconvolutionmatrix.dylib genericconvolutionmatrix.cpp -lopencv_core -lopencv_imgproc
cp libgenericconvolutionmatrix.dylib ../tactview-core/src/main/resources/darwin/.

g++ -std=c++11 -w -shared -fPIC -Wl,-install_name,libffmpegmediadecoder.dylib -o libffmpegmediadecoder.dylib ffmpegmediadecoder.cpp  -lavcodec -lavformat -lavutil -lswscale
cp libffmpegmediadecoder.dylib ../tactview-core/src/main/resources/darwin/.

g++ -std=c++11 -w -shared -fPIC -Wl,-install_name,libffmpegconinousimagequeryservice.dylib -o libffmpegconinousimagequeryservice.dylib ffmpegconinousimagequeryservice.cpp  -lavcodec -lavformat -lavutil -lswscale
cp libffmpegconinousimagequeryservice.dylib ../tactview-core/src/main/resources/darwin/.

g++ -std=c++11 -w -shared -fPIC -Wl,-install_name,libavcodecaudiodecoder.dylib -o libavcodecaudiodecoder.dylib avcodecaudiodecoder.cpp  -lavcodec -lavformat -lavutil -lswscale -lswresample
cp libavcodecaudiodecoder.dylib ../tactview-core/src/main/resources/darwin/.

g++ -std=c++11 -I/usr/local/Cellar/opencv/4.5.0_4/include/opencv4/ -shared -fPIC -Wl,-install_name,libopencvblur.dylib -o libopencvblur.dylib opencvblur.cpp -lopencv_core -lopencv_imgproc 
cp libopencvblur.dylib ../tactview-core/src/main/resources/darwin/.

g++ -std=c++11 -I/usr/local/Cellar/opencv/4.5.0_4/include/opencv4/ -shared -fPIC -Wl,-install_name,libopencvscale.dylib -o libopencvscale.dylib opencvscale.cpp -lopencv_core -lopencv_imgproc 
cp libopencvscale.dylib ../tactview-core/src/main/resources/darwin/.

g++ -std=c++11 -I/usr/local/Cellar/opencv/4.5.0_4/include/opencv4/ -shared -fPIC -Wl,-install_name,libopencvdenoise.dylib -o libopencvdenoise.dylib opencvdenoise.cpp -lopencv_core -lopencv_imgproc -lopencv_photo
cp libopencvdenoise.dylib ../tactview-core/src/main/resources/darwin/.

g++ -std=c++11 -I/usr/local/Cellar/opencv/4.5.0_4/include/opencv4/ -shared -fPIC -Wl,-install_name,libopencvthreshold.dylib -o libopencvthreshold.dylib opencvthreshold.cpp -lopencv_core -lopencv_imgproc 
cp libopencvthreshold.dylib ../tactview-core/src/main/resources/darwin/.

g++ -std=c++11 -I/usr/local/Cellar/opencv/4.5.0_4/include/opencv4/ -shared -fPIC -Wl,-install_name,libopencverodedilate.dylib -o libopencverodedilate.dylib opencverodedilate.cpp -lopencv_core -lopencv_imgproc
cp libopencverodedilate.dylib ../tactview-core/src/main/resources/darwin/.

g++ -std=c++11 -I/usr/local/Cellar/opencv/4.5.0_4/include/opencv4/ -shared -fPIC -Wl,-install_name,libopencvedgedetect.dylib -o libopencvedgedetect.dylib opencvedgedetect.cpp -lopencv_core -lopencv_imgproc
cp libopencvedgedetect.dylib ../tactview-core/src/main/resources/darwin/.

g++ -std=c++11 -I/usr/local/Cellar/opencv/4.5.0_4/include/opencv4/ -shared -fPIC -Wl,-install_name,libopencvrotate.dylib -o libopencvrotate.dylib opencvrotate.cpp -lopencv_core -lopencv_imgproc 
cp libopencvrotate.dylib ../tactview-core/src/main/resources/darwin/.

g++ -w -shared -fPIC -Wl,-install_name,libffmpegmediaencoder.dylib -o libffmpegmediaencoder.dylib ffmpegmediaencoder.cpp -lavcodec -lavformat -lavutil -lswscale -lswresample
cp libffmpegmediaencoder.dylib ../tactview-core/src/main/resources/darwin/.

g++ -std=c++11 -I/usr/local/Cellar/opencv/4.5.0_4/include/opencv4/ -shared -fPIC -Wl,-install_name,libopencvgreenscreen.dylib -o libopencvgreenscreen.dylib opencvgreenscreen.cpp -lopencv_core -lopencv_imgproc
cp libopencvgreenscreen.dylib ../tactview-core/src/main/resources/darwin/.

g++ -std=c++11 -I/usr/local/Cellar/opencv/4.5.0_4/include/opencv4/ -shared -fPIC -Wl,-install_name,libopencvhistogram.dylib -o libopencvhistogram.dylib opencvhistogram.cpp -lopencv_core -lopencv_imgproc
cp libopencvhistogram.dylib ../tactview-core/src/main/resources/darwin/.

g++ -std=c++11 -I/usr/local/Cellar/opencv/4.5.0_4/include/opencv4/ -shared -fPIC -Wl,-install_name,libopencvcartoon.dylib -o libopencvcartoon.dylib opencvcartoon.cpp -lopencv_core -lopencv_imgproc -lopencv_photo
cp libopencvcartoon.dylib ../tactview-core/src/main/resources/darwin/.

g++ -std=c++11 -I/usr/local/Cellar/opencv/4.5.0_4/include/opencv4/ -shared -fPIC -Wl,-install_name,libopencvpencil.dylib -o libopencvpencil.dylib opencvpencil.cpp -lopencv_core -lopencv_imgproc -lopencv_photo
cp libopencvpencil.dylib ../tactview-core/src/main/resources/darwin/.

g++ -std=c++11 -shared -fPIC -Wl,-install_name,libnativealphablend.dylib -o libnativealphablend.dylib nativealphablend.cpp
cp libnativealphablend.dylib ../tactview-core/src/main/resources/darwin/.

g++ -std=c++11  -shared -fPIC -Wl,-soname,libnativememoryoperations.so -o libnativememoryoperations.so nativememoryoperations.cpp
cp libnativememoryoperations.so ../tactview-core/src/main/resources/linux-x86-64/.