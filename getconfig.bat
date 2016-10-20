@echo off

set JAVA_OPT=-Xmx256m

java -jar %~dp0/build/libs/gradle-server-acceptance-0.1.0-all.jar %*

exit /b %ERRORLEVEL%
