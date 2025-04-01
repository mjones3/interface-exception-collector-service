import { AsyncPipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatStepperModule } from '@angular/material/stepper';
import { ProcessHeaderComponent, ProcessHeaderService } from '@shared';
import { ActionButtonComponent } from 'app/shared/components/buttons/action-button.component';
import { ShipmentFilterDTO } from '../../models/recovered-plasma.dto';
import { FilterShipmentComponent } from '../filter-shipment/filter-shipment.component';
import { CreateShipmentComponent } from '../create-shipment/create-shipment.component';
import { RecoveredPlasmaService } from '../../services/recovered-plasma.service';
import { Cookie } from 'app/shared/types/cookie.enum';
import { CookieService } from 'ngx-cookie-service';

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
    isRecoveredPlasmaFacility: boolean = false;
    get facilityCode(){
        return this.cookieService.get(Cookie.XFacility);
    }

    constructor(
        public header: ProcessHeaderService,
        private matDialog: MatDialog,
        private recoveredPlasmaService: RecoveredPlasmaService,
        private cookieService: CookieService
    ) {}

    ngOnInit(): void {
        this.checkRecoveredPlasmaFacility();
    }


    checkRecoveredPlasmaFacility() {
         this.recoveredPlasmaService.checkRecoveredPlasmaFacility(this.facilityCode).subscribe((res) => {
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
