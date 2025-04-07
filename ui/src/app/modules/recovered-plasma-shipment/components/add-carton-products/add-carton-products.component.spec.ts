import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddCartonProductsComponent } from './add-carton-products.component';

describe('AddCartonProductsComponent', () => {
    let component: AddCartonProductsComponent;
    let fixture: ComponentFixture<AddCartonProductsComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [AddCartonProductsComponent],
        }).compileComponents();

        fixture = TestBed.createComponent(AddCartonProductsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
