FROM tomcat:8.5-jdk8
ENV TZ=Asia/Shanghai LANG=C.UTF-8 LANGUAGE=C.UTF-8 LC_ALL=C.UTF-8
ENV ROTATE_LOG_SIZE=1000M
ENV ROTATE_LOG_INTERVAL=86400
ENV ROTATE_LOG_FILE_NAME=catalina
ENV NCE_APPNAME=hango-portal
ENV CRON_TASK="30 * * * *"
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
WORKDIR /usr/local/tomcat
RUN rm -rf webapps/* \
    && mkdir webapps/ROOT
COPY envoy-infra/target/*.jar webapps/ROOT
RUN ls webapps/ROOT
ENTRYPOINT ["java","-jar","/usr/local/tomcat/webapps/ROOT/envoy-infra-0.0.1-SNAPSHOT.jar", \
"-DAsyncLoggerConfig.RingBufferSize=16384", \
"-Xmx2048m", "-Xms512m", "-verbose:gc", "-XX:+PrintGCDetails", "-XX:SurvivorRatio=2", "-XX:NewRatio=1", \
"-XX:+PrintGCTimeStamps", "-Xloggc:/data/log/apigw-gateway/gc/gc.log", \
"-XX:+UseG1GC", "-XX:MaxMetaspaceSize=256m", "-XX:+HeapDumpOnOutOfMemoryError", "-XX:HeapDumpPath=/data/log/hango-gateway/java_gateway_heapdump.hprof"]