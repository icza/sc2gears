@echo off

%~d0
cd "%~dp0"

if NOT EXIST "release-files\Sc2gears\Sc2gears.exe" goto error


rmdir /s /q bin\hu
rmdir /s /q bin\org
rmdir /s /q bin-shared\hu
rmdir /s /q bin-updater\hu
rmdir /s /q bin-util\hu
rmdir /s /q bin-plugins\hu
rmdir /s /q bin-sc2gearspluginapi\hu
rmdir /s /q gwt-unitCache
rmdir /s /q "app-folder\User Content\Replay cache"
rmdir /s /q war\WEB-INF\classes\hu
rmdir /s /q war\WEB-INF\deploy

:Clear war\WEB-INF\lib folder:
ren war\WEB-INF\lib\sc2gears-parsing-engine.jar sc2gears-parsing-engine.ja_
del /f /q war\WEB-INF\lib\*.jar
ren war\WEB-INF\lib\sc2gears-parsing-engine.ja_ sc2gears-parsing-engine.jar

goto end

:error
echo It looks the script was launched from improper folder. Aborting...
pause

:end
