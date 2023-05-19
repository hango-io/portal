package org.hango.cloud.common.infra.base.meta;

import java.io.Serializable;

/**
 * @version 1.0
 * @Type
 * @Desc
 * @date 2019/10/25
 */
public class Pair<K, V> implements Serializable {

    private K key;

    private V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        // name's hashCode is multiplied by an arbitrary prime number (13)
        // in order to make sure there is a difference in the hashCode between
        // these two parameters:
        //  name: a  value: aa
        //  name: aa value: a
        return key.hashCode() * 13 + (value == null ? 0 : value.hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof Pair) {
            Pair pair = (Pair) o;
            if (key != null ? !key.equals(pair.key) : pair.key != null) {
                return false;
            }
            if (value != null ? !value.equals(pair.value) : pair.value != null) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return key + "=" + value;
    }

}

