@echo off
set /a n=1
:loop
call 1.bat
java Main

call 2.bat
choice /t 2 /d y /n 

goto loop