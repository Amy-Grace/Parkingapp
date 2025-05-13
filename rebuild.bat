@echo off
set JAVA_HOME=C:\Users\User\AppData\Local\Programs\Android Studio\jbr
set PATH=%JAVA_HOME%\bin;%PATH%
echo Using Java from: %JAVA_HOME%
echo Cleaning project...
call gradlew.bat clean
echo Building APK with simplified options...
call gradlew.bat --info assembleDebug --stacktrace -x lint -x lintVitalRelease -x test -x kaptDebugKotlin -x kaptGenerateStubsDebugKotlin -x kaptReleaseKotlin 