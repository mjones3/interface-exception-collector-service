import { DOCUMENT } from '@angular/common';
import { HttpResponse } from '@angular/common/http';
import { Component, ElementRef, Inject, OnInit, TemplateRef, ViewChild } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import {
  BarcodeService,
  BarcodeTranslationResponseDTO,
  Description,
  DocumentDto,
  FacilityService,
  FileService,
  getDocumentExtensionIcon,
  InventoryDto,
  InventoryService,
  LabelStatus,
  LockService,
  OrderDto,
  OrderItemAttachmentDto,
  OrderItemProductDTO,
  OrderService,
  ProcessHeaderService,
  ProductsService,
  RsaValidators,
  TranslateInterpolationPipe,
  ValidateRuleDto,
  ValidationType,
} from '@rsa/commons';
import { OrderProduct, ORDER_ITEM_LOCK_TYPE, SHIPPED_OPTION_VALUE } from '@rsa/distribution/core/models/orders.model';
import { ProductSelectionComponent } from '@rsa/distribution/shared/components/product-selection/product-selection.component';
import { ConfirmationDialogComponent, OptionsPickerDialogComponent } from '@rsa/touchable';
import { startCase } from 'lodash';
import { ToastrService } from 'ngx-toastr';
import { Table } from 'primeng/table';
import { forkJoin, Observable, of } from 'rxjs';
import { catchError, finalize, switchMap } from 'rxjs/operators';

@Component({
  selector: 'rsa-fill-order',
  templateUrl: './fill-order.component.html',
  styleUrls: ['./fill-order.component.scss'],
})
export class FillOrderComponent implements OnInit {
  readonly validationType = ValidationType;

  @ViewChild('fileUpload') fileUpload: ElementRef;
  @ViewChild('productTable') productTable: Table;
  @ViewChild('optionDescriptionTpl') optionDescriptionTpl: TemplateRef<any>;
  @ViewChild('productSelectionComponent') productSelection: ProductSelectionComponent;

  currentOrder: OrderDto;
  orderProduct: OrderProduct;
  products: OrderItemProductDTO[] = [];
  loading = false;
  orderInfoDescriptions: Description[] = [];
  billInfoDescriptions: Description[] = [];
  shippingInfoDescriptions: Description[] = [];
  feesInfoDescriptions: Description[] = [];
  prodInfoDescriptions: Description[] = [];
  prodIcon: string;
  unitNumberFocus = true;
  productCodeFocus = false;
  filledProducts: string;
  totalShippedProducts: number;

  uploadDocumentFiles: File[] = [];
  uploadFormData: FormArray;
  documentsFiles: DocumentDto[] = [];
  totalFiles = 0;

  constructor(
    private _router: Router,
    private route: ActivatedRoute,
    public header: ProcessHeaderService,
    private inventoryService: InventoryService,
    private lockService: LockService,
    private orderService: OrderService,
    private toaster: ToastrService,
    private productService: ProductsService,
    private translateInterpolationPipe: TranslateInterpolationPipe,
    private barcodeService: BarcodeService,
    private fileService: FileService,
    private facilityService: FacilityService,
    private matDialog: MatDialog,
    protected fb: FormBuilder,
    @Inject(DOCUMENT) private document: Document
  ) {
    this.shouldProceedWithProductSelection = this.shouldProceedWithProductSelection.bind(this);
  }

