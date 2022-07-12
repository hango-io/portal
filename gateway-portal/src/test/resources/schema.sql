CREATE TABLE `apigw_envoy_health_check_rule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `create_time` bigint(20) NOT NULL COMMENT '创建时间',
  `update_time` bigint(20) NOT NULL COMMENT '修改时间',
  `service_id` bigint(20) NOT NULL COMMENT '服务id',
  `gw_id` bigint(20) NOT NULL COMMENT '网关id',
  `active_switch` tinyint(4) NOT NULL COMMENT '主动检查开关，1表示开启，0表示关闭',
  `path` varchar(255) DEFAULT '' COMMENT '接口path',
  `timeout` int(11) DEFAULT NULL COMMENT '超时时间，单位毫秒',
  `expected_statuses` varchar(255) DEFAULT '' COMMENT '健康状态码集合 ',
  `healthy_interval` int(11) DEFAULT NULL COMMENT '健康实例检查间隔，单位毫秒 ',
  `healthy_threshold` int(11) DEFAULT NULL COMMENT '健康阈值，单位次',
  `unhealthy_interval` int(11) DEFAULT NULL COMMENT '异常实例检查间隔，单位毫秒',
  `unhealthy_threshold` int(11) DEFAULT NULL COMMENT '异常阈值，单位次',
  `passive_switch` tinyint(4) NOT NULL COMMENT '被动检查开关，1表示开启，0表示关闭',
  `consecutive_errors` int(11) DEFAULT NULL COMMENT '连续失败次数',
  `base_ejection_time` int(11) DEFAULT NULL COMMENT '驱逐时间，单位毫秒',
  `max_ejection_percent` tinyint(4) DEFAULT NULL COMMENT '最多可驱逐的实例比',
  `min_health_percent` tinyint(4) DEFAULT NULL COMMENT '实例最小健康百分比',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_service_gw_id` (`service_id`,`gw_id`),
  KEY `index_service_gw_id` (`service_id`,`gw_id`)
);

CREATE TABLE `apigw_envoy_plugin_binding` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `plugin_type` varchar(255) NOT NULL COMMENT '绑定的插件类型，全局唯一，如：RateLimiter、WhiteList等',
  `binding_object_type` varchar(255) NOT NULL COMMENT '插件所绑定的对象类型，包含路由规则、服务等',
  `binding_object_id` varchar(255) NOT NULL COMMENT '插件所绑定的对象的唯一标识，与binding_object_type共同决定某一具体对象',
  `plugin_configuration` text NOT NULL COMMENT '插件配置',
  `create_time` bigint(20) NOT NULL COMMENT '最新绑定时间，时间戳格式，精确到毫秒',
  `update_time` bigint(20) NOT NULL COMMENT '绑定（配置）修改时间，时间戳格式，精确到毫秒',
  `gw_id` bigint(11) NOT NULL COMMENT '对象-插件绑定关系作用的网关id',
  `project_id` bigint(11) NOT NULL COMMENT '插件绑定关系所属项目id',
  `plugin_priority` bigint(11) NOT NULL COMMENT '插件优先级',
  `binding_status` varchar(127) NOT NULL DEFAULT 'enable' COMMENT '插件绑定关系状态，enable/disable',
  `template_id` bigint(20) DEFAULT '0' COMMENT '关联插件模板id',
  `template_version` bigint(20) DEFAULT '0' COMMENT '关联插件模板版本号',
  PRIMARY KEY (`id`)
);

CREATE TABLE `apigw_envoy_plugin_info` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `plugin_name` varchar(255) NOT NULL COMMENT '插件名称，用于前端展示',
  `plugin_type` varchar(255) NOT NULL COMMENT '插件类型，全局唯一，如：RateLimiter、WhiteList等',
  `author` varchar(255) NOT NULL COMMENT '插件开发着，若为system则代表系统预置，系统预置的插件不允许修改',
  `create_time` bigint(20) NOT NULL COMMENT '插件创建时间，时间戳格式，精确到毫秒',
  `update_time` bigint(20) NOT NULL COMMENT '插件最近时间，时间戳格式，精确到毫秒',
  `plugin_scope` varchar(255) NOT NULL COMMENT '插件作用范围，可选值为：virtual host、route rule、service',
  `instruction_for_use` text NOT NULL COMMENT '插件使用说明，用于前端展示，指导用户使用',
  `plugin_schema` text NOT NULL COMMENT '插件表单schema，用于前端展示',
  `plugin_handler` text NOT NULL COMMENT '插件逻辑，用于api-plane生成XDS及Envoy数据面使用',
  `plugin_priority` bigint(11) NOT NULL COMMENT '插件优先级，数字越小优先级越高',
  PRIMARY KEY (`id`)
);

CREATE TABLE `apigw_envoy_plugin_template` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `create_time` bigint(20) NOT NULL COMMENT '模板创建时间，时间戳格式，精确到毫秒',
  `update_time` bigint(20) NOT NULL COMMENT '模板最后更新时间，时间戳格式，精确到毫秒',
  `plugin_type` varchar(255) NOT NULL COMMENT '绑定的插件类型，全局唯一，如：RateLimiter、WhiteList等',
  `plugin_configuration` text NOT NULL COMMENT '插件配置',
  `project_id` bigint(11) NOT NULL COMMENT '插件绑定关系所属项目id',
  `template_version` bigint(10) NOT NULL COMMENT '插件模板版本',
  `template_name` varchar(255) NOT NULL COMMENT '插件模板名称',
  `template_notes` varchar(255) DEFAULT NULL COMMENT '插件模板',
  PRIMARY KEY (`id`)
);

