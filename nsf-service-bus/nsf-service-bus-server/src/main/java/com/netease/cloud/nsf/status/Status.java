package com.netease.cloud.nsf.status;

import java.util.*;

/**
 * @author wupenghuai@corp.netease.com
 * @date 2020/4/23
 **/
public class Status {
    private final Property[] properties;

    public Status(Property[] properties) {
        Arrays.sort(properties, Comparator.comparing(o -> o.key));
        this.properties = properties;
    }

    public String get(String key) {
        for (Property p : properties) {
            if (p.key.equals(key)) {
                return p.value;
            }
        }
        return null;
    }

    public Property[] getProperties() {
        return properties;
    }

    public Difference compare(Status other) {
        Difference diff = new Difference();
        int i = 0, j = 0;
        int thisL = this.properties.length;
        int otherL = other.properties.length;
        while (i < thisL && j < otherL) {
            if (Objects.equals(this.properties[i].key, other.properties[j].key)) {
                // update
                if (!Objects.equals(this.properties[i].value, other.properties[j].value)) {
                    diff.update.add(other.properties[j]);
                }
                i++;
                j++;
            } else {
                if (this.properties[i].key.compareTo(other.properties[j].key) > 0) {
                    // add
                    diff.add.add(other.properties[j]);
                    j++;
                } else {
                    // delete
                    diff.delete.add(this.properties[i]);
                    i++;
                }
            }
        }
        // delete
        while (i < thisL) {
            diff.delete.add(this.properties[i]);
            i++;
        }
        // add
        while (j < otherL) {
            diff.add.add(other.properties[j]);
            j++;
        }
        return diff;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Status status = (Status) o;
        return Arrays.equals(properties, status.properties);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(properties);
    }

    public static final class Property {
        public final String key;
        public final String value;

        public Property(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Property property = (Property) o;
            return Objects.equals(key, property.key) &&
                    Objects.equals(value, property.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, value);
        }
    }

    public static final class Difference {
        private List<Property> add = new ArrayList<>();
        private List<Property> update = new ArrayList<>();
        private List<Property> delete = new ArrayList<>();

        public List<Property> getAdd() {
            return add;
        }

        public List<Property> getUpdate() {
            return update;
        }

        public List<Property> getDelete() {
            return delete;
        }
    }
}
