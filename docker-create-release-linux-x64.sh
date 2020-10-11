# Creates release build using the docker build image, that contains an Ubuntu with all dependencies set up. The result of this build is the same as if you run ./create-release-linux-x64.sh script, but without additional dependencies, reproducible + strips debug symbols from dependencies for performance and smaller size.
# This script only need a Docker installed & image to be present.
# Build image can be build by running "docker build -t helospark/tactview_build ." in the root of tactview, however building that image takes a long time due to download, compilation and installation of all dependencies.
#!/bin/sh

sudo docker run -v $(pwd):/tactview -v "$HOME/.m2/repository:/tmp/.m2" -u $(id -u ${USER}):$(id -g ${USER}) helospark/tactview_build:latest
