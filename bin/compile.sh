#!/bin/sh

echo Creating temporary classes directory
mkdir ../temp_classes
cd ../temp_classes

echo Compiling Comet Way Agent Kernel...
CLASSPATH=../temp_classes
javac -classpath $CLASSPATH -d ../temp_classes `find ../import/jakarta-oro-2.0.7/src -name '*.java' -print` `find ../src -name '*.java' -print` 
echo Creating ak.jar...
jar cf ../ak.jar *

echo Removing temporary classes directory
rm -r ../temp_classes

