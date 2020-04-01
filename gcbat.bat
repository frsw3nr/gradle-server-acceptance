@echo off

set getconfig_home="%~dp0"

python "%~dp0/gcbat.py" %*

exit /b %ERRORLEVEL%
