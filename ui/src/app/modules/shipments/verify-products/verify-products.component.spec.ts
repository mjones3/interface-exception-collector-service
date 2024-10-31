import { ComponentFixture, TestBed } from '@angular/core/testing';

import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ActivatedRoute } from '@angular/router';
import { provideMockStore } from '@ngrx/store/testing';
import { VerifyProductsComponent } from './verify-products.component';

const SHIPMENT_ID = 1;
describe('VerifyProductsComponent', () => {
    let component: VerifyProductsComponent;
    let fixture: ComponentFixture<VerifyProductsComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [VerifyProductsComponent],
            providers: [
                provideHttpClientTesting(),
                provideMockStore({}),
                {
                    provide: ActivatedRoute,
                    useValue: {
                        snapshot: {
                            params: {
                                id: SHIPMENT_ID,
                            },
                        },
                    },
                },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(VerifyProductsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
