package org.hango.cloud.dashboard.apiserver.meta;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @author zhangbaojun
 * @version $Id: UserIdentityEntity.java, v 1.0 2018年07月23日 11:05
 */
public class UserIdentityEntity {

    @JSONField(name = "AccountId")
    private String accountId;

    @JSONField(name = "UserName")
    private String userName;

    @JSONField(name = "AccessKeyId")
    private String accessKeyId;

    public UserIdentityEntity(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    @Override
    public String toString() {
        return "UserIdentityEntity{" +
                "accountId='" + accountId + '\'' +
                ", userName='" + userName + '\'' +
                ", accessKeyId='" + accessKeyId + '\'' +
                '}';
    }
}
