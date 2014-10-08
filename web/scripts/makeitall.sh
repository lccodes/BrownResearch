#This file creates a folder full of empty files to hold the data
#!/bin/sh
value=`cat theFile.txt`

#!/usr/bin/env bash
cd /vol/web/html/respice/

arr=$(echo $value | tr ";" "\n")

for x in $arr
do
     mkdir "${x%%,*}"
     cd "${x%%,*}"
     touch "${x%%,*}.txt"
     touch "${x%%,*}-happy.txt"
     touch "${x%%,*}-survey.txt"
     cd ..
done
