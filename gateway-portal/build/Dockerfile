# 通过SRE提供的基础tomcat镜像打出一个gportal需要的tomcat镜像（一般不需要重新打，只需要打一次就可以一直用，除非当前Dockerfile内部组件有更新，需要重新打）
# 由于是harbor.cloud.netease.com仓库，使用该Dockerfile开始打镜像前请确认当前执行的编译服务器上的docker有权限
FROM hub.c.163.com/qingzhou/tomcat-8.5-jdk8-openjdk-skiff:v1.2-191015
COPY detect_server_architecture.sh /tmp
RUN cd /usr/local \
    && mkdir protobuf \
    && cd protobuf \
    && ARCH=$(bash /tmp/detect_server_architecture.sh) \
    && wget https://github.com/protocolbuffers/protobuf/releases/download/v3.5.1/protoc-3.5.1-linux-"${ARCH}".zip \
    && unzip protoc-3.5.1-linux-"${ARCH}".zip \
    && cd /usr/local/protobuf \
    && wget https://github.com/grpc-ecosystem/grpc-gateway/archive/master.zip \
    && unzip master.zip \
    && rm -f /tmp/detect_server_architecture.sh