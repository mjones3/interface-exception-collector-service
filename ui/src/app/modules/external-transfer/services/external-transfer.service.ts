import { Injectable } from '@angular/core';
import { ApolloQueryResult } from '@apollo/client';
import { MutationResult } from 'apollo-angular';
import { DynamicGraphqlPathService } from 'app/core/services/dynamic-graphql-path.service';
import { ALL_CUSTOMER_LIST_INFO } from 'app/shared/graphql/discard/mutation-definitions/customer-list.graphql';
import { RuleResponseDTO } from 'app/shared/models/rule.model';
import { Observable } from 'rxjs';
import {
    COMPLETE_EXTERNAL_TRANSFER,
    EXTERNAL_TRANSFER_ITEM,
} from '../graphql/external-transfer-item.graphql';
import {
    CANCEL_EXTERNAL_TRANSFER_PROCESS,
    CONFIRM_CANCEL_EXTERNAL_TRANSFER_PROCESS,
    CancelExternalTransferRequest,
    VERIFY_TRANSFER_INFO,
} from '../graphql/external-transfer.graphql';
import {
    CreateExternalTransferRequestDTO,
    CustomerOptionDTO,
    ExternalTransferItemDTO,
} from '../models/external-transfer.dto';

@Injectable({
    providedIn: 'root',
})
export class ExternalTransferService {
    readonly servicePath = 'recoveredplasmashipping/graphql';

    constructor(private dynamicGraphqlPathService: DynamicGraphqlPathService) {}

    public createExternalTransferInfo(
        VerifyTransferInfo: CreateExternalTransferRequestDTO
    ): Observable<MutationResult<{ createExternalTransfer: RuleResponseDTO }>> {
        return this.dynamicGraphqlPathService.executeMutation(
            this.servicePath,
            VERIFY_TRANSFER_INFO,
            VerifyTransferInfo
        );
    }

    public customerInfo(): Observable<
        ApolloQueryResult<{ findAllCustomers: CustomerOptionDTO }>
    > {
        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            ALL_CUSTOMER_LIST_INFO
        );
    }

    public verifyExternalTransferItem(
        verifyExternalTransferProduct: ExternalTransferItemDTO
    ): Observable<
        MutationResult<{ addExternalTransferProduct: RuleResponseDTO }>
    > {
        return this.dynamicGraphqlPathService.executeMutation(
            this.servicePath,
            EXTERNAL_TRANSFER_ITEM,
            verifyExternalTransferProduct
        );
    }

    public completeExternalTransfer(inputs: {
        externalTransferId: number;
        hospitalTransferId: string;
        employeeId: string;
    }): Observable<
        MutationResult<{ completeExternalTransfer: RuleResponseDTO }>
    > {
        return this.dynamicGraphqlPathService.executeMutation(
            this.servicePath,
            COMPLETE_EXTERNAL_TRANSFER,
            inputs
        );
    }

    public cancelExternalTransferProcess(
        cancelExternalTransferRequest: CancelExternalTransferRequest
    ): Observable<
        MutationResult<{
            cancelExternalTransfer: RuleResponseDTO;
        }>
    > {
        return this.dynamicGraphqlPathService.executeMutation(
            this.servicePath,
            CANCEL_EXTERNAL_TRANSFER_PROCESS,
            cancelExternalTransferRequest
        );
    }

    public confirmCancelExternalTransferProcess(
        confirmCancelExternalTransferProcessRequest: CancelExternalTransferRequest
    ): Observable<
        MutationResult<{
            confirmCancelExternalTransfer: RuleResponseDTO;
        }>
    > {
        return this.dynamicGraphqlPathService.executeMutation(
            this.servicePath,
            CONFIRM_CANCEL_EXTERNAL_TRANSFER_PROCESS,
            confirmCancelExternalTransferProcessRequest
        );
    }
}
