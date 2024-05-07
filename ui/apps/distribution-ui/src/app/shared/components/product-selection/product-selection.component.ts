import { HttpResponse } from '@angular/common/http';
import { AfterViewInit, Component, EventEmitter, Input, OnInit, Output, TemplateRef, ViewChild } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import {
  BarcodeService,
  BarcodeTranslationResponseDTO,
  FacilityService,
  InventoryDto,
  InventoryService,
  OrderDto,
  OrderItemProductDTO,
  ProductsService,
  RsaValidators,
  TranslateInterpolationPipe,
  ValidateRuleDto,
  ValidationType,
} from '@rsa/commons';
import { OrderProduct } from '@rsa/distribution/core/models/orders.model';
import { OptionsPickerDialogComponent } from '@rsa/touchable';
import { startCase } from 'lodash';
import { ToastrService } from 'ngx-toastr';
import { Observable, of } from 'rxjs';
import { catchError, finalize, switchMap } from 'rxjs/operators';

@Component({
  selector: 'rsa-product-selection',
  templateUrl: './product-selection.component.html',
})
export class ProductSelectionComponent implements AfterViewInit {
  constructor(
    protected fb: FormBuilder,
    private barcodeService: BarcodeService,
    private inventoryService: InventoryService,
    private facilityService: FacilityService,
    private toaster: ToastrService,
    private translateInterpolationPipe: TranslateInterpolationPipe,
    private productService: ProductsService,
    private matDialog: MatDialog
  ) {
    this.productGroup = fb.group({
      unitNumber: ['', RsaValidators.unitNumber],
      productCode: ['', RsaValidators.fullProductCode],
    });
  }
  get isLabeledOrder() {
    return this.currentOrder && this.currentOrder.labelStatus && this.currentOrder.labelStatus !== 'UNLABELED';
  }
  readonly validationType = ValidationType;

  @Input()
  orderProductId: number;

  @Input() shouldProceedWithProductSelection: () => boolean;

  @Input() selectedInventoryIds = new Array<number>();

  @Input()
  currentOrder: OrderDto;

  @Output()
  updateLoadingState = new EventEmitter<boolean>();

  @ViewChild('optionDescriptionTpl') optionDescriptionTpl: TemplateRef<any>;

  productGroup: FormGroup;
  unitNumberFocus = true;
  productCodeFocus = false;

  @Output()
  unitNumberProductCodeSelected = new EventEmitter<{ unitNumber: string; productCode: string }>();

  protected readonly ValidationType = ValidationType;

  ngAfterViewInit(): void {
    if (!this.isLabeledOrder) {
      this.productGroup.removeControl('productCode');
    }
  }

  resetFormGroup() {
    this.productGroup.reset();
  }

  onUnitNumberKeyOrTab(event: Event) {
    event.preventDefault();
    this.unitNumberFocus = false;
    this.productCodeFocus = true;
    this.onEnterKeyOrTab('unitNumber');
  }

  onProductCodeKeyOrTab(event: Event) {
    event.preventDefault();
    this.onEnterKeyOrTab('productCode');
  }

  onEnterKeyOrTab(controlName: 'unitNumber' | 'productCode') {
    const value = this.productGroup.get(controlName).value;
    if (value) {
      this.barcodeService
        .getBarcodeTranslation(value)
        .pipe(
          catchError(() =>
            of({ body: { barcodeTranslation: { [controlName]: value } } } as HttpResponse<
              BarcodeTranslationResponseDTO
            >)
          ),
          switchMap(
            (response): Observable<{ unitNumber: string; productCode?: string }> => {
              const translations = response?.body?.barcodeTranslation;

              this.productGroup.patchValue(translations);

              this.unitNumberFocus = false;
              this.productCodeFocus = this.isLabeledOrder && controlName === 'unitNumber';

              this.updateLoadingState.emit(
                (this.isLabeledOrder && controlName === 'productCode') ||
                  (!this.isLabeledOrder && controlName === 'unitNumber')
              );

              if (this.shouldProceedWithProductSelection != null && !this.shouldProceedWithProductSelection()) {
                return of(null);
              }

              if (controlName === 'productCode' && this.productGroup.valid && this.isLabeledOrder) {
                return of({
                  unitNumber: this.productGroup.value.unitNumber,
                  productCode: this.productGroup.value.productCode,
                });
              } else if (controlName === 'unitNumber' && this.productGroup.valid && !this.isLabeledOrder) {
                return of({ unitNumber: this.productGroup.value.unitNumber, productCode: null });
              }
              return of(null);
            }
          ),
          switchMap(
            (unitNumberOrProductCode): Observable<{ unitNumber: string; productCode: string }> => {
              if (!unitNumberOrProductCode) {
                return of(null);
              }
              if (unitNumberOrProductCode.productCode) {
                return of({
                  productCode: unitNumberOrProductCode.productCode,
                  unitNumber: unitNumberOrProductCode.unitNumber,
                });
              } else {
                return this.validateUnlabeledUnitNumber(this.productGroup.value.unitNumber).pipe(
                  switchMap(tempResponse => {
                    if (tempResponse) {
                      return of({
                        productCode: tempResponse?.productCode,
                        unitNumber: unitNumberOrProductCode.unitNumber,
                      });
                    }
                  })
                );
              }
            }
          )
        )
        .subscribe(unitNumberAndProductCode => {
          if (unitNumberAndProductCode) {
            this.unitNumberProductCodeSelected.emit(unitNumberAndProductCode);
          }
        });
    }
  }

  validateUnlabeledUnitNumber(unitNumber: string): Observable<InventoryDto> {
    return this.inventoryService
      .validate({
        unitNumber,
        ruleName: 'rul-0115-get-products-by-unit-number-and-status',
        facilityId: this.facilityService.getFacilityId(),
        productCategoryKey: this.currentOrder.productCategoryKey,
        inventoryIDList: this.selectedInventoryIds,
      })
      .pipe(
        switchMap(response => {
          const notification = response.body?.notifications?.length ? response.body.notifications[0] : null;
          if (notification) {
            this.toaster.show(
              this.translateInterpolationPipe.transform(notification.message, []),
              startCase(notification.notificationType),
              {},
              notification.notificationType
            );
          } else {
            const inventories = response.body?.results?.inventories?.[0] ?? [];
            if (inventories.length === 1) {
              return of(inventories[0]);
            } else if (inventories.length > 1) {
              return this.selectInventoryForUnitNumber(inventories);
            }
          }
          return of(null);
        }),
        finalize(() => {
          this.updateLoadingState.emit(false);
          this.unitNumberFocus = true;
        })
      );
  }

  selectInventoryForUnitNumber(inventories: InventoryDto[]) {
    return this.productService.getProductsByProductCodes(inventories.map(i => i.productCode)).pipe(
      switchMap(products => {
        const dialogRef = this.matDialog.open(OptionsPickerDialogComponent, {
          data: {
            dialogTitle: 'product-selection.label',
            options: inventories.map(i => ({
              ...i,
              icon: products.body.find(p => p.productCode === i.productCode)?.properties['icon'],
            })),
            optionsLabel: 'descriptionKey',
          },
        });
        dialogRef.componentInstance.optionDescriptionTpl = this.optionDescriptionTpl;
        return dialogRef.afterClosed().pipe(switchMap((inventory: InventoryDto) => of(inventory)));
      })
    );
  }
}
