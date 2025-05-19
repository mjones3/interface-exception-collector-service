package com.arcone.biopro.distribution.order.application.usecase;

import com.arcone.biopro.distribution.order.application.exception.NoResultsFoundException;
import com.arcone.biopro.distribution.order.application.mapper.CustomerMapper;
import com.arcone.biopro.distribution.order.domain.repository.CustomerRepository;
import com.arcone.biopro.distribution.order.domain.service.CustomerService;
import com.arcone.biopro.distribution.order.infrastructure.service.dto.CustomerDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerUseCase implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;


    @Override
    public Flux<CustomerDTO> getCustomers() {
        return customerRepository.getCustomers()
            .switchIfEmpty(Mono.error(NoResultsFoundException::new))
            .map(customerMapper::mapToDTO);
    }

    @Override
    public Mono<CustomerDTO> getCustomerByCode(String code) {
        return customerRepository.getCustomerByCode(code)
            .switchIfEmpty(Mono.error(NoResultsFoundException::new))
            .map(customerMapper::mapToDTO);
    }
}
