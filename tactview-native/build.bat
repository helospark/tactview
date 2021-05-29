@echo off
setlocal ENABLEDELAYEDEXPANSION

set visual_studio_path=C:\Program Files (x86)\Microsoft Visual Studio\2017\Community\VC
set ffmpeg_path=C:\lib\ffmpeg-20210407-557953a397-win64-shared
set opencv_path=C:\lib\opencv

call "%visual_studio_path%\Auxiliary\Build\vcvarsall.bat" x86_amd64

del /S *.obj
del /S *.dll

for %%i in (*.cpp) do (
	set inputFile="%%~ni.cpp"
	set objFile="%%~ni.obj"
	set dllFile="%%~ni.dll"
	echo Compiling !inputFile!
	"%visual_studio_path%\Tools\MSVC\14.16.27023\bin\HostX64\x64\CL.exe" /c /I"%ffmpeg_path%\include" /IC:\lib\opencv\build\include /Zi /nologo /diagnostics:classic /O2 /Oi /GL /D _WINDLL /D _MBCS /Gm- /EHsc /MD /GS /Gy /fp:precise /permissive- /Zc:wchar_t /Zc:forScope /Zc:inline /Gd /TP /FC /errorReport:prompt /Fo!objFile! !inputFile!
	"%visual_studio_path%\Tools\MSVC\14.16.27023\bin\HostX64\x64\link.exe" /ERRORREPORT:PROMPT /OUT:"!dllFile!" /NOLOGO /LIBPATH:"%ffmpeg_path%\bin" /LIBPATH:%opencv_path%\build\x64\vc15\lib opencv_world401.lib avcodec.lib avformat.lib swscale.lib swresample.lib avutil.lib kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib odbc32.lib odbccp32.lib /MANIFEST /LTCG /MANIFESTUAC:"level='asInvoker' uiAccess='false'" /manifest:embed /OPT:REF /OPT:ICF /TLBID:1 /DYNAMICBASE /NXCOMPAT /MACHINE:X64 /DLL !objFile!
	echo -
)

if not exist "../tactview-core/src/main/resources/win32-x86-64" mkdir "../tactview-core/src/main/resources/win32-x86-64"

for /R %%f in (*.dll) do copy %%f "../tactview-core/src/main/resources/win32-x86-64"

"%visual_studio_path%\Tools\MSVC\14.16.27023\bin\HostX64\x64\CL.exe" /EHsc /Fostartup_win.obj /c /Zi /nologo windows/startup_win.cpp
rc.exe tactview.rc
"%visual_studio_path%\Tools\MSVC\14.16.27023\bin\HostX64\x64\link.exe" /ERRORREPORT:PROMPT /OUT:"startup.exe" /NOLOGO /MACHINE:X64 /subsystem:windows /entry:mainCRTStartup startup_win.obj tactview.res