package com.arcone.biopro.distribution.inventory.application.usecase;

import com.arcone.biopro.distribution.inventory.application.dto.PackedProductInput;

public interface UseCase<T,V> {
    T  execute(V args);
}
