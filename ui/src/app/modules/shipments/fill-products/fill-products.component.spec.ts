import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatIconTestingModule } from '@angular/material/icon/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { provideMockStore } from '@ngrx/store/testing';
import { TranslateModule } from '@ngx-translate/core';
import { ApolloTestingModule } from 'apollo-angular/testing';
import { ToastrModule } from 'ngx-toastr';
import { of } from 'rxjs';
import { ShipmentService } from '../services/shipment.service';
import { FillProductsComponent } from './fill-products.component';

describe('FillProductsComponent', () => {
    let component: FillProductsComponent;
    let fixture: ComponentFixture<FillProductsComponent>;
    let router: Router;
    let service: ShipmentService;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                FillProductsComponent,
                NoopAnimationsModule,
                ApolloTestingModule,
                MatIconTestingModule,
                ToastrModule.forRoot(),
                TranslateModule.forRoot(),
            ],
            providers: [
                {
                    provide: ActivatedRoute,
                    useValue: {
                        paramMap: of({}),
                        snapshot: {
                            params: { id: 1, productId: '1' },
                        },
                    },
                },
                provideMockStore(),
                ShipmentService,
            ],
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(FillProductsComponent);
        router = TestBed.inject(Router);
        component = fixture.componentInstance;
        service = TestBed.inject(ShipmentService);
        jest.spyOn(service, 'getShipmentById').mockReturnValue(of());
        jest.spyOn(router, 'navigateByUrl');
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should navigate back to shipment details page', () => {
        component.backToShipmentDetails();
        expect(router.navigateByUrl).toHaveBeenCalledWith(
            '/shipment/1/shipment-details'
        );
    });
});