CREATE TABLE `apigw_envoy_route_rule` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `service_id` varchar(11) DEFAULT NULL COMMENT '服务id（路由规则所属的服务id）',
  `route_rule_name` varchar(255) NOT NULL COMMENT '路由规则名称',
  `uri` text,
  `method` varchar(1024) DEFAULT NULL COMMENT '匹配方法列表，GETPOSTPUTDELETEHEAD等',
  `header` text COMMENT '匹配header列表',
  `query_param` text COMMENT '匹配路由queryParam列表',
  `host` varchar(1024) DEFAULT NULL COMMENT '路由host列表',
  `priority` bigint(11) DEFAULT NULL COMMENT '路由规则优先级priority',
  `orders` bigint(11) DEFAULT NULL COMMENT '路由规则orders，与pilot交互',
  `project_id` bigint(11) DEFAULT NULL COMMENT '路由规则所属项目id',
  `publish_status` int(4) DEFAULT '0' COMMENT '路由规则发布状态，0未发布，1已发布',
  `create_time` bigint(20) NOT NULL DEFAULT '0' COMMENT '创建时间，时间戳格式，精确到毫秒',
  `update_time` bigint(20) NOT NULL DEFAULT '0' COMMENT '更新时间，时间戳格式，精确到毫秒',
  `description` varchar(255) DEFAULT NULL COMMENT '路由规则描述信息',
  `route_rule_source` varchar(255) DEFAULT NULL COMMENT '路由来源Gateway/NSF',
  `header_operation` varchar(1024) DEFAULT NULL COMMENT 'l路由头处理',
  PRIMARY KEY (`id`)
);

CREATE TABLE `apigw_envoy_route_rule_proxy` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `route_rule_id` bigint(11) NOT NULL COMMENT '路由规则id',
  `gw_id` bigint(11) NOT NULL COMMENT '路由规则发布到的网关id',
  `destination_services` varchar(1024) NOT NULL COMMENT '目的地址信息',
  `project_id` bigint(11) NOT NULL COMMENT '路由规则发布所属项目id',
  `priority` bigint(11) DEFAULT NULL COMMENT '路由规则优先级',
  `orders` bigint(11) DEFAULT NULL COMMENT '路由规则orders，与pilot交互',
  `create_time` bigint(20) NOT NULL DEFAULT '0' COMMENT '路由规则发布时间，时间戳格式，精确到毫秒',
  `update_time` bigint(20) NOT NULL DEFAULT '0' COMMENT '路由规则更新时间，时间戳格式，精确到毫秒',
  `service_id` bigint(11) DEFAULT NULL COMMENT '路由规则发布关联的服务id',
  `enable_state` varchar(10) NOT NULL DEFAULT 'enable' COMMENT '路由规则使能状态',
  `hosts` text COMMENT 'vs中对应的hosts信息',
  `timeout` bigint(200) DEFAULT '60000',
  `http_retry` text,
  `uri` varchar(1024) DEFAULT NULL COMMENT '路由uri，与匹配模式共同作用',
  `method` varchar(1024) DEFAULT NULL COMMENT '匹配方法列表，GETPOSTPUTDELETEHEAD等',
  `header` text COMMENT '匹配header列表',
  `query_param` text COMMENT '匹配路由queryParam列表',
  `host` varchar(1024) DEFAULT NULL COMMENT '路由host列表',
  PRIMARY KEY (`id`),
  KEY `idx_gw_id` (`gw_id`),
  KEY `idx_route_rule_id` (`route_rule_id`)
);

CREATE TABLE `apigw_envoy_service_info` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `create_time` bigint(11) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(11) DEFAULT NULL COMMENT '更新时间',
  `service_name` varchar(255) NOT NULL COMMENT '服务名称',
  `publish_status` int(4) DEFAULT '0' COMMENT '发布状态，0未发布，1已发布',
  `project_id` bigint(11) DEFAULT NULL COMMENT '服务所属项目id',
  `contact` varchar(255) DEFAULT NULL COMMENT '服务联系人',
  `description` varchar(255) DEFAULT NULL COMMENT '服务描述信息',
  `service_type` varchar(11) DEFAULT 'http' COMMENT '服务类型',
  PRIMARY KEY (`id`)
);

CREATE TABLE `apigw_envoy_service_protobuf` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `create_date` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `modify_date` bigint(20) DEFAULT NULL COMMENT '修改时间',
  `service_id` bigint(11) unsigned NOT NULL COMMENT '服务id',
  `pb_file_name` varchar(255) NOT NULL DEFAULT '' COMMENT '上传的pb文件名',
  `pb_file_content` text NOT NULL COMMENT 'pb文件内容',
  `pb_service_list` text NOT NULL COMMENT 'pb文件包含的服务列表',
  PRIMARY KEY (`id`),
  KEY `service_id` (`service_id`)
);

CREATE TABLE `apigw_envoy_service_protobuf_proxy` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `create_date` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `modify_date` bigint(20) DEFAULT NULL COMMENT '修改时间',
  `service_id` bigint(20) NOT NULL COMMENT '服务id',
  `gw_id` bigint(20) NOT NULL COMMENT '网关id',
  `pb_file_name` varchar(255) NOT NULL DEFAULT '' COMMENT '上传的pb文件名',
  `pb_file_content` text NOT NULL COMMENT 'pb文件内容',
  `pb_service_list` text NOT NULL COMMENT 'pb文件包含的服务列表',
  PRIMARY KEY (`id`)
);

CREATE TABLE `apigw_envoy_service_proxy` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `service_id` bigint(11) NOT NULL COMMENT '服务元数据id',
  `create_time` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `update_time` bigint(20) DEFAULT NULL COMMENT '更新时间',
  `code` varchar(255) NOT NULL COMMENT '服务元数据标识',
  `publish_protocol` varchar(10) DEFAULT 'http' COMMENT '发布服务，服务协议，http/https,默认为http',
  `backend_service` text COMMENT '发布关联真实网关服务',
  `publish_type` varchar(255) DEFAULT NULL COMMENT '发布策略，STATIC/DYNAMIC',
  `project_id` bigint(11) DEFAULT '0' COMMENT '发布所属项目id',
  `gw_id` bigint(11) NOT NULL COMMENT '服务发布所属网关id',
  `load_balancer` varchar(127) NOT NULL DEFAULT 'ROUND_ROBIN' COMMENT '负载均衡',
  `subsets` text COMMENT '版本集合',
  `registry_center_addr` varchar(255) DEFAULT NULL COMMENT '注册中心地址',
  `registry_center_type` varchar(255) DEFAULT NULL COMMENT '注册中心类型Consul/Kubernetes/Eureka/Zookeeper，DYNAMIC时必填，默认Kubernetes',
  `traffic_policy` text COMMENT '负载均衡和连接池配置',
  PRIMARY KEY (`id`)
);

