import { CdkDragDrop, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';
import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { ValidationType } from '@rsa/commons';
import { TransferProduct } from '@rsa/distribution/core/models/external-transfers.model';
import { TreoScrollbarDirective } from '@treo';

@Component({
  selector: 'rsa-add-products-modal',
  templateUrl: './add-products-modal.component.html',
  styleUrls: ['./add-products-modal.component.scss'],
})
export class AddProductsModalComponent implements OnInit, AfterViewInit {
  readonly validationType = ValidationType;

  availableProducts: TransferProduct[] = [];
  addedProducts: TransferProduct[] = [];
  unitNumber: string;
  productSelection: FormGroup;

  @ViewChild('availableProdScrollbar', {
    read: TreoScrollbarDirective,
    static: false,
  })
  availableProdScrollbar: TreoScrollbarDirective;

  @ViewChild('addedProdScrollbar', {
    read: TreoScrollbarDirective,
    static: false,
  })
  currentlyAddedProdScrollbar: TreoScrollbarDirective;
  unitNumberControl = new FormControl('');

  constructor(private matDialogRef: MatDialogRef<AddProductsModalComponent>) {}

  ngOnInit(): void {
    this.unitNumberControl.setValue(this.unitNumber);
    this.unitNumberControl.disable();
  }

  ngAfterViewInit(): void {
    this.updateProductScrollbars();
  }

  addProduct() {
    this.matDialogRef.close(this.addedProducts);
  }

  selectAvailablePro(item: TransferProduct, index: number) {
    this.addedProducts.push(item);
    this.availableProducts.splice(index, 1);
    this.updateProductScrollbars();
  }

  removeAllProds() {
    this.availableProducts.push(...this.addedProducts);
    this.addedProducts = [];
    this.updateProductScrollbars();
  }

  removeProduct(item: TransferProduct) {
    this.availableProducts.push({ ...item });
    this.addedProducts.splice(this.addedProducts.indexOf(item), 1);
    this.updateProductScrollbars();
  }

  drop(event: CdkDragDrop<TransferProduct[]>) {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      transferArrayItem(event.previousContainer.data, event.container.data, event.previousIndex, event.currentIndex);
    }
  }

  private updateProductScrollbars() {
    setTimeout(() => {
      this.availableProdScrollbar.update();
      this.currentlyAddedProdScrollbar.update();
    }, 250);
  }
}
