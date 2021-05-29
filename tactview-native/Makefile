.PHONY: clean
CXXFLAGS = 
CCFLAGS = 

COMMON_HEADERS = common.h logging.h

clean:
	mkdir -p ../tactview-core/src/main/resources/linux-x86-64
	rm -r ../tactview-core/src/main/resources/linux-x86-64/* || true
	rm *.so || true

ALL_TARGETS = libopencvsharpen.so libopencvlensdistort.so libgenericconvolutionmatrix.so libffmpegmediadecoder.so \
 libffmpegconinousimagequeryservice.so libavcodecaudiodecoder.so libopencvblur.so libopencvscale.so \
 libopencvdenoise.so libopencvthreshold.so libopencverodedilate.so libopencvedgedetect.so libopencvrotate.so \
 libffmpegmediaencoder.so libopencvgreenscreen.so libopencvhistogram.so libopencvcartoon.so libopencvpencil.so \
 libopencvvideostab.so libv4l2loopback.so libnativealphablend.so libnativememoryoperations.so

release: CXXFLAGS += -O2
release: $(ALL_TARGETS)

debug: CXXFLAGS += -DDEBUG_LOG -g -O1
debug: $(ALL_TARGETS)


libopencvsharpen.so : opencvsharpen.cpp $(COMMON_HEADERS)
	$(CXX) $(CXXFLAGS)  `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libopencvsharpen.so -o libopencvsharpen.so opencvsharpen.cpp `pkg-config --libs opencv4`
	cp libopencvsharpen.so ../tactview-core/src/main/resources/linux-x86-64/.


libopencvlensdistort.so : opencvlensdistort.cpp $(COMMON_HEADERS)
	$(CXX) $(CXXFLAGS)  `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libopencvlensdistort.so -o libopencvlensdistort.so opencvlensdistort.cpp `pkg-config --libs opencv4`
	cp libopencvlensdistort.so ../tactview-core/src/main/resources/linux-x86-64/.


libgenericconvolutionmatrix.so : genericconvolutionmatrix.cpp $(COMMON_HEADERS)
	$(CXX) $(CXXFLAGS)  `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libgenericconvolutionmatrix.so -o libgenericconvolutionmatrix.so genericconvolutionmatrix.cpp `pkg-config --libs opencv4`
	cp libgenericconvolutionmatrix.so ../tactview-core/src/main/resources/linux-x86-64/.


libffmpegmediadecoder.so : ffmpegmediadecoder.cpp $(COMMON_HEADERS)
	$(CXX) $(CXXFLAGS)  -w -shared -fPIC -Wl,-soname,libffmpegmediadecoder.so -o libffmpegmediadecoder.so ffmpegmediadecoder.cpp  `pkg-config --libs libavcodec libavformat libavutil libswscale`
	cp libffmpegmediadecoder.so ../tactview-core/src/main/resources/linux-x86-64/.


libffmpegconinousimagequeryservice.so : ffmpegconinousimagequeryservice.cpp $(COMMON_HEADERS)
	$(CXX) $(CXXFLAGS)  -w -shared -fPIC -Wl,-soname,libffmpegconinousimagequeryservice.so -o libffmpegconinousimagequeryservice.so ffmpegconinousimagequeryservice.cpp  `pkg-config --libs libavcodec libavformat libavutil libswscale`
	cp libffmpegconinousimagequeryservice.so ../tactview-core/src/main/resources/linux-x86-64/.


libavcodecaudiodecoder.so : avcodecaudiodecoder.cpp $(COMMON_HEADERS)
	$(CXX) $(CXXFLAGS)  -w -shared -fPIC -Wl,-soname,libavcodecaudiodecoder.so -o libavcodecaudiodecoder.so avcodecaudiodecoder.cpp  `pkg-config --libs libavcodec libavformat libavutil libswscale`
	cp libavcodecaudiodecoder.so ../tactview-core/src/main/resources/linux-x86-64/.


libopencvblur.so : opencvblur.cpp $(COMMON_HEADERS)
	$(CXX) $(CXXFLAGS)  `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libopencvblur.so -o libopencvblur.so opencvblur.cpp `pkg-config --libs opencv4` 
	cp libopencvblur.so ../tactview-core/src/main/resources/linux-x86-64/.


libopencvscale.so : opencvscale.cpp $(COMMON_HEADERS)
	$(CXX) $(CXXFLAGS)  `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libopencvscale.so -o libopencvscale.so opencvscale.cpp `pkg-config --libs opencv4` 
	cp libopencvscale.so ../tactview-core/src/main/resources/linux-x86-64/.


libopencvdenoise.so : opencvdenoise.cpp $(COMMON_HEADERS)
	$(CXX) $(CXXFLAGS)  `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libopencvdenoise.so -o libopencvdenoise.so opencvdenoise.cpp `pkg-config --libs opencv4` 
	cp libopencvdenoise.so ../tactview-core/src/main/resources/linux-x86-64/.


libopencvthreshold.so : opencvthreshold.cpp $(COMMON_HEADERS)
	$(CXX) $(CXXFLAGS)  `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libopencvthreshold.so -o libopencvthreshold.so opencvthreshold.cpp `pkg-config --libs opencv4` 
	cp libopencvthreshold.so ../tactview-core/src/main/resources/linux-x86-64/.


libopencverodedilate.so : opencverodedilate.cpp $(COMMON_HEADERS)
	$(CXX) $(CXXFLAGS)  `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libopencverodedilate.so -o libopencverodedilate.so opencverodedilate.cpp `pkg-config --libs opencv4`
	cp libopencverodedilate.so ../tactview-core/src/main/resources/linux-x86-64/.


libopencvedgedetect.so : opencvedgedetect.cpp $(COMMON_HEADERS)
	$(CXX) $(CXXFLAGS)  `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libopencvedgedetect.so -o libopencvedgedetect.so opencvedgedetect.cpp `pkg-config --libs opencv4`
	cp libopencvedgedetect.so ../tactview-core/src/main/resources/linux-x86-64/.


libopencvrotate.so : opencvrotate.cpp $(COMMON_HEADERS)
	$(CXX) $(CXXFLAGS)  `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libopencvrotate.so -o libopencvrotate.so opencvrotate.cpp `pkg-config --libs opencv4` 
	cp libopencvrotate.so ../tactview-core/src/main/resources/linux-x86-64/.


libffmpegmediaencoder.so : ffmpegmediaencoder.cpp $(COMMON_HEADERS)
	g++ -w -shared -fPIC -Wl,-soname,libffmpegmediaencoder.so -o libffmpegmediaencoder.so ffmpegmediaencoder.cpp `pkg-config --libs libavcodec libavformat libavutil libswscale`
	cp libffmpegmediaencoder.so ../tactview-core/src/main/resources/linux-x86-64/.


libopencvgreenscreen.so : opencvgreenscreen.cpp $(COMMON_HEADERS)
	$(CXX) $(CXXFLAGS)  `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libopencvgreenscreen.so -o libopencvgreenscreen.so opencvgreenscreen.cpp `pkg-config --libs opencv4`
	cp libopencvgreenscreen.so ../tactview-core/src/main/resources/linux-x86-64/.


libopencvhistogram.so : opencvhistogram.cpp $(COMMON_HEADERS)
	$(CXX) $(CXXFLAGS)  `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libopencvhistogram.so -o libopencvhistogram.so opencvhistogram.cpp `pkg-config --libs opencv4`
	cp libopencvhistogram.so ../tactview-core/src/main/resources/linux-x86-64/.


libopencvcartoon.so : opencvcartoon.cpp $(COMMON_HEADERS)
	$(CXX) $(CXXFLAGS)  `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libopencvcartoon.so -o libopencvcartoon.so opencvcartoon.cpp `pkg-config --libs opencv4`
	cp libopencvcartoon.so ../tactview-core/src/main/resources/linux-x86-64/.


libopencvpencil.so : opencvpencil.cpp $(COMMON_HEADERS)
	$(CXX) $(CXXFLAGS)  `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libopencvpencil.so -o libopencvpencil.so opencvpencil.cpp `pkg-config --libs opencv4`
	cp libopencvpencil.so ../tactview-core/src/main/resources/linux-x86-64/.

libnativealphablend.so : nativealphablend.cpp $(COMMON_HEADERS)
	$(CXX) $(CXXFLAGS)  -shared -fPIC -Wl,-soname,libnativealphablend.so -o libnativealphablend.so nativealphablend.cpp
	cp libnativealphablend.so ../tactview-core/src/main/resources/linux-x86-64/.

libnativememoryoperations.so : nativememoryoperations.cpp $(COMMON_HEADERS)
	$(CXX) $(CXXFLAGS)  -shared -fPIC -Wl,-soname,libnativememoryoperations.so -o libnativememoryoperations.so nativememoryoperations.cpp
	cp libnativememoryoperations.so ../tactview-core/src/main/resources/linux-x86-64/.

libopencvvideostab.so : linux/opencvvideostab.cpp $(COMMON_HEADERS)
	$(CXX) $(CXXFLAGS)  `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libopencvvideostab.so -o libopencvvideostab.so linux/opencvvideostab.cpp `pkg-config --libs opencv4` -lopencv_videostab
	cp libopencvvideostab.so ../tactview-core/src/main/resources/linux-x86-64/.

# Possibly an option needed whether realtime module enabled
libv4l2loopback.so : linux/v4l2loopback.cpp $(COMMON_HEADERS)
	$(CXX) $(CXXFLAGS)  `pkg-config --cflags opencv4` -shared -fPIC -Wl,-soname,libv4l2loopback.so -o libv4l2loopback.so linux/v4l2loopback.cpp `pkg-config --libs opencv4`
	cp libv4l2loopback.so ../tactview-core/src/main/resources/linux-x86-64/.
