import { Injectable } from '@angular/core';
import { FuseNavigationItem } from '@fuse/components/navigation';
import { FuseMockApiService } from '@fuse/lib/mock-api';
import { EnvironmentConfigService } from '@shared';
import {
    defaultNavigation,
    location,
    locations,
} from 'app/mock-api/common/navigation/data';
import { cloneDeep } from 'lodash-es';

@Injectable({ providedIn: 'root' })
export class NavigationMockApi {
    private readonly _defaultNavigation: FuseNavigationItem[] =
        defaultNavigation;
    private readonly _locations: any[] = locations;
    private readonly _location: any = location;

    /**
     * Constructor
     */
    constructor(
        private environmentConfigService: EnvironmentConfigService,
        private _fuseMockApiService: FuseMockApiService
    ) {}

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
            .onGet(`${this.environmentConfigService.env.serverApiURL}/v1/menus`)
            .reply(() => {
                // Return the response
                return [200, cloneDeep(this._defaultNavigation)];
            });

        this._fuseMockApiService
            .onGet(
                `${this.environmentConfigService.env.serverApiURL}/v1/locations`
            )
            .reply(() => {
                // Return the response
                return [200, cloneDeep(this._locations)];
            });

        this._fuseMockApiService
            .onGet(
                `${this.environmentConfigService.env.serverApiURL}/v1/facilities/:id`
            )
            .reply(() => {
                // Return the response
                return [200, cloneDeep(this._location)];
            });
    }
}
