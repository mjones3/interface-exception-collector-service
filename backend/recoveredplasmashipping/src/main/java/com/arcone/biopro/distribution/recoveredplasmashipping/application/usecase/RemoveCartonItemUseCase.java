package com.arcone.biopro.distribution.recoveredplasmashipping.application.usecase;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.CartonOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.RemoveCartonItemCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessage;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseMessageType;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseNotificationOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.exception.DomainNotFoundForKeyException;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.mapper.CartonOutputMapper;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.model.RemoveCartonItemCommand;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonItemRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.repository.CartonRepository;
import com.arcone.biopro.distribution.recoveredplasmashipping.domain.service.RemoveCartonItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RemoveCartonItemUseCase implements RemoveCartonItemService {

    private final CartonRepository cartonRepository;
    private final CartonOutputMapper cartonOutputMapper;
    private final CartonItemRepository cartonItemRepository;

    @Override
    public Mono<UseCaseOutput<CartonOutput>> removeCartonItem(RemoveCartonItemCommandInput removeCartonItemCommandInput) {
        return cartonRepository.findOneById(removeCartonItemCommandInput.cartonId())
            .publishOn(Schedulers.boundedElastic())
            .switchIfEmpty(Mono.error(() -> new DomainNotFoundForKeyException(String.format("%s", removeCartonItemCommandInput.cartonId()))))
            .flatMap(carton -> Mono.fromSupplier(() -> carton.removeCartonItem(new RemoveCartonItemCommand(removeCartonItemCommandInput.cartonId(), removeCartonItemCommandInput.employeeId()
                , removeCartonItemCommandInput.cartonItemIds()))))
            .flatMap(cartonItems -> {
                return Flux.fromIterable(cartonItems)
                    .flatMap(item -> cartonItemRepository.deleteOneById(item.getId()))
                    .collectList();
            })
            .flatMap(removedProducts -> cartonRepository.findOneById(removeCartonItemCommandInput.cartonId())
                .flatMap(carton -> Flux.fromIterable(carton.getVerifiedProducts())
                    .flatMap(cartonItem -> cartonItemRepository.save(cartonItem.resetVerification()))
                    .collectList()).then(cartonRepository.findOneById(removeCartonItemCommandInput.cartonId())
                    .map(carton -> new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                        .builder()
                        .useCaseMessage(UseCaseMessage
                            .builder()
                            .code(UseCaseMessageType.CARTON_ITEM_REMOVED_SUCCESS.getCode())
                            .message(UseCaseMessageType.CARTON_ITEM_REMOVED_SUCCESS.getMessage())
                            .type(UseCaseMessageType.CARTON_ITEM_REMOVED_SUCCESS.getType())
                            .build())
                        .build())
                        , cartonOutputMapper.toOutput(carton)
                        , null)))).onErrorResume(error -> {
                log.error("Not able to remove carton items {}", error.getMessage());
                return Mono.just(new UseCaseOutput<>(List.of(UseCaseNotificationOutput
                    .builder()
                    .useCaseMessage(
                        UseCaseMessage
                            .builder()
                            .message(UseCaseMessageType.CARTON_ITEM_REMOVED_ERROR.getMessage())
                            .code(UseCaseMessageType.CARTON_ITEM_REMOVED_ERROR.getCode())
                            .type(UseCaseMessageType.CARTON_ITEM_REMOVED_ERROR.getType())
                            .build())
                    .build()), null, null));
            });


    }
}