CREATE TABLE `apigw_envoy_strategy` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `plugin_id` bigint(11) NOT NULL COMMENT '策略所属插件在插件表的id',
  `plugin_type` varchar(255) NOT NULL COMMENT '策略所属插件类型',
  `strategy_name` varchar(255) NOT NULL COMMENT '策略名称',
  `create_time` bigint(20) NOT NULL COMMENT '策略创建时间，时间戳格式，精确到毫秒',
  `update_time` bigint(20) NOT NULL COMMENT '策略更新时间，时间戳个实例，精确到毫秒',
  `config` text NOT NULL COMMENT '策略配置',
  PRIMARY KEY (`id`)
);

CREATE TABLE `apigw_envoy_virtual_host_info` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `project_id` bigint(20) NOT NULL COMMENT '项目id',
  `gw_id` bigint(11) NOT NULL COMMENT '虚拟网关所在物理网关id',
  `hosts` text COMMENT '虚拟网关中的域名列表',
  `virtual_host_code` varchar(255) NOT NULL COMMENT 'vh唯一标识，${projectCode}-${projectId}-{gwId}',
  `create_time` bigint(20) NOT NULL COMMENT 'vh创建时间',
  `update_time` bigint(20) NOT NULL COMMENT 'vh更新时间',
  `bind_type` VARCHAR(10) NOT NULL DEFAULT 'host',
  `projects` TEXT DEFAULT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `apigw_gportal_api` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `create_date` bigint(11) DEFAULT NULL COMMENT '创建时间',
  `modify_date` bigint(20) DEFAULT NULL,
  `api_name` varchar(255) NOT NULL COMMENT 'api名称',
  `api_path` varchar(255) NOT NULL COMMENT 'api路径',
  `api_method` varchar(255) NOT NULL COMMENT '方法',
  `description` text,
  `type` varchar(255) NOT NULL COMMENT '风格,Action | Restful',
  `service_id` bigint(20) NOT NULL COMMENT '服务id',
  `status` varchar(255) NOT NULL DEFAULT '0' COMMENT 'API状态，0:待发布 1:已发布 2:已下线',
  `regex` varchar(255) DEFAULT NULL,
  `document_status_id` bigint(20) DEFAULT '0' COMMENT 'API文档状态id',
  `request_example_value` text,
  `response_example_value` text,
  `alias_name` varchar(128) DEFAULT NULL COMMENT 'api英文标识，用于SDK生成',
  `project_id` bigint(11) DEFAULT NULL COMMENT '基于项目隔离，项目id',
  `sync_status` tinyint(2) DEFAULT '0' COMMENT '同步状态 0-本地数据 1-同步 2-失步',
  `ext_api_id` bigint(20) DEFAULT NULL COMMENT '外部apiId',
  `swagger_sync` tinyint(2) DEFAULT '0' COMMENT 'swagger同步状态 0-本地数据 1-同步 2-失步',
  PRIMARY KEY (`id`)
);

CREATE TABLE `apigw_gportal_api_document_status` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `status` varchar(255) NOT NULL COMMENT 'API文档状态',
  PRIMARY KEY (`id`,`status`)
);

INSERT INTO `apigw_gportal_api_document_status` (`id`, `status`)
VALUES
	(1,'开发中'),
	(2,'联调中'),
	(3,'提测中'),
	(4,'已上线');

