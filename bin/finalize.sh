#!/bin/sh

cd ..

echo Removing javadocs
rm -r javadocs/*

# echo Setting file and directory dates
# find . -print | xargs touch

echo Building `date +"ak-%m-%d-20%y.tar.gz"`
cd ..
tar cfz `date +"ak-%m-%d-20%y.tar.gz"` ak
ls -l `date +"ak-%m-%d-20%y.tar.gz"`

echo Completed.

