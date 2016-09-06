#!/bin/sh            
sed -i "s/<version>.*?<\/version><!-- Xill Platform Version -->/<version>$1<\/version><!-- Xill Platform Version -->/" distpom.xml
sed -i "s/<xill.version>.*?<\/xill.version>/<xill.version>$1<\/xill.version>/" xill-parent/pom.xml
if [ $2 != "-jenkins" ]
	then
		mvn versions:set -DnewVersion=$1
		cd xill-parent/
		mvn versions:set -DnewVersion=$1
		cd ..
fi