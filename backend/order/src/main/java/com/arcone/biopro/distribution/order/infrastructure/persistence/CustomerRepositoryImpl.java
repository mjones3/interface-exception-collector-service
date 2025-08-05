package com.arcone.biopro.distribution.order.infrastructure.persistence;

import com.arcone.biopro.distribution.order.domain.model.Customer;
import com.arcone.biopro.distribution.order.domain.repository.CustomerRepository;
import com.arcone.biopro.distribution.order.infrastructure.mapper.CustomerEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class CustomerRepositoryImpl implements CustomerRepository {

    private final CustomerEntityRepository customerEntityRepository;
    private final CustomerEntityMapper customerEntityMapper;
    private final CustomerAddressEntityRepository customerAddressEntityRepository;


    @Override
    public Flux<Customer> getCustomers() {
        return customerEntityRepository.findAllByActiveIsTrueOrderByNameAsc()
            .flatMap(customer -> Flux.from(customerAddressEntityRepository.findAllByCustomerIdAndActiveTrue(customer.getId()))
                .collectList()
                .map(list -> customerEntityMapper.entityToModel(customer,list)
                )
            );
    }

    @Override
    public Mono<Customer> getCustomerByCode(String code) {
        return customerEntityRepository.findByCode(code)
            .flatMap(customer -> Flux.from(customerAddressEntityRepository.findAllByCustomerIdAndActiveTrue(customer.getId()))
                .collectList()
                .map(list -> customerEntityMapper.entityToModel(customer,list)
                )
            );
    }
}
