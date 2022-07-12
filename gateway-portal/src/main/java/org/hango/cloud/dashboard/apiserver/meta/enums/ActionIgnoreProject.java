package org.hango.cloud.dashboard.apiserver.meta.enums;

//FIXME 不需要租户id和项目id，不需要租户id，项目id进行区分
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
