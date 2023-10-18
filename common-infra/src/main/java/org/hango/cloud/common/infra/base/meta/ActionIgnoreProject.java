package org.hango.cloud.common.infra.base.meta;

//FIXME 不需要租户id和项目id，不需要租户id，项目id进行区分
@SuppressWarnings("java:S115")
public enum ActionIgnoreProject {
    //下载SDK
    GetRestfulApiSdk,
    GetRestfulServiceSdk,
    //下载swagger相关
    DownloadSwaggerDetails,
    //级联删除
    DeleteProjectId,
    //OpenApi接口
    DescribeApiList,
    DescribeApiById,
    RegisterApi,
    DescribeServices,
    DescribeServicesForGoApi,
    DescribeGwList,
    DownloadMarkdownApiById,
    DownloadMarkdownServiceById,
    DescribeSwaggerApiById,
    DescribeSwaggerServiceById,
    DescribeGatewayInfoCount,
    RefreshEngineRule,
    HasResourceRemain;

    ActionIgnoreProject() {
    }

    public static String getActionIgnoreProject(String action) {
        for (ActionIgnoreProject actionIgnoreProject : ActionIgnoreProject.values()) {
            if (actionIgnoreProject.name().equals(action)) {
                return actionIgnoreProject.name();
            }
        }
        return null;
    }
}
