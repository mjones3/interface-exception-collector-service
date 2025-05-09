import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';

import {
  ViewUnacceptableProductsComponent,
  UnacceptableProductsReportDTO,
  UnacceptableProductDTO
} from './view-unacceptable-products.component';

describe('ViewUnacceptableProductsComponent', () => {
  let component: ViewUnacceptableProductsComponent;
  let fixture: ComponentFixture<ViewUnacceptableProductsComponent>;

  function createMockProduct(index: number): UnacceptableProductDTO {
    return {
      unitNumber: `UNIT-${index}`,
      productCode: `CODE-${index}`,
      cartonNumber: `CARTON-${index}`,
      cartonSequence: index + 1,
      reasonForFailure: `Reason ${index}`
    };
  }

  function createMockUnacceptableProductsReport(): UnacceptableProductsReportDTO {
    const products: UnacceptableProductDTO[] = [];
    for (let i = 0; i < 5; i++) {
      products.push(createMockProduct(i));
    }

    return {
      shipmentNumber: 12345,
      products: products
    };
  }

  describe('with provided data', () => {
    const mockUnacceptableProductsReport = createMockUnacceptableProductsReport();

    beforeEach(async () => {
      await TestBed.configureTestingModule({
        imports: [ViewUnacceptableProductsComponent],
        providers: [
          { provide: MAT_DIALOG_DATA, useValue: mockUnacceptableProductsReport }
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
      expect(component.reportModel()).toEqual(mockUnacceptableProductsReport);
    });

    it('should display the shipment number correctly', () => {
      const shipmentNumberElement = fixture.debugElement.query(By.css('#unnaceptableProductsTable thead tr th div'));
      expect(shipmentNumberElement.nativeElement.textContent).toContain(`Shipment Number: ${mockUnacceptableProductsReport.shipmentNumber}`);
    });

    it('should display all products in the table', () => {
      const tableRows = fixture.debugElement.queryAll(By.css('#unnaceptableProductsTable tbody tr'));
      expect(tableRows.length).toBe(mockUnacceptableProductsReport.products.length);

      // Check each row's content
      mockUnacceptableProductsReport.products.forEach((product, index) => {
        const row = tableRows[index];
        const cells = row.queryAll(By.css('td'));

        expect(cells[0].nativeElement.textContent).toBe(product.unitNumber);
        expect(cells[1].nativeElement.textContent).toBe(product.productCode);
        expect(cells[2].nativeElement.textContent).toBe(product.cartonNumber);
        expect(cells[3].nativeElement.textContent).toBe(product.cartonSequence.toString());
        expect(cells[4].nativeElement.textContent).toBe(product.reasonForFailure);
      });
    });

    it('should have correct table headers', () => {
      const headerCells = fixture.debugElement.queryAll(By.css('#unnaceptableProductsTable thead tr.bg-blue-100 th'));
      expect(headerCells.length).toBe(5);
      expect(headerCells[0].nativeElement.textContent).toBe('Unit Number');
      expect(headerCells[1].nativeElement.textContent).toBe('Product Code');
      expect(headerCells[2].nativeElement.textContent).toBe('Carton Number');
      expect(headerCells[3].nativeElement.textContent).toBe('Carton Sequence');
      expect(headerCells[4].nativeElement.textContent).toBe('Reason for Failure');
    });
  });

  describe('with no data provided (using mock data)', () => {
    beforeEach(async () => {
      await TestBed.configureTestingModule({
        imports: [ViewUnacceptableProductsComponent],
        providers: [
          { provide: MAT_DIALOG_DATA, useValue: null }
        ]
      }).compileComponents();

      fixture = TestBed.createComponent(ViewUnacceptableProductsComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should set reportModel to mockReport when no data is injected', () => {
      // The component should fall back to mockReport when no data is provided
      expect(component.reportModel()).not.toBeNull();
      expect(component.reportModel().shipmentNumber).toBeDefined();
      expect(Array.isArray(component.reportModel().products)).toBe(true);
    });

    it('should display the mock shipment number correctly', () => {
      const shipmentNumberElement = fixture.debugElement.query(By.css('#unnaceptableProductsTable thead tr th div'));
      expect(shipmentNumberElement.nativeElement.textContent).toContain(`Shipment Number: ${component.reportModel().shipmentNumber}`);
    });

    it('should display mock products in the table', () => {
      const tableRows = fixture.debugElement.queryAll(By.css('#unnaceptableProductsTable tbody tr'));
      expect(tableRows.length).toBe(component.reportModel().products.length);

      // Check a sample of rows to verify data is displayed
      const firstRow = tableRows[0];
      const firstRowCells = firstRow.queryAll(By.css('td'));
      const firstProduct = component.reportModel().products[0];

      expect(firstRowCells[0].nativeElement.textContent).toBe(firstProduct.unitNumber);
      expect(firstRowCells[1].nativeElement.textContent).toBe(firstProduct.productCode);
      expect(firstRowCells[2].nativeElement.textContent).toBe(firstProduct.cartonNumber);
      expect(firstRowCells[3].nativeElement.textContent).toBe(firstProduct.cartonSequence.toString());
      expect(firstRowCells[4].nativeElement.textContent).toBe(firstProduct.reasonForFailure);
    });
  });
});
