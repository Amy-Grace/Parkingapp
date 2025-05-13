@echo off
set JAVA_HOME=C:\Users\User\AppData\Local\Programs\Android Studio\jbr
set PATH=%JAVA_HOME%\bin;%PATH%
echo Using Java from: %JAVA_HOME%
echo Building APK with Firebase integration...
call gradlew.bat clean
call gradlew.bat assembleDebug -x lint -x test -x kaptDebugKotlin -x kaptGenerateStubsDebugKotlin -x kaptReleaseKotlin 