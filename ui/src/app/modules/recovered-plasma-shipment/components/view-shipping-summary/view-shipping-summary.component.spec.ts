import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { DomSanitizer } from '@angular/platform-browser';
import { DatePipe } from '@angular/common';
import { BrowserPrintingService } from '../../../../core/services/browser-printing/browser-printing.service';

import { ViewShippingSummaryComponent } from './view-shipping-summary.component';
import { ShippingSummaryReportDTO } from '../../graphql/query-definitions/print-shipping-summary-report.graphql';
import { MatIconTestingModule } from '@angular/material/icon/testing';

describe('ViewShippingSummaryComponent', () => {
    let component: ViewShippingSummaryComponent;
    let fixture: ComponentFixture<ViewShippingSummaryComponent>;
    let browserPrintingService: jest.Mocked<BrowserPrintingService>;

    function createCartonItem(index: number) {
        return {
            cartonNumber: `CARTON-${index}`,
            productCode: `CODE-${index}`,
            productDescription: `Description ${index}`,
            totalProducts: index + 1
        };
    }

    function createShippingSummaryReport(numberOfCartons: number = 3): ShippingSummaryReportDTO {
        const cartonList = Array(numberOfCartons)
            .fill(null)
            .map((_, index) => createCartonItem(index));

        return {
            reportTitle: 'Shipping Summary Report',
            employeeName: 'John Doe',
            employeeId: 'EMP123',
            closeDateTime: '2024-01-20T10:00:00',
            closeDate: '2024-01-20',
            shipmentDetailShipmentNumber: 'SHIP-001',
            shipmentDetailProductType: 'Plasma',
            shipmentDetailProductCode: 'PLS-001',
            shipmentDetailTotalNumberOfCartons: numberOfCartons,
            shipmentDetailTotalNumberOfProducts: cartonList.reduce((sum, item) => sum + item.totalProducts, 0),
            shipmentDetailTransportationReferenceNumber: 'TRANS-001',
            shipmentDetailDisplayTransportationNumber: true,
            shipToAddress: '123 Receiver St, City, State 12345',
            shipToCustomerName: 'Receiving Center',
            shipFromBloodCenterName: 'Sending Blood Center',
            shipFromLocationAddress: '456 Sender Ave, City, State 54321',
            shipFromPhoneNumber: '(555) 123-4567',
            testingStatement: 'All products have been tested according to regulations',
            displayHeader: true,
            headerStatement: 'Plasma Shipment Summary',
            cartonList
        };
    }

    beforeEach(async () => {
        browserPrintingService = {
            print: jest.fn()
        } as any;

        await TestBed.configureTestingModule({
            imports: [
                ViewShippingSummaryComponent,
                MatIconTestingModule
            ],
            providers: [
                { provide: MAT_DIALOG_DATA, useValue: createShippingSummaryReport() },
                { provide: DomSanitizer, useValue: { bypassSecurityTrustHtml: (val: string) => val } },
                { provide: DatePipe, useValue: { transform: () => '01/20/2024' } },
                { provide: BrowserPrintingService, useValue: browserPrintingService }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(ViewShippingSummaryComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should display blood center name in header', () => {
        const headerElement = fixture.debugElement.query(By.css('.text-center.text-xl'));
        expect(headerElement.nativeElement.textContent).toBe('Sending Blood Center');
    });

    describe('Ship To/From Section', () => {
        it('should display ship to information correctly', () => {
            const shipToTable = fixture.debugElement.query(By.css('[data-testid="ship-to-blood-center"]'));
            const rows = shipToTable.queryAll(By.css('tr'));
            
            expect(rows[0].nativeElement.textContent).toContain('Receiving Center');
            expect(rows[1].nativeElement.textContent).toContain('123 Receiver St, City, State 12345');
        });

        it('should display ship from information correctly', () => {
            const shipFromTable = fixture.debugElement.query(By.css('[data-testid="ship-from-blood-center"]'));
            const rows = shipFromTable.queryAll(By.css('tr'));
            
            expect(rows[0].nativeElement.textContent).toContain('Sending Blood Center');
            expect(rows[1].nativeElement.textContent).toContain('456 Sender Ave, City, State 54321');
            expect(rows[2].nativeElement.textContent).toContain('(555) 123-4567');
        });
    });

    describe('Shipment Details Section', () => {
        it('should display shipment details correctly', () => {
            const shipmentDetailsTable = fixture.debugElement.query(By.css('[data-testid="shipment-details"]'));
            const rows = shipmentDetailsTable.queryAll(By.css('tr'));
            
            expect(rows[0].nativeElement.textContent).toContain('TRANS-001');
            expect(rows[1].nativeElement.textContent).toContain('SHIP-001');
            expect(rows[2].nativeElement.textContent).toContain('2024-01-20T10:00:00');
        });
    });

    describe('Product Shipped Section', () => {
        it('should display product information correctly', () => {
            const productTable = fixture.debugElement.query(By.css('[data-testid="product-shipped"]'));
            const rows = productTable.queryAll(By.css('tr'));
            
            expect(rows[0].nativeElement.textContent).toContain('Plasma');
            expect(rows[1].nativeElement.textContent).toContain('PLS-001');
        });
    });

    describe('Shipment Information Section', () => {
        it('should display shipment totals correctly', () => {
            const shipmentInfoTable = fixture.debugElement.query(By.css('[data-testid="shipment-information"]'));
            const rows = shipmentInfoTable.queryAll(By.css('tr'));
            
            expect(rows[0].nativeElement.textContent).toContain('3'); // Total cartons
            expect(rows[1].nativeElement.textContent).toContain('6'); // Total products (sum of 1+2+3)
        });
    });

    describe('Carton Information Section', () => {
        it('should display correct table headers', () => {
            const headerCells = fixture.debugElement.queryAll(By.css('[data-testid="carton-information"] thead th'));
            
            expect(headerCells[0].nativeElement.textContent).toBe('Carton Number');
            expect(headerCells[1].nativeElement.textContent).toBe('Product Code');
            expect(headerCells[2].nativeElement.textContent).toBe('Product Description');
            expect(headerCells[3].nativeElement.textContent).toBe('Total Number of Products');
        });

        it('should display all cartons in the table', () => {
            const tableRows = fixture.debugElement.queryAll(By.css('[data-testid="carton-information"] tbody tr'));
            expect(tableRows.length).toBe(3);

            // Check first row content
            const firstRow = tableRows[0];
            const cells = firstRow.queryAll(By.css('td'));
            expect(cells[0].nativeElement.textContent).toContain('CARTON-0');
            expect(cells[1].nativeElement.textContent).toContain('CODE-0');
            expect(cells[2].nativeElement.textContent).toContain('Description 0');
            expect(cells[3].nativeElement.textContent).toContain('1');
        });
    });

    describe('Testing Statement Section', () => {
        it('should display testing statement correctly', () => {
            const testingStatement = fixture.debugElement.query(By.css('[data-testid="testing-statement"]'));
            expect(testingStatement.nativeElement.textContent)
                .toBe('All products have been tested according to regulations');
        });
    });

    describe('Shipment Closing Details Section', () => {
        it('should display closing details correctly', () => {
            const closingDetailsTable = fixture.debugElement.query(By.css('[data-testid="shipment-closing-details"]'));
            const rows = closingDetailsTable.queryAll(By.css('tr'));
            
            expect(rows[0].nativeElement.textContent).toContain('John Doe');
            expect(rows[2].nativeElement.textContent).toContain('01/20/2024');
        });
    });

    describe('Print functionality', () => {
        it('should call browserPrintingService.print when print button is clicked', () => {
            const printButton = fixture.debugElement.query(By.css('#viewShippingSummaryReportPrintBtn'));
            printButton.triggerEventHandler('clickEvent', null);
            
            expect(browserPrintingService.print).toHaveBeenCalledWith('viewShippingSummaryReport');
        });
    });

    describe('Today getter', () => {
        it('should return formatted current date', () => {
            expect(component.today).toBe('01/20/2024');
        });
    });
});