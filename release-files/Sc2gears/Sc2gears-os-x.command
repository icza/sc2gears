#!/bin/bash
cd "${0%/*}"
java -Xmx512m -cp lib/Sc2gears.jar:lib/jna.jar:lib/platform.jar:lib/jl1.0.1.jar:lib/mp3spi1.9.4.jar:lib/tritonus_share.jar:lib/squareness.jar:lib/OfficeLnFs_2.7.jar hu/belicza/andras/sc2gears/Sc2gears "$@"