import { ComponentRef } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ShippingCartonInformationCardComponent } from './shipping-carton-information-card.component';

describe('ShippingCartonInformationCardComponent', () => {
    let component: ShippingCartonInformationCardComponent;
    let fixture: ComponentFixture<ShippingCartonInformationCardComponent>;
    let componentRef: ComponentRef<ShippingCartonInformationCardComponent>;

    const mockCartonData = {
        cartonNumber: 'CARTON123',
        totalVolume: 500,
    };

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                ShippingCartonInformationCardComponent,
                NoopAnimationsModule,
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(
            ShippingCartonInformationCardComponent
        );
        component = fixture.componentInstance;
        componentRef = fixture.componentRef;
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

    it('should render carton information correctly', () => {
        componentRef.setInput('cartonInput', mockCartonData);
        fixture.detectChanges();

        const shippingCartonInfoDescriptions =
            fixture.debugElement.nativeElement.querySelector(
                '#shippingCartonInfoDescriptions'
            ) as HTMLElement;

        const cartonNumberValue = shippingCartonInfoDescriptions.querySelector(
            '#informationDetails-Carton-Number-value'
        ) as HTMLSpanElement;
        expect(cartonNumberValue.textContent).toContain(
            mockCartonData.cartonNumber
        );

        const totalVolumeValue = shippingCartonInfoDescriptions.querySelector(
            '#informationDetails-Total-Volume-\\(L\\)-value'
        ) as HTMLSpanElement;
        expect(totalVolumeValue.textContent).toContain(
            mockCartonData.totalVolume.toString()
        );
    });

    it('should handle button state correctly', () => {
        component.showBasicButton = true;
        component.isButtonDisabled = true;
        fixture.detectChanges();

        const button = fixture.debugElement.nativeElement.querySelector(
            'button'
        ) as HTMLButtonElement;
        expect(button).toBeTruthy();
        expect(button.disabled).toBeTruthy();

        component.isButtonDisabled = false;
        fixture.detectChanges();
        expect(button.disabled).toBeFalsy();
    });

    it('should compute carton descriptions correctly', () => {
        componentRef.setInput('cartonInput', mockCartonData);
        fixture.detectChanges();

        const cartonDescriptions = component['carton']();

        expect(cartonDescriptions).toEqual([
            {
                label: 'Carton Number',
                value: mockCartonData.cartonNumber,
            },
            {
                label: 'Total Volume (L)',
                value: mockCartonData.totalVolume,
            },
            {
                label: 'Minimum Products',
            },
            {
                label: 'Maximum Products',
            },
        ]);
    });
});
