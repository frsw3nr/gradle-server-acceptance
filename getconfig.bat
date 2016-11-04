@echo off

set JAVA_OPT="-Dlogback.configurationFile=%~dp0/config/logback.xml"

java -jar %JAVA_OPT% %~dp0/build/libs/gradle-server-acceptance-0.1.2-all.jar %*

exit /b %ERRORLEVEL%