  ngOnInit(): void {
    const state = history?.state;

    if (state && state.product) {
      this.orderProduct = state.product;
      this.currentOrder = state.currentOrder;
      this.prodIcon = state.prodIcon;
      this.totalShippedProducts = state.totalShippedProducts;
      this.orderInfoDescriptions = state.orderInfoDescriptions;
      this.billInfoDescriptions = state.billInfoDescriptions;
      this.shippingInfoDescriptions = state.shippingInfoDescriptions;
      this.feesInfoDescriptions = state.feesInfoDescriptions;

      this.orderService
        .getOrderItemProductsByOrderItemIdAndCriteria(this.orderProduct.id, {
          currentLocationId: this.facilityService.getFacilityId(),
        })
        .pipe(catchError(() => of({ body: [] } as HttpResponse<OrderItemProductDTO[]>)))
        .subscribe(orderItemProductsRes => {
          this.documentsFiles = this.orderProduct.attachments.map(att => {
            return {
              id: att.documentId,
              filename: att.description,
            };
          });
          this.setProducts((orderItemProductsRes.body as OrderItemProductDTO[]) || []);
          this.setProdInfo();
        });
    } else {
      this.back();
    }
  }

  get isLabeledOrder() {
    return this.currentOrder && this.currentOrder.labelStatus && this.currentOrder.labelStatus !== 'UNLABELED';
  }

  get priceOverrideVisible() {
    return this.currentOrder && this.currentOrder.shipmentType && this.currentOrder.shipmentType !== 'INTERNAL';
  }

  back() {
    this._router.navigateByUrl(`/orders/${this.orderId}/details`);
  }

  cancel() {
    if (this.products?.length) {
      const dialogConfig = new MatDialogConfig();
      dialogConfig.width = '32rem';
      const dialogRef = this.matDialog.open(ConfirmationDialogComponent, dialogConfig);
      dialogRef.componentInstance.dialogTitle = 'cancel-fill-order-confirmation.label';
      dialogRef.componentInstance.dialogText = 'cancel-fill-order-confirmation-dialog.label';
      dialogRef.componentInstance.acceptBtnTittle = 'continue.label';
      dialogRef.afterClosed().subscribe((result: boolean) => {
        if (result) {
          const sub$ = [this.orderService.deleteNotFilledInventories(this.orderProduct.id)];

          if (!this.products.some(prod => prod.orderItemInventory.filled)) {
            sub$.push(this.lockService.unlock(ORDER_ITEM_LOCK_TYPE, this.orderProduct.id));
          }

          forkJoin(sub$)
            .pipe(
              finalize(() => {
                //Ignoring error because error means that the order item products are already unlocked or there is no filled products to be removed
                this.toaster.success('cancel-fill-order-success.label');
                this.back();
              })
            )
            .subscribe();
        }
      });
    } else {
      this.lockService.unlock(ORDER_ITEM_LOCK_TYPE, this.orderProduct.id).subscribe();
      this.back();
    }
  }

  save() {
    const formData = this.uploadFiles();
    let sub$;

    if (formData.has('files')) {
      sub$ = this.fileService.uploadDocument(this.orderProduct.id, 'ORDER_ITEM_MEDICAL_FORM', formData).pipe(
        catchError(err => {
          this.toaster.error('something-went-wrong.label');
          throw err;
        }),
        switchMap(documentRes => {
          const docs = documentRes.body || [];
          const attachments: OrderItemAttachmentDto[] = docs.map(doc => {
            return {
              orderItemId: doc.referenceId,
              documentId: doc.id,
              description: doc.filename,
              createDate: doc.createDate,
              modificationDate: doc.modificationDate,
            };
          });

          return this.getSaveFillOrder(attachments);
        })
      );
    } else {
      sub$ = this.getSaveFillOrder();
    }

    sub$.subscribe(() => {
      this.toaster.success('rul-0091-add-product-to-order-item-success.label');
      this._router.navigateByUrl(`/orders/${this.orderId}/details`);
    });
  }

  shouldProceedWithProductSelection(): boolean {
    if (this.products?.length === this.orderProduct?.quantity) {
      this.toaster.warning('fill-order-product-limit-reached.label');
      this.loading = false;
      return false;
    }
    return true;
  }

