set -e

usage() {
        echo "./$(basename $0) [arguments]"
}

if [ "$(id -u)" != "0" ]; then
   echo "This script must be run as root"
   exit 1
fi

brew install wget \
  unzip \
  zip \
  imagemagick \
  autoconf \
  automake \
  cmake \
  libass \
  freetype2 \
  libvorbis \
  libxcb \
  texinfo \
  zlib \
  nasm \
  lame \
  opus \
  libvpx \
  x264 \
  x265 \
  libbluray \
  snappy \
  speex \
  theora \
  webp \
  xvid \
  bzip2 \
  lzip \
  jpeg \
  libpng \
  libtiff \
  wavpack \
  imagemagick


echo "Installing dependencies using $(nproc) threads"

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

cd /tmp

echo "Downloading FFmpeg"

# 4.3 onward has an issue with SSSE3, therefore cannot be used until that is fixed:
# https://trac.ffmpeg.org/ticket/8747
# fire.webm test fails with that and crashes when no scaling applied
FFMPEG_VERSION=5.0.1

if [ ! -e ffmpeg-$FFMPEG_VERSION.tar.gz ]; then
  wget "http://ffmpeg.org/releases/ffmpeg-$FFMPEG_VERSION.tar.gz"
fi
tar -xvf "ffmpeg-$FFMPEG_VERSION.tar.gz"

cd "ffmpeg-$FFMPEG_VERSION"

echo "Configuring FFmpeg"

./configure --pkg-config-flags="--static"  --extra-libs="-lpthread -lm"    --enable-gpl   --enable-libass   --enable-libfreetype   --enable-libmp3lame   --enable-libopus   --enable-libvorbis   --enable-libvpx   --enable-libx264  --enable-libx265 --enable-shared --enable-pthreads --enable-version3 --enable-bzlib --enable-fontconfig --enable-iconv --enable-libbluray --enable-libopencore-amrnb --enable-libopencore-amrwb --enable-libsnappy --enable-libtheora --enable-libwavpack --enable-libwebp --enable-libxml2 --enable-lzma --enable-zlib --enable-libvorbis --enable-libspeex --enable-libxvid --enable-libaom

echo "Building FFmpeg"

make -j$(nproc)

echo "Installing FFmpeg (Requires root)"

make install

cd /tmp

echo "Installing OpenCV"

echo "Cloning OpenCV"

brew install --build-from-source opencv


cd /tmp
rm -r ffmpeg-$FFMPEG_VERSION

rm ffmpeg-$FFMPEG_VERSION.tar.gz
rm -r libaom
rm -r nv-codec-headers