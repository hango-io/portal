/**
 *
 */
package org.hango.cloud.dashboard.apiserver.meta.audit;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author peifei
 *
 */
@Document(collection = "HourlyCallCountInfoG0")
public class HourlyCallCountInfo {

    @Id
    private String id;

    private long count;

    @Indexed
    private int timeInt;

    @Field(value = "index")
    private int callIndex;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public int getTimeInt() {
        return timeInt;
    }

    public void setTimeInt(int timeInt) {
        this.timeInt = timeInt;
    }

    public int getCallIndex() {
        return callIndex;
    }

    public void setCallIndex(int callIndex) {
        this.callIndex = callIndex;
    }
}
