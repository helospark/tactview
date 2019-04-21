@echo off
setlocal ENABLEDELAYEDEXPANSION

for /r %%i in (*.cpp) do (
	set inputFile="%%~ni.cpp"
	set objFile="%%~ni.obj"
	set dllFile="%%~ni.dll"
	echo "!inputFile! !objFile! !dllFile!" 
)