import { StepperSelectionEvent } from '@angular/cdk/stepper';
import { HttpResponse } from '@angular/common/http';
import { Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MatDatepickerInputEvent } from '@angular/material/datepicker';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { MatHorizontalStepper } from '@angular/material/stepper';
import { Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import {
  BarcodeService,
  BarcodeTranslationResponseDTO,
  Column,
  CustomerDto,
  CustomerService,
  ExternalTransferDto,
  FacilityService,
  InventoryService,
  OrderService,
  ProcessHeaderService,
  RsaValidators,
  RuleResponseDto,
  ShipmentService,
  ValidationType,
} from '@rsa/commons';
import {
  AddProductRuleRequest,
  AddProductRuleResult,
  ADD_PRODUCT_TO_BATCH_VALIDATION_RULE,
  TransferProduct,
} from '@rsa/distribution/core/models/external-transfers.model';
import { AddProductsModalComponent } from '@rsa/distribution/modules/external-transfers/add-products-modal/add-products-modal.component';
import { ConfirmationDialogComponent } from '@rsa/touchable';
import { startCase } from 'lodash';
import * as moment from 'moment';
import { ToastrService } from 'ngx-toastr';
import { Table } from 'primeng/table';
import { Observable, of } from 'rxjs';
import { catchError, debounceTime, distinctUntilChanged, finalize, map, switchMap } from 'rxjs/operators';

@Component({
  selector: 'rsa-external-transfers',
  templateUrl: './external-transfers.component.html',
  styleUrls: ['./external-transfers.component.scss'],
})
export class ExternalTransfersComponent implements OnInit {
  readonly validationType = ValidationType;
  readonly EXTERNAL_ORDER_ID_MAX_LENGTH = 50; //maxLength on the database

  @ViewChild('stepper') stepper: MatHorizontalStepper;
  @ViewChild('productTable') productTable: Table;

  transferInfoGroup: FormGroup;
  productSelection: FormGroup;
  productSelectionStep: boolean;
  products: TransferProduct[] = [];
  columns: Column[] = [
    {
      field: 'unitNumber',
      header: 'unit-number.label',
      headerCls: 'w-48',
      sortable: true,
    },
    {
      field: 'productCode',
      header: 'product-code.label',
      headerCls: 'w-40',
      sortable: true,
    },
    {
      field: 'originalDateShipped',
      header: 'original-date-shipped.label',
      headerCls: 'w-52',
      templateRef: 'dateTpl',
      sortable: true,
    },
    {
      field: 'originallyShippedFrom',
      header: 'originally-shipped-from.label',
      sortable: true,
    },
    {
      field: 'originallyShippedTo',
      header: 'originally-shipped-to.label',
      sortable: true,
    },
    {
      field: 'lastTransferDate',
      header: 'last-transfer-date.label',
      templateRef: 'dateTpl',
      headerCls: 'w-52',
      sortable: true,
    },
    {
      field: '',
      header: '',
      templateRef: 'actionTpl',
      headerCls: 'w-40',
      hideHeader: true,
    },
  ];
  unitNumberFocus = false;
  unitNumberControl = new FormControl('', [Validators.required, RsaValidators.unitNumber]);
  customers: Observable<CustomerDto[]>;
  selectedCustomer: CustomerDto;
  selectedTransferDate: Date;
  today = new Date();

  constructor(
    private _router: Router,
    private matDialog: MatDialog,
    private orderService: OrderService,
    private customerService: CustomerService,
    private shipmentService: ShipmentService,
    private inventoryService: InventoryService,
    private facilityService: FacilityService,
    private translateService: TranslateService,
    private barcodeService: BarcodeService,
    private toaster: ToastrService,
    public header: ProcessHeaderService,
    protected fb: FormBuilder
  ) {
    this.transferInfoGroup = fb.group({
      externalTransferOrderId: ['', Validators.maxLength[this.EXTERNAL_ORDER_ID_MAX_LENGTH]],
      customerSearchCriteria: ['name', Validators.required],
      transferToCustomer: ['', Validators.required],
      transferDate: ['', Validators.required],
    });

    this.customers = this.transferInfoGroup.controls.transferToCustomer.valueChanges.pipe(
      debounceTime(800),
      distinctUntilChanged(),
      map(val => {
        if (typeof val === 'string') {
          this.selectedCustomer = null;
          return val;
        }
        return null;
      }), // Added because of `displayFn`, which makes the `valueChanges` to be called again.
      switchMap((val: string | null) => {
        if (val) {
          return this.filterCustomers(val);
        }

        return of([]);
      })
    );
  }

  ngOnInit(): void {}

  cancel() {
    const dialogConfig = new MatDialogConfig();
    const dialogRef = this.matDialog.open(ConfirmationDialogComponent, dialogConfig);
    dialogRef.componentInstance.dialogText = 'cancel-external-transfer-message.label';
    dialogRef.componentInstance.dialogTitle = 'cancel-external-transfer-title.label';
    dialogRef.componentInstance.acceptBtnTittle = 'continue.label';
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this._router.navigateByUrl('/home');
      }
    });
  }

  stepSelectionChange($event: StepperSelectionEvent) {
    this.productSelectionStep = $event.selectedIndex === 1;
    if (this.productSelectionStep) {
      this.selectedTransferDate = this.transferInfoGroup.controls?.transferDate?.value;
      this.unitNumberFocus = true;
    }
  }

  private showToaster(notification = { message: 'something-went-wrong.label', notificationType: 'error' }) {
    this.toaster.show(
      this.translateService.instant(notification.message),
      startCase(notification.notificationType),
      {},
      notification.notificationType
    );
  }

  private resetProcess() {
    this.productTable.reset();
    this.stepper.reset();
    this.unitNumberControl.reset();
    this.transferInfoGroup.reset();
    this.products = [];
    this.selectedCustomer = null;
    this.selectedTransferDate = null;
    this.unitNumberFocus = false;
    this.transferInfoGroup.controls.customerSearchCriteria.setValue('name');
  }

  //#region STEP 1

  step1NextClick() {
    if (this.transferInfoGroup.valid) {
      const externalTransferOrderIdControl = this.transferInfoGroup.get('externalTransferOrderId');
      if (externalTransferOrderIdControl.value) {
        this.orderService.getOrderByCriteria({ 'externalId.equals': externalTransferOrderIdControl.value }).subscribe(
          orderRes => {
            if (!orderRes.body?.length) {
              this.showToaster({ message: 'transfer-order-id-doesnt-exist.label', notificationType: 'error' });
              externalTransferOrderIdControl.setValue('');
            } else {
              this.stepper.next();
            }
          },
          err => {
            this.showToaster();
            throw err;
          }
        );
      } else {
        this.stepper.next();
      }
    }
  }

  transferDateChanged(event: MatDatepickerInputEvent<Date>) {
    const transferDateControl = this.transferInfoGroup.controls.transferDate;
    if (event.value !== this.selectedTransferDate) {
      if (
        transferDateControl.hasError('matDatepickerParse') ||
        moment(event.value).startOf('day') > moment().startOf('day')
      ) {
        transferDateControl.setErrors({ invalidDate: true });
        return;
      }
      if (this.products?.length) {
        const dialogConfig = new MatDialogConfig();
        const dialogRef = this.matDialog.open(ConfirmationDialogComponent, dialogConfig);
        dialogRef.componentInstance.dialogTitle = 'confirmation-dialog.label';
        dialogRef.componentInstance.dialogText = 'modifying-transfer-date-message.label';
        dialogRef.componentInstance.acceptBtnTittle = 'continue.label';
        dialogRef.afterClosed().subscribe(result => {
          if (result) {
            this.products = [];
            this.selectedTransferDate = null;
          } else {
            transferDateControl.setValue(this.selectedTransferDate);
          }
        });
      }
    }
  }

  displayCustomerFn(customer: CustomerDto): string {
    return customer ? `${customer.externalId} - ${customer.name}` : '';
  }

  filterCustomers(val: string): Observable<CustomerDto[]> {
    const criteria =
      this.transferInfoGroup.value.customerSearchCriteria === 'id' ? { externalId: val } : { 'name.contains': val };
    return this.customerService.getCustomerByCriteria({ ...criteria, active: 'true' }).pipe(
      catchError(() => of({ body: [] } as HttpResponse<CustomerDto[]>)),
      map(response => {
        const customers = response.body ?? [];
        if (!customers.length) {
          this.transferInfoGroup.controls.transferToCustomer.setErrors({ noMatchingCustomer: true });
        } else {
          this.transferInfoGroup.controls.transferToCustomer.setErrors(null);
        }

        return customers;
      })
    );
  }

  customerChanged(customer: CustomerDto) {
    this.selectedCustomer = customer;
  }

  //#endregion

  //#region STEP 2

  onUnitNumberKeyOrTab() {
    if (this.unitNumberControl?.value && this.transferInfoGroup.valid) {
      this.barcodeService
        .getBarcodeTranslation(this.unitNumberControl?.value)
        .pipe(
          catchError(() =>
            of({ body: { barcodeTranslation: { unitNumber: this.unitNumberControl.value } } } as HttpResponse<
              BarcodeTranslationResponseDTO
            >)
          ),
          switchMap(response => {
            this.unitNumberControl.patchValue(response?.body?.barcodeTranslation?.unitNumber);
            this.unitNumberControl.updateValueAndValidity();

            return this.unitNumberControl.valid
              ? this.inventoryService.validate(this.addProductRuleRequest).pipe(
                  finalize(() => {
                    this.unitNumberFocus = true;
                    this.unitNumberControl.setValue('');
                  })
                )
              : of(null);
          })
        )
        .subscribe(
          (response: HttpResponse<RuleResponseDto> | null) => {
            if (response?.body) {
              const value = response.body;

              if (value.ruleCode !== 'BAD_REQUEST') {
                for (const notification of value.notifications ?? []) {
                  this.showToaster(notification);
                  this.unitNumberControl.patchValue('');
                  this.unitNumberControl.setErrors(null);
                }

                const result = value.results as AddProductRuleResult;
                if (result?.externalTransferItems?.length) {
                  this.openAddProductDialog(result.externalTransferItems[0]);
                }
              } else {
                this.showToaster();
              }
            }
          },
          err => {
            this.showToaster();
            throw err;
          }
        );
    }
  }

  openAddProductDialog(externalTransferItems: TransferProduct[]) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = '76rem';
    const dialogRef = this.matDialog.open(AddProductsModalComponent, dialogConfig);
    dialogRef.componentInstance.unitNumber = this.unitNumberControl.value;
    dialogRef.componentInstance.availableProducts = externalTransferItems;
    dialogRef.afterClosed().subscribe((products: TransferProduct[]) => {
      if (products?.length) {
        this.unitNumberControl.reset();
        this.setProducts([...this.products, ...products]);
      }
    });
  }

  onComplete() {
    if (this.transferInfoGroup.valid && this.products?.length) {
      const transferInfo = this.transferInfoGroup.value;
      const externalTransfer: ExternalTransferDto = {
        externalOrderId: transferInfo.externalTransferOrderId,
        locationId: this.facilityService.getFacilityId(),
        customerId: transferInfo.transferToCustomer.id,
        transferDate: moment(transferInfo.transferDate).format('YYYY-MM-DD'),
        externalTransferItems: this.products,
      };

      this.shipmentService.createExternalTransfer(externalTransfer).subscribe(
        () => {
          this.showToaster({ message: 'products-successfully-transferred.label', notificationType: 'success' });
          this.stepper.reset();
          this.resetProcess();
        },
        err => {
          this.showToaster();
          throw err;
        }
      );
    }
  }

  remove(product: TransferProduct) {
    this.products.splice(
      this.products.findIndex(prod => prod.inventoryId === product.inventoryId),
      1
    );
    this.setProducts(this.products);
  }

  removeAll() {
    const dialogConfig = new MatDialogConfig();
    const dialogRef = this.matDialog.open(ConfirmationDialogComponent, dialogConfig);
    dialogRef.componentInstance.dialogText = 'external-transfer-remove-all-products-confirmation-message.label';
    dialogRef.componentInstance.dialogTitle = 'external-transfer-remove-all-products-confirmation-title.label';
    dialogRef.componentInstance.acceptBtnTittle = 'continue.label';
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.showToaster({
          message: 'external-transfer-products-remove-all-success.label',
          notificationType: 'success',
        });
        this.setProducts([]);
      }
    });
  }

  step2BackClick() {
    this.unitNumberControl.reset();
  }

  get addProductRuleRequest() {
    return <AddProductRuleRequest>{
      ruleName: ADD_PRODUCT_TO_BATCH_VALIDATION_RULE,
      unitNumber: this.unitNumberControl.value,
      inventoryIDList: this.products.map(prod => prod.inventoryId),
      transferDate: moment(this.transferInfoGroup.value.transferDate).format('YYYY-MM-DD'),
    };
  }

  private setProducts(products: TransferProduct[]) {
    this.products = products;
    this.productTable?.reset();
  }

  //#endregion
}
