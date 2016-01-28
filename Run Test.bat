@ECHO OFF
call gw clean build test
echo Open result in 5 sec...
timeout /t 5 
call start build/reports/tests/index.html
pause