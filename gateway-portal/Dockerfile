FROM docker.io/hangoio/tomcat-8.5:v1.1-202206301526
ENV TZ=Asia/Shanghai LANG=C.UTF-8 LANGUAGE=C.UTF-8 LC_ALL=C.UTF-8
ENV ROTATE_LOG_SIZE=1000M
ENV ROTATE_LOG_INTERVAL=86400
ENV ROTATE_LOG_FILE_NAME=catalina
ENV NCE_APPNAME=api-gateway-portal
ENV CRON_TASK="30 * * * *"
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
WORKDIR /usr/local/tomcat
RUN rm -rf webapps/* \
    && mkdir webapps/ROOT
COPY gateway-portal/build bin/
COPY gateway-portal/target/*.war webapps/ROOT
RUN cd webapps/ROOT \
    && jar -xvf $(ls | grep '.war') \
    && rm $(ls | grep '.war') \
    && chmod +x $CATALINA_HOME/bin/docker-entrypoint.sh
ENTRYPOINT ["bin/docker-entrypoint.sh"]
