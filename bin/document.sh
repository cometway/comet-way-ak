#!/bin/sh

echo Removing old javadocs...
rm -r ../javadocs/*

echo Compiling javadocs...

CLASSPATH=../src:../classes
export CLASSPATH

SOURCEPATH=../src:../import/jakarta-oro-2.0.5/src/java
export SOURCEPATH

javadoc -classpath $CLASSPATH -sourcepath $SOURCEPATH -d ../javadocs com.cometway.ak com.cometway.email com.cometway.httpd com.cometway.io com.cometway.jdbc com.cometway.net com.cometway.om com.cometway.props com.cometway.states com.cometway.swing com.cometway.text com.cometway.tools com.cometway.util com.cometway.xml org.apache.oro.io org.apache.oro.text org.apache.oro.text.awk org.apache.oro.text.perl org.apache.oro.text.regex org.apache.oro.util

echo Building `date +"ak_docs-%m-%d-20%y.tar.gz"`
cd ../..
tar cfz `date +"ak_docs-%m-%d-20%y.tar.gz"` ak/docs ak/javadocs
ls -l `date +"ak_docs-%m-%d-20%y.tar.gz"`