  unitNumberProductCodeSelected(unitNumber: string, productCode: string) {
    return this.inventoryService
      .validate(
        this.getFillOrderAddProductToOrderRuleDto(
          this.isLabeledOrder ? 'LABELED' : 'UNLABELED',
          unitNumber,
          productCode
        )
      )
      .pipe(
        catchError(err => {
          this.loading = false;
          this.toaster.error('something-went-wrong.label');
          throw err;
        }),
        finalize(() => {
          this.loading = false;
          this.productSelection.resetFormGroup();
          this.unitNumberFocus = true;
        })
      )
      .subscribe(response => {
        const ruleResult = response?.body;
        if (ruleResult) {
          this.loading = false;
          const notification = ruleResult.notifications?.length ? ruleResult.notifications[0] : null;

          if (notification) {
            if (notification.notificationType === 'success') {
              const orderItemProducts = ruleResult.results?.orderItemProduct;
              if (orderItemProducts?.length) {
                this.addProductToFilledProducts(orderItemProducts);
              }
            } else {
              this.toaster.show(
                this.translateInterpolationPipe.transform(notification.message, []),
                startCase(notification.notificationType),
                {},
                notification.notificationType
              );
            }
          }
        }
      });
  }

  private addProductToFilledProducts(orderItemProducts) {
    // If only 1 product add directly to array
    if (orderItemProducts?.length === 1) {
      const result = orderItemProducts[0] as OrderItemProductDTO;
      if (result.unlicensed) {
        this.toaster.warning('fill-order-medical-form-needed.label');
      }
      this.setProducts([...this.products, result]);
    } else {
      this.openProductModal(orderItemProducts);
    }
  }

  openProductModal(products?: OrderItemProductDTO[]): void {
    const dialogRef = this.matDialog.open(OptionsPickerDialogComponent, {
      data: {
        dialogTitle: 'product-selection.label',
        options: products,
        optionsLabel: 'productCode',
      },
    });
    dialogRef.componentInstance.optionDescriptionTpl = this.optionDescriptionTpl;
    dialogRef.afterClosed().subscribe((result: OrderItemProductDTO) => {
      if (result) {
        this.setProducts([...this.products, result]);
      }
    });
  }

  setProducts(products: OrderItemProductDTO[]) {
    this.products = products;
    this.productTable?.reset();
    if (!this.products?.length) {
      this.uploadDocumentFiles = [];
    }
    this.productSelection.selectedInventoryIds = products.map(p => p.orderItemInventory.inventoryId);
  }

