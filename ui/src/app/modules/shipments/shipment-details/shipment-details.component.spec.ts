import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatIconTestingModule } from '@angular/material/icon/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { provideMockStore } from '@ngrx/store/testing';
import { TranslateModule } from '@ngx-translate/core';
import { ApolloModule } from 'apollo-angular';
import { ApolloTestingModule } from 'apollo-angular/testing';
import { ToastrModule } from 'ngx-toastr';
import { of } from 'rxjs';
import { ShipmentService } from '../services/shipment.service';
import { ShipmentDetailsComponent } from './shipment-details.component';

const SHIPMENT_ID = 1;
describe('ShipmentDetailsComponent', () => {
    let component: ShipmentDetailsComponent;
    let fixture: ComponentFixture<ShipmentDetailsComponent>;
    let shipmentService: ShipmentService;
    let router: Router;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                ShipmentDetailsComponent,
                ApolloTestingModule,
                NoopAnimationsModule,
                MatIconTestingModule,
                ApolloModule,
                TranslateModule.forRoot(),
                ToastrModule.forRoot(),
            ],
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
                            data: {
                                shipmentDetailsConfigData: [],
                            },
                        },
                    },
                },
                ShipmentService,
            ],
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(ShipmentDetailsComponent);
        component = fixture.componentInstance;
        shipmentService = TestBed.inject(ShipmentService);
        router = TestBed.inject(Router);
        jest.spyOn(shipmentService, 'getShipmentById').mockReturnValue(of());
        jest.spyOn(router, 'navigateByUrl');
        jest.spyOn(shipmentService, 'completeShipment').mockReturnValue(of());
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should fetch shipment details', () => {
        component.fetchShipmentDetails();
        expect(shipmentService.getShipmentById).toHaveBeenCalledWith(
            SHIPMENT_ID
        );
    });

    it('should navigate back to Order Details', () => {
        component.shipmentInfo = {
            id: 1,
            orderNumber: 10,
            status: 'open',
            createDate: '2024-03-05',
            shippingMethod: '',
            customerAddressPostalCode: '',
            customerAddressCountryCode: '',
        };
        component.backToOrderDetails();
        expect(router.navigateByUrl).toHaveBeenCalledWith(
            `/orders/10/order-details`
        );
    });

    it('should navigate to fill product when click on Fill Product button', () => {
        const shipment = {
            id: '1',
        };
        component.fillProducts(shipment);
        expect(router.navigateByUrl).toHaveBeenCalledWith(
            'shipment/1/fill-products/1'
        );
    });

    it('should complete on click complete shipment button', () => {
        component.loggedUserId = 'user-id-12';
        component.completeShipment();
        expect(shipmentService.completeShipment).toHaveBeenCalledWith({
            shipmentId: 1,
            employeeId: 'user-id-12',
        });
    });

    it('should hide verify products button if showVerifyProductOption is false', () => {
        component.packedItems = [
            { unitNumber: 'W121212121212', productCode: 'W121F22' },
        ];
        jest.spyOn(component, 'isProductComplete', 'get').mockReturnValue(
            false
        );
        component.showVerifyProductOption = false;
        fixture.detectChanges();
        expect(
            fixture.debugElement.nativeElement.querySelector(
                '#verifyProductsBtn'
            )
        ).toBeFalsy();
    });

    it('should display complete shipment button', () => {
        component.packedItems = [
            { unitNumber: 'W121212121212', productCode: 'W121F22' },
        ];
        jest.spyOn(component, 'isProductComplete', 'get').mockReturnValue(
            false
        );
        component.showVerifyProductOption = false;
        fixture.detectChanges();
        expect(
            fixture.debugElement.nativeElement.querySelector(
                '#completeShipmentBtn'
            )
        ).toBeTruthy();
    });
});
