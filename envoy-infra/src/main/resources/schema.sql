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
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='API信息表';

    CREATE TABLE `apigw_gportal_api_document_status` (
      `id` bigint(20) NOT NULL AUTO_INCREMENT,
      `status` varchar(255) NOT NULL COMMENT 'API文档状态',
      PRIMARY KEY (`id`,`status`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='API文档状态表';


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
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='数据模型(自定义类型)表';



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
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;



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
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Body和其包含的参数间的对应关系';


    create table hango_gateway (
        id              bigint auto_increment comment '网关ID' primary key,
        name            varchar(255)  not null comment '网关名称',
        env_id          varchar(255)  not null comment '所属环境',
        svc_type        varchar(255)  not null comment '网关service类型， ClusterIP/NodePort',
        svc_name        varchar(255)  not null comment '网关service名称',
        type            varchar(255)  not null comment '网关类型',
        gw_cluster_name varchar(255)  not null comment '网关集群名称',
        conf_addr       varchar(255)  not null comment '配置下发地址',
        description     varchar(1000) null comment '备注',
        create_time     bigint        null comment '创建时间',
        modify_time     bigint        null comment '修改时间'
    )  comment '网关信息表';


    create table hango_gateway_advanced (
        id              bigint auto_increment comment '主键ID'
        primary key,
        gw_id           bigint        not null comment '网关(物理)ID',
        metric_url      varchar(255)  null comment '监控地址',
        audit_db_config varchar(1024) null comment '审计数据源配置'
    )  comment '网关环境进阶表';


    create table hango_virtual_gateway (
        id          bigint auto_increment comment '虚拟网关ID'
        primary key,
        gw_id       bigint       not null comment '网关ID',
        name        varchar(255) not null comment '虚拟网关名称',
        code        varchar(255) not null comment '虚拟网关标识',
        addr        varchar(255) null comment '虚拟网关访问地址',
        project_id  text         null comment '基于项目隔离，项目id',
        description varchar(255) null comment '虚拟网关描述',
        type        varchar(255) not null comment '虚拟网关类型',
        protocol    varchar(255) not null comment '监听协议类型',
        port        int(10)      null comment '监听端口',
        create_time bigint       null comment '创建时间',
        update_time bigint       null comment '修改时间',
        domain_id   varchar(255) null comment '域名id列表',
        `advanced_setting` text
    ) comment '虚拟网关信息表';



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
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Header和其包含的参数间的对应关系';



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
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='数据模型和其包含的参数之间的对应关系';



    CREATE TABLE `apigw_gportal_param_object` (
      `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
      `create_date` bigint(11) DEFAULT NULL COMMENT '创建时间',
      `modify_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
      `object_value` varchar(1024) DEFAULT NULL COMMENT '匿名类型取值，json字符串',
      PRIMARY KEY (`id`),
      KEY `index_param_object_id` (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='参数类型表';

    CREATE TABLE `apigw_gportal_param_type` (
      `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
      `create_date` bigint(11) DEFAULT NULL COMMENT '创建时间',
      `modify_date` bigint(20) DEFAULT NULL,
      `param_type` varchar(255) NOT NULL COMMENT '参数类型,基本类型|自定义类型',
      `location` varchar(255) NOT NULL COMMENT '位置，取值为HEADER|BODY',
      `model_id` bigint(20) DEFAULT '0',
      PRIMARY KEY (`id`,`param_type`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='参数类型表';

    CREATE TABLE `hango_health_check_rule` (
      `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
      `create_time` bigint(20) NOT NULL COMMENT '创建时间',
      `update_time` bigint(20) NOT NULL COMMENT '修改时间',
      `service_id` bigint(20) NOT NULL COMMENT '服务id',
      `active_switch` tinyint(4) NOT NULL COMMENT '主动检查开关，1表示开启，0表示关闭',
      `path` varchar(255) DEFAULT '' COMMENT '接口path',
      `timeout` int(11) DEFAULT NULL COMMENT '超时时间，单位毫秒',
      `expected_statuses` varchar(255) DEFAULT '' COMMENT '健康状态码集合	',
      `healthy_interval` int(11) DEFAULT NULL COMMENT '健康实例检查间隔，单位毫秒	',
      `healthy_threshold` int(11) DEFAULT NULL COMMENT '健康阈值，单位次',
      `unhealthy_interval` int(11) DEFAULT NULL COMMENT '异常实例检查间隔，单位毫秒',
      `unhealthy_threshold` int(11) DEFAULT NULL COMMENT '异常阈值，单位次',
      `passive_switch` tinyint(4) NOT NULL COMMENT '被动检查开关，1表示开启，0表示关闭',
      `consecutive_errors` int(11) DEFAULT NULL COMMENT '连续失败次数',
      `base_ejection_time` int(11) DEFAULT NULL COMMENT '驱逐时间，单位毫秒',
      `max_ejection_percent` tinyint(4) DEFAULT NULL COMMENT '最多可驱逐的实例比',
      `min_health_percent` tinyint(4) DEFAULT 50 COMMENT '最小健康实例数',
      PRIMARY KEY (`id`),
      UNIQUE KEY `unique_service_gw_id` (`service_id`),
      KEY `index_service_gw_id` (`service_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='路由规则表';

    CREATE TABLE `hango_plugin_binding` (
        id bigint(11) NOT NULL AUTO_INCREMENT,
        plugin_type          varchar(255)                  not null comment '绑定的插件类型，全局唯一，如：RateLimiter、WhiteList等',
        plugin_name          varchar(255)                  null comment '绑定的插件名称',
        binding_object_type  varchar(255)                  not null comment '插件所绑定的对象类型，包含路由规则、服务等',
        binding_object_id    varchar(255)                  not null comment '插件所绑定的对象的唯一标识，与binding_object_type共同决定某一具体对象',
        plugin_configuration text                          not null comment '插件配置',
        create_time          bigint                        not null comment '最新绑定时间，时间戳格式，精确到毫秒',
        update_time          bigint                        not null comment '绑定（配置）修改时间，时间戳格式，精确到毫秒',
        virtual_gw_id        bigint(11)                    not null comment '对象-插件绑定关系作用的虚拟网关id',
        project_id           bigint(11)                    not null comment '插件绑定关系所属项目id',
        binding_status       varchar(127) default 'enable' not null comment '插件绑定关系状态，enable/disable',
        template_id          bigint       default 0        null comment '关联插件模板id',
        template_version     bigint       default 0        null comment '关联插件模板版本号',
        gw_type              varchar(10)                   not null comment '网关类型',
        version              bigint       default 0        null comment '版本号',
        PRIMARY KEY (`id`)
    ) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COMMENT='插件配置表';

    create table hango_route_rule_proxy
    (
        id                   bigint(11) auto_increment
        primary key,
        name                 varchar(255)                 not null comment '路由名称',
        alias                varchar(255)                 null comment '路由别名',
        description          varchar(255)                 null comment '描述信息',
        virtual_gw_id        bigint(11)                   null comment '虚拟网关id',
        destination_services varchar(1024)                not null comment '目的地址信息',
        project_id           bigint(11)                   not null comment '路由规则发布所属项目id',
        priority             bigint(11)                   null comment '路由规则优先级',
        orders               bigint(11)                   null comment '路由规则orders，与pilot交互',
        enable_state         varchar(10) default 'enable' null comment '路由规则使能状态，默认为enable',
        create_time          bigint      default 0        not null comment '路由规则发布时间，时间戳格式，精确到毫秒',
        update_time          bigint      default 0        not null comment '路由规则更新时间，时间戳格式，精确到毫秒',
        service_ids          text                         null comment '路由规则发布关联的服务id',
        timeout              bigint(11)  default 60000    null comment '路由超时时间',
        http_retry           text                         null comment '路由重试配置',
        mirror_traffic       text                         null comment '流量镜像配置',
        mirror_service_id    bigint(11)                   null comment '流量镜像指向服务id',
        uri                  varchar(1024)                null comment '路由uri，与匹配模式共同作用',
        method               varchar(1024)                null comment '匹配方法列表GETPOSTPUTDELETEHEAD等',
        header               text                         null comment '匹配header列表',
        query_param          text                         null comment '匹配路由queryParam列表',
        gw_type              varchar(255)                 null comment '网关类型',
        version              bigint      default 0        null comment '版本号'
    )
    comment '路由规则发布信息表';

    create index idx_r_name on hango_route_rule_proxy (name);

    create table hango_service_proxy
    (
        id                   bigint(11) auto_increment comment '主键'
        primary key,
        create_time          bigint                             null comment '创建时间',
        update_time          bigint                             null comment '更新时间',
        name                 varchar(255)                       not null comment '服务名称',
        virtual_gw_id        bigint(11)                         not null comment '服务发布所属虚拟网关id',
        project_id           bigint(11)   default 0             null comment '发布所属项目id',
        alias                varchar(255)                       null comment '服务别名',
        hosts                varchar(255)                       not null comment '域名',
        protocol             varchar(255) default 'http'        null comment '服务协议',
        backend_service      text                               null comment '发布关联真实网关服务',
        publish_type         varchar(255)                       null comment '发布策略，STATIC/DYNAMIC',
        load_balancer        varchar(127) default 'ROUND_ROBIN' not null comment '负载均衡',
        subsets              text                               null comment '版本集合',
        registry_center_type varchar(255)                       null comment '注册中心类型Consul/Kubernetes/Eureka/Zookeeper，DYNAMIC时必填，默认Kubernetes',
        traffic_policy       text                               null comment '负载均衡和连接池配置',
        gw_type              varchar(255)                       null comment '网关类型',
        version              bigint       default 0             null comment '版本号',
        description          varchar(255)                       null comment '备注'
    )comment '服务发布表' charset = utf8;

    create table hango_domain_info (
        id             int unsigned auto_increment comment '主键'
        primary key,
        create_time    bigint       not null comment '创建时间',
        update_time    bigint       not null comment '更新时间',
        host           varchar(255) not null comment '域名',
        project_id     bigint(11)   not null comment '项目id',
        protocol       varchar(255) not null comment '协议，可以支持多个协议',
        status         varchar(64)  not null comment '域名状态',
        certificate_id bigint(64)   null comment '服务端证书id',
        description    varchar(255) null comment '备注信息'
    ) comment '域名表';

    create table hango_certificate_info (
        id             int unsigned auto_increment comment '主键'
        primary key,
        create_time    bigint       not null comment '创建时间',
        update_time    bigint       not null comment '修改时间',
        name           varchar(255) not null comment '证书名称',
        type           varchar(64)  null comment '证书类型 serverCert/caCert',
        domain         varchar(64)  not null comment '证书域名',
        signature      text         not null comment '公钥指纹',
        issuing_agency varchar(64)  not null comment '签发机构',
        issuing_time   bigint       not null comment '证书签发时间',
        expired_time   bigint       not null comment '证书过期时间',
        content        text         not null comment '证书信息',
        private_key    text         not null comment '私钥'
    ) comment '证书表';

    CREATE TABLE `hango_dubbo_meta` (
         `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
         `application_name` varchar(255) NOT NULL COMMENT '应用名称',
         `protocol_version` varchar(255)  COMMENT 'dubbo协议版本',
         `interface_name` varchar(255) NOT NULL COMMENT '接口名称',
         `dubbo_group` varchar(255) DEFAULT NULL COMMENT '分组',
         `dubbo_version` varchar(255) DEFAULT NULL COMMENT '版本',
         `method` varchar(255) NOT NULL COMMENT '方法名称',
         `dubbo_params` varchar(4000) DEFAULT NULL COMMENT '参数列表',
         `dubbo_returns` varchar(255) DEFAULT NULL COMMENT '返回类型',
         `create_time` bigint(11) DEFAULT NULL COMMENT '创建时间',
         `virtual_gw_id`    bigint(11)    not null comment '网关ID',
         PRIMARY KEY (`id`)
       ) ENGINE=InnoDB AUTO_INCREMENT=53200 DEFAULT CHARSET=utf8mb4 COMMENT='Dubbo 元数据信息表';




    CREATE TABLE `hango_dubbo_binding` (
      `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
      `object_id` bigint(11) NOT NULL COMMENT '关联ID',
      `object_type` varchar(255) NOT NULL COMMENT '关联类型 api/route',
      `dubbo_info` text COMMENT 'dubbo参数类型',
      PRIMARY KEY (`id`)
    ) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COMMENT='dubbo参数表';


    CREATE TABLE `hango_service_wsdl_info` (
      `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
      `create_date` bigint(20) DEFAULT NULL,
      `modify_date` bigint(20) DEFAULT NULL,
      `gw_id` bigint(20) NOT NULL,
      `service_id` bigint(11) NOT NULL,
      `wsdl_file_name` varchar(255) NOT NULL,
      `wsdl_file_content` text NOT NULL,
      `wsdl_binding_list` text NOT NULL,
      PRIMARY KEY (`id`)
    ) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4;

    CREATE TABLE `hango_service_protobuf` (
    `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `create_date` bigint(20) DEFAULT NULL COMMENT '创建时间',
    `modify_date` bigint(20) DEFAULT NULL COMMENT '修改时间',
    `service_id` bigint(11) unsigned NOT NULL COMMENT '服务id',
    `pb_file_name` varchar(255) NOT NULL DEFAULT '' COMMENT '上传的pb文件名',
    `pb_file_content` text NOT NULL COMMENT 'pb文件内容',
    `pb_service_list` text NOT NULL COMMENT 'pb文件包含的服务列表',
    PRIMARY KEY (`id`),
    KEY `service_id` (`service_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='gRPC服务对应的pb文件存储表';

    CREATE TABLE `hango_service_protobuf_proxy` (
    `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
    `create_date` bigint(20) DEFAULT NULL COMMENT '创建时间',
    `modify_date` bigint(20) DEFAULT NULL COMMENT '修改时间',
    `service_id` bigint(20) NOT NULL COMMENT '服务id',
    `virtual_gw_id` bigint(20) NOT NULL COMMENT '虚拟网关id',
    `pb_file_name` varchar(255) NOT NULL DEFAULT '' COMMENT '上传的pb文件名',
    `pb_file_content` text NOT NULL COMMENT 'pb文件内容',
    `pb_service_list` text NOT NULL COMMENT 'pb文件包含的服务列表',
    PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='gRPC服务对应的pb文件发布表';


    CREATE TABLE `hango_plugin_template` (
      `id` bigint(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
      `create_time` bigint(20) NOT NULL COMMENT '模板创建时间，时间戳格式，精确到毫秒',
      `update_time` bigint(20) NOT NULL COMMENT '模板最后更新时间，时间戳格式，精确到毫秒',
      `plugin_type` varchar(255) NOT NULL COMMENT '绑定的插件类型，全局唯一，如：RateLimiter、WhiteList等',
      `plugin_name` varchar(255) null comment '绑定的插件名称',
      `plugin_configuration` text NOT NULL COMMENT '插件配置',
      `project_id` bigint(11) NOT NULL COMMENT '插件绑定关系所属项目id',
      `template_version` bigint(10) NOT NULL COMMENT '插件模板版本',
      `template_name` varchar(255) NOT NULL COMMENT '插件模板名称',
      `template_notes` varchar(255) DEFAULT NULL COMMENT '插件模板',
      PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='插件模板信息表';


    CREATE TABLE apigw_gportal_entry_traffic_policy
    (
        id                 bigint unsigned auto_increment
            primary key,
        virtual_gw_id              bigint(11) not null comment '虚拟网关id',
        gw_name            varchar(255) null comment '网关名',
        traffic_color_name varchar(255) null comment '流量染色规则名称',
        application_name   varchar(255) null comment '应用名称',
        service_name       varchar(255) null comment '服务名称',
        route_rule_names   varchar(255) null comment '路由名称列表',
        create_time        bigint       not null comment '创建时间',
        update_time        bigint       not null comment '修改时间',
        route_rule_ids     varchar(255) not null,
        enable_status      int(4) default 0 null comment '入口流量染色状态，0未启用，1为启用',
        traffic_match      varchar(255) null comment '流量匹配',
        color_tag          varchar(255) null comment '染色标识',
        protocol           varchar(255) null comment '协议',
        param              text null comment '流量匹配参数列表',
        project_id         bigint(11) null comment '流量染色规则所属的项目id'
    ) charset = utf8mb4 COMMENT='流量染色规则表';

    CREATE TABLE hango_pb_service_info
    (
        id             bigint auto_increment comment '主键'
        primary key,
        service_name   varchar(255)         not null comment 'grpc接口名，如helloworld.Greeter',
        pb_id          bigint               not null comment 'proto文件(apigw_envoy_service_protobuf)表id',
        pb_proxy_id    bigint               null comment 'proto发布信息(apigw_envoy_service_protobuf_proxy)表id',
        publish_status tinyint(1) default 0 not null comment '发布状态'
    )comment 'pb文件服务信息表';

    create table hango_route_ws_param_info(
        id                       bigint unsigned auto_increment
        primary key,
        create_date              bigint       null,
        modify_date              bigint       null,
        gw_id                    bigint       not null,
        service_id               bigint       not null,
        route_id                 bigint(11)   not null,
        request_template         text         not null,
        response_array_type_list text         not null,
        ws_port_type             varchar(255) not null,
        ws_operation             varchar(255) not null,
        ws_binding               varchar(255) not null,
        ws_address               varchar(255) not null
    )   charset = utf8mb4;

    create table hango_webservice_param (
        id                 bigint(11) auto_increment comment '主键'
        primary key,
        create_date        bigint(11)      null comment '创建时间',
        modify_date        bigint(11)      null comment '修改时间',
        api_id             bigint(11)      not null comment 'api id',
        param_name         varchar(255)    not null comment '参数名称',
        param_type         varchar(255)    null comment '参数类型',
        type               varchar(255)    null comment '区分参数的类型，包含webservice接口定义中的Service|Method|RequestParam|ResponseParam四种',
        param_type_id      bigint unsigned null,
        array_data_type_id bigint          null,
        param_sort         int default 0   not null comment '参数序号',
        description        varchar(255)    null comment '描述'
    ) comment 'API对应的soap接口信息';

CREATE ALIAS FIND_IN_SET FOR "org.mvnsearch.h2.mysql.StringFunctions.findInSet";