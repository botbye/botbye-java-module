package com.botbye.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Headers implements Serializable {
    private Map<String, List<String>> entries;

    public Headers() {
        this.entries = Collections.emptyMap();
    }

    public Headers(Map<String, List<String>> entries) {
        this.entries = entries;
    }

    public Map<String, List<String>> getEntries() {
        return entries;
    }

    public void setEntries(Map<String, List<String>> entries) {
        this.entries = entries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Headers headers = (Headers) o;
        return Objects.equals(entries, headers.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entries);
    }

    @Override
    public String toString() {
        return "Headers{" +
                "entries=" + entries +
                '}';
    }
}
