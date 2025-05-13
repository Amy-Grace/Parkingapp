@echo off
set JAVA_HOME=C:\Users\User\AppData\Local\Programs\Android Studio\jbr
set PATH=%JAVA_HOME%\bin;%PATH%
echo Using Java from: %JAVA_HOME%
echo Building APK with minimal checks...
call gradlew.bat --info packageDebug --stacktrace -x compileDebugKotlin -x compileDebugJavaWithJavac -x processDebugGoogleServices -x lint 