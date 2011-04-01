;
; imagej.nsi
;

; An NSIS script to build the ImageJ.exe Windows launcher.

; Script adapted from:
; http://nsis.sourceforge.net/A_slightly_better_Java_Launcher

Name "ImageJ"
Caption "ImageJ image processing software"
Icon "..\logo\icon128.ico"
OutFile "ImageJ.exe"

SilentInstall silent
AutoCloseWindow true
ShowInstDetails nevershow

!define CLASSPATH "plugins;jar\imagej-2.0-SNAPSHOT.jar"
!define CLASS "imagej.ImageJ"
!define JVM_ARGS "-mx512m"

Section ""
  Call GetJRE
  Pop $R0

  StrCpy $0 '"$R0" -classpath "${CLASSPATH}" ${JVM_ARGS} ${CLASS}'

  SetOutPath $EXEDIR
  ExecWait $0
SectionEnd

;Function .onInit
;  SetOutPath $TEMP
;  File /oname=imagej-logo.bmp "..\logo\logo.bmp"
;
;  splash::show 1500 $TEMP\imagej-logo
;
;  Pop $0
;
;  Delete $TEMP\imagej-logo.bmp
;FunctionEnd

Function GetJRE
;
;  Find JRE (javaw.exe)
;  1 - in .\jre directory (JRE Installed with application)
;  2 - in JAVA_HOME environment variable
;  3 - in the registry
;  4 - assume javaw.exe in current dir or PATH

  Push $R0
  Push $R1

  ClearErrors
  StrCpy $R0 "$EXEDIR\jre\bin\javaw.exe"
  IfFileExists $R0 JreFound
  StrCpy $R0 ""

  ClearErrors
  ReadEnvStr $R0 "JAVA_HOME"
  StrCpy $R0 "$R0\bin\javaw.exe"
  IfErrors 0 JreFound

  ClearErrors
  ReadRegStr $R1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  ReadRegStr $R0 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$R1" "JavaHome"
  StrCpy $R0 "$R0\bin\javaw.exe"

  IfErrors 0 JreFound
  StrCpy $R0 "javaw.exe"

 JreFound:
  Pop $R1
  Exch $R0
FunctionEnd
