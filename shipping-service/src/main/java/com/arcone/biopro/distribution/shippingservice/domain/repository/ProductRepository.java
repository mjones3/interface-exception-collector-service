package com.arcone.biopro.distribution.shippingservice.domain.repository;

import com.arcone.biopro.distribution.shippingservice.domain.model.Product;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ProductRepository extends ReactiveCrudRepository<Product, Long> {
}
