package com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase;


import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CustomerOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.exception.NoResultsFoundException;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.CustomerOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CustomerRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.CustomerService;
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
    private final CustomerOutputMapper customerOutputMapper;

    @Override
    public Flux<CustomerOutput> findAll() {
        return customerRepository.findAll()
            .switchIfEmpty(Mono.error(NoResultsFoundException::new))
            .map(customerOutputMapper::toCustomerOutput);
    }

    @Override
    public Mono<CustomerOutput> findById(Long id) {
        return customerRepository.findOneById(id)
            .switchIfEmpty(Mono.error(() -> new DomainNotFoundForKeyException(String.format("%s", id))))
            .map(customerOutputMapper::toCustomerOutput);
    }

    @Override
    public Mono<CustomerOutput> findByCode(String code) {
        return customerRepository.findOneByCode(code)
            .switchIfEmpty(Mono.error(() -> new DomainNotFoundForKeyException(String.format("%s", code))))
            .map(customerOutputMapper::toCustomerOutput);
    }




}
