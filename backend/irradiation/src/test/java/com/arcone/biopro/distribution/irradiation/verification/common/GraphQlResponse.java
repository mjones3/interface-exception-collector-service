package com.arcone.biopro.distribution.irradiation.verification.common;

import lombok.Getter;
import org.springframework.graphql.ResponseError;

import java.util.List;
import java.util.Map;

@Getter
public class GraphQlResponse<T> {
    private final T data;
    private final List<ResponseError> errors;

    public GraphQlResponse(T data, List<ResponseError> errors) {
        this.data = data;
        this.errors = errors;
    }

    public boolean hasData() {
        return data != null;
    }

    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getDataAsMap(String path) {
        if (data instanceof Map) {
            Object result = ((Map<?, ?>) data).get(path);
            if (result instanceof Map) {
                return (Map<String, Object>) result;
            }
        }
        return null;
    }

}
