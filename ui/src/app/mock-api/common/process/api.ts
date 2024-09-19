import { Injectable } from '@angular/core';
import { FuseMockApiService } from '@fuse/lib/mock-api';
import { cloneDeep } from 'lodash-es';
import { process, productVersion } from './data';

@Injectable({ providedIn: 'root' })
export class ProcessMockApi {
    private readonly _process = process;
    private readonly _productVersion = productVersion;

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
            .onGet('/v1/processes/products/:uuid')
            .reply(() => {
                // Return the response
                return [200, cloneDeep(this._process)];
            });

        this._fuseMockApiService
            .onGet('/v1/processes/products-version/:uuid')
            .reply(() => {
                // Return the response
                return [200, cloneDeep(this._productVersion)];
            });
    }
}
