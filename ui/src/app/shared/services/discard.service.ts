import { Injectable } from '@angular/core';
import { MutationResult } from 'apollo-angular';
import { Observable } from 'rxjs';
import { DynamicGraphqlPathService } from '../../core/services/dynamic-graphql-path.service';
import { ADD_DISCARD } from '../graphql/discard/mutation-definitions/discard.graphql';
import { DiscardRequestDTO, DiscardResponseDTO } from '../models/discard.model';

@Injectable({
    providedIn: 'root',
})
export class DiscardService {
    readonly servicePath = '/discard/graphql';

    constructor(private dynamicGraphqlPathService: DynamicGraphqlPathService) {}

    public discardProduct(
        discardRequest: DiscardRequestDTO
    ): Observable<MutationResult<{ discardProduct: DiscardResponseDTO }>> {
        return this.dynamicGraphqlPathService.executeMutation(
            this.servicePath,
            ADD_DISCARD,
            { discardRequest }
        );
    }
}
