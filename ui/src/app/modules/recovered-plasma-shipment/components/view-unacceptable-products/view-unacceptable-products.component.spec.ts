import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';

import { ViewUnacceptableProductsComponent } from './view-unacceptable-products.component';
import {
    UnacceptableUnitReportItemOutput,
    UnacceptableUnitReportOutput
} from '../../graphql/query-definitions/print-unacceptable-units-report.graphql';
import { DateTime } from 'luxon';
import { MatIconTestingModule } from '@angular/material/icon/testing';

describe('ViewUnacceptableProductsComponent', () => {
  let component: ViewUnacceptableProductsComponent;
  let fixture: ComponentFixture<ViewUnacceptableProductsComponent>;

  function createProduct(index: number): UnacceptableUnitReportItemOutput {
    return {
      unitNumber: `UNIT-${index}`,
      productCode: `CODE-${index}`,
      cartonNumber: `CARTON-${index}`,
      cartonSequenceNumber: index + 1,
      failureReason: `Reason ${index}`,
      createDate: DateTime.now().toISO()
    };
  }

  function createUnacceptableProductsReport(amountOfFlaggedProducts: number): UnacceptableUnitReportOutput {
    const products: UnacceptableUnitReportItemOutput[] = [];
    for (let i = 0; i < amountOfFlaggedProducts; i++) {
      products.push(createProduct(i));
    }

    return {
      reportTitle: 'View Unacceptable Products Report',
      dateTimeExported: DateTime.now().toISO(),
      noProductsFlaggedMessage: 'No unacceptable products found',
      shipmentNumber: '12345',
      failedProducts: products,
    };
  }

  describe('with provided data', () => {
    const unacceptableProductsReport = createUnacceptableProductsReport(10);

    beforeEach(async () => {
      await TestBed.configureTestingModule({
        imports: [
            ViewUnacceptableProductsComponent,
            MatIconTestingModule,
        ],
        providers: [
          { provide: MAT_DIALOG_DATA, useValue: unacceptableProductsReport }
        ]
      }).compileComponents();

      fixture = TestBed.createComponent(ViewUnacceptableProductsComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should set reportModel property from injected data', () => {
      expect(component.data).toEqual(unacceptableProductsReport);
    });

    it('should display the shipment number correctly', () => {
      const shipmentNumberElement = fixture.debugElement.query(By.css('#unacceptableProductsTable thead tr th div'));
      expect(shipmentNumberElement.nativeElement.textContent).toContain(`Shipment Number: ${unacceptableProductsReport.shipmentNumber}`);
    });

    it('should display all products in the table', () => {
      const tableRows = fixture.debugElement.queryAll(By.css('#unacceptableProductsTable tbody tr'));
      expect(tableRows.length).toBe(unacceptableProductsReport.failedProducts.length);

      // Check each row's content
      unacceptableProductsReport.failedProducts.forEach((product, index) => {
        const row = tableRows[index];
        const cells = row.queryAll(By.css('td'));

        expect(cells[0].nativeElement.textContent).toBe(product.unitNumber);
        expect(cells[1].nativeElement.textContent).toBe(product.productCode);
        expect(cells[2].nativeElement.textContent).toBe(product.cartonNumber);
        expect(cells[3].nativeElement.textContent).toBe(product.cartonSequenceNumber.toString());
        expect(cells[4].nativeElement.textContent).toBe(product.failureReason);
      });
    });

    it('should have correct table headers', () => {
      const headerCells = fixture.debugElement.queryAll(By.css('#unacceptableProductsTable thead tr.bg-blue-100 th'));
      expect(headerCells.length).toBe(5);
      expect(headerCells[0].nativeElement.textContent).toBe('Unit Number');
      expect(headerCells[1].nativeElement.textContent).toBe('Product Code');
      expect(headerCells[2].nativeElement.textContent).toBe('Carton Number');
      expect(headerCells[3].nativeElement.textContent).toBe('Carton Sequence');
      expect(headerCells[4].nativeElement.textContent).toBe('Reason for Failure');
    });
  });

  describe('with provided data but no products flagged', () => {
    const unacceptableProductsReport = createUnacceptableProductsReport(0);
    beforeEach(async () => {
      await TestBed.configureTestingModule({
        imports: [
          ViewUnacceptableProductsComponent,
          MatIconTestingModule,
        ],
        providers: [
          { provide: MAT_DIALOG_DATA, useValue: unacceptableProductsReport }
        ]
      }).compileComponents();

      fixture = TestBed.createComponent(ViewUnacceptableProductsComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should display the correct message when no products are found', () => {
      expect(component.data.failedProducts.length).toBe(0);
      const noProductsMessageElement = fixture.debugElement.query(By.css('#viewUnacceptableProductsNoProductsFlaggedMessage'));
      expect(noProductsMessageElement.nativeElement.textContent).toBe(unacceptableProductsReport.noProductsFlaggedMessage);
    });
  });
});
