FROM java:8
ENV TZ=Asia/Shanghai LANG=C.UTF-8 LANGUAGE=C.UTF-8 LC_ALL=C.UTF-8
ENV NCE_APPNAME=nsb-worker
ENV CRON_TASK="30 * * * *"

WORKDIR /usr/local

COPY nsf-service-bus/build bin/
COPY nsf-service-bus/nsf-service-bus-worker/target camel/
RUN chmod +x bin/setenv.sh

CMD bin/setenv.sh && java -jar camel/nsb-worker.jar
