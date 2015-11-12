# Sc2gears

The COMPLETE (!) source code of the Sc2gears universe (Sc2gears app + Sc2gears Database bundled in an Eclipse project).

Sc2gears home page: https://sites.google.com/site/sc2gears/

The complete source code of Sc2gears is more than a hundred thousands lines of code. Enjoy :)

## Directory structure

[directory-info.html](https://github.com/icza/sc2gears/blob/master/directory-info.html) in the root explains / details the main folders / files:

<table>
	<tr><th colspan=3>File/folder</th><th>Description</th>
	<tr><td colspan=3>/src</td><td>Main source folder of Sc2gears.</td>
	<tr><td colspan=3>/src-shared</td><td>Shared source folder for both Sc2gears and Sc2gears Updater.</td>
	<tr><td colspan=3>/src-updater</td><td>Main source folder of the Sc2gears Updater.</td>
	<tr><td colspan=3>/src-util</td><td>Source of some utility/test application, not part of Sc2gears.</td>
	<tr><td colspan=3>/src-common</td><td>Common source for both Sc2gears and Sc2gears Database.</td>
	<tr><td colspan=3>/src-sc2gearsdb</td><td>Source folder of the web application of the back-end server for Sc2gears.</td>
	<tr><td colspan=3>/app-folder</td><td>Contains the off-line files and static files/folders required to run Sc2gears (including the language files).</td>
	<tr><td colspan=3>/release-files</td><td>Static resource files needed to create a release.</td>
	<tr><td colspan=3>/release</td><td>Target folder for creating a release</td>
	<tr><td colspan=3>/doc</td><td>Contains some info and help files. </td>
	<tr><td colspan=3>/resources</td><td>Some external resources with the intent to provide information only, not required for the project.</td>
	<tr><td rowspan=5>/war</td><td colspan=2>/</td><td>Files of the Sc2gears Database web application.</td>
	<tr><td rowspan=4>/hosted</td><td>/</td><td>Hosted files that are intended for Sc2gears clients.</td>
	<tr><td>/latest_version.xml</td><td>XML document containing required info about the latest release.</td>
	<tr><td>/start_pageXXX.html</td><td>HTML document to be displayed as the Start page of Sc2gears.</td>
	<tr><td>/custom_protraits.xml</td><td>XML document containing the custom portraits definition.</td>
	<tr><td colspan=3>/clear_for_archiving.cmd</td><td>Windows script to clear unnecessary, compiler generated files to make backups smaller.</td>
	<tr><td colspan=3>/create-release.cmd</td><td>Windows script to create a release.</td>
	<tr><td colspan=3>/directory-info.html</td><td>This document.</td>
	<tr><td colspan=3>/Sc2gears.launch</td><td>Eclipse launch configuration to run Sc2gears.</td>
</table>
