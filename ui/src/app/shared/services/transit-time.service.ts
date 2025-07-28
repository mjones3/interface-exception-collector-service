import { Injectable } from '@angular/core';
import { DynamicGraphqlPathService } from '../../core/services/dynamic-graphql-path.service';
import { Observable } from 'rxjs';
import { ApolloQueryResult } from '@apollo/client';
import { UseCaseResponseDTO } from '../models/use-case-response.dto';
import { ValidationResultDTO } from '../models/validation-result-dto.model';
import { VALIDATE_TRANSIT_TIME, ValidateTransitTimeRequestDTO } from '../graphql/transit-time/validate-transit-time.graphql';

@Injectable({
    providedIn: 'root',
})
export class TransitTimeService {
    private readonly servicePath = '/receiving/graphql';

    constructor(private dynamicGraphqlPathService: DynamicGraphqlPathService) {}

    public validateTransitTime(validateTransitTimeRequestDTO: ValidateTransitTimeRequestDTO)
        : Observable<ApolloQueryResult<{ validateTransitTime: UseCaseResponseDTO<ValidationResultDTO> }>> {
        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            VALIDATE_TRANSIT_TIME,
            validateTransitTimeRequestDTO
        );
    }
}