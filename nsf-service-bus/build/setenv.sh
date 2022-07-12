#!/bin/bash

set -ex

if [ -z "$NCE_XMS" ]; then
NCE_XMS=1024m
fi

if [ -z "$NCE_XMX" ]; then
NCE_XMX=2048m
fi

if [ -z "$NCE_META" ]; then
NCE_META=256m
fi

if [ -z "$NCE_APPNAME" ]; then
NCE_APPNAME=
fi

export NCE_JAVA_OPTS="$NCE_JAVA_OPTS -Xms${NCE_XMS} -Xmx${NCE_XMX} -XX:MaxMetaspaceSize=${NCE_META} -Dcom.netease.appname=${NCE_APPNAME}"

export JAVA_OPTS="-Djava.awt.headless=true -server -verbose:gc -Djava.security.egd=file:/dev/./urandom -XX:+HeapDumpOnOutOfMemoryError -XX:+PrintGCDetails --XX:+PrintGCTimeStamps -Dforeign.domain=true -Dsun.rmi.transport.tcp.responseTimeout=20000 -Dsun.rmi.dgc.client.gcInterval=7200000 -Dsun.rmi.dgc.server.gcInterval=7200000 -Duser.timezone=Asia/Shanghai -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 $JAVA_OPTS $NCE_JAVA_OPTS"
