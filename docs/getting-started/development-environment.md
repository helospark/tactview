# Setup dependencies and development environment

Setting up the development environment requires
 1. installing dependencies which are native libraries mainly: FFmpeg, OpenCV
 2. Installing Java tooling

## Native dependencies

### Linux 64 development environment

You need to setup environment first, run script in tactview_native/ folder:

    sudo ./build_dependencies.sh

**WARN: this will override FFmpeg and OpenCV in your machine and install lots of other native libraries**

This should install all dependencies (however this script runs rarely it may happen that some dependency conflicts are created). You only need to run this once or when native library changes.


### OSX development environment

You need homebrew and build tools (git, compiler)

You need to setup environment first, run script in tactview_native/ folder:

    sudo ./build_dependencies_mac.sh

**WARN: this will override FFmpeg and OpenCV in your machine and install lots of other native libraries**

This should install all dependencies (however this script runs rarely it may happen that some dependency conflicts are created). You only need to run this once or when native library changes.

### Windows development environment

You have to install FFmpeg, OpenCV, Cygwin, Visual Studio.

_TBD on details of installing_


### Docker Linux64 release environment

**This is only required if you want to build release with Docker on Linux 64.**

You only need Docker on your machine.

Then you can build a Docker image that will build the release of the Tactview. You only need to do this once & when native libraries are updated.

    docker build -t tactview_build:latest .

Once done you can build a release build using:

    ./docker-create-release-linux-x64.sh

Then the built releases will appear in this folder names tactview_linux64_{date}.tar.gz and tactview_{version}.deb

## Java

### Tools

 1. You need to download & install Apache Maven for you OS: [Official website](https://maven.apache.org/download.cgi)
 2. Installed you preferred IDE. I use [Eclipse](https://www.eclipse.org/downloads/) but [IDEA](https://www.jetbrains.com/idea/download) will works just as well.

### Import & run

Import tactview folder as Maven project into your IDE

Run `application.HackyMain` as regular Java file using these VM arguments: `-Djdk.gtk.version=2 -Dtactview.profile=development`
