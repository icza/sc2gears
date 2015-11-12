set PATH=%PATH%;c:\Program Files (x86)\Java\jdk1.7.0_40\bin\

rmdir /s /q jar-output
mkdir jar-output

date /T >build.info
time /T >>build.info


jar cvf jar-output\sc2gears-parsing-engine.jar -C bin . build.info
