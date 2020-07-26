set -e

if [ "$(id -u)" != "0" ]; then
   echo "This script must be run as root"
   exit 1
fi

apt-get update

echo "Installing AVcodec ..."

echo "Installing dependencies"

apt-get -y install \
  autoconf \
  automake \
  build-essential \
  cmake \
  git-core \
  libass-dev \
  libfreetype6-dev \
  libsdl2-dev \
  libtool \
  libva-dev \
  libvdpau-dev \
  libvorbis-dev \
  libxcb1-dev \
  libxcb-shm0-dev \
  libxcb-xfixes0-dev \
  texinfo \
  zlib1g-dev \
  nasm \
  libmp3lame-dev \
  libopus-dev \
  libvpx-dev \
  libx264-dev \
  libx265-dev \
  libnuma-dev \
  libbluray-dev \
  libmysofa-dev \
  libopencore-amrnb-dev \
  libopencore-amrwb-dev \
  libshine-dev \
  libsnappy-dev \
  libspeex-dev \
  libtheora-dev \
  libtwolame-dev \
  libwavpack-dev \
  libwebp-dev \
  libxvidcore-dev \
  libbz2-dev \
  libbz2-1.0 \
  liblzma-dev

cd /tmp

echo "Installing libaom for AV1 decoding"
mkdir -p libaom && \
  cd libaom && \
  git clone https://aomedia.googlesource.com/aom && \
  cmake ./aom -DBUILD_SHARED_LIBS=1 && \
  make && \
  sudo make install

cd ..

echo "Installing Nvidia headers"
git clone https://git.videolan.org/git/ffmpeg/nv-codec-headers.git
cd nv-codec-headers
make
sudo make install

cd ..

echo "Downloading FFmpeg"

# 4.3 onward has an issue with SSSE3, therefore cannot be used until that is fixed:
# https://trac.ffmpeg.org/ticket/8747
# fire.webm test fails with that and crashes when no scaling applied
FFMPEG_VERSION=4.2.2

wget http://ffmpeg.org/releases/ffmpeg-4.3.1.tar.gz
tar -xvf "ffmpeg-$FFMPEG_VERSION.tar.gz"

cd "ffmpeg-$FFMPEG_VERSION"

echo "Configuring FFmpeg"

./configure  --prefix=/usr/local  --pkg-config-flags="--static"  --extra-libs="-lpthread -lm"    --enable-gpl   --enable-libass   --enable-libfreetype   --enable-libmp3lame   --enable-libopus   --enable-libvorbis   --enable-libvpx   --enable-libx264  --enable-vaapi --enable-libx265 --enable-shared --enable-pthreads --enable-version3 --enable-bzlib --enable-fontconfig --enable-iconv --enable-libbluray --enable-libopencore-amrnb --enable-libopencore-amrwb --enable-libshine --enable-libsnappy --enable-libtheora --enable-libtwolame --enable-libwavpack --enable-libwebp --enable-libxml2 --enable-lzma --enable-zlib --enable-libvorbis --enable-libspeex --enable-libxvid --enable-cuvid --enable-nvenc --enable-libaom
# Enabled debugging: --enable-debug --disable-stripping
# --enable-libmfx - Intel HW accelerated encoding/decoding, could be useful
# --enable-libopenjpeg - decode jpeg


echo "Building FFmpeg"

make -j$(nproc)

echo "Installing FFmpeg (Requires root)"

make install


echo "Installing OpenCV"

echo "Installing dependencies"

apt-get install -y cmake git libgtk2.0-dev pkg-config python-dev python-numpy libtbb2 libtbb-dev libjpeg-dev libpng-dev libtiff-dev libdc1394-22-dev

cd /tmp

echo "Cloning OpenCV"

wget -O opencv.tar.gz https://github.com/opencv/opencv/archive/4.1.0.tar.gz
tar -xvzf opencv.tar.gz
mv  opencv-4.1.0 opencv

wget -O opencv_contrib.tar.gz https://github.com/opencv/opencv_contrib/archive/4.1.1.tar.gz
tar -xvzf opencv_contrib.tar.gz
mv  opencv_contrib-4.1.1 opencv_contrib

cd ./opencv
mkdir build
cd build
cmake  -D OPENCV_GENERATE_PKGCONFIG=YES -D OPENCV_EXTRA_MODULES_PATH=../../opencv_contrib/modules -D CMAKE_BUILD_TYPE=Release -D CMAKE_INSTALL_PREFIX=/usr/local ..

make -j$(nproc)

make install

cd ..
cd ..
rm opencv.tar.gz opencv_contrib.tar.gz
rm -r opencv
rm -r opencv_contrib

ldconfig -v
