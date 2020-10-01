# Tactview

Tactview is an open-source advanced multi-track video editor aims to provide both powerful feature and intuitive usage.

![Screenshot](/images/screenshot-2.png)

## Running

### Binary

Download the latest binary release for your OS:
  
  - [Linux-x64](https://helospark.com/tactview/download/tactview_linux64_snapshot.zip)
  - [Windows-x64](https://helospark.com/tactview/download/tactview_win64_snapshot.zip)

Or check out all releases: [all releases](https://helospark.com/tactview/download/)

Extract the zip, and run `tactview` executable.

### Apt (Debian like systems)

You can install from the helospark apt repository on Ubuntu and other Debian based systems:

    sudo su -c "echo 'deb [trusted=yes] https://helospark.com/apt /' >> /etc/apt/sources.list.d/helospark.list"
    sudo apt-get update
    sudo apt-get install tactview

After install you can find Tactview either in your OS's program menu and under /opt/tactview/

## Build

### Dependencies


 - FFmpeg (AVCodec)
 - OpenCV
 - Java 15
 - Apache Maven (for building)
 - ImageMagick (on Linux during release used to convert icons)


(On Linux you can install all dependencies with `tactview-native/build_dependencies.sh`)

### Release build


Create a release

	./create-release-{platform}.sh

You will find the created releases in `release` folder under. The created zip can be copied to and run on a different computer, as it already contains almost all shared libraries and dependencies and only a minimal set of shared libraries are used on the platform where it is run.

### Development

 - Get all native dependencies, on Linux you can do that by running `tactview-native/build_dependencies.sh` as root
 - You need to build all native code by running `build.bat (Windows)`, `build.sh (Linux)`, `buildmac.sh (Mac)` script in `tactview_native`. You need to do this every time you modify native code (which should be rare). 
 - You can import `tactview-parent` into your IDE as a Maven project.
 - Run `HackyMain` class as Java application with these arguments `-Djdk.gtk.version=2  -Xmx4g -Dprism.order=sw  -Dtactview.profile=development`
