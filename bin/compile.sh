#!/bin/sh

echo Creating temporary classes directory
mkdir ../temp_classes

echo Expanding Imported classes...

cd ../temp_classes
jar xf ../import/jakarta-oro-2.0.7/jakarta-oro-2.0.7.jar
rm -r META-INF

echo Compiling Comet Way Agent Kernel...
CLASSPATH=../temp_classes
javac -classpath $CLASSPATH -d ../temp_classes `find ../src -name '*.java' -print`

echo Creating ak.jar...
jar cf ../ak.jar *

echo Removing temporary classes directory
rm -r ../temp_classes

