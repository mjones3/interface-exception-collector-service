package com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.persistence;

import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.Customer;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CustomerRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.infrastructure.mapper.CustomerEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class CustomerRepositoryImpl implements CustomerRepository {

    private final CustomerEntityRepository customerEntityRepository;
    private final CustomerEntityMapper customerEntityMapper;

    @Override
    public Flux<Customer> findAll() {
        return customerEntityRepository.findAllByActiveIsTrueOrderByNameAsc()
            .map(customerEntityMapper::entityToModel);
    }

    @Override
    public Mono<Customer> findOneById(Long id) {
        return customerEntityRepository.findById(id).map(customerEntityMapper::entityToModel);
    }

    @Override
    public Mono<Customer> findOneByCode(String code) {
        return customerEntityRepository.findByCode(code).map(customerEntityMapper::entityToModel);
    }
}
