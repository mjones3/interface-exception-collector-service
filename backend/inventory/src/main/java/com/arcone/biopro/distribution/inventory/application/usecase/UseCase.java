package com.arcone.biopro.distribution.inventory.application.usecase;

public interface UseCase<T,V> {
    T  execute(V args);
}
