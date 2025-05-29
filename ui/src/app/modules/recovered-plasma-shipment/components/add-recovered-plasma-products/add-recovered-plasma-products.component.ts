import { AsyncPipe, CommonModule, JsonPipe } from '@angular/common';
import { Component, EventEmitter, Output, ViewChild, computed, input } from '@angular/core';
import { FormBuilder,  } from '@angular/forms';
import { MatDividerModule } from '@angular/material/divider';
import { ActivatedRoute, Router } from '@angular/router';
import { FuseCardComponent } from '@fuse/components/card/public-api';
import { Store } from '@ngrx/store';
import {
    ProcessHeaderComponent,
    ProcessHeaderService,
    ScanUnitNumberCheckDigitComponent,
} from '@shared';
import { ActionButtonComponent } from 'app/shared/components/buttons/action-button.component';
import { ScanUnitNumberProductCodeComponent } from 'app/shared/components/scan-unit-number-product-code/scan-unit-number-product-code.component';
import { UnitNumberCardComponent } from 'app/shared/components/unit-number-card/unit-number-card.component';
import { ProductIconsService } from 'app/shared/services/product-icon.service';
import { CookieService } from 'ngx-cookie-service';
import {
    CartonDTO,
    CartonPackedItemResponseDTO,
} from '../../models/recovered-plasma.dto';
import { RecoveredPlasmaShipmentCommon } from '../../recovered-plasma-shipment.common';
import { RecoveredPlasmaService } from '../../services/recovered-plasma.service';
import { ShippingCartonInformationCardComponent } from '../../shared/shipping-carton-information-card/shipping-carton-information-card.component';
import { ShippingInformationCardComponent } from '../../shared/shipping-information-card/shipping-information-card.component';
import { ToastrService } from 'ngx-toastr';
import { BasicButtonComponent } from 'app/shared/components/buttons/basic-button.component';

@Component({
    selector: 'biopro-add-recovered-plasma-products',
    standalone: true,
    imports: [
        AsyncPipe,
        ProcessHeaderComponent,
        CommonModule,
        MatDividerModule,
        ScanUnitNumberCheckDigitComponent,
        UnitNumberCardComponent,
        ShippingInformationCardComponent,
        ShippingCartonInformationCardComponent,
        ActionButtonComponent,
        FuseCardComponent,
        BasicButtonComponent,
        ScanUnitNumberProductCodeComponent
    ],
    templateUrl: './add-recovered-plasma-products.component.html',
})
export class AddRecoveredPlasmaProductsComponent
    extends RecoveredPlasmaShipmentCommon
{
    maxProductsComputed = computed(
        () => this.cartonDetails()?.maxNumberOfProducts
    );
    selectedProducts: CartonPackedItemResponseDTO[] = [];
    minProductsComputed = computed(
        () => this.cartonDetails()?.minNumberOfProducts
    );
    @ViewChild('scanUnitNumberProductCode') scanUnitNumberProductCode: ScanUnitNumberProductCodeComponent;
    cartonDetails = input<CartonDTO>();
    @Output()unitNumberProductCode: EventEmitter<CartonDTO> = new EventEmitter<CartonDTO>();
    @Output()removeCartonProducts: EventEmitter<number[]> = new EventEmitter<number[]>();

    constructor(
        public header: ProcessHeaderService,
        protected fb: FormBuilder,
        protected router: Router,
        protected route: ActivatedRoute,
        protected store: Store,
        protected toastr: ToastrService,
        protected productIconService: ProductIconsService,
        protected recoveredPlasmaService: RecoveredPlasmaService,
        protected cookieService: CookieService
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

    addProduct(event): void {
        this.unitNumberProductCode.emit(event);
    }

    getPackedProducts(){
        return this.cartonDetails()?.packedProducts ?? [];
    }

    disableInputsIfMaxCartonProduct(cartonDetails): void {
        if (
            cartonDetails?.packedProducts?.length ===
            cartonDetails?.maxNumberOfProducts
        ) {
            this.scanUnitNumberProductCode?.disableUnitProductGroup();
        }else{
            this.scanUnitNumberProductCode?.resetUnitProductGroup();
        }
    }

    disableProductGroup() {
        this.scanUnitNumberProductCode?.disableUnitProductGroup();
    }

    resetProductGroup() {
        this.scanUnitNumberProductCode?.resetUnitProductGroup();
    }

    focusOnUnitNumber() {
        this.scanUnitNumberProductCode?.focusOnUnitNumber();
    }

    toggleProduct(product: CartonPackedItemResponseDTO) {
        
        if (this.selectedProducts.includes(product)) {
            const index = this.selectedProducts.findIndex(
                (filterProduct) =>
                    filterProduct.id === product.id 
            );
            this.selectedProducts.splice(index, 1);
        } else {
            this.selectedProducts.push(product);
        }
    }

    get numberOfProducts() {
        return this.cartonDetails()?.packedProducts?.length || 0;
    }

    get numberOfSelectedProducts() {
        return this.selectedProducts.length;
    }

    get selectAllTextRule() {
        return (
            this.numberOfSelectedProducts < this.numberOfProducts ||
            this.numberOfProducts === 0
        );
    }

    selectAllProducts() {
        if (this.selectedProducts.length === this.cartonDetails().packedProducts.length) {
            this.selectedProducts = [];
        } else {
            this.selectedProducts = [].concat(this.cartonDetails().packedProducts);
        }
    }

    removeSelectedProducts(): void {
        const cartonItemIds: number[] = [];
        this.selectedProducts.forEach((product) => {
            cartonItemIds.push(product.id);
        });
        this.removeCartonProducts.emit([...cartonItemIds]);
    }
}