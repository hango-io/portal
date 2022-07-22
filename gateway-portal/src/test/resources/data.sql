insert into apigw_gportal_gateway_info (gw_name, gw_addr, create_date, modify_date, description, health_interface_path,project_id,env_id,auth_addr,mongo_addr,mysql_addr,audit_datasource_switch,gw_uni_id,metric_url,api_plane_addr, gw_cluster_name, gw_type,audit_db_config,prom_addr,camel_addr)
values
( 'envoy',  'http://10.178.207.105',  1658390903257,  1658390903257,  '',  NULL,  '1,1,1',  NULL,  NULL,  NULL,  NULL,  NULL,  NULL,  NULL,  'http://api-plane.hango.org',  'istio-aa',  'envoy',  NULL,  NULL,  NULL);



insert into apigw_envoy_virtual_host_info (project_id, gw_id, hosts, virtual_host_code, bind_type, projects, create_time, update_time) 
values
 ( 1,  1,  '["istio.com"]',  'null-1-2',  'host',  '[]',  1658390903260,  1658390903260);