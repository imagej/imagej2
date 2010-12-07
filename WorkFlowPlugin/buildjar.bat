@echo on

mkdir build

call javac-codeblocks

cd build

copy ..\src\codeblocks\*.png codeblocks
copy ..\src\codeblocks\*.wav codeblocks
jar cf codeblocks.jar codeblocks codeblockutil controller workspace renderable

del /q /s codeblocks > nul
rmdir codeblocks
del /q /s codeblockutil > nul
rmdir codeblockutil
del /q /s controller > nul
rmdir controller
del /q /s workspace > nul
rmdir workspace
del /q /s renderable > nul
rmdir renderable

cd ..

:end
