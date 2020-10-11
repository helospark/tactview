#!/bin/bash

export DEBIAN_FRONTEND=noninteractive

apt-get update

apt-get -y install \
  wget \
  unzip \
  zip \
  imagemagick \
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
  liblzma-dev \
  cmake \
  git \
  libgtk2.0-dev \
  pkg-config \
  python-dev \
  python-numpy \
  libtbb2 \
  libtbb-dev \
  libjpeg-dev \
  libpng-dev \
  libtiff-dev \
  libdc1394-22-dev