CREATE TABLE `apigw_gportal_api_model` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `create_date` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `modify_date` bigint(20) DEFAULT NULL COMMENT '修改时间',
  `model_name` varchar(255) NOT NULL COMMENT '模型名称',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `type` tinyint(11) DEFAULT NULL COMMENT '数据模型的类型',
  `format` tinyint(11) DEFAULT NULL COMMENT '数据模型的类别',
  `service_id` bigint(20) NOT NULL COMMENT '服务Id',
  `swagger_sync` tinyint(2) DEFAULT '0' COMMENT 'swagger同步状态 0-本地数据 1-同步 2-失步',
  `project_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `index_model_id` (`id`)
);

CREATE TABLE `apigw_gportal_api_proxy` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `create_date` bigint(20) NOT NULL COMMENT '创建时间',
  `modify_date` bigint(20) DEFAULT NULL,
  `api_id` bigint(11) NOT NULL COMMENT 'api id',
  `gw_id` bigint(11) NOT NULL,
  `service_id` bigint(11) DEFAULT NULL,
  `traffic_control_policy_id` bigint(11) DEFAULT '0' COMMENT '流控策略Id',
  `time_range` varchar(255) DEFAULT NULL COMMENT 'API不可用时间段',
  `project_id` bigint(11) DEFAULT NULL COMMENT '基于项目隔离，服务项目id',
  PRIMARY KEY (`id`)
);

CREATE TABLE `apigw_gportal_api_status_code` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `create_date` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `modify_date` bigint(20) DEFAULT NULL COMMENT '修改时间',
  `error_code` varchar(255) DEFAULT NULL,
  `message` varchar(255) DEFAULT NULL,
  `status_code` bigint(20) DEFAULT NULL COMMENT 'http status code',
  `object_id` bigint(20) DEFAULT NULL,
  `type` varchar(255) NOT NULL,
  `description` text COMMENT '描述',
  PRIMARY KEY (`id`),
  KEY `index_api_id` (`object_id`)
);

CREATE TABLE `apigw_gportal_body_param` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `create_date` bigint(11) DEFAULT NULL COMMENT '创建时间',
  `modify_date` bigint(20) DEFAULT NULL,
  `api_id` bigint(11) NOT NULL COMMENT 'api id',
  `param_name` varchar(255) NOT NULL COMMENT '参数名称',
  `required` varchar(20) NOT NULL DEFAULT '0' COMMENT '是否必输项, 1表示必须输入，0表示非必须',
  `def_value` varchar(255) DEFAULT NULL COMMENT '默认值',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `type` varchar(255) NOT NULL COMMENT '区分REQUEST|RESPONSE',
  `param_type_id` bigint(20) DEFAULT NULL,
  `array_data_type_id` bigint(20) DEFAULT NULL,
  `association_type` varchar(63) DEFAULT 'NORMAL' COMMENT '关联类型 NORMAL/DUBBO',
  PRIMARY KEY (`id`)
);

CREATE TABLE `apigw_gportal_dubbo_param` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `create_date` bigint(11) DEFAULT NULL COMMENT '创建时间',
  `modify_date` bigint(20)  DEFAULT NULL COMMENT '修改时间',
  `api_id` bigint(11) NOT NULL COMMENT 'api id',
  `param_name` varchar(255) NOT NULL COMMENT '参数名称',
  `required` varchar(20) NOT NULL DEFAULT '0' COMMENT '是否必输项, 1表示必须输入，0表示非必须',
  `def_value` varchar(255) DEFAULT NULL COMMENT '默认值',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `dubbo_type` varchar(255) NOT NULL COMMENT '区分DubboInterface|DubboMethod|DubboVersion|DubboGroup|DubboParam|DubboResponse',
  `param_type_id` bigint(20) DEFAULT NULL,
  `array_data_type_id` bigint(20) DEFAULT NULL,
  `param_sort` bigint(4) NOT NULL DEFAULT '0' COMMENT '参数序号',
  `param_alias` varchar(255) DEFAULT NULL COMMENT '参数别名',
  PRIMARY KEY (`id`)
);

CREATE TABLE `apigw_gportal_gateway_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `gw_name` varchar(255) NOT NULL COMMENT '网关名称，区分G0和G1',
  `gw_addr` varchar(255) NOT NULL COMMENT '网关IP地址',
  `description` varchar(255) DEFAULT NULL,
  `create_date` bigint(20) DEFAULT NULL,
  `modify_date` bigint(20) DEFAULT NULL COMMENT '修改时间',
  `status` int(11) DEFAULT '1',
  `last_check_time` bigint(20) DEFAULT '0',
  `health_interface_path` varchar(255) DEFAULT NULL COMMENT '健康检查接口path',
  `mongo_addr` varchar(255) DEFAULT NULL COMMENT 'mongo addr地址',
  `project_id` text COMMENT '基于项目隔离，租户id',
  `env_id` varchar(128) DEFAULT NULL COMMENT '网关所属环境id',
  `auth_addr` varchar(128) DEFAULT NULL COMMENT '认证中心地址',
  `audit_datasource_switch` varchar(255) DEFAULT NULL COMMENT 'mongo审计数据源切换选项mongo/mysql',
  `mysql_addr` text COMMENT 'mysql addr 地址',
  `gw_uni_id` varchar(128) DEFAULT NULL COMMENT '网关英文标识',
  `metric_url` varchar(255) DEFAULT NULL COMMENT '网关监控地址',
  `api_plane_addr` varchar(255) DEFAULT NULL COMMENT 'envoy网关对应api_plane_addr地址',
  `gw_cluster_name` varchar(255) DEFAULT NULL COMMENT 'envoy网关对应的网关集群名称',
  `gw_type` varchar(20) DEFAULT 'g0' COMMENT '网关类型，api-gateway/envoy-proxy',
  `audit_db_config` varchar(1024) DEFAULT NULL COMMENT '审计数据源配置',
  `prom_addr` varchar(255) DEFAULT NULL COMMENT 'Prometheus地址',
  PRIMARY KEY (`id`)
);

CREATE TABLE `apigw_gportal_grpc_param` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `create_date` bigint(11) DEFAULT NULL COMMENT '创建时间',
  `modify_date` bigint(11) DEFAULT NULL COMMENT '修改时间',
  `api_id` bigint(11) NOT NULL COMMENT 'api id',
  `service_id` bigint(11) NOT NULL COMMENT 'service id',
  `pb_name` varchar(255) NOT NULL DEFAULT '' COMMENT 'pb文件别名',
  `pb_package_name` varchar(255) NOT NULL DEFAULT '' COMMENT 'package名称',
  `pb_service_name` varchar(255) NOT NULL DEFAULT '' COMMENT 'service名称',
  `pb_method_name` varchar(255) NOT NULL DEFAULT '' COMMENT 'method名称',
  PRIMARY KEY (`id`),
  KEY `api_id` (`api_id`),
  KEY `pb_name` (`pb_name`)
);

CREATE TABLE `apigw_gportal_header_param` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `create_date` bigint(11) DEFAULT NULL COMMENT '创建时间',
  `modify_date` bigint(20) DEFAULT NULL,
  `api_id` bigint(20) NOT NULL COMMENT 'api id',
  `param_name` varchar(255) NOT NULL COMMENT '参数名称,包含自定义参数以及原生的Header提供的类型,如content-type',
  `param_value` varchar(255) DEFAULT NULL COMMENT '参数值',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `type` varchar(255) NOT NULL COMMENT '区分REQUEST|RESPONSE',
  PRIMARY KEY (`id`)
);

CREATE TABLE `apigw_gportal_meta_history` (
  `id` bigint(20) NOT NULL COMMENT '元数据历史ID',
  `meta_type` varchar(255) NOT NULL COMMENT '元数据类型 服务/路由',
  `meta_id` bigint(20) NOT NULL COMMENT '元数据ID',
  `meta_name` varchar(255) NOT NULL COMMENT '元数据名称',
  `meta_tag` varchar(255) NOT NULL COMMENT '元数据标识',
  `project_id` bigint(20) NOT NULL COMMENT '项目ID',
  `gw_id` bigint(20) NOT NULL COMMENT '网关ID',
  `parent_id` bigint(20) DEFAULT NULL COMMENT '父ID，如果元数据为路由，父ID为服务ID，如果元数据为服务，父ID为空',
  `create_date` bigint(11) DEFAULT NULL COMMENT '创建时间',
  `modify_date` bigint(11) DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
);

