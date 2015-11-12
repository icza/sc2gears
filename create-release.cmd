@echo off

%~d0
cd "%~dp0"

if NOT EXIST "release-files\Sc2gears\Sc2gears.exe" goto error

set PATH=%PATH%;c:\Program Files (x86)\Java\jdk1.7.0_40\bin\

rmdir /s /q release


:============================================================ PREPARATIONS ============================================================
: Prepare Plugin API: Exclude HTMLs (which are part of the javadoc)!
xcopy /e /q bin-sc2gearspluginapi release\temp-bin-sc2gearspluginapi\
del /s release\temp-bin-sc2gearspluginapi\*.html


:============================================================ SC2GEARS RELEASE ============================================================


md "release\Sc2gears\User Content\Logs"
md "release\Sc2gears\User Content\Plugin settings"
md "release\Sc2gears\User Content\Plugin file cache"
md "release\Sc2gears\User Content\Replay sources"
md "release\Sc2gears\User Content\Replay lists"
md "release\Sc2gears\User Content\Replay cache"
md "release\Sc2gears\User Content\Profile cache"
md "release\Sc2gears\User Content\Search filters"
md "release\Sc2gears\lib-updater"
md "release\Sc2gears\Plugins\Build Orders Table"
md "release\Sc2gears\Plugins\The Sound of Victory"

xcopy /e /q app-folder\Languages release\Sc2gears\Languages\
xcopy /e /q app-folder\lib release\Sc2gears\lib\
xcopy /e /q release-files\Sc2gears release\Sc2gears\

jar cvfm release\Sc2gears\lib-updater\Sc2gearsUpdater.jar release-files\updater-manifest.mf -C bin-shared hu/belicza/andras/sc2gears/shared -C bin-updater .

jar cvfm release\Sc2gears\lib\Sc2gears.jar release-files\Sc2gears-manifest.mf -C release-files org.apache_LICENSE.txt -C bin-shared hu/belicza/andras/sc2gears/shared -C bin . -C release/temp-bin-sc2gearspluginapi hu/belicza/andras/sc2gearspluginapi -C war/WEB-INF/classes hu/belicza/andras/mpq -C war/WEB-INF/classes hu/belicza/andras/sc2gearsdbapi -C war/WEB-INF/classes org/apache/tools/bzip2 -C war/WEB-INF/classes hu/belicza/andras/util -C war/WEB-INF/classes hu/belicza/andras/smpd


cd release\Sc2gears
jar i lib-updater\Sc2gearsUpdater.jar
jar i lib\Sc2gears.jar lib/jna.jar lib/platform.jar lib/jl1.0.1.jar lib/mp3spi1.9.4.jar lib/tritonus_share.jar lib/squareness.jar lib/OfficeLnFs_2.7.jar
cd ..\..


:========================================================== SC2GEARS PLUGIN API ===========================================================
: TODO this should be a different batch because release is created with different compile settings
: the plugin api jar should contain the parameter and variable names...


md "release\Sc2gears Plugin API\lib"
md "release\Sc2gears Plugin API\java-doc"
md "release\Sc2gears Plugin API\example\File info"
md "release\Sc2gears Plugin API\example\Build Orders Table"
md "release\Sc2gears Plugin API\example\The Sound of Victory"
md "release\Sc2gears Plugin API\example-src\File info\hu\belicza\andras\fileinfoplugin\"
md "release\Sc2gears Plugin API\example-src\Build Orders Table\hu\belicza\andras\buildorderstableplugin\"
md "release\Sc2gears Plugin API\example-src\The Sound of Victory\hu\belicza\andras\thesoundofvictoryplugin\"

xcopy /e /q "release-files\Sc2gears Plugin API" "release\Sc2gears Plugin API\"

jar cvfm "release\Sc2gears Plugin API\lib\Sc2gears-plugin-api.jar" release-files\Sc2gears-plugin-api-manifest.mf -C release\temp-bin-sc2gearspluginapi .


javadoc -author -version -use -windowtitle "Sc2gears plugin API documentation" -top "<a href='https://sites.google.com/site/sc2gears/features/plugin-interface' target='_blank'>Sc2gears Plugin Interface</a>&nbsp;&nbsp;|&nbsp;&nbsp;<a href='https://sites.google.com/site/sc2gears/' target='_blank'>Sc2gears home page</a>" -overview src-sc2gearspluginapi/overview.html -encoding UTF-8 -sourcepath "src-sc2gearspluginapi" -d "release\Sc2gears Plugin API\java-doc" -subpackages hu.belicza.andras.sc2gearspluginapi

jar cvf "release\Sc2gears Plugin API\example\File info\file-info-plugin.jar" -C bin-plugins hu/belicza/andras/fileinfoplugin
copy "src-plugins\hu\belicza\andras\fileinfoplugin\*.*" "release\Sc2gears Plugin API\example-src\File info\hu\belicza\andras\fileinfoplugin\"
jar cvf "release\Sc2gears Plugin API\example\Build Orders Table\build-orders-table-plugin.jar" -C bin-plugins hu/belicza/andras/buildorderstableplugin
copy "src-plugins\hu\belicza\andras\buildorderstableplugin\*.*" "release\Sc2gears Plugin API\example-src\Build Orders Table\hu\belicza\andras\buildorderstableplugin\"
jar cvf "release\Sc2gears Plugin API\example\The Sound of Victory\the-sound-of-victory-plugin.jar" -C bin-plugins hu/belicza/andras/thesoundofvictoryplugin
copy "src-plugins\hu\belicza\andras\thesoundofvictoryplugin\*.*" "release\Sc2gears Plugin API\example-src\The Sound of Victory\hu\belicza\andras\thesoundofvictoryplugin\"


xcopy /e /q "release\Sc2gears Plugin API\example\Build Orders Table" "release\Sc2gears\plugins\Build Orders Table"
xcopy /e /q "release\Sc2gears Plugin API\example\The Sound of Victory" "release\Sc2gears\plugins\The Sound of Victory"


:========================================================== CLEANUP ===========================================================


rmdir /s /q release\temp-bin-sc2gearspluginapi


goto end


:error
echo It looks like the script was launched from improper folder. Aborting...
pause

:end
