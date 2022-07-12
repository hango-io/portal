### API Gateway Portal

#### 简介

API网关控制中心

####注意事项
#####权限相关
1、开发人员需要关注新引入的功能是否需要在轻舟平台进行鉴权，并按照
https://g.hz.netease.com/nsf/doc/tree/master/API%E7%BD%91%E5%85%B3/%E8%AE%BE%E8%AE%A1%E6%96%87%E6%A1%A3/%E6%9D%83%E9%99%90
文档相关，对权限进行划分。主要包括：运维管理员是否拥有该操作权限、项目管理员、项目普通用户分别是否拥有该操作权限，更新API网关权限.xsl以及api_gateway_skiff.sql

代码层次通过filter进行实现，只需要对需要鉴权的操作，按照RBAC模型整理相关操作资源以及操作。
修改ActtionPermissionEnum，加入新的RBAC模型下的相关操作即可。

#####审计相关
1、审计操作只对关键增、删、改操作进行审计，网关侧不对查询操作进行审计
   同时，需要注意对UserPermisson.getAccountId方法的使用。网关不对accountId进行强校验
   只有通过权限认证的相关接口，才会有确定的AccountId。因此慎用accountId。不进行鉴权的接口，不保证AccountId会有确定的值