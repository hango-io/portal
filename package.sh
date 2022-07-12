#!/bin/bash
mvn clean package  -Dmaven.test.skip=true
dates=`date +%s`
echo $dates
module=apigw-portal
export HUB=hub.c.163.com/qingzhou/$module
branch=$(git symbolic-ref --short -q HEAD)
commit=$(git rev-parse --short HEAD)
dates=`date +%s`
tag=$(git show-ref --tags| grep $commit | awk -F"[/]" '{print $3}')
if [ -z $tag ]
then
   export TAG=$branch-$commit
else
   export TAG=$tag
fi
if ! git diff-index --quiet HEAD --; then
  TAG=$TAG-$dates-dirty
fi

docker build -f gateway-portal/Dockerfile -t hub.c.163.com/qingzhou/$module:$TAG .

docker  push  hub.c.163.com/qingzhou/$module:$TAG