  removeProd(index: number) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = '32rem';
    const dialogRef = this.matDialog.open(ConfirmationDialogComponent, dialogConfig);
    dialogRef.componentInstance.dialogTitle = 'remove-product-confirmation.label';
    dialogRef.componentInstance.dialogText = 'remove-product-confirmation-dialog.label';
    dialogRef.componentInstance.acceptBtnTittle = 'continue.label';
    dialogRef.afterClosed().subscribe((result: boolean) => {
      if (result) {
        this.orderService.removeOrderItemInventory(this.products[index]?.orderItemInventory?.id).subscribe(
          () => {
            this.products.splice(index, 1);
            this.setProducts(this.products);
            this.toaster.success('remove-product-success.label');
          },
          err => {
            this.toaster.error('something-went-wrong.label');
            throw err;
          }
        );
      }
    });
  }

  removeAll() {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = '32rem';
    const dialogRef = this.matDialog.open(ConfirmationDialogComponent, dialogConfig);
    dialogRef.componentInstance.dialogTitle = 'remove-all-products-confirmation.label';
    dialogRef.componentInstance.dialogText = 'remove-all-products-confirmation-dialog.label';
    dialogRef.componentInstance.acceptBtnTittle = 'continue.label';
    dialogRef.afterClosed().subscribe((result: boolean) => {
      if (result) {
        this.orderService.removeAllOrderItemInventory(this.orderProduct.id).subscribe(
          () => {
            this.setProducts([]);
            this.toaster.success('remove-all-product-success.label');
          },
          err => {
            this.toaster.error('something-went-wrong.label');
            throw err;
          }
        );
      }
    });
  }

  getSaveFillOrder(attachments: OrderItemAttachmentDto[] = []) {
    return this.orderService
      .saveFillOrder(this.orderProduct.id, {
        orderItemInventories: this.products.map(prod => prod.orderItemInventory),
        orderItemAttachments: attachments,
      })
      .pipe(
        catchError(err => {
          this.toaster.error('something-went-wrong.label');
          throw err;
        })
      );
  }

  priceChange(event, product: OrderItemProductDTO) {
    if (event && !isNaN(event) && +event > 0) {
      product.orderItemInventory.price = event;
    }
  }

  //#region DOCUMENT

  openDocument(id: number, filename: string): void {
    const extension = filename.split('.')?.pop();
    this.fileService.downloadDocument(id).subscribe(response => {
      if (response.body?.fileContent) {
        const a = this.document.createElement('a');
        a.href = `data:application/${extension};base64,${response.body?.fileContent}`;
        a.download = filename;
        a.click();
      }
    });
  }

  updateFiles(files: File[]): void {
    if (files.length > this.totalFiles) {
      this.toaster.success('medical-need-form-uploaded-successfully.label');
    }
    this.totalFiles = files.length;
    this.uploadDocumentFiles = files;
  }

  getFormData(form): void {
    this.uploadFormData = form;
  }

  uploadFiles(): FormData {
    const formData = new FormData();
    const files = this.uploadDocumentFiles;

    if (files?.length) {
      files.forEach((file, index) => {
        const document = this.uploadFormData.controls['documents'].controls[index].value;
        const tempFileName = file.name.slice(0, file.name.lastIndexOf('.'));
        let documentName = document.documentName.endsWith(document.format)
          ? document.documentName.substr(0, document.documentName.lastIndexOf(document.format) - 1)
          : document.documentName;

        if (!documentName) {
          documentName = tempFileName;
        }

        formData.append('files', file, `${documentName}.${document.format}`);
      });
    }

    return formData;
  }

  getIconType(filename: string): string {
    const extension = filename.split('.')?.pop();
    return getDocumentExtensionIcon(extension);
  }

  get checkForUnlicensedProducts() {
    return this.products.some(prod => prod.unlicensed);
  }

  get cantSave() {
    return (
      this.products?.length === 0 ||
      (this.checkForUnlicensedProducts && !this.uploadDocumentFiles?.length && !this.documentsFiles?.length)
    );
  }

  //#endregion

  get orderId() {
    return this.route.snapshot.params?.id;
  }

  get orderShipped() {
    return this.currentOrder?.statusKey === SHIPPED_OPTION_VALUE;
  }

  get quantity() {
    return this.orderProduct?.quantity - this.totalShippedProducts;
  }

  private setProdInfo() {
    this.prodInfoDescriptions = [
      {
        label: 'product-family.label',
        value: this.orderProduct.productFamily.descriptionKey,
      },
      {
        label: 'blood-type.label',
        value: this.orderProduct.bloodType.descriptionKey,
      },
      {
        label: 'product-attributes.label',
        valueType: 'badge',
        valueCls: 'badge',
        valueStyle: this.orderProduct.productAttributes.map(attr => {
          return {
            'background-color': attr.color,
          };
        }),
        value: this.orderProduct.productAttributes.map(attr => attr.descriptionKey),
      },
      {
        label: 'product-comments.label',
        value: this.orderProduct.productComment,
      },
    ];
  }

  private getFillOrderAddProductToOrderRuleDto(labelStatus: LabelStatus, unitNumber: string, productCode: string) {
    return <ValidateRuleDto>{
      ruleName: 'rul-0091-fillorder-add-product-to-order',
      labelStatus,
      unitNumber: unitNumber,
      isbtProductCode: labelStatus === 'LABELED' ? productCode : null,
      productCode: labelStatus === 'UNLABELED' ? productCode : null,
      orderItemId: this.orderProduct.id,
      shipmentType: this.currentOrder?.shipmentType?.toUpperCase(),
      currentLocationId: this.facilityService.getFacilityId(),
    };
  }
}
