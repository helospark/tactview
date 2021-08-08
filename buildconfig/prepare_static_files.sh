# This script is invoked during the build process and should copy files into the release. Fully qualified path of release directory is in $1
#!/bin/bash
set -e

cd ${BASH_SOURCE%/*}

mkdir -p "$1/dropin/luts"
unzip -u "data/FG Free Cine LUTs Pack v2.zip" -d "$1/dropin/luts"

# Add default plugin
echo "Adding GLSL plugin as default"
glslPluginVersion="0.0.2"
pluginName="tactview-glsl-plugin-${glslPluginVersion}.zip"
glslPlugin="https://github.com/helospark/tactview-glsl-plugin/releases/download/v${glslPluginVersion}/${pluginName}"
glslPluginPath="tmp/$pluginName"
mkdir -p "tmp"

if [ ! -e "$glslPluginPath" ]
then
    echo "Downloading $glslPlugin"
    wget "$glslPlugin" -O "$glslPluginPath"
fi

mkdir -p "$1/dropin/plugins"
unzip -u "$glslPluginPath" -d "$1/dropin/plugins/${pluginName%.zip}"
