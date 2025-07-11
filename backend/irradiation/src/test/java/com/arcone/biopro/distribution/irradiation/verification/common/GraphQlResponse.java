package com.arcone.biopro.distribution.irradiation.verification.common;

import lombok.Getter;
import org.springframework.graphql.ResponseError;

import java.util.List;

@Getter
public class GraphQlResponse<T> {
    private final T data;
    private final List<ResponseError> errors;

    public GraphQlResponse(T data, List<ResponseError> errors) {
        this.data = data;
        this.errors = errors;
    }

}
