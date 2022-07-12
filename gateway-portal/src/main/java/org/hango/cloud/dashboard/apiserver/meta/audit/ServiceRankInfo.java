package org.hango.cloud.dashboard.apiserver.meta.audit;

import java.io.Serializable;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/11/28
 */
public class ServiceRankInfo implements Serializable {

    private static final long serialVersionUID = 5911179754200582106L;

    private String name;
    private int count;

    public ServiceRankInfo() {
    }

    public ServiceRankInfo(String name, int count) {
        this.name = name;
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
