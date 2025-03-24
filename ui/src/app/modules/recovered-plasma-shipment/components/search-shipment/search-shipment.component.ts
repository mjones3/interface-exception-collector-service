import { AsyncPipe } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatStepperModule } from '@angular/material/stepper';
import { ProcessHeaderComponent, ProcessHeaderService } from '@shared';
import { ShipmentFilterDTO } from '../../models/recovered-plasma.dto';
import { FilterShipmentComponent } from '../filter-shipment/filter-shipment.component';

@Component({
    selector: 'app-search-shipment',
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
        AsyncPipe,
    ],
    templateUrl: './search-shipment.component.html',
})
export class SearchShipmentComponent {
    isFilterToggled = false;
    currentFilter: ShipmentFilterDTO;

    constructor(public header: ProcessHeaderService) {}

    toggleFilter(toggleFlag: boolean): void {
        this.isFilterToggled = toggleFlag;
    }
}
