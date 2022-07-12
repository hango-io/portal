#!/bin/bash
# 此脚本用于打grpc-tomcat镜像提供编译服务器架构检查能力

# 检测环境的架构
COMPILE_ARCH=amd64
LOCAL_ARCH=$(uname -m)
if [ "${TARGET_ARCH}" ]; then
    LOCAL_ARCH=${TARGET_ARCH}
fi

case "${LOCAL_ARCH}" in
  arm*|aarch64*)
    COMPILE_ARCH=aarch_64
    ;;
  amd64|x86_64)
    COMPILE_ARCH=x86_64
    ;;
  *)
    echo "This system's architecture, ${LOCAL_ARCH}, isn't supported"
    exit 1
    ;;
esac
# 输出架构类型
echo "${COMPILE_ARCH}"