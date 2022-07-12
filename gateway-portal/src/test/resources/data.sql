INSERT INTO `apigw_gportal_gateway_info`
VALUES
(1, 'gateway', 'http://apigw-gateway.qa-ci.service.163.org', '研发联调测试', 1563515833712, 1565684534680, 1,
1566287640040, '/ngw?Action=Health&Version=2017-11-16', '', '3', 'prod', 'http://platform-service-auth.qa-ci.service.163.org', 'mysql', '{"DriverClassName":"com.mysql.jdbc.Driver","Password":"72169df41122ec03","url":"jdbc:mysql://10.182.2.155:3306/apigw_audit?&autoReconnect=true&connectTimeout=5000&socketTimeout=50000&generateSimpleParameterMetadata=true","username":"qzmysql"}', 'prod1', 'http://prometheus-nms.qa-ci.service.163.org','','','g0','','','');

INSERT INTO `apigw_gportal_gateway_info`
VALUES
(2, 'envoy', 'http://103.196.65.124:31448', 'envoy网关环境', 1563515833712, 1565684534680, 1, 1566287640040, '', '', '3',
 'prod', 'http://platform-service-auth.qa-ci.service.163.org', 'elasticsearch', '', 'prod1', 'http://prometheus.qa-ci.service.163.org','http://103.196.65.105:32536','demo','envoy','{"spring.elasticsearch.jest.uris":["http://10.177.16.13:30844","http://10.177.16.14:30844","http://10.177.16.15:30844"]}','http://prometheus.qa-ci.service.163.org','');

INSERT INTO `apigw_envoy_virtual_host_info`
VALUES
(1, 3, 2, '["istio.com"]', 'project1-3-1', 1565684534680, 1566287640040, 'host', '');