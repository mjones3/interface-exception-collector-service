import { Injectable } from '@angular/core';
import { DynamicGraphqlPathService } from '../../../core/services/dynamic-graphql-path.service';
import { Observable } from 'rxjs';
import { ApolloQueryResult } from '@apollo/client';
import { UseCaseResponseDTO } from '../../../shared/models/use-case-response.dto';
import { VALIDATE_TRANSFER_ORDER_NUMBER, ValidateTransferOrderNumberDTO } from '../graphql/query-definitions/transfer-receipt-order-number-validation.graphql';
import { ValidationResultDTO } from 'app/modules/imports/models/validation-result-dto.model';
import { TransferInformationDTO } from '../models/internal-transfer-order.dto';

@Injectable({
    providedIn: 'root',
})
export class TransferReceiptService {

    private readonly servicePath = '/receiving/graphql';

    constructor(private dynamicGraphqlPathService: DynamicGraphqlPathService) {
    }


    public validateTransferOrderNumber(validateTransferOrderNumberDTO: ValidateTransferOrderNumberDTO)
        : Observable<ApolloQueryResult<{ validateTransferOrderNumber: UseCaseResponseDTO<TransferInformationDTO> }>> {

        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            VALIDATE_TRANSFER_ORDER_NUMBER,
            validateTransferOrderNumberDTO
        );
    }

}
