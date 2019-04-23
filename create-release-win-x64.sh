#!/bin/bash

set -e

rm -r release/win64 || true
mkdir -p release/win64
cd tactview-native
cmd /c build.bat
cd ..
mvn clean install

cp tactview-ui/target/tactview-ui*.jar release/win64/tactview.jar

jlink --no-header-files --no-man-pages --compress=2 --strip-debug --add-modules java.base,java.xml,java.naming,java.desktop,jdk.unsupported --output release/win64/java-runtime

cp tactview-native/startup.exe release/win64/tactview.exe

echo "Copying dynamic library dependencies"

BLACKLISTED_DEPENDENCIES="advapi32.dll\|bcrypt.dll\|bcryptPrimitives.dll\|cfgmgr32.dll\|combase.dll\|COMDLG32.dll\|CONCRT140.dll\|CRYPT32.dll\|CRYPTBASE.DLL\|cryptsp.dll\|d3d11.dll\|dxgi.dll\|GDI32.dll\|gdi32full.dll\|IMM32.DLL\|kernel.appcore.dll\|KERNEL32.DLL\|KERNELBASE.dll\|ksuser.dll\|MF.dll\|MFCORE.DLL\|MFPlat.DLL\|MFReadWrite.dll\|MSASN1.dll\|msvcp_win.dll\|MSVCP140.dll\|msvcrt.dll\|ntdll.dll\|ole32.dll\|OLEAUT32.dll\|powrprof.dll\|profapi.dll\|RPCRT4.dll\|RTWorkQ.DLL\|sechost.dll\|Secur32.dll\|shcore.dll\|SHELL32.dll\|shlwapi.dll\|SHLWAPI.dll\|SSPICLI.DLL\|ucrtbase.dll\|USER32.dll\|win32u.dll\|windows.storage.dll\|WS2_32.dll\|COMCTL32.dll"

ldd tactview-native/*.dll | grep "=> /" | grep -i -v $BLACKLISTED_DEPENDENCIES | awk '{print $3}' | sort -u | xargs -I '{}' cp -v '{}' release/win64/.

cd release

builddate=`date '+%Y%M%d_%H%m%S'`
filename="tactview_win64_$builddate.zip"
zip -r $filename win64/
