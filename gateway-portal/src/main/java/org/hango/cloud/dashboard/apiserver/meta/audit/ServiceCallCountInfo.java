/**
 *
 */
package org.hango.cloud.dashboard.apiserver.meta.audit;

import java.io.Serializable;

/**
 * @author peifei
 *
 */
public class ServiceCallCountInfo implements Serializable {

    private static final long serialVersionUID = 5911179754200582106L;
    private String name;
    private int count;

    public ServiceCallCountInfo() {
    }

    public ServiceCallCountInfo(String name, int count) {
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
