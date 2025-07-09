package com.arcone.biopro.distribution.irradiation.domain.irradiation.valueobject;

import java.util.Objects;

public class BatchId {
    private final Long value;

    public BatchId(Long value) {
        this.value = value;
    }

    public static BatchId of(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("BatchId cannot be null or empty");
        }
        return new BatchId(value);
    }

    public Long getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BatchId batchId = (BatchId) o;
        return Objects.equals(value, batchId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return "BatchId{" +
            "value='" + value + '\'' +
            '}';
    }
}
