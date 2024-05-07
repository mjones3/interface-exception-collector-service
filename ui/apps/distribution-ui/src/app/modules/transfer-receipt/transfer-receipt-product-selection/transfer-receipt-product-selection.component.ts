import { HttpResponse } from '@angular/common/http';
import {
  AfterViewInit,
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnInit,
  Output,
  TemplateRef,
  ViewChild,
} from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import {
  BarcodeService,
  BarcodeTranslationResponseDTO,
  Column,
  commonRegex,
  InventoryService,
  NotificationDto,
  OrderDto,
  ProductsService,
  RsaValidators,
  RuleResponseDto,
  RulesService,
  ValidationType,
} from '@rsa/commons';
import {
  ProductSelectionItemModel,
  ProductSelectionRuleModel,
} from '@rsa/distribution/core/models/internal-transfers.model';
import { OptionsPickerDialogComponent } from '@rsa/touchable';
import { ToastrService } from 'ngx-toastr';
import { Observable, of } from 'rxjs';
import { catchError, finalize, map, switchMap, tap } from 'rxjs/operators';

@Component({
  selector: 'rsa-transfer-receipt-product-selection',
  templateUrl: `./transfer-receipt-product-selection.component.html`,
})
export class TransferReceiptProductSelectionComponent implements OnInit, AfterViewInit {
  constructor(
    public toaster: ToastrService,
    private matDialog: MatDialog,
    private fb: FormBuilder,
    private ruleService: RulesService,
    private inventoryService: InventoryService,
    public barcodeService: BarcodeService,
    private productService: ProductsService
  ) {
    this.productSelectionGroup = fb.group({
      unitNumber: ['', [Validators.required, RsaValidators.unitNumber]],
      productCode: ['', [Validators.required, RsaValidators.fullProductCode]],
    });
  }

  get isUnlabeled() {
    return this.currentOrder?.labelStatus === 'UNLABELED';
  }

  get isLabeled() {
    return this.currentOrder?.labelStatus === 'LABELED';
  }

  products: Partial<ProductSelectionItemModel>[] = [];
  @Input()
  markForQuarantine: boolean;
  @Input()
  transferInfoGroup: FormGroup;
  @Input()
  currentOrder: OrderDto;
  @Input()
  productSelectionGroup: FormGroup;

  @Output()
  rsaOnProductSelectionChange = new EventEmitter<{
    products: Partial<ProductSelectionItemModel>[];
    markForQuarantine?: boolean;
  }>();

  unitNumberFocus = true;
  readonly validationType = ValidationType;

  @ViewChild('optionDescriptionTpl') optionDescriptionTpl: TemplateRef<any>;

  columns: Column[];
  selectedColumns: Column[];

  @ViewChild('unitNumberInput')
  unitNumberInput: ElementRef<HTMLInputElement>;

  @ViewChild('productCodeInput')
  productCodeInput: ElementRef<HTMLInputElement>;

  ngOnInit() {
    this.columns = [
      {
        field: 'unitNumber',
        header: 'unit-number.label',
        headerCls: 'w-30 ml-6',
      },
      {
        field: 'productCode',
        header: 'product-code.label',
        headerCls: 'w-30',
        hidden: this.isUnlabeled,
      },
      {
        field: 'descriptionKey',
        header: 'description.label',
        headerCls: 'w-30',
      },
      {
        field: 'quarantine',
        header: 'quarantine.label',
        headerCls: 'w-40',
        templateRef: 'quarantineTpl',
        hidden: true,
      },
      {
        field: '',
        header: '',
        templateRef: 'actionTpl',
        headerCls: 'w-40',
        hideHeader: true,
      },
    ];
    this.selectedColumns = this.columns.filter(col => !col.hidden);
  }

  ngAfterViewInit(): void {
    setTimeout(() => this.unitNumberInput.nativeElement.focus());
  }

  remove(product: ProductSelectionItemModel) {
    const index = this.products.indexOf(product);
    this.products.splice(index, 1);
    this.handleQuarantineField();
    this.rsaOnProductSelectionChange.emit({ products: this.products });
  }

  productIsNotAddedAlready(inventoryId: number) {
    return this.products.filter(product => product.id === inventoryId).length === 0;
  }

  removeAll() {
    this.products = [];
    this.productSelectionGroup?.reset();
    this.handleQuarantineField();
    this.rsaOnProductSelectionChange.emit({ products: this.products });
  }

