# v1.6.0
**(2020-02-20, http://jira.netease.com/browse/CLDNSF-5229)**

## Branch:
- productization-20200220

## Features:
- 1、获取客户端ip可配置
- 2、监控告警一期 （平台侧业务配置） 王添
- 3、监控二期：告警对象支持项目等
- 4、静态降级插件
- 5、网关灰度建设，测试21环境验证中
- 6、负载均衡，连接池配置
- 7、路由超时，重试配置
- 8、网关配置变更，记录日志
- 9、审计二期，项目隔离，日志可查看，网关耗时，阈值查询
- 10、trace插件优化
- 11、批量fix数据接口
- 12、网关项目隔离，项目级别插件（trace、cors、ip黑白名单）
- 13、添加注册中心配置

## BugFix:
- 无

## dependencies 
- pilot: 2.0.qingzhou
- envoy: envoy-gateway-lua:master_d3893165_envoy-gateway-v0.4.1-cm-cf344d69
- api-plane: release-1.1
- pom-nms: 1.8.x

# v1.5.0
**(2020-01-02, http://jira.netease.com/browse/CLDNSF-4762)**

## Branch:
- productization-20200102

## Features:
- 服务/路由支持多标签
- 路由复制支持插件复制
- 网关管理增加权限
- api-plane增加日志挂盘，api-plane下线服务bugfix
- 网关项目支持搜索
- envoy插件：
  （1）transform插件优化，支持url等功能;
  （2）accessLog插件优化细化
- 性能测试，优化插件

## BugFix:
- 无

## dependencies 
- hub.c.163.com/qingzhou/istio/pilot:1.2.6-qingzhou-b43ab83ff
- hub.c.163.com/qingzhou/envoy-gateway-lua:master_ff212136_envoy-gateway-v0.3.0-cm-e7af7b4a
- hub.c.163.com/qingzhou/nsf-api-plane-server:release-1.1-20191231-071021-de293ff7

# v1.4.1
**(2019-12-19)**

## Branch:
- productization-20191219

## Features:
- CLDNSF-4396 【Envoy网关】路由规则在不同服务间迁移
- CLDNSF-4397 【Envoy网关】服务发布支持选择应用端口（包括模糊匹配，路由发布更新，动态发布服务禁止发布更新等点）
- 控制台增加全局插件入口 CLDNSF-4608 【Envoy网关】全局插件自测
- 审计功能自测，遗留问题就绪。包括http://jira.netease.com/browse/CLDNSF-4575
- 1125插件优化

## BugFix:
- 无

## dependencies 
- pilot:pilot:1.2.6-qingzhou-ec3cc7289
- envoy:envoy-gateway-v0.2-cm-69ff8f9a4411a70b5080737cfa318e849419b6a9
- api-plane: release-1.1-20191219-070439-18e84490



# v1.3.1
**(2019-10-15)**

## Branch:
- productization-20190905

## Features:
- CLDNSF-3930 支持Consul高可用配置

## BugFix:
- 无

## dependencies 
- nsf-meta(nsf元数据服务，@张子豪): 2.5.9
- service-auth(鉴权服务，@陈重阳): v1.2.1 
- qingzhou-front(前端，@邹欣华): v1.3.x
- apigw-gateway : v1.3.x
- nsf-turbine(nsf监控服务 ，@ 张武) 2.5.1
- prometheus-nms(告警服务，@王添) v1.4.0

# v1.3.0
**(2019-09-26)**

## Branch:
- productization-20190905

## Features:
- CLDNSF-3757 【API网关】服务发布时支持“移除服务标识”
- CLDNSF-3597 【API网关】数据表名称修改加上

## BugFix:
- 无

## dependencies 
- nsf-meta(nsf元数据服务，@张子豪): 2.5.9
- service-auth(鉴权服务，@陈重阳): v1.2.1 
- qingzhou-front(前端，@邹欣华): v1.3.x
- apigw-gateway : v1.3.x
- nsf-turbine(nsf监控服务 ，@ 张武) 2.5.1
- prometheus-nms(告警服务，@王添) v1.4.0


# v1.2.1
**(2019-09-04)**

## Branch:
- productization-20190822

## Features:
- 新增日志清理配置（注意打镜像时需要使用build_gprotal.sh，然后进行tag）


# v1.2.0
**(2019-09-03)**

## Branch:
- productization-20190822

## Features:

## BugFix:
- 通过注册中心获取应用时出现的数组越界问题

## dependencies 
- nsf-meta(nsf元数据服务，@张子豪): 2.5.9
- service-auth(鉴权服务，@陈重阳): v1.2.1 
- qingzhou-front(前端，@邹欣华): v1.3.x
- apigw-gateway : v1.1.4
- nsf-turbine(nsf监控服务 ，@ 张武) 2.5.1
- prometheus-nms(告警服务，@王添) v1.4.0

# v1.1.12
**(2019-08-30)**

## Branch:
- productization-20190725

## Features:
- CLDNSF-2871 【API网关】统一授权管理
- CLDNSF-3095 【API网关-优化】G-Portal和网关优化及公有云Swagger导入优化
- CLDNSF-2982 【API网关】性能测试优化三期
- CLDNSF-3093 【API网关】分流逻辑性能优化
- CLDNSF-2955 NSF知识库重构--网关同步知识库修改
- CLDNSF-2970 【API网关】监控，统计告警功能 二期
- 网关支持HTTP转gRPC (CLDNSF-3008、CLDNSF-2781、CLDNSF-2873)

## BugFix:  
- 调整分流参数判断策略；
- 修复因实时更新API调用统计，导致0~1点之间查询实时统计数据时，统计数据的callIndex由24变为25，出现的数组越界问题；

## dependencies 
- nsf-meta(nsf元数据服务，@张子豪): 2.5.9
- service-auth(鉴权服务，@陈重阳): v1.2.1 
- qingzhou-front(前端，@邹欣华): v1.3.x
- apigw-gateway : v1.1.4
- nsf-turbine(nsf监控服务 ，@ 张武) 2.5.1
- prometheus-nms(告警服务，@王添) v1.4.0
- audit (审计服务，@张宝军) v1.2.x


# v1.1.11
**(2019-09-04)**

## Branch:
- productization-20190725

## Features:
- 新增日志清理配置（注意打镜像时需要使用build_gprotal.sh，然后进行tag）


# v1.1.10
**(2019-08-30)**

## Branch:
- productization-20190725

## Features:
- CLDNSF-2871 【API网关】统一授权管理
- CLDNSF-3095 【API网关-优化】G-Portal和网关优化及公有云Swagger导入优化
- CLDNSF-2982 【API网关】性能测试优化三期
- CLDNSF-3093 【API网关】分流逻辑性能优化
- CLDNSF-2955 NSF知识库重构--网关同步知识库修改
- CLDNSF-2970 【API网关】监控，统计告警功能 二期
- 网关支持HTTP转gRPC (CLDNSF-3008、CLDNSF-2781、CLDNSF-2873)

## BugFix:  
- 修复dubbo status code问题（之前未修改成功）

## dependencies 
- nsf-meta(nsf元数据服务，@张子豪): 2.5.9
- service-auth(鉴权服务，@陈重阳): v1.2.1 
- qingzhou-front(前端，@邹欣华): v1.3.x
- apigw-gateway : v1.1.4
- nsf-turbine(nsf监控服务 ，@ 张武) 2.5.1
- prometheus-nms(告警服务，@王添) v1.4.0
- audit (审计服务，@张宝军) v1.2.x

# v1.1.9
**(2019-08-29)**

## Branch:
- productization-20190725

## Features:
- CLDNSF-2871 【API网关】统一授权管理
- CLDNSF-3095 【API网关-优化】G-Portal和网关优化及公有云Swagger导入优化
- CLDNSF-2982 【API网关】性能测试优化三期
- CLDNSF-3093 【API网关】分流逻辑性能优化
- CLDNSF-2955 NSF知识库重构--网关同步知识库修改
- CLDNSF-2970 【API网关】监控，统计告警功能 二期
- 网关支持HTTP转gRPC (CLDNSF-3008、CLDNSF-2781、CLDNSF-2873)

## BugFix:  
- CLDNSF-3634 解决多机房环境下，有一个机房下调用grpc接口会报NoSuchApi的问题

## dependencies 
- nsf-meta(nsf元数据服务，@张子豪): 2.5.9
- service-auth(鉴权服务，@陈重阳): v1.2.1 
- qingzhou-front(前端，@邹欣华): v1.3.x
- apigw-gateway : v1.1.4
- nsf-turbine(nsf监控服务 ，@ 张武) 2.5.1
- prometheus-nms(告警服务，@王添) v1.4.0


# v1.1.8
**(2019-08-28)**

## Branch:
- productization-20190725

## Features:
- CLDNSF-2871 【API网关】统一授权管理
- CLDNSF-3095 【API网关-优化】G-Portal和网关优化及公有云Swagger导入优化
- CLDNSF-2982 【API网关】性能测试优化三期
- CLDNSF-3093 【API网关】分流逻辑性能优化
- CLDNSF-2955 NSF知识库重构--网关同步知识库修改
- CLDNSF-2970 【API网关】监控，统计告警功能 二期
- 网关支持HTTP转gRPC (CLDNSF-3008、CLDNSF-2781、CLDNSF-2873)

## BugFix:  
- 修改EurekaClientUtils中方法重载参数顺序问题

## dependencies 
- nsf-meta(nsf元数据服务，@张子豪): 2.5.9
- service-auth(鉴权服务，@陈重阳): v1.2.1 
- qingzhou-front(前端，@邹欣华): v1.3.x
- apigw-gateway : v1.1.4
- nsf-turbine(nsf监控服务 ，@ 张武) 2.5.1
- prometheus-nms(告警服务，@王添) v1.4.0

# v1.1.7
**(2019-08-28)**

## Branch:
- productization-20190725

## Features:
- CLDNSF-2871 【API网关】统一授权管理
- CLDNSF-3095 【API网关-优化】G-Portal和网关优化及公有云Swagger导入优化
- CLDNSF-2982 【API网关】性能测试优化三期
- CLDNSF-3093 【API网关】分流逻辑性能优化
- CLDNSF-2955 NSF知识库重构--网关同步知识库修改
- CLDNSF-2970 【API网关】监控，统计告警功能 二期
- 网关支持HTTP转gRPC (CLDNSF-3008、CLDNSF-2781、CLDNSF-2873)

## BugFix:  
- 修复总览页：API调用数据和服务调用数据不一致的的情况

## dependencies 
- nsf-meta(nsf元数据服务，@张子豪): 2.5.9
- service-auth(鉴权服务，@陈重阳): v1.2.1 
- qingzhou-front(前端，@邹欣华): v1.3.x
- apigw-gateway : v1.1.4
- nsf-turbine(nsf监控服务 ，@ 张武) 2.5.1
- prometheus-nms(告警服务，@王添) v1.4.0


# v1.1.6
**(2019-08-27)**

## Branch:
- productization-20190725

## Features:
- CLDNSF-2871 【API网关】统一授权管理
- CLDNSF-3095 【API网关-优化】G-Portal和网关优化及公有云Swagger导入优化
- CLDNSF-2982 【API网关】性能测试优化三期
- CLDNSF-3093 【API网关】分流逻辑性能优化
- CLDNSF-2955 NSF知识库重构--网关同步知识库修改
- CLDNSF-2970 【API网关】监控，统计告警功能 二期
- 网关支持HTTP转gRPC (CLDNSF-3008、CLDNSF-2781、CLDNSF-2873)

## BugFix:  
- dubbo接口的修改记录；
- 创建网关时，允许网关地址为http://或https://开头；

## dependencies 
- nsf-meta(nsf元数据服务，@张子豪): 2.5.9
- service-auth(鉴权服务，@陈重阳): v1.2.1 
- qingzhou-front(前端，@邹欣华): v1.3.x
- apigw-gateway : v1.1.4
- nsf-turbine(nsf监控服务 ，@ 张武) 2.5.1
- prometheus-nms(告警服务，@王添) v1.4.0


# v1.1.5
**(2019-08-23)**

## Branch:
- productization-20190725

## Features:
- CLDNSF-2871 【API网关】统一授权管理
- CLDNSF-3095 【API网关-优化】G-Portal和网关优化及公有云Swagger导入优化
- CLDNSF-2982 【API网关】性能测试优化三期
- CLDNSF-3093 【API网关】分流逻辑性能优化
- CLDNSF-2955 NSF知识库重构--网关同步知识库修改
- CLDNSF-2970 【API网关】监控，统计告警功能 二期
- 网关支持HTTP转gRPC (CLDNSF-3008、CLDNSF-2781、CLDNSF-2873)

## BugFix:  
- 创建dubbo服务时，默认创建2种状态码

## dependencies 
- nsf-meta(nsf元数据服务，@张子豪): 2.5.9
- service-auth(鉴权服务，@陈重阳): v1.2.1 
- qingzhou-front(前端，@邹欣华): v1.3.x
- apigw-gateway : v1.1.4
- nsf-turbine(nsf监控服务 ，@ 张武) 2.5.1
- prometheus-nms(告警服务，@王添) v1.4.0

# v1.1.4
**(2019-08-21)**

## Branch:
- productization-20190725

## Features:
- CLDNSF-2871 【API网关】统一授权管理
- CLDNSF-3095 【API网关-优化】G-Portal和网关优化及公有云Swagger导入优化
- CLDNSF-2982 【API网关】性能测试优化三期
- CLDNSF-3093 【API网关】分流逻辑性能优化
- CLDNSF-2955 NSF知识库重构--网关同步知识库修改
- CLDNSF-2970 【API网关】监控，统计告警功能 二期
- 网关支持HTTP转gRPC (CLDNSF-3008、CLDNSF-2781、CLDNSF-2873)

## BugFix:  
- CLDNSF-3257 【API网关】bugbash问题修复(审计bug修复)
- CLDNSF-3156 【API网关】数据库配置问题修复
- 注册中心部署方案调整：网关需要支持多注册中心且初始化环境时，NSFRegistry
- nsf-turbine部署方案调整：读写地址分离，网关向两个turbine实例写入

## dependencies 
- nsf-meta(nsf元数据服务，@张子豪): 2.5.9
- service-auth(鉴权服务，@陈重阳): v1.2.1 
- qingzhou-front(前端，@邹欣华): v1.3.21
- apigw-gateway : v1.1.4
- nsf-turbine(nsf监控服务 ，@ 张武) 2.5.0
- prometheus-nms(告警服务，@王添) v1.4.0

# v1.1.3
**(2019-08-13)**

## Branch:
- productization-20190725

## Features:
- CLDNSF-2871 【API网关】统一授权管理
- CLDNSF-3095 【API网关-优化】G-Portal和网关优化及公有云Swagger导入优化
- CLDNSF-2982 【API网关】性能测试优化三期
- CLDNSF-3093 【API网关】分流逻辑性能优化
- CLDNSF-2955 NSF知识库重构--网关同步知识库修改
- CLDNSF-2970 【API网关】监控，统计告警功能 二期
- 网关支持HTTP转gRPC (CLDNSF-3008、CLDNSF-2781、CLDNSF-2873)

## BugFix:  
- CLDNSF-3257 【API网关】bugbash问题修复(审计bug修复)

## dependencies 
- nsf-meta(nsf元数据服务，@张子豪): 2.5
- service-auth(鉴权服务，@陈重阳): v1.2.0 
- qingzhou-front(前端，@邹欣华): v1.3.10 
- apigw-gateway : v1.1.2
- nsf-turbine(nsf监控服务 ，@ 张武)2.5.0
- prometheus-nms(告警服务，@王添)1.4.0

# v1.1.2
**(2019-08-12)**

## Branch:
- productization-20190725

## Features:
- CLDNSF-2871 【API网关】统一授权管理
- CLDNSF-3095 【API网关-优化】G-Portal和网关优化及公有云Swagger导入优化
- CLDNSF-2982 【API网关】性能测试优化三期
- CLDNSF-3093 【API网关】分流逻辑性能优化
- CLDNSF-2955 NSF知识库重构--网关同步知识库修改
- CLDNSF-2970 【API网关】监控，统计告警功能 二期
- 网关支持HTTP转gRPC (CLDNSF-3008、CLDNSF-2781、CLDNSF-2873)

## BugFix:  
- CLDNSF-3257 【API网关】bugbash问题修复

## dependencies 
- nsf-meta(nsf元数据服务，@张子豪): 2.5
- service-auth(鉴权服务，@陈重阳): v1.2.0 
- qingzhou-front(前端，@邹欣华): v1.3.10 
- apigw-gateway : v1.1.2
- nsf-turbine(nsf监控服务 ，@ 张武)2.5.0
- prometheus-nms(告警服务，@王添)1.4.0


# v1.1.1
**(2019-08-05)**

## Branch:
- productization-20190725

## Features:
- CLDNSF-2871 【API网关】统一授权管理
- CLDNSF-3095 【API网关-优化】G-Portal和网关优化及公有云Swagger导入优化
- CLDNSF-2982 【API网关】性能测试优化三期
- CLDNSF-3093 【API网关】分流逻辑性能优化
- CLDNSF-2955 NSF知识库重构--网关同步知识库修改
- CLDNSF-2970 【API网关】监控，统计告警功能 二期
- 网关支持HTTP转gRPC (CLDNSF-3008、CLDNSF-2781、CLDNSF-2873)

## BugFix:  
- CLDNSF-3326 服务、API指标页面有些图表最后两个点显示时间一样修复
- CLDNSF-3322 修改描述信息未返回的问题
- 优化同步知识库功能未闭环--模型不同步问题
- 增加webservice导入导出相关逻辑
- 监控数据提供初始值
- dubbo参数限制长度，监控数据提供初始值
- 修复wsdl url能更新的问题
- 大盘精确显示服务发布的时间


## dependencies 
- nsf-meta(nsf元数据服务，@张子豪): 2.5
- service-auth(鉴权服务，@陈重阳): v1.2.0 
- qingzhou-front(前端，@邹欣华): v1.2.3 
- apigw-gateway : v1.1.0
- nsf-turbine(nsf监控服务 ，@ 张武)2.5.0
- prometheus-nms(告警服务，@王添)1.4.0


# v1.1.0
**(2019-07-29)**

## Branch:
- productization-20190725

## Features:
- CLDNSF-2871 【API网关】统一授权管理
- CLDNSF-3095 【API网关-优化】G-Portal和网关优化及公有云Swagger导入优化
- CLDNSF-2982 【API网关】性能测试优化三期
- CLDNSF-3093 【API网关】分流逻辑性能优化
- CLDNSF-2955 NSF知识库重构--网关同步知识库修改
- CLDNSF-2970 【API网关】监控，统计告警功能 二期
- 网关支持HTTP转gRPC (CLDNSF-3008、CLDNSF-2781、CLDNSF-2873)

## BugFix:  
- 无

## dependencies 
- nsf-meta(nsf元数据服务，@张子豪): 2.5
- service-auth(鉴权服务，@陈重阳): v1.2.0 
- qingzhou-front(前端，@邹欣华): v1.2.3 
- apigw-gateway : v1.1.0
- nsf-turbine(nsf监控服务 ，@ 张武)2.5.0
- prometheus-nms(告警服务，@王添)1.4.0

# v1.0.3
**(2019-07-04)**

## Branch:
- productization-20190613

## Features:
- 暂无

## BugFix:  
- 下线接口时，解绑流控策略
- 告警的GwEnv改为GwName

## dependencies 
- nsf-meta(nsf元数据服务，@张子豪): v1.3.0 
- service-auth(鉴权服务，@陈重阳): v1.1.0 
- nsf-front(前端，@邹欣华): v1.0.0 
- apigw-gateway : v1.0.2

# v1.0.2
**(2019-06-28)**

## Branch:
- productization-20190613

## Features:
- 暂无

## BugFix:  
- 发布dubbo API时取消BodyMap中encoding apiName
- dubbo/webservice api 创建 自动生成statusCode时添加Description

## dependencies 
- nsf-meta(nsf元数据服务，@张子豪): v1.3.0 
- service-auth(鉴权服务，@陈重阳): v1.1.0 
- nsf-front(前端，@邹欣华): v1.0.0 
- apigw-gateway : v1.0.2

# v1.0.1
**(2019-06-24)**

## Branch:
- productization-20190613

## Features:
- 暂无

## BugFix:  
- 参数分流-名单分流-正则表达式:只支持一个正则表达式，正则表达式中允许","

## dependencies 
- nsf-meta(nsf元数据服务，@张子豪): v1.3.0 
- service-auth(鉴权服务，@陈重阳): v1.1.0 
- nsf-front(前端，@邹欣华): v1.0.0 
- apigw-gateway : v1.0.2

# v1.0.0
**(2019-06-13)**

## Branch:
- productization-20190613

## Features:
- 新版参数分流：支持按标签、版本、应用名称分流（CLDNSF-2830）
- 支持dubbo服务调用审计 （CLDNSF-2778）

## BugFix:  
- 暂无

## dependencies 
- nsf-meta(nsf元数据服务，@张子豪): v1.3.0 
- service-auth(鉴权服务，@陈重阳): v1.1.0 
- nsf-front(前端，@邹欣华): v1.0.0 
- apigw-gateway : v1.0.2