CREATE TABLE `apigw_gportal_model_param` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `create_date` bigint(11) DEFAULT NULL COMMENT '创建时间',
  `modify_date` bigint(20) DEFAULT NULL,
  `model_id` varchar(255) NOT NULL COMMENT '模型名称',
  `param_name` varchar(255) NOT NULL COMMENT '参数名称',
  `param_type_id` bigint(11) NOT NULL COMMENT '模型中的参数Id,包含基本类型如String等和自定义类型',
  `array_data_type_id` bigint(20) DEFAULT NULL,
  `object_id` bigint(20) DEFAULT NULL,
  `def_value` varchar(255) DEFAULT NULL COMMENT '默认值',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `required` varchar(20) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `index_model_param_id` (`id`)
);

CREATE TABLE `apigw_gportal_operation_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `create_date` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `email` varchar(255) DEFAULT NULL COMMENT '邮箱',
  `object_id` bigint(20) DEFAULT NULL COMMENT '对象Id',
  `operation` text COMMENT '具体记录',
  `type` varchar(255) DEFAULT NULL COMMENT '对象类型',
  PRIMARY KEY (`id`),
  KEY `index_object_id` (`object_id`,`type`)
);

CREATE TABLE `apigw_gportal_param_object` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `create_date` bigint(11) DEFAULT NULL COMMENT '创建时间',
  `modify_date` bigint(11) DEFAULT NULL COMMENT '修改时间',
  `object_value` varchar(1024) DEFAULT NULL COMMENT '匿名类型取值，json字符串',
  PRIMARY KEY (`id`),
  KEY `index_param_object_id` (`id`)
);

CREATE TABLE `apigw_gportal_param_type` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `create_date` bigint(11) DEFAULT NULL COMMENT '创建时间',
  `modify_date` bigint(20) DEFAULT NULL,
  `param_type` varchar(255) NOT NULL COMMENT '参数类型,基本类型|自定义类型',
  `location` varchar(255) NOT NULL COMMENT '位置，取值为HEADER|BODY',
  `model_id` bigint(20) DEFAULT '0',
  PRIMARY KEY (`id`,`param_type`)
);

INSERT INTO `apigw_gportal_param_type` (`id`, `create_date`, `modify_date`, `param_type`, `location`, `model_id`)
VALUES
	(1, 1515548961293, 1515548961293, 'String', 'BODY', 0),
	(2, 1515548961293, 1515548961293, 'Content-Type', 'HEADER', 0),
	(3, 1515548961293, 1515548961293, 'Accept-Language', 'HEADER', 0),
	(4, 1515548961293, 1515548961293, 'Array', 'BODY', 0),
	(5, 1515548961293, 1515548961293, 'Boolean', 'BODY', 0),
	(6, 1515548961293, 1515548961293, 'File', 'BODY', 0),
	(7, 1515548961293, 1515548961293, 'Number', 'BODY', 0),
	(8, 1515548961293, 1515548961293, 'Object', 'BODY', 0),
	(9, 1515548961293, 1515548961293, 'Variable', 'BODY', 0),
	(10, 1515548961293, 1515548961293, 'Accept-Charset', 'HEADER', 0),
	(11, 1515548961293, 1515548961293, 'Accept', 'HEADER', 0),
	(12, 1515548961293, 1515548961293, 'Accept-Encoding', 'HEADER', 0),
	(13, 1515548961293, 1515548961293, 'Accept-Datetime', 'HEADER', 0),
	(14, 1515548961293, 1515548961293, 'Authorization', 'HEADER', 0),
	(15, 1515548961293, 1515548961293, 'Cache-Control', 'HEADER', 0),
	(16, 1515548961293, 1515548961293, 'Connection', 'HEADER', 0),
	(17, 1515548961293, 1515548961293, 'Cookie', 'HEADER', 0),
	(18, 1515548961293, 1515548961293, 'Content-Disposition', 'HEADER', 0),
	(19, 1515548961293, 1515548961293, 'Content-Length', 'HEADER', 0),
	(20, 1515548961293, 1515548961293, 'Content-MD5', 'HEADER', 0),
	(21, 1515548961293, 1515548961293, 'Date', 'HEADER', 0),
	(22, 1515548961293, 1515548961293, 'Expect', 'HEADER', 0),
	(23, 1515548961293, 1515548961293, 'From', 'HEADER', 0),
	(24, 1515548961293, 1515548961293, 'Host', 'HEADER', 0),
	(25, 1515548961293, 1515548961293, 'If-Match', 'HEADER', 0),
	(26, 1515548961293, 1515548961293, 'If-Modified-Since', 'HEADER', 0),
	(27, 1515548961293, 1515548961293, 'If-None-Match', 'HEADER', 0),
	(28, 1515548961293, 1515548961293, 'If-Range', 'HEADER', 0),
	(29, 1515548961293, 1515548961293, 'If-Unmodified-Since', 'HEADER', 0),
	(30, 1515548961293, 1515548961293, 'Max-Forwards', 'HEADER', 0),
	(31, 1515548961293, 1515548961293, 'Origin', 'HEADER', 0),
	(32, 1515548961293, 1515548961293, 'Pragma', 'HEADER', 0),
	(33, 1515548961293, 1515548961293, 'Proxy-Authorization', 'HEADER', 0),
	(34, 1515548961293, 1515548961293, 'Range', 'HEADER', 0),
	(35, 1515548961293, 1515548961293, 'Referer', 'HEADER', 0),
	(36, 1515548961293, 1515548961293, 'TE', 'HEADER', 0),
	(37, 1515548961293, 1515548961293, 'User-Agent', 'HEADER', 0),
	(38, 1515548961293, 1515548961293, 'Upgrade', 'HEADER', 0),
	(39, 1515548961293, 1515548961293, 'Via', 'HEADER', 0),
	(40, 1515548961293, 1515548961293, 'Warning', 'HEADER', 0),
	(41, 1515548961293, 1515548961293, 'X-Requested-With', 'HEADER', 0),
	(42, 1515548961293, 1515548961293, 'DNT', 'HEADER', 0),
	(43, 1515548961293, 1515548961293, 'X-Forwarded-For', 'HEADER', 0),
	(44, 1515548961293, 1515548961293, 'X-Forwarded-Host', 'HEADER', 0),
	(45, 1515548961291, 1515548961293, 'Int', 'BODY', 0),
	(46, 1515548961291, 1515548961293, 'Long', 'BODY', 0),
	(47, 1515548961291, 1515548961293, 'Double', 'BODY', 0),
	(48, 1515548961291, 1515548961293, 'Float', 'BODY', 0);

CREATE TABLE `apigw_gportal_plugin` (
  `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `create_date` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `modify_date` bigint(20) DEFAULT NULL COMMENT '修改时间',
  `gw_id` bigint(20) DEFAULT NULL COMMENT '网关实例id',
  `plugin_name` varchar(255) DEFAULT NULL COMMENT '插件名称',
  `plugin_version` varchar(255) DEFAULT NULL COMMENT '插件版本',
  `plugin_file_name` varchar(255) DEFAULT NULL COMMENT '上传的插件文件名称',
  `plugin_content` text COMMENT '插件内容',
  `plugin_variable` text COMMENT '插件变量',
  `plugin_status` int(11) DEFAULT NULL COMMENT '当前状态：1表示启用；0表示停用',
  `last_start_time` bigint(20) DEFAULT NULL COMMENT '最近一次启动时间点',
  `plugin_starting_time` bigint(11) DEFAULT NULL COMMENT '最近一次启动时长',
  `plugin_call_number` bigint(11) DEFAULT NULL COMMENT '被调用次数',
  PRIMARY KEY (`id`)
);

