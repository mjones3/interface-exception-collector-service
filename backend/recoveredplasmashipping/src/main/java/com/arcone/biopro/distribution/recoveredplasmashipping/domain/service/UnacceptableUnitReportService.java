package com.arcone.biopro.distribution.recoveredplasmashipping.domain.service;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.PrintUnacceptableUnitReportCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UnacceptableUnitReportOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import reactor.core.publisher.Mono;

public interface UnacceptableUnitReportService {

    Mono<UseCaseOutput<UnacceptableUnitReportOutput>> printUnacceptableUnitReport(PrintUnacceptableUnitReportCommandInput printUnacceptableUnitReportCommandInput);
}
