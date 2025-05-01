import { Component, computed, EventEmitter, Input, OnInit, Output, signal, ViewChild } from '@angular/core';
import { RecoveredPlasmaShipmentCommon } from '../../recovered-plasma-shipment.common';
import { CartonDTO } from '../../models/recovered-plasma.dto';
import { ScanUnitNumberProductCodeComponent } from 'app/shared/components/scan-unit-number-product-code/scan-unit-number-product-code.component';
import { CookieService } from 'ngx-cookie-service';
import { Router, ActivatedRoute } from '@angular/router';
import { Store } from '@ngrx/store';
import { ToastrImplService } from '@shared';
import { ProductIconsService } from 'app/shared/services/product-icon.service';
import { RecoveredPlasmaService } from '../../services/recovered-plasma.service';
import { MatDividerModule } from '@angular/material/divider';
import { ActionButtonComponent } from 'app/shared/components/buttons/action-button.component';
import { UnitNumberCardComponent } from 'app/shared/components/unit-number-card/unit-number-card.component';
import { ShippingCartonInformationCardComponent } from '../../shared/shipping-carton-information-card/shipping-carton-information-card.component';
import { ShippingInformationCardComponent } from '../../shared/shipping-information-card/shipping-information-card.component';
import { FuseCardComponent } from '@fuse/components/card/public-api';

@Component({
  selector: 'biopro-verify-recovered-plasma-products',
  standalone: true,
  imports: [
        MatDividerModule,
        UnitNumberCardComponent,
        ShippingInformationCardComponent,
        ShippingCartonInformationCardComponent,
        ActionButtonComponent,
        FuseCardComponent,
        ScanUnitNumberProductCodeComponent,
  ],
  templateUrl: './verify-recovered-plasma-products.component.html'
})
export class VerifyRecoveredPlasmaProductsComponent 
extends RecoveredPlasmaShipmentCommon implements OnInit
{
@Input() cartonDetails = signal<CartonDTO>(null);
@ViewChild('scanUnitNumberProductCode')
scanUnitNumberProductCode: ScanUnitNumberProductCodeComponent;
@Output()verifyUnitNumberProductCode: EventEmitter<CartonDTO> = new EventEmitter<CartonDTO>();

constructor(
    protected router: Router,
    protected route: ActivatedRoute,
    protected store: Store,
    protected toastr: ToastrImplService,
    protected productIconService: ProductIconsService,
    protected recoveredPlasmaService: RecoveredPlasmaService,
    protected cookieService: CookieService,
) {
    super(
        route,
        router,
        store,
        recoveredPlasmaService,
        toastr,
        productIconService,
        cookieService
    );
}

ngOnInit(): void {
    this.disableInputsIfMaxCartonProduct()
}

verifyProducts(event): void {
    this.verifyUnitNumberProductCode.emit(event);
}

resetProductGroup(): void {
    this.scanUnitNumberProductCode?.resetUnitProductGroup();
}

focusOnUnitNumber(): void {
    this.scanUnitNumberProductCode?.focusOnUnitNumber();
}

disableInputsIfMaxCartonProduct(): void {
    if (!this.cartonDetails()?.canVerify) 
    {
        this.disableProductGroup();
    }
}

disableProductGroup(): void {
    this.scanUnitNumberProductCode?.disableUnitProductGroup();
}
}