CREATE TABLE `apigw_gportal_registry_center` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '注册中心配置主键',
  `registry_type` varchar(255) NOT NULL COMMENT '注册中心类型',
  `registry_addr` varchar(255) NOT NULL COMMENT '注册中心地址',
  `registry_alias` varchar(255) DEFAULT NULL COMMENT '注册中心别名',
  `create_date` bigint(11) DEFAULT NULL COMMENT '创建时间',
  `modify_date` bigint(11) DEFAULT NULL COMMENT '修改时间',
  `project_id` bigint(11) DEFAULT NULL COMMENT '项目ID',
  `is_shared` tinyint(4) DEFAULT NULL COMMENT '是否共享 0-否 1-是',
  `gw_id` BIGINT(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `apigw_gportal_service` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `create_date` bigint(11) DEFAULT NULL COMMENT '创建时间',
  `modify_date` bigint(11) DEFAULT NULL COMMENT '修改时间',
  `display_name` varchar(255) NOT NULL,
  `service_name` varchar(255) DEFAULT NULL COMMENT '服务方名称',
  `contacts` varchar(255) DEFAULT NULL COMMENT '负责人',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `status` int(11) NOT NULL DEFAULT '0' COMMENT '状态，0表示未发布，1表示已发布',
  `health_interface_path` varchar(255) DEFAULT NULL COMMENT '健康检查接口path',
  `service_type` varchar(63) DEFAULT NULL COMMENT '服务类型',
  `project_id` bigint(11) DEFAULT NULL COMMENT '基于项目隔离，项目id',
  `wsdl_url` varchar(255) DEFAULT NULL COMMENT 'wsdl文件地址',
  `sync_status` tinyint(2) DEFAULT '0' COMMENT '同步状态 0-本地数据 1-同步 2-失步',
  `ext_service_id` bigint(20) DEFAULT NULL COMMENT '外部serviceId',
  PRIMARY KEY (`id`)
);

CREATE TABLE `apigw_gportal_service_instance` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `create_date` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `modify_date` bigint(20) DEFAULT NULL COMMENT '修改时间',
  `service_id` bigint(11) NOT NULL COMMENT '服务id',
  `gw_id` bigint(20) NOT NULL COMMENT '网关环境id',
  `service_addr` varchar(255) DEFAULT NULL COMMENT '服务实例地址',
  `status` int(11) unsigned NOT NULL DEFAULT '1' COMMENT '状态',
  `last_check_time` bigint(20) NOT NULL DEFAULT '0' COMMENT '最近一次检查时间',
  `registry_center_addr` varchar(511) DEFAULT NULL COMMENT '注册中心地址',
  PRIMARY KEY (`id`)
);

CREATE TABLE `apigw_gportal_service_lb` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `create_date` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `modify_date` bigint(20) DEFAULT NULL COMMENT '修改时间',
  `service_id` bigint(20) DEFAULT NULL,
  `gw_id` bigint(20) DEFAULT NULL,
  `service_addr` varchar(255) NOT NULL COMMENT '服务地址',
  `weight` int(11) NOT NULL COMMENT '权重',
  `status` int(11) DEFAULT '1',
  `last_check_time` bigint(20) DEFAULT '0',
  PRIMARY KEY (`id`)
);

CREATE TABLE `apigw_gportal_service_lb_rule` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `create_date` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `modify_date` bigint(20) DEFAULT NULL COMMENT '修改时间',
  `service_id` bigint(11) NOT NULL COMMENT '服务id',
  `gw_id` bigint(20) NOT NULL COMMENT '网关id',
  `shunt_way` varchar(255) NOT NULL DEFAULT '' COMMENT '分流方式',
  `instance_weight_list` text COMMENT '实例列表(含权重)',
  `param_shunt_type` varchar(255) DEFAULT '' COMMENT '分流方式(参数分流)',
  `param_type` varchar(255) DEFAULT '' COMMENT '参数类型',
  `param_name` varchar(255) DEFAULT '' COMMENT '参数名',
  `modulus_threshold` int(11) DEFAULT NULL COMMENT '取模阈值',
  `item_list` text COMMENT '名单列表',
  `regex` int(11) DEFAULT NULL COMMENT '是否是正则表达式',
  `instance_list` text COMMENT '实例列表',
  `rule_name` varchar(255) DEFAULT NULL COMMENT '规则名称',
  PRIMARY KEY (`id`)
);

