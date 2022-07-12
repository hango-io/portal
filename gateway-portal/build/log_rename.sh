#!/usr/bin/env bash
sed -i "s/\(.*log\.file\.name\">\).*\(<.*\)/\1$(date '+%Y-%m-%d')\2/g" $CATALINA_HOME/webapps/ROOT/WEB-INF/classes/log4j2-spring.xml
