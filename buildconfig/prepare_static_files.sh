# This script is invoked during the build process and should copy files into the release. Fully qualified path of release directory is in $1
#!/bin/bash

cd ${BASH_SOURCE%/*}

mkdir -p "$1/dropin/luts"
unzip "data/FG Free Cine LUTs Pack v2.zip" -d "$1/dropin/luts"
