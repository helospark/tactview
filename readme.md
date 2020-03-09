# Tactview

Tactview is an open-source advanced multi-track video editor aims to provide both powerful feature and simple usage.

![Screenshot](/images/screenshot-1.png)

## Running

Download the latest version for your OS:
  
  - [Linux-x64](https://helospark.com/tactview/download/tactview_linux64_snapshot.zip)
  - [Windows-x64](https://helospark.com/tactview/download/tactview_win64_snapshot.zip)

Or check out all releases: [all releases](https://helospark.com/tactview/download/)

Extract the zip, and run `tactview` executable.

## Build

### Dependencies


 - FFmpeg (AVCodec)
 - OpenCV
 - Java 11


You can install all dependencies running `build.bat (Windows)`, `build.sh (Linux)`, `buildmac.sh (Mac)`. script in `tactview_native/build` as root.

### Release build

(On Linux you can install all dependencies with `tactview-native/build_dependencies.sh`)

Create a release

	./create-release-{platform}.sh

You will find the created zip in `release` folder.

### Development

 - You need to build all native dependencies by running `build.bat (Windows)`, `build.sh (Linux)`, `buildmac.sh (Mac)` script in `tactview_native/build` as root. 
 - You can import `tactview-parent` into your IDE as a Maven project.
 - Run `HackyMain` class as Java application
 - During development set these JVM arguments: `-Djdk.gtk.version=2 -Dshow.dialog.dirty-save=false -Xmx2g -Dprism.order=sw`
