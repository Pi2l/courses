package org.m.courses.filtering;

import java.util.Objects;

public class SearchCriteria {

    private String key;

    private FilteringOperation operation;

    private Object value;

    public SearchCriteria(String key, FilteringOperation operation, Object value) {
        this.key = key;
        this.operation = operation;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public FilteringOperation getOperation() {
        return operation;
    }

    public void setOperation(FilteringOperation operation) {
        this.operation = operation;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchCriteria that = (SearchCriteria) o;
        return Objects.equals(key, that.key) && Objects.equals(operation, that.operation) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, operation, value);
    }
}
