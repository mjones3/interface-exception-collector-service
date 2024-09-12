import { Injectable } from '@angular/core';
import { FuseMockApiService } from '@fuse/lib/mock-api';
import { cloneDeep } from 'lodash-es';
import { discardResponse } from './data';

@Injectable({ providedIn: 'root' })
export class DiscardMockApi {
    private readonly _discardResponse = discardResponse;

    /**
     * Constructor
     */
    constructor(private _fuseMockApiService: FuseMockApiService) {
        this.registerHandlers();
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Public methods
    // -----------------------------------------------------------------------------------------------------

    /**
     * Register Mock API handlers
     */
    registerHandlers(): void {
        // -----------------------------------------------------------------------------------------------------
        // @ Navigation - GET
        // -----------------------------------------------------------------------------------------------------
        this._fuseMockApiService
            .onPost('http://localhost:4200/discard/graphql')
            .reply(() => {
                // Return the response
                return [
                    200,
                    {
                        errors: [],
                        data: {
                            discardProduct: cloneDeep(this._discardResponse),
                        },
                    },
                ];
            });
    }
}
