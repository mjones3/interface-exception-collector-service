import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
  TemplateRef,
} from '@angular/core';
import { FormControl } from '@angular/forms';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { AutoUnsubscribe, extractUnitNumber, RsaValidators, ValidationType } from '@rsa/commons';
import { ToastrService } from 'ngx-toastr';
import { Subscription } from 'rxjs';
import { Product } from '../../shared/models/product.model';
import { ConfirmationDialogComponent } from '../confirmation-dialog/confirmation-dialog.component';
import { OptionsPickerDialogComponent } from '../options-picker-dialog/options-picker-dialog.component';

export type BloodTypeParts = 'ABO+RH' | 'ABO' | undefined;

@Component({
  selector: 'rsa-unit-object-for-batch-screen',
  exportAs: 'rsaUnitObjectBatchScreen',
  templateUrl: './unit-object-for-batch-screen.component.html',
  styleUrls: ['./unit-object-for-batch-screen.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
@AutoUnsubscribe()
export class UnitObjectForBatchScreenComponent implements OnChanges, OnInit {
  @Input() addedUnits: Product[] = [];
  @Input() headerTitle = 'added-units.label';
  @Input() showCentrifugeCode = false;
  @Input() productsInventory: any[] = [];
  @Input() productLabel = 'descriptionKey';
  @Input() allUnitsAddedToasterMessage = 'all-available-units-added.label';
  @Input() allUnitsRemovedToasterMessage = 'all-units-removed.message';
  @Input() header: TemplateRef<any>;
  @Input() footer: TemplateRef<any>;
  @Input() useCustomUnitCard = false;
  @Input() addProductDirectly = true;
  @Input() minAddedUnits = 1;
  @Input() maxAddedUnits = 20;
  @Input() addBloodTypeToCard: BloodTypeParts;
  @Input() unitFocus = true;
  @Input() centrifugeFocus = false;
  @Input() addPrtKitDescriptionToCard = false;
  @Input() applyLogicalOrderAndValidations = false;

  @Output() unitNumberScanned = new EventEmitter<string>();
  @Output() centrifugeBarcodeScanned = new EventEmitter<string>();
  @Output() clearUnitNumber = new EventEmitter<void>();
  @Output() productSelected = new EventEmitter<Product>();
  @Output() unitsChange = new EventEmitter<Product[]>();
  @Output() invalidUnitDeleted = new EventEmitter<Product>();
  @Output() unitFocusChange = new EventEmitter<boolean>();
  @Output() centrifugeFocusChange = new EventEmitter<boolean>();
  @Output() reachMaxUnitsAddedLimit = new EventEmitter<number>();
  @Output() lastUnitScanned = new EventEmitter<void>();
  @Output() removeAllAddedUnits = new EventEmitter<void>();

  validationType = ValidationType;
  dialogRef: MatDialogRef<ConfirmationDialogComponent, any>;
  unitNumberControl = new FormControl('', [RsaValidators.unitNumber]);
  centrifugeBarcodeControl = new FormControl({ value: '', disabled: true });
  errorToasterSub: Subscription;

  constructor(private matDialog: MatDialog, private cd: ChangeDetectorRef, public toaster: ToastrService) {
    // this.unitNumberControl.valueChanges.pipe(debounceTime(150)).subscribe(() => {
    //   cd.detectChanges();
    // });
  }

  ngOnInit(): void {
    if (this.applyLogicalOrderAndValidations) {
      this.enableDisableUnitNumber(true);
    }
  }
  /**
   * Emit scanned unit value
   */
  onTabOrEnterPressed(): void {
    if (this.unitNumberControl.valid && this.unitNumberControl.value && this.maxAddedUnits > this.addedUnits.length) {
      this.unitNumberScanned.emit(extractUnitNumber(this.unitNumberControl.value));
    } else {
      this.reachMaxUnitsAddedLimit.emit(this.addedUnits.length);
    }
  }

  /**
   * Emit Centrifuge Barcode scanned value
   */
  centrifugeScanned(): void {
    if (this.centrifugeBarcodeControl.value) {
      this.centrifugeBarcodeScanned.emit(this.centrifugeBarcodeControl.value);
    }
  }

  /**
   * Emit Centrifuge Barcode scanned value after centrifuge input change
   */
  inputChangeCentrifugeBarcode(event: any): void {
    if (!event) {
      this.centrifugeBarcodeScanned.emit(null);
    }
  }

  deleteProduct(index): void {
    if (this.addedUnits[index].invalidProduct) {
      // Deleting Invalid Units
      // TODO delete emitter if not necessary
      this.invalidUnitDeleted.emit(this.addedUnits[index]);
      const toaster = this.toaster.error(this.addedUnits[index].invalidProduct);
      this.errorToasterSub = toaster.onHidden.subscribe(() => this.onDeleteProduct(index));
    } else {
      this.onDeleteProduct(index);
    }
  }

  private onDeleteProduct(index) {
    this.addedUnits.splice(index, 1);
    this.unitsChange.emit(this.addedUnits);
  }

  removeAll(): void {
    if (this.addedUnits.length > 0) {
      this.dialogRef = this.matDialog.open(ConfirmationDialogComponent);
      this.dialogRef.componentInstance.iconName = 'rsa:dialog-confirmation';
      this.dialogRef.componentInstance.dialogText = 'remove-all-units-confirmation.message';
      this.dialogRef.componentInstance.dialogTitle = 'remove-all-units.label';
      this.dialogRef.componentInstance.cancelBtnTittle = 'no.label';
      this.dialogRef.componentInstance.acceptBtnTittle = 'yes.label';
      this.dialogRef.afterClosed().subscribe(result => {
        if (result === true) {
          this.clearAddedUnits();
          this.toaster.success(this.allUnitsRemovedToasterMessage);
          this.removeAllAddedUnits.emit();
        }
      });
    }
  }

  isLastUnitAdded(): boolean {
    return (
      this.minAddedUnits &&
      this.maxAddedUnits &&
      //this.minAddedUnits === this.maxAddedUnits &&
      //above line commented because minAddedunits can never be equal to maxaddedUnits

      this.addedUnits.length === this.maxAddedUnits
    );
  }

  addUnits(productSelected: any) {
    this.addedUnits = [productSelected, ...this.addedUnits];
    this.unitNumberControl.reset();
    this.unitsChange.emit(this.addedUnits);
    if (!this.isLastUnitAdded()) {
      this.focusUnitNumber();
    }
    // we can add else condition if the focus doesnt happen on its own this will help in switching the focus
    else {
      this.lastUnitScanned.emit();
      this.cd.detectChanges();
      // this.focusCentrifugeBarcode();
    }
  }

  private focusUnitNumber(): void {
    this.unitFocus = true;
    this.unitFocusChange.emit(true);
  }

  focusCentrifugeBarcode() {
    this.centrifugeFocus = true;
    this.centrifugeFocusChange.emit(true);
    // this.cd.detectChanges();
  }

  enableDisableCentrifugeBarcode() {
    if (this.addedUnits.length >= this.minAddedUnits) {
      this.centrifugeBarcodeControl.enable();
    } else {
      this.centrifugeBarcodeControl.disable();
      this.centrifugeBarcodeControl.reset();
    }
  }

  clearAddedUnits() {
    this.addedUnits = [];
    this.clearUnitNumber.emit();
    this.unitsChange.emit(this.addedUnits);
    this.cd.detectChanges();
  }

  setAddedUnits(units: any[]) {
    this.addedUnits = units;
    // this.cd.detectChanges();
    this.unitsChange.emit(this.addedUnits);
  }

  resetUnitControl() {
    this.unitNumberControl.reset();
  }

  resetCentrifugeBarcodeControl() {
    this.centrifugeBarcodeControl.reset();
  }

  validAddedUnits(): boolean {
    return this.addedUnits.length > 0 && this.addedUnits.filter(value => value.invalidProduct).length === 0;
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.productsInventory && changes.productsInventory.currentValue) {
      // Filter Products that are already part of Added Unit List
      this.productsInventory = changes.productsInventory.currentValue.filter(product => {
        return !this.addedUnits.find(addedUnit => {
          return addedUnit.descriptionKey === product.descriptionKey && addedUnit.unitNumber === product.unitNumber;
        });
      });
      // Display Modal Products if Unit is divided if not add it directly to Added Units
      if (this.productsInventory.length > 1) {
        this.openDonationTypeModal();
      } else if (this.productsInventory.length === 1) {
        if (this.addProductDirectly) {
          this.addUnits(this.productsInventory[0]);
        } else {
          this.productSelected.emit(this.productsInventory[0]);
        }
      } else if (this.productsInventory.length === 0 && changes.productsInventory.currentValue.length > 0) {
        this.resetUnitControl();
        this.toaster.error(this.allUnitsAddedToasterMessage);
      }
    }
  }

  openDonationTypeModal(): void {
    const dialogRef = this.matDialog.open(OptionsPickerDialogComponent, {
      data: {
        dialogTitle: 'select-product.label',
        options: this.productsInventory,
        optionsLabel: this.productLabel,
      },
    });
    dialogRef.afterClosed().subscribe((result: Product) => {
      if (result) {
        if (this.addProductDirectly) {
          this.addUnits(result);
        } else {
          this.productSelected.emit(result);
        }
      }
    });
  }

  enableDisableUnitNumber(disable: boolean): void {
    if (disable) {
      this.unitNumberControl.disable();
      this.unitNumberControl.reset();
    } else {
      this.unitNumberControl.enable();
    }
  }
}