CREATE TABLE `apigw_gportal_service_lb_rule_binding` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `rule_id` bigint(20) NOT NULL COMMENT '策略ID',
  `gw_id` bigint(20) NOT NULL COMMENT '网关ID',
  `service_id` bigint(20) NOT NULL COMMENT '服务ID',
  `binding_time` bigint(20) NOT NULL COMMENT '绑定时间',
  PRIMARY KEY (`id`)
);

CREATE TABLE `apigw_gportal_service_lb_rule_multi` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `create_date` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `modify_date` bigint(20) DEFAULT NULL COMMENT '修改时间',
  `service_id` bigint(11) NOT NULL COMMENT '服务id',
  `rule_name` varchar(255) DEFAULT NULL COMMENT '规则名称',
  `shunt_way` varchar(255) DEFAULT NULL COMMENT '分流方式：parameter(参数分流)和weight(权重分流)',
  `param_matching_mode` varchar(255) DEFAULT NULL COMMENT '匹配方式：modulusShunt(取模阈值)和listShunt(名单分流)',
  `param_type` varchar(255) DEFAULT NULL COMMENT '参数类型：Header、Query、Cookie',
  `param_name` varchar(255) DEFAULT NULL COMMENT '参数名称',
  `instance_type` varchar(255) DEFAULT NULL COMMENT '目标类型，分为Aoolicatuin(应用)、Ip、Version(版本)和Tag(标签)',
  `instance_list` text COMMENT '实例列表及其对应的参数取值',
  `color` varchar(255) DEFAULT NULL COMMENT 'NSF-流量染色-颜色配置',
  PRIMARY KEY (`id`)
);

CREATE TABLE `apigw_gportal_service_protobuf` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `create_date` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `modify_date` bigint(20) DEFAULT NULL COMMENT '修改时间',
  `service_id` bigint(11) unsigned NOT NULL COMMENT '服务id',
  `pb_name` varchar(255) NOT NULL DEFAULT '' COMMENT 'pb对应的别名',
  `pb_file_name` varchar(255) NOT NULL DEFAULT '' COMMENT '上传的pb文件名',
  `pb_file_content` text NOT NULL COMMENT 'pb文件内容',
  `desc_file_content` text NOT NULL COMMENT 'description文件内容',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `pb_status` int(11) NOT NULL DEFAULT '0' COMMENT '发布状态，1表示已发布，0表示未发布',
  PRIMARY KEY (`id`)
);

CREATE TABLE `apigw_gportal_service_protobuf_proxy` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `create_date` bigint(20) DEFAULT NULL COMMENT '创建时间',
  `modify_date` bigint(20) DEFAULT NULL COMMENT '修改时间',
  `pb_id` bigint(20) NOT NULL COMMENT 'pb文件id',
  `gw_id` bigint(20) NOT NULL COMMENT '网关id',
  PRIMARY KEY (`id`)
);

CREATE TABLE `apigw_gportal_service_proxy` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `create_date` bigint(20) NOT NULL COMMENT '创建时间',
  `modify_date` bigint(20) DEFAULT NULL,
  `service_id` bigint(11) NOT NULL COMMENT '服务名称',
  `gw_id` bigint(11) NOT NULL COMMENT '网关环境Id',
  `service_addr` text COMMENT '服务地址',
  `time_range` varchar(255) DEFAULT NULL,
  `class_name` varchar(255) DEFAULT NULL,
  `flow_replication_addr` varchar(255) DEFAULT NULL,
  `authentication` tinyint(4) DEFAULT '1',
  `shunt_way` varchar(255) DEFAULT NULL COMMENT '分流方式',
  `shunt_switch` int(11) DEFAULT '0' COMMENT '分流开关',
  `project_id` bigint(11) DEFAULT NULL COMMENT '基于项目隔离，服务项目id',
  `addr_acquire_strategy` varchar(64) DEFAULT NULL COMMENT '服务地址拉取策略',
  `registry_center_addr` varchar(255) DEFAULT NULL COMMENT '注册中心地址',
  `application_name` varchar(255) DEFAULT NULL COMMENT '应用名称',
  `transparent` tinyint(1) DEFAULT '0' COMMENT '服务透传标识，1代表透传，0代表不透传',
  `custom_header` varchar(4000) DEFAULT NULL COMMENT '自定义header',
  `registry_center_type` varchar(255) DEFAULT NULL COMMENT '注册中心类型',
  `adapt_service_name` int(4) DEFAULT '0' COMMENT '是否移除服务标识，采用path适配服务标识',
  PRIMARY KEY (`id`)
);

CREATE TABLE `apigw_gportal_traffic_control_binding` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `api_id` bigint(11) NOT NULL COMMENT '绑定接口的id',
  `policy_id` bigint(11) NOT NULL COMMENT '绑定策略的id',
  `binding_time` bigint(11) NOT NULL COMMENT '策略绑定的时间',
  `gw_id` bigint(11) NOT NULL COMMENT '策略作用网关id',
  `gateway_name` varchar(128) NOT NULL DEFAULT '' COMMENT '网关名称',
  `policy_name` varchar(128) NOT NULL DEFAULT '' COMMENT '策略名称',
  `project_id` varchar(20) DEFAULT NULL COMMENT '策略所属项目id',
  PRIMARY KEY (`id`)
);

