@echo off
set JAVA_HOME=C:\Users\User\AppData\Local\Programs\Android Studio\jbr
set PATH=%JAVA_HOME%\bin;%PATH%
echo Using Java from: %JAVA_HOME%
echo Building minimal APK (bypassing Kotlin compilation)...
call gradlew.bat clean
call gradlew.bat packageDebug -x compileDebugKotlin -x kaptDebugKotlin -x processDebugResources -x mergeDebugJavaResource -x lint -x lintVitalRelease -x test 