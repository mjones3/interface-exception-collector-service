import { AsyncPipe } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatStepperModule } from '@angular/material/stepper';
import {
    LookUpDto,
    ProcessHeaderComponent,
    ProcessHeaderService,
} from '@shared';
import { ActionButtonComponent } from 'app/shared/components/buttons/action-button.component';
import { ShipmentFilterDTO } from '../../models/recovered-plasma.dto';
import { FilterShipmentComponent } from '../filter-shipment/filter-shipment.component';
import { CreateShipmentComponent } from '../create-shipment/create-shipment.component';
import { RecoveredPlasmaService } from '../../services/recovered-plasma.service';
import { Cookie } from 'app/shared/types/cookie.enum';
import { CookieService } from 'ngx-cookie-service';
import { forkJoin } from 'rxjs';
import { RecoveredPlasmaLocationDTO } from '../../graphql/query-definitions/location.graphql';
import { RecoveredPlasmaCustomerDTO } from '../../graphql/query-definitions/customer.graphql';
import { PageDTO } from '../../../../shared/models/page.model';
import { RecoveredPlasmaShipmentReportDTO } from '../../graphql/query-definitions/shipment.graphql';

@Component({
    selector: 'biopro-search-shipment',
    standalone: true,
    imports: [
        MatButtonModule,
        MatStepperModule,
        FormsModule,
        ReactiveFormsModule,
        ProcessHeaderComponent,
        MatFormFieldModule,
        MatInputModule,
        FilterShipmentComponent,
        ActionButtonComponent,
        AsyncPipe,
    ],
    templateUrl: './search-shipment.component.html',
})
export class SearchShipmentComponent implements OnInit {
    isFilterToggled = false;
    currentFilter: ShipmentFilterDTO;

    recoveredPlasmaShipments = signal<PageDTO<RecoveredPlasmaShipmentReportDTO>>(null);
    locations = signal<RecoveredPlasmaLocationDTO[]>([]);
    customers = signal<RecoveredPlasmaCustomerDTO[]>([]);
    productTypes = signal<LookUpDto[]>([]);
    shipmentTypes = signal<LookUpDto[]>([]);

    isRecoveredPlasmaFacility = false;

    get facilityCode() {
        return this.cookieService.get(Cookie.XFacility);
    }

    constructor(
        public header: ProcessHeaderService,
        private matDialog: MatDialog,
        private recoveredPlasmaService: RecoveredPlasmaService,
        private cookieService: CookieService
    ) {}

    ngOnInit(): void {
        this.load();
        this.checkRecoveredPlasmaFacility();
    }

    load(): void {
        forkJoin({
            searchShipments:
                this.recoveredPlasmaService.searchRecoveredPlasmaShipments({
                    locationCode: [this.facilityCode],
                }),
            locationsResponse: this.recoveredPlasmaService.findAllLocations(),
            customersResponse: this.recoveredPlasmaService.findAllCustomers(),
            productTypes:
                this.recoveredPlasmaService.findAllLookupsByType(
                    'RPS_PRODUCT_TYPE'
                ),
            shipmentTypes:
                this.recoveredPlasmaService.findAllLookupsByType(
                    'RPS_SHIPMENT_TYPE'
                ),
        }).subscribe(
            ({
                searchShipments,
                locationsResponse,
                customersResponse,
                productTypes,
                shipmentTypes,
            }) => {
                this.recoveredPlasmaShipments.set(searchShipments.data.searchShipment.data)
                this.locations.set(locationsResponse.data.findAllLocations);
                this.customers.set(customersResponse.data.findAllCustomers);
                this.productTypes.set(productTypes.data.findAllLookupsByType);
                this.shipmentTypes.set(shipmentTypes.data.findAllLookupsByType);
            }
        );
    }

    checkRecoveredPlasmaFacility() {
        this.recoveredPlasmaService
            .checkRecoveredPlasmaFacility(this.facilityCode)
            .subscribe((res) => {
                this.isRecoveredPlasmaFacility = res;
            });
    }

    toggleFilter(toggleFlag: boolean): void {
        this.isFilterToggled = toggleFlag;
    }

    openCreateShipment() {
        this.matDialog.open(CreateShipmentComponent, {
            width: '50rem',
            disableClose: true,
        });
    }
}