  public addProduct(product: Partial<ProductSelectionItemModel>) {
    if (this.productIsNotAddedAlready(product.id)) {
      const obs = this.validateSelectedProduct(product, '');

      obs.subscribe(response => {
        if (this.getSuccessNotification(response)) {
          const markForQuarantine = response.body.results?.markForQuarantine[0];
          this.addValidatedProduct(product, markForQuarantine);
        }
      });
    }
  }

  private addValidatedProduct(product: Partial<ProductSelectionItemModel>, markForQuarantine = false) {
    product.quarantine = markForQuarantine;
    this.products = [...this.products, product];
    this.handleQuarantineField();
    this.productSelectionGroup?.reset();
    this.rsaOnProductSelectionChange.emit({
      products: this.products,
      markForQuarantine: markForQuarantine || this.markForQuarantine,
    });
  }

  private handleQuarantineField(): void {
    if (this.products.find(p => p.quarantine) || this.markForQuarantine) {
      this.columns.find(c => c.field === 'quarantine').hidden = false;
    } else {
      this.columns.find(c => c.field === 'quarantine').hidden = true;
    }
    this.selectedColumns = this.columns.filter(col => !col.hidden);
  }

  private validateSelectedProduct(product: Partial<ProductSelectionItemModel>, isbtProductCode: string) {
    return this.inventoryService
      .validate({
        ruleName: 'rul-0117-transfer-receipt-product-selected-validation',
        unitNumber: product.unitNumber,
        productCode: product.productCode,
        isbtProductCode: isbtProductCode,
        orderId: this.currentOrder.id,
        labelStatus: this.currentOrder.labelStatus,
      })
      .pipe(
        map(response => {
          this.handleNotificationsAndErrors(response);
          return response;
        })
      );
  }

  private validateUnitNumber(unitNumber: string): Observable<HttpResponse<ProductSelectionRuleModel>> {
    return this.inventoryService
      .validate({
        ruleName: 'rul-0115-get-products-by-unit-number-and-status',
        unitNumber: unitNumber,
        orderId: this.currentOrder.id,
        transferDate: this.currentOrder.createDate.split('T').shift(),
        status: 'in-transit',
        productCategoryKey: this.currentOrder.productCategoryKey,
      })
      .pipe(
        map(response => {
          this.handleNotificationsAndErrors(response);
          return response;
        })
      );
  }

  private handleNotificationsAndErrors(ruleResult: HttpResponse<RuleResponseDto>): void {
    if (ruleResult.status !== 200 || ruleResult.body.ruleCode === 'BAD_REQUEST') {
      this.toaster.error('something-went-wrong.label', 'error.label');
      throw new Error('something-went-wrong');
    }
    const notificationError = ruleResult.body?.notifications?.find(notif => notif.statusCode === '400');
    if (notificationError) {
      this.toaster.error(notificationError.message);
      return;
    }

    const notificationWarning = ruleResult.body?.notifications?.find(notif => notif.notificationType === 'warning');
    if (notificationWarning) {
      this.toaster.warning(notificationWarning.message);
      return;
    }
  }

  private getSuccessNotification(response: HttpResponse<RuleResponseDto>): NotificationDto {
    return response.body?.notifications?.find(notif => notif.statusCode === '200');
  }

  updateUnitNumberInputValue(value: string) {
    this.productSelectionGroup.get('unitNumber').setValue(value);
  }

  updateProductCodeInputValue(value: string) {
    this.productSelectionGroup.get('productCode').setValue(value);
  }

  filterProductsNotAdded(products?: ProductSelectionItemModel[]): ProductSelectionItemModel[] {
    return products?.filter(product => this.productIsNotAddedAlready(product.id)) || [];
  }

