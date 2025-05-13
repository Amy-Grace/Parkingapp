@echo off
set JAVA_HOME=C:\Users\User\AppData\Local\Programs\Android Studio\jbr
set PATH=%JAVA_HOME%\bin;%PATH%
echo Using Java from: %JAVA_HOME%
echo Building minimal APK, skipping most checks...
call gradlew.bat --info assembleDebug --stacktrace -x lint -x lintVitalRelease -x test -x kaptDebugKotlin -x kaptGenerateStubsDebugKotlin -x kaptReleaseKotlin -x processDebugGoogleServices 