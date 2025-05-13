@echo off
set JAVA_HOME=C:\Users\User\AppData\Local\Programs\Android Studio\jbr
set PATH=%JAVA_HOME%\bin;%PATH%
echo Using Java from: %JAVA_HOME%
echo Assembling debug APK only...
call gradlew.bat assembleDebug -x kaptDebugKotlin -x kaptGenerateStubsDebugKotlin 