  onUnitNumberKeyOrTab() {
    const barcode = this.productSelectionGroup.get('unitNumber').value;
    if (!this.productSelectionGroup.get('unitNumber').valid) return;

    this.barcodeTranslate(barcode, 'unitNumber')
      .pipe(
        switchMap(translation => {
          this.updateUnitNumberInputValue(translation);
          if (this.isLabeled)
            return of({
              addProduct: false,
              product: null,
            });
          return this.validateUnitNumber(translation).pipe(
            switchMap(response => {
              const products = this.filterProductsNotAdded(response.body.results.inventories?.[0]);
              if (products.length === 0) {
                const errorNotification = response.body?.notifications?.find(notif => notif.statusCode === '400');
                if (!errorNotification) {
                  this.toaster.warning('this-product-has-been-entered.message');
                }
                if (response.body.notifications)
                  return of({
                    addProduct: false,
                    product: null,
                  });
              }
              if (products.length === 1) {
                return of({
                  addProduct: true,
                  product: {
                    ...products[0],
                    unitNumber: translation,
                  },
                });
              } else {
                return this.openProductSelectionModal(products).pipe(
                  map(result => ({
                    addProduct: !!result,
                    product: { ...result, unitNumber: translation },
                  }))
                );
              }
            })
          );
        })
      )
      .subscribe(({ addProduct, product }) => {
        if (addProduct) this.addProduct(product);
        if (this.isUnlabeled) this.productSelectionGroup.reset();
      });
  }

  onProductCodeKeyOrTab() {
    if (!this.productSelectionGroup.valid) {
      return;
    }

    let productCode = this.productSelectionGroup.get('productCode').value;
    const unitNumber = this.productSelectionGroup.get('unitNumber').value;
    this.barcodeTranslate(productCode, 'productCode')
      .pipe(
        switchMap(translation => {
          this.updateProductCodeInputValue(translation);
          productCode = this.extractProductCode(translation);
          const isbtProductCode = this.extractIsbtProductCode(translation);
          return this.validateSelectedProduct(
            {
              unitNumber,
              productCode,
            },
            isbtProductCode
          ).pipe(
            map(response => {
              const inventory = response.body?.results?.inventory?.[0];
              return {
                addProduct: !!inventory,
                product: {
                  ...inventory,
                  productCode: this.productSelectionGroup?.get('productCode')?.value ?? inventory?.productCode,
                  unitNumber,
                },
                markForQuarantine: response.body.results?.markForQuarantine[0],
              };
            })
          );
        }),
        finalize(() => {
          this.productSelectionGroup?.reset();
        })
      )
      .subscribe(({ addProduct, product, markForQuarantine }) => {
        if (addProduct) {
          if (this.productIsNotAddedAlready(product.id)) {
            this.addValidatedProduct(product, markForQuarantine);
          } else {
            this.toaster.warning('this-product-has-been-entered.message');
          }
        }
      });
  }

  barcodeTranslate(barcode: string, formControlName: 'unitNumber' | 'productCode'): Observable<string> {
    return this.barcodeService.getBarcodeTranslation(barcode).pipe(
      catchError(() =>
        of({
          body: {
            barcodeTranslation: { [formControlName]: barcode },
          },
        } as HttpResponse<BarcodeTranslationResponseDTO>)
      ),
      map(response => response.body.barcodeTranslation[formControlName])
    );
  }

  openProductSelectionModal(products?: ProductSelectionItemModel[]): Observable<ProductSelectionItemModel> {
    return this.productService.getProductsByProductCodes(products.map(p => p.productCode)).pipe(
      switchMap(response => {
        const dialogRef = this.matDialog.open(OptionsPickerDialogComponent, {
          data: {
            dialogTitle: 'product-selection.label',
            options: products.map(p => ({
              ...p,
              icon: response.body.find(r => r.productCode === p.productCode)?.properties['icon'],
            })),
            optionsLabel: 'descriptionKey',
          },
        });
        dialogRef.componentInstance.optionDescriptionTpl = this.optionDescriptionTpl;
        return dialogRef.afterClosed().pipe(map(result => result as ProductSelectionItemModel));
      })
    );
  }

  extractIsbtProductCode(productCode: string) {
    if (new RegExp(commonRegex.scannedProductCode).test(productCode)) {
      return productCode.replace(new RegExp(commonRegex.extractProductCode), (match, g1, g2, g3, g4) => g2 + g4);
    }
    return productCode;
  }

  extractProductCode(productCode: string) {
    if (new RegExp(commonRegex.scannedProductCode).test(productCode)) {
      return productCode.replace(new RegExp(commonRegex.extractProductCode), (match, g1, g2, g3, g4) => g2 + g4);
    }
    if (new RegExp(commonRegex.fullProductCode).test(productCode)) {
      return productCode.replace(new RegExp(commonRegex.extractFullProductCode), (match, g1, g2, g3, g4) => g2 + g4);
    }
    return productCode;
  }
}
