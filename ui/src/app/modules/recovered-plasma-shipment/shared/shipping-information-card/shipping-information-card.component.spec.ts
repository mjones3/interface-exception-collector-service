import { DatePipe } from '@angular/common';
import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ProductFamilyMap } from 'app/shared/models/product-family.model';
import { ShippingInformationCardComponent } from './shipping-information-card.component';

describe('ShippingInformationCardComponent', () => {
    let component: ShippingInformationCardComponent;
    let fixture: ComponentFixture<ShippingInformationCardComponent>;
    let componentRef: ComponentRef<ShippingInformationCardComponent>;
    let datePipe: DatePipe;

    const mockShippingData = {
        shipmentNumber: 'SHIP123',
        customerCode: 'CUST456',
        customerName: 'Test Customer',
        status: 'In Transit',
        productType: 'PLASMA',
        shipmentDate: '2024-01-15',
        totalCartons: 10,
        totalProducts: 100,
        totalVolume: 1000,
        transportationReferenceNumber: 'TRANS789',
    };

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [ShippingInformationCardComponent, NoopAnimationsModule],
            providers: [DatePipe],
        }).compileComponents();

        fixture = TestBed.createComponent(ShippingInformationCardComponent);
        component = fixture.componentInstance;
        componentRef = fixture.componentRef;
        datePipe = TestBed.inject(DatePipe);
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should emit event when handleButtonClick is called', () => {
        const emitSpy = jest.spyOn(component.handleClick, 'emit');
        component.handleButtonClick();
        expect(emitSpy).toHaveBeenCalled();
    });

    it('should map product type using ProductFamilyMap', () => {
        const shippingValue = component['shipping']();
        const productTypeItem = shippingValue.find(
            (item) => item.label === 'Product Type'
        );
        expect(productTypeItem?.value).toBe(
            ProductFamilyMap[mockShippingData.productType]
        );
    });

    it('should render shipping information Card', () => {
        const shipmentNumber = '123';
        const transportationReferenceNumber = null;
        const shipmentDate = new Date().toISOString();
        componentRef.setInput('shippingInput', {
            shipmentNumber: shipmentNumber,
            transportationReferenceNumber: transportationReferenceNumber,
            shipmentDate: shipmentDate,
        });
        fixture.detectChanges();

        const shippingInfoDescriptions =
            fixture.debugElement.nativeElement.querySelector(
                '#shippingInfoDescriptions'
            ) as HTMLElement;

        const transportationReferenceNumberValue =
            shippingInfoDescriptions.querySelector(
                '#informationDetails-Transportation-Reference-Number-value'
            ) as HTMLSpanElement;
        const shipmentNumberValue = shippingInfoDescriptions.querySelector(
            '#informationDetails-Shipment-Number-value'
        ) as HTMLSpanElement;

        const shipmentDateValue = shippingInfoDescriptions.querySelector(
            '#informationDetails-Shipment-Date-value'
        ) as HTMLSpanElement;
        expect(shipmentDateValue.textContent).toContain(
            datePipe.transform(shipmentDate, 'MM/dd/yyyy')
        );
        expect(shipmentNumberValue.textContent).toContain(
            shipmentNumber.toUpperCase()
        );
        expect(transportationReferenceNumberValue).toBe(null);
    });
});
