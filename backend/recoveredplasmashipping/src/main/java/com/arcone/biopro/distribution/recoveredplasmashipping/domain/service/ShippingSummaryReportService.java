package com.arcone.biopro.distribution.recoveredplasmashipping.domain.service;

import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.PrintShippingSummaryReportCommandInput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.ShippingSummaryReportOutput;
import com.arcone.biopro.distribution.recoveredplasmashipping.application.dto.UseCaseOutput;
import reactor.core.publisher.Mono;

public interface ShippingSummaryReportService {

    Mono<UseCaseOutput<ShippingSummaryReportOutput>> printShippingSummaryReport(PrintShippingSummaryReportCommandInput printShippingSummaryReportCommandInput);
}
