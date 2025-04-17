package com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CartonOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessage;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.CartonOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.CartonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FindCartonUseCase implements CartonService {
    private final CartonRepository cartonRepository;
    private final CartonOutputMapper cartonOutputMapper;


    @Override
    public Mono<UseCaseOutput<CartonOutput>> findOneById(Long id) {
        return cartonRepository.findOneById(id)
            .switchIfEmpty(Mono.error(() -> new DomainNotFoundForKeyException(String.format("%s", id))))
            .flatMap(carton -> {
                return Mono.just(new UseCaseOutput<>(Collections.emptyList(), cartonOutputMapper.toOutput(carton), null ));
            })
            .onErrorResume(error -> {
                log.error("Error Finding Carton by id ", error);
                return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                    .builder()
                    .useCaseMessage(new UseCaseMessage(10, UseCaseNotificationType.WARN, error.getMessage()))
                    .build()), null, null));
            });
    }
}
