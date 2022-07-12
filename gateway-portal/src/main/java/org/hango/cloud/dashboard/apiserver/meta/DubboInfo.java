package org.hango.cloud.dashboard.apiserver.meta;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * @author zhangbj
 * @version 1.0
 * @Type
 * @Desc
 * @date 2020/12/2
 */
public class DubboInfo implements Serializable {

    private static final long serialVersionUID = 5828041465916084813L;

    /**
     * 主键ID
     */
    private long id;

    /**
     * 关联ID
     */
    private long objectId;

    /**
     * 关联类型 api/route
     */
    private String objectType;

    /**
     * dubbo参数类型
     */
    private String dubboInfo;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getObjectId() {
        return objectId;
    }

    public void setObjectId(long objectId) {
        this.objectId = objectId;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getDubboInfo() {
        return dubboInfo;
    }

    public void setDubboInfo(String dubboInfo) {
        this.dubboInfo = dubboInfo;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
