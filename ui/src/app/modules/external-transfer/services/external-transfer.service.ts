import { Injectable } from '@angular/core';
import { ApolloQueryResult } from '@apollo/client';
import { MutationResult } from 'apollo-angular';
import { DynamicGraphqlPathService } from 'app/core/services/dynamic-graphql-path.service';
import { ALL_CUSTOMER_LIST_INFO } from 'app/shared/graphql/discard/mutation-definitions/customer-list.graphql';
import { RuleResponseDTO } from 'app/shared/models/rule.model';
import { Observable } from 'rxjs';
import { VERIFY_TRANSFER_INFO } from '../graphql/external-transfer.graphql';
import {
    CreateTransferInfoDTO,
    customerOptionDto,
} from '../models/external-transfer.dto';

@Injectable({
    providedIn: 'root',
})
export class ExternalTransferService {
    readonly servicePath = '/shipping/graphql';

    constructor(private dynamicGraphqlPathService: DynamicGraphqlPathService) {}

    public createExternalTransferInfo(
        VerifyTransferInfo: CreateTransferInfoDTO
    ): Observable<MutationResult<{ createExternalTransfer: RuleResponseDTO }>> {
        return this.dynamicGraphqlPathService.executeMutation(
            this.servicePath,
            VERIFY_TRANSFER_INFO,
            VerifyTransferInfo
        );
    }

    public customerInfo(): Observable<
        ApolloQueryResult<{ findAllCustomers: customerOptionDto }>
    > {
        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            ALL_CUSTOMER_LIST_INFO
        );
    }
}