CREATE TABLE `apigw_gportal_traffic_control_dimension` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `traffic_control_policy_id` bigint(11) NOT NULL COMMENT '维度所属策略id',
  `slot` bigint(11) NOT NULL COMMENT '流控窗口的大小',
  `unit` varchar(128) NOT NULL COMMENT '流控窗口的时间单位',
  `dimension_limit` bigint(11) NOT NULL COMMENT '该维度下的流量限制',
  `capacity` bigint(11) NOT NULL COMMENT '令牌桶的桶大小，即容忍的突发流量数',
  `dimension_type` varchar(64) NOT NULL COMMENT '维度类型，API维度为全局的，同时还支持IP、Header、cookie、queryString、path',
  `param_name` varchar(128) DEFAULT NULL COMMENT '参数名称，当为参数限流时必填',
  `match_mode` varchar(128) DEFAULT NULL COMMENT '匹配方式，正则表达式或名单取值',
  `param_value` varchar(2048) DEFAULT NULL COMMENT '参数取值',
  PRIMARY KEY (`id`)
);

CREATE TABLE `apigw_gportal_traffic_control_policy` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL COMMENT '策略名称',
  `create_time` bigint(20) NOT NULL COMMENT '策略创建时间',
  `update_time` bigint(20) NOT NULL COMMENT '策略更新时间',
  `project_id` varchar(20) DEFAULT NULL COMMENT '策略所属项目id',
  PRIMARY KEY (`id`)
);

CREATE TABLE `apigw_gportal_webservice_param` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `create_date` bigint(11) DEFAULT NULL COMMENT '创建时间',
  `modify_date` bigint(11) DEFAULT NULL COMMENT '修改时间',
  `api_id` bigint(11) NOT NULL COMMENT 'api id',
  `param_name` varchar(255) NOT NULL COMMENT '参数名称',
  `type` varchar(255) DEFAULT NULL COMMENT '区分参数的类型，包含webservice接口定义中的Service|Method|RequestParam|ResponseParam四种',
  `param_type_id` bigint(20) unsigned DEFAULT NULL,
  `array_data_type_id` bigint(20) DEFAULT NULL,
  `param_sort` int(11) NOT NULL DEFAULT '0' COMMENT '参数序号',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  PRIMARY KEY (`id`)
);

CREATE TABLE `apigw_gportal_white_list` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键Id',
  `policy_name` varchar(127) NOT NULL DEFAULT '' COMMENT '策略名称',
  `policy_type` varchar(63) NOT NULL DEFAULT '' COMMENT '策略类型',
  `white_list` text NOT NULL COMMENT '白名单列表',
  `create_time` bigint(11) NOT NULL DEFAULT '0' COMMENT '策略创建时间',
  `update_time` bigint(11) NOT NULL DEFAULT '0' COMMENT '策略最近更新时间',
  `project_id` varchar(127) NOT NULL COMMENT '所属项目id',
  PRIMARY KEY (`id`)
);

CREATE TABLE `apigw_gportal_white_list_binding` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键Id',
  `gw_id` bigint(11) NOT NULL COMMENT '网关环境Id',
  `policy_id` bigint(11) NOT NULL DEFAULT '0' COMMENT '策略id',
  `binding_time` bigint(11) NOT NULL DEFAULT '0' COMMENT '策略绑定时间',
  `binding_object_id` varchar(127) NOT NULL DEFAULT '' COMMENT '策略绑定对象',
  `binding_object_type` varchar(63) NOT NULL DEFAULT '' COMMENT '策略绑定对象类型',
  `project_id` varchar(127) NOT NULL COMMENT '所属项目id',
  `policy_name` varchar(127) NOT NULL DEFAULT '' COMMENT '策略名称',
  `binding_object_name` varchar(127) NOT NULL DEFAULT '' COMMENT '绑定对象名称',
  PRIMARY KEY (`id`)
);


ALTER TABLE apigw_envoy_route_rule_proxy ADD COLUMN `virtual_cluster`  text DEFAULT NULL COMMENT '路由指标virtual_cluster';

CREATE TABLE `apigw_envoy_integration_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `integration_name` varchar(255) NOT NULL COMMENT '集成名称',
  `publish_status` int(4) DEFAULT '0' COMMENT '集成模块发布状态，0未发布，1已发布',
  `publish_time` bigint(20) NOT NULL DEFAULT '0' COMMENT '发布时间，时间戳格式，精确到毫秒',
  `update_time` bigint(20) NOT NULL DEFAULT '0' COMMENT '更新时间，时间戳格式，精确到毫秒',
  `create_time` bigint(20) NOT NULL DEFAULT '0' COMMENT '创建时间，时间戳格式，精确到毫秒',
  `project_id` bigint(11) DEFAULT NULL COMMENT '集成所属项目id',
  `description` varchar(255) DEFAULT NULL COMMENT '集成规则描述信息',
  `step` text COMMENT 'json格式保存的集成规则',
  `type` varchar(255) DEFAULT NULL COMMENT '集成类型，sub表示子流程，main表示主流程',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;

CREATE TABLE `apigw_envoy_service_wsdl_info` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `create_date` bigint(20) DEFAULT NULL,
  `modify_date` bigint(20) DEFAULT NULL,
  `gw_id` bigint(20) NOT NULL,
  `service_id` bigint(11) NOT NULL,
  `wsdl_file_name` varchar(255) NOT NULL,
  `wsdl_file_content` text NOT NULL,
  `wsdl_binding_list` text NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `apigw_envoy_route_ws_param_info` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `create_date` bigint(20) DEFAULT NULL,
  `modify_date` bigint(20) DEFAULT NULL,
  `gw_id` bigint(20) NOT NULL,
  `service_id` bigint(20) NOT NULL,
  `route_id` bigint(11) NOT NULL,
  `request_template` text NOT NULL,
  `response_array_type_list` text NOT NULL,
  `ws_port_type` varchar(255) NOT NULL,
  `ws_operation` varchar(255) NOT NULL,
  `ws_binding` varchar(255) NOT NULL,
  `ws_address` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4;

CREATE TABLE `apigw_gportal_dubbo_info` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT,
  `object_id` bigint(11) NOT NULL ,
  `object_type` varchar(255) NOT NULL  ,
  `dubbo_info` text ,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;

ALTER TABLE `apigw_gportal_gateway_info` ADD COLUMN `camel_addr` VARCHAR(255);
