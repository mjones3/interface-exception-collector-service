package com.arcone.biopro.distribution.receiving.application.usecase;


import com.arcone.biopro.distribution.receiving.application.dto.LookupOutput;
import com.arcone.biopro.distribution.receiving.application.exception.NoResultsFoundException;
import com.arcone.biopro.distribution.receiving.application.mapper.LookupOutputMapper;
import com.arcone.biopro.distribution.receiving.domain.repository.LookupRepository;
import com.arcone.biopro.distribution.receiving.domain.service.LookupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class LookupUseCase implements LookupService {

    private final LookupRepository lookupRepository;
    private final LookupOutputMapper lookupOutputMapper;

    @Override
    public Flux<LookupOutput> findAllByType(String type) {
        return this.lookupRepository.findAllByType(type)
            .switchIfEmpty(Mono.error(NoResultsFoundException::new))
            .map(lookupOutputMapper::mapToOutput);
    }

}
