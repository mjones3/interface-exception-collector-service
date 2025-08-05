package com.arcone.biopro.exceptioncollector.adapter.in.web.controller;

import com.arcone.biopro.exceptioncollector.adapter.in.web.dto.InterfaceExceptionDTO;
import com.arcone.biopro.exceptioncollector.adapter.in.web.dto.RetryResponseDTO;
import com.arcone.biopro.exceptioncollector.application.usecase.ExceptionCollectorUseCase;
import com.arcone.biopro.exceptioncollector.application.usecase.ExceptionRetryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/exceptions")
@RequiredArgsConstructor
public class InterfaceExceptionController {

    private final ExceptionCollectorUseCase exceptionCollectorUseCase;
    private final ExceptionRetryUseCase exceptionRetryUseCase;

    @GetMapping
    public ResponseEntity<Page<InterfaceExceptionDTO>> getExceptions(
            @RequestParam(required = false) String interfaceType,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            Pageable pageable) {
        
        Page<InterfaceExceptionDTO> exceptions = exceptionCollectorUseCase
            .getExceptions(interfaceType, fromDate, toDate, pageable);
        return ResponseEntity.ok(exceptions);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<InterfaceExceptionDTO> getExceptionDetails(
            @PathVariable String transactionId) {
        
        InterfaceExceptionDTO exception = exceptionCollectorUseCase
            .getExceptionWithOriginalPayload(transactionId);
        return ResponseEntity.ok(exception);
    }

    @PostMapping("/{transactionId}/retry")
    public ResponseEntity<RetryResponseDTO> retryException(
            @PathVariable String transactionId) {
        
        RetryResponseDTO response = exceptionRetryUseCase.retryException(transactionId);
        return ResponseEntity.ok(response);
    }
}
