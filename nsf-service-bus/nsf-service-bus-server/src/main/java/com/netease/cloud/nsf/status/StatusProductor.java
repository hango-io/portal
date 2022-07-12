package com.netease.cloud.nsf.status;

/**
 * status生成器
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/23
 **/
public interface StatusProductor {
    /**
     * 获得当前最新的status。默认实现是读一次status表。
     * @return
     */
    Status product();
}
