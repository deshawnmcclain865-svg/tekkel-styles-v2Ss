@echo off
setlocal
cd /d "%~dp0"
where java >nul 2>nul || (echo Java 21 fehlt. Installiere JDK 21: https://adoptium.net & pause & exit /b 1)
set GV=9.2.1
if not exist ".gradle-dist\gradle-%GV%\bin\gradle.bat" (
  echo Lade Gradle %GV% herunter...
  powershell -NoProfile -Command "New-Item -ItemType Directory -Force .gradle-dist | Out-Null; Invoke-WebRequest https://services.gradle.org/distributions/gradle-%GV%-bin.zip -OutFile .gradle-dist\g.zip; Expand-Archive -Force .gradle-dist\g.zip .gradle-dist"
)
call ".gradle-dist\gradle-%GV%\bin\gradle.bat" build
echo.
echo Fertig! Deine Mod liegt in build\libs\skinmenu-1.0.0.jar
pause
