set -e

usage() {
        echo "./$(basename $0) [arguments]"
        echo "Arguments:"
        echo "   -r Release build, strip debug symbols, use optimization for libraries"
        echo "   -c Clean downloaded libraries after install"
}

if [ "$(id -u)" != "0" ]; then
   echo "This script must be run as root"
   exit 1
fi

RELEASE_BUILD=false
CLEAN_DOWNLOADS=false

while getopts "rc" arg; do
  case ${arg} in
    r)
      RELEASE_BUILD=true
      ;;
    c)
      CLEAN_DOWNLOADS=true
      ;;
    ?)
      echo "Invalid option: -${OPTARG}."
      usage
      exit 2
      ;;
  esac
done

echo "RELEASE_BUILD=$RELEASE_BUILD"
echo "CLEAN_DOWNLOADS=$CLEAN_DOWNLOADS"

echo "Installing dependencies using $(nproc) threads"

./dependency_fragments/install_apt_dependencies.sh

cd /tmp

echo "Installing libaom for AV1 decoding"
mkdir -p libaom && \
cd libaom

if [ ! -d aom ]; then
  git clone --depth 1 https://aomedia.googlesource.com/aom
else
  git pull
fi

cmake ./aom -DBUILD_SHARED_LIBS=1 && \
make -j$(nproc) && \
make install

cd /tmp

echo "Installing Nvidia headers"
if [ ! -d nv-codec-headers ]; then
  git clone --depth 1 https://git.videolan.org/git/ffmpeg/nv-codec-headers.git
  cd nv-codec-headers
else
  cd nv-codec-headers
  git pull
  # make clean
fi
make
make install

cd ..

echo "Downloading FFmpeg"

FFMPEG_VERSION=5.0.1

if [ ! -e ffmpeg-$FFMPEG_VERSION.tar.gz ]; then
  wget "http://ffmpeg.org/releases/ffmpeg-$FFMPEG_VERSION.tar.gz"
fi
tar -xvf "ffmpeg-$FFMPEG_VERSION.tar.gz"

cd "ffmpeg-$FFMPEG_VERSION"

# 4.3 onward has an issue with SSSE3, therefore cannot be used until that is fixed:
# https://trac.ffmpeg.org/ticket/8747 -> maybe this, but this was fixed
# fire.webm test fails with that and crashes when no scaling applied
# This line turns off this feature for yuv2rgb.c file
echo "Applying FFmpeg patch"
sed -i "s/if (EXTERNAL_SSSE3/if (0 \&\& EXTERNAL_SSSE3/g" libswscale/x86/yuv2rgb.c


echo "Configuring FFmpeg"

additional_ffmpeg_arguments=""

if [ "$RELEASE_BUILD" = false ]; then
  additional_ffmpeg_arguments="--enable-debug --disable-stripping"
fi

./configure  --prefix=/usr/local  --pkg-config-flags="--static"  --extra-libs="-lpthread -lm"    --enable-gpl   --enable-libass   --enable-libfreetype   --enable-libmp3lame   --enable-libopus   --enable-libvorbis   --enable-libvpx   --enable-libx264  --enable-vaapi --enable-libx265 --enable-shared --enable-pthreads --enable-version3 --enable-bzlib --enable-fontconfig --enable-iconv --enable-libbluray --enable-libopencore-amrnb --enable-libopencore-amrwb --enable-libshine --enable-libsnappy --enable-libtheora --enable-libtwolame --enable-libwebp --enable-libxml2 --enable-lzma --enable-zlib --enable-libvorbis --enable-libspeex --enable-libxvid --enable-cuvid --enable-nvenc --enable-libaom $additional_ffmpeg_arguments

# --enable-libmfx - Intel HW accelerated encoding/decoding, could be useful
# --enable-libopenjpeg - decode jpeg


echo "Building FFmpeg"

make -j$(nproc)

echo "Installing FFmpeg (Requires root)"

make install

cd /tmp

echo "Installing OpenCV"

echo "Cloning OpenCV"

OPENCV_VERSION=4.1.0
OPENCV_CONTRIB_VERSION=4.1.1

if [ ! -e opencv-$OPENCV_VERSION.tar.gz ]; then
  wget -O opencv-$OPENCV_VERSION.tar.gz https://github.com/opencv/opencv/archive/$OPENCV_VERSION.tar.gz
fi
tar -xvzf opencv-$OPENCV_VERSION.tar.gz
mv  opencv-$OPENCV_VERSION opencv

if [ ! -e opencv_contrib-$OPENCV_CONTRIB_VERSION.tar.gz ]; then
  wget -O opencv_contrib-$OPENCV_CONTRIB_VERSION.tar.gz https://github.com/opencv/opencv_contrib/archive/$OPENCV_CONTRIB_VERSION.tar.gz  
fi
tar -xvzf opencv_contrib-$OPENCV_CONTRIB_VERSION.tar.gz
mv  opencv_contrib-$OPENCV_CONTRIB_VERSION opencv_contrib

cd ./opencv
mkdir build
cd build
cmake  -D OPENCV_GENERATE_PKGCONFIG=YES -D OPENCV_EXTRA_MODULES_PATH=../../opencv_contrib/modules -D CMAKE_BUILD_TYPE=Release -D CMAKE_INSTALL_PREFIX=/usr/local ..

make -j$(nproc)

make install

cd /tmp
rm -r opencv
rm -r opencv_contrib
rm -r ffmpeg-$FFMPEG_VERSION

if [ "$CLEAN_DOWNLOADS" = true ]; then
  rm opencv-$OPENCV_VERSION.tar.gz
  rm opencv_contrib-$OPENCV_CONTRIB_VERSION.tar.gz
  rm ffmpeg-$FFMPEG_VERSION.tar.gz
  rm -r libaom
  rm -r nv-codec-headers
fi

ldconfig -v
