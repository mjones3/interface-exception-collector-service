package com.arcone.biopro.exceptioncollector.domain.repository;

import com.arcone.biopro.exceptioncollector.domain.model.InterfaceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Optional;

public interface InterfaceExceptionRepository {
    InterfaceException save(InterfaceException exception);
    Optional<InterfaceException> findByTransactionId(String transactionId);
    Page<InterfaceException> findAll(Pageable pageable);
    Page<InterfaceException> findByInterfaceType(String interfaceType, Pageable pageable);
    Page<InterfaceException> findByDateRange(LocalDate fromDate, LocalDate toDate, Pageable pageable);
}
