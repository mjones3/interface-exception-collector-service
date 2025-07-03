package com.arcone.biopro.distribution.irradiation.application.usecase;

public interface UseCase<T,V> {
    T  execute(V args);
}
