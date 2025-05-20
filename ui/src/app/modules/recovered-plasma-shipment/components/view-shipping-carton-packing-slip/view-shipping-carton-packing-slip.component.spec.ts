import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ViewShippingCartonPackingSlipComponent } from './view-shipping-carton-packing-slip.component';
import {
    CartonPackingSlipDTO,
    PackingSlipProductDTO
} from '../../graphql/query-definitions/generate-carton-packing-slip.graphql';

describe('ViewShippingCartonPackingSlipComponent', () => {
  let component: ViewShippingCartonPackingSlipComponent;
  let fixture: ComponentFixture<ViewShippingCartonPackingSlipComponent>;

  function createMockProduct(index: number): PackingSlipProductDTO {
    return {
      unitNumber: `UNIT-${index}`,
      volume: `${250 + index}`,
      collectionDate: `2025-05-${String(index + 1).padStart(2, '0')}`
    }
  }

  function createMockCartonPackingSlipWithFewProducts(): CartonPackingSlipDTO {
    const products: PackingSlipProductDTO[] = [];
    for (let i = 0; i < 5; i++) {
      products.push(createMockProduct(i));
    }

    return {
      cartonId: 12345,
      cartonNumber: 'CTN-98765',
      cartonSequence: 3,
      totalProducts: products.length,
      dateTimePacked: '2023-06-15T14:30:00',
      packedByEmployeeId: 'EMP-001',
      testingStatement: 'All products have been tested according to protocol XYZ-123.',
      shipFromBloodCenterName: 'Central Blood Center',
      shipFromLicenseNumber: 'LIC-12345',
      shipFromLocationAddress: '123 Donation Ave, Blood City, BC 12345',
      shipToAddress: '456 Plasma St, Medical City, MC 67890',
      shipToCustomerName: 'Medical Research Institute',
      shipmentNumber: 'SHP-54321',
      shipmentProductType: 'Plasma',
      shipmentProductDescription: 'Recovered Plasma',
      cartonProductCode: 'PL-001',
      cartonProductDescription: 'Human Plasma, Recovered',
      shipmentTransportationReferenceNumber: 'TRN-987654',
      displaySignature: true,
      displayTransportationReferenceNumber: true,
      displayTestingStatement: true,
      displayLicenceNumber: true,
      products: products
    };
  }

  function createMockCartonPackingSlipWithManyProducts(): CartonPackingSlipDTO {
    const products: PackingSlipProductDTO[] = [];
    for (let i = 0; i < 20; i++) { // more rows than splitGroupsTableRowsCount limit
      products.push(createMockProduct(i));
    }

    const mockData = createMockCartonPackingSlipWithFewProducts();
    mockData.products = products;
    mockData.totalProducts = products.length;

    return mockData;
  }

  describe('with few products', () => {
    const mockCartonPackingSlip = createMockCartonPackingSlipWithFewProducts();

    beforeEach(async () => {
      await TestBed.configureTestingModule({
        imports: [ViewShippingCartonPackingSlipComponent],
        providers: [
          { provide: MAT_DIALOG_DATA, useValue: mockCartonPackingSlip }
        ]
      }).compileComponents();

      fixture = TestBed.createComponent(ViewShippingCartonPackingSlipComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should set reportModel property from injected data', () => {
      expect(component.data).toEqual(mockCartonPackingSlip);
    });

    it('should display blood center name and license number', () => {
      const bloodCenterElement = fixture.debugElement.query(By.css('[data-testid="blood-center-name"]'));
      const licenseElement = fixture.debugElement.query(By.css('[data-testid="license-number"]'));

      expect(bloodCenterElement.nativeElement.textContent).toBe(mockCartonPackingSlip.shipFromBloodCenterName);
      expect(licenseElement.nativeElement.textContent).toBe(`License #${mockCartonPackingSlip.shipFromLicenseNumber}`);
    });

    it('should display shipping information correctly', () => {
      const headerTable = fixture.debugElement.query(By.css('#packingSlipHeader'));
      const tableContent = headerTable.nativeElement.textContent;

      expect(tableContent).toContain(mockCartonPackingSlip.shipToCustomerName);
      expect(tableContent).toContain(mockCartonPackingSlip.shipToAddress);
      expect(tableContent).toContain(mockCartonPackingSlip.shipFromBloodCenterName);
      expect(tableContent).toContain(mockCartonPackingSlip.shipFromLocationAddress);
    });

    it('should display summary information correctly', () => {
      const summaryTable = fixture.debugElement.query(By.css('#packingSlipSummaryTable'));
      const tableContent = summaryTable.nativeElement.textContent;

      expect(tableContent).toContain(mockCartonPackingSlip.shipmentProductDescription);
      expect(tableContent).toContain(mockCartonPackingSlip.cartonProductDescription);
      expect(tableContent).toContain(mockCartonPackingSlip.cartonProductCode);
      expect(tableContent).toContain(mockCartonPackingSlip.cartonNumber);
      expect(tableContent).toContain(mockCartonPackingSlip.shipmentNumber);
      expect(tableContent).toContain(mockCartonPackingSlip.cartonSequence.toString());
      expect(tableContent).toContain(mockCartonPackingSlip.packedByEmployeeId);
      expect(tableContent).toContain(mockCartonPackingSlip.dateTimePacked);
      expect(tableContent).toContain(mockCartonPackingSlip.totalProducts.toString());
      expect(tableContent).toContain(mockCartonPackingSlip.shipmentTransportationReferenceNumber);
    });

    it('should display products in the standard table for few products', () => {
      const unitsTable = fixture.debugElement.query(By.css('#packingSlipUnitsTable'));
      expect(unitsTable).toBeTruthy();

      const unitGroupsTable = fixture.debugElement.query(By.css('#packingSlipUnitGroupsTable'));
      expect(unitGroupsTable).toBeFalsy();

      // Check that all products are displayed
      mockCartonPackingSlip.products.forEach(product => {
        const tableContent = unitsTable.nativeElement.textContent;
        expect(tableContent).toContain(product.unitNumber);
        expect(tableContent).toContain(product.collectionDate);
        expect(tableContent).toContain(`${product.volume} mL`);
      });
    });

    it('should display testing statement when displayTestingStatement is true', () => {
      const testingStatement = fixture.debugElement.query(By.css('span[data-testid=testing-statement]'));
      expect(testingStatement.nativeElement.textContent).toBe(mockCartonPackingSlip.testingStatement);
    });

    it('should display signature field when displaySignature is true', () => {
      const signatureText = fixture.debugElement.query(By.css('span[data-testid=signature]'));
      expect(signatureText.nativeElement.textContent.trim()).toBe('Signature:');
    });

    it('should display transportation reference number when displayTransportationReferenceNumber is true', () => {
      const transportationReferenceNumberLabel = fixture.debugElement.query(By.css('td[data-testid=transportation-reference-number-label]'));
      expect(transportationReferenceNumberLabel.nativeElement.textContent).toBe('Transportation Reference Number:');
      const transportationReferenceNumberValue = fixture.debugElement.query(By.css('td[data-testid=transportation-reference-number-value]'));
      expect(transportationReferenceNumberValue.nativeElement.textContent).toBe('TRN-987654');
    });
  });

  describe('with many products', () => {
    const mockCartonPackingSlip = createMockCartonPackingSlipWithManyProducts();

    beforeEach(async () => {
      await TestBed.configureTestingModule({
        imports: [ViewShippingCartonPackingSlipComponent],
        providers: [
          { provide: MAT_DIALOG_DATA, useValue: mockCartonPackingSlip }
        ]
      }).compileComponents();

      fixture = TestBed.createComponent(ViewShippingCartonPackingSlipComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should display products in the grouped table for many products', () => {
      const unitsTable = fixture.debugElement.query(By.css('#packingSlipUnitsTable'));
      expect(unitsTable).toBeFalsy();

      const unitGroupsTable = fixture.debugElement.query(By.css('#packingSlipUnitGroupsTable'));
      expect(unitGroupsTable).toBeTruthy();

      // Check that the table has the correct number of rows (products are displayed in pairs)
      const tableRows = unitGroupsTable.queryAll(By.css('tbody tr'));
      const expectedRowCount = Math.ceil(mockCartonPackingSlip.products.length / 2);
      expect(tableRows.length).toBe(expectedRowCount);

      // Check the first row's content
      const firstRowCells = tableRows[0].queryAll(By.css('td'));
      expect(firstRowCells[0].nativeElement.textContent).toBe(mockCartonPackingSlip.products[0].unitNumber);
      expect(firstRowCells[1].nativeElement.textContent).toBe(mockCartonPackingSlip.products[0].collectionDate);
      expect(firstRowCells[2].nativeElement.textContent).toContain(`${mockCartonPackingSlip.products[0].volume} mL`);
      expect(firstRowCells[3].nativeElement.textContent).toBe(mockCartonPackingSlip.products[1].unitNumber);
      expect(firstRowCells[4].nativeElement.textContent).toBe(mockCartonPackingSlip.products[1].collectionDate);
      expect(firstRowCells[5].nativeElement.textContent).toContain(`${mockCartonPackingSlip.products[1].volume} mL`);
    });

    it('should correctly build product split groups', () => {
      const products = mockCartonPackingSlip.products;
      const splitGroups = component.buildReportModelProductsSplitGroups(products);

      expect(splitGroups.length).toBe(Math.ceil(products.length / 2));

      // Check first group
      expect(splitGroups[0].leftSide).toBe(products[0]);
      expect(splitGroups[0].rightSide).toBe(products[1]);

      // Check last group (might have only leftSide if odd number of products)
      const lastGroupIndex = splitGroups.length - 1;
      const lastProductIndex = products.length - 1;
      const isOddCount = products.length % 2 !== 0;

      if (isOddCount) {
        expect(splitGroups[lastGroupIndex].leftSide).toBe(products[lastProductIndex]);
        expect(splitGroups[lastGroupIndex].rightSide).toBeUndefined();
      } else {
        expect(splitGroups[lastGroupIndex].leftSide).toBe(products[lastProductIndex - 1]);
        expect(splitGroups[lastGroupIndex].rightSide).toBe(products[lastProductIndex]);
      }
    });
  });

  describe('with conditional display flags set to false', () => {
    const mockCartonPackingSlip = createMockCartonPackingSlipWithFewProducts();
    mockCartonPackingSlip.displayLicenceNumber = false;
    mockCartonPackingSlip.displayTestingStatement = false;
    mockCartonPackingSlip.displaySignature = false;
    mockCartonPackingSlip.displayTransportationReferenceNumber = false;

    beforeEach(async () => {
      await TestBed.configureTestingModule({
        imports: [ViewShippingCartonPackingSlipComponent],
        providers: [
          { provide: MAT_DIALOG_DATA, useValue: mockCartonPackingSlip }
        ]
      }).compileComponents();

      fixture = TestBed.createComponent(ViewShippingCartonPackingSlipComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should not display license number when displayLicenceNumber is false', () => {
      const licenseElement = fixture.debugElement.query(By.css('span[data-testid=license-number]'));
      expect(licenseElement).toBeFalsy();
    });

    it('should not display testing statement when displayTestingStatement is false', () => {
      const testingStatement = fixture.debugElement.query(By.css('span[data-testid=testing-statement]'));
      expect(testingStatement).toBeFalsy()
    });

    it('should not display signature field when displaySignature is false', () => {
      const signatureText = fixture.debugElement.query(By.css('span[data-testid=signature]'));
      expect(signatureText).toBeFalsy();
    });

    it('should not display transportation reference number when displayTransportationReferenceNumber is false', () => {
      const transportationReferenceNumberLabel = fixture.debugElement.query(By.css('td[data-testid=transportation-reference-number-label]'));
      expect(transportationReferenceNumberLabel).toBeFalsy();
    });
  });
});
