import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { EnterProductsComponent } from './enter-products.component';

describe('EnterProductsComponent', () => {
    let component: EnterProductsComponent;
    let fixture: ComponentFixture<EnterProductsComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [EnterProductsComponent, BrowserAnimationsModule],
        }).compileComponents();

        fixture = TestBed.createComponent(EnterProductsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('Should disable Product Code field  when Check Digit value is blank', () => {
        component.productGroup.controls['unitNumber'].setValue('XY33');
        fixture.detectChanges();
        expect(
            component.productGroup.controls.productCode.disabled
        ).toBeTruthy();
    });
});
