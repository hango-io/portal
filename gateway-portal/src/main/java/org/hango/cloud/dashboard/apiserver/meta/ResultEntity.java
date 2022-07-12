package org.hango.cloud.dashboard.apiserver.meta;

public class ResultEntity<C, V> {
    public final static String SUCCESS = "succ";
    public final static String FAILURE = "failure";

    private C code;
    private V value;

    public ResultEntity(C code, V value) {
        this.code = code;
        this.value = value;
    }

    public C getCode() {
        return code;
    }

    public void setCode(C code) {
        this.code = code;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }
}
