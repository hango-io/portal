package org.hango.cloud.dashboard.apiserver.meta.grpc;

import java.io.Serializable;

/**
 * @author TC_WANG
 * @date 2019/7/8
 */
public class ServiceProtobufProxy implements Serializable {

    private static final long serialVersionUID = -8678836644806911754L;

    private long id;
    private long createDate;
    private long modifyDate;
    private long pbId;
    private long gwId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(long createDate) {
        this.createDate = createDate;
    }

    public long getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(long modifyDate) {
        this.modifyDate = modifyDate;
    }

    public long getPbId() {
        return pbId;
    }

    public void setPbId(long pbId) {
        this.pbId = pbId;
    }

    public long getGwId() {
        return gwId;
    }

    public void setGwId(long gwId) {
        this.gwId = gwId;
    }
}
