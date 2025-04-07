import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ShippingInformationCardComponent } from './shipping-information-card.component';

describe('ShippingInformationCardComponent', () => {
    let component: ShippingInformationCardComponent;
    let fixture: ComponentFixture<ShippingInformationCardComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [ShippingInformationCardComponent],
        }).compileComponents();

        fixture = TestBed.createComponent(ShippingInformationCardComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
