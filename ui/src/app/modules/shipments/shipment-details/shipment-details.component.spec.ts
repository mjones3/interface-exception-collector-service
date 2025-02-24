import { DatePipe } from '@angular/common';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatIconTestingModule } from '@angular/material/icon/testing';
import { By } from '@angular/platform-browser';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { provideMockStore } from '@ngrx/store/testing';
import { TranslateModule } from '@ngx-translate/core';
import { ApolloModule } from 'apollo-angular';
import { ApolloTestingModule } from 'apollo-angular/testing';
import { ProductIconsService } from 'app/shared/services/product-icon.service';
import { ToastrModule } from 'ngx-toastr';
import { of } from 'rxjs';
import { ShipmentService } from '../services/shipment.service';
import { ShipmentDetailsComponent } from './shipment-details.component';

const SHIPMENT_ID = 1;
describe('ShipmentDetailsComponent', () => {
    let component: ShipmentDetailsComponent;
    let fixture: ComponentFixture<ShipmentDetailsComponent>;
    let shipmentService: ShipmentService;
    let productIconService: ProductIconsService;
    let router: Router;
    let datePipe: DatePipe;

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
                DatePipe,
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
        productIconService = TestBed.inject(ProductIconsService);
        router = TestBed.inject(Router);
        datePipe = TestBed.inject(DatePipe);
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
        component.manageProducts(shipment);
        expect(router.navigateByUrl).toHaveBeenCalledWith(
            'shipment/1/manage-products/1'
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

    describe('manage products button', () => {
        it('should display if shipment status is not completed', () => {
            jest.spyOn(component, 'isProductComplete', 'get').mockReturnValue(
                false
            );
            component.products = [{}];
            fixture.detectChanges();
            const manageProductsBtn0 = fixture.debugElement.query(
                By.css('#manageProductsBtn0')
            );
            expect(manageProductsBtn0).toBeTruthy();
        });

        it('should not display if shipment status is completed', () => {
            jest.spyOn(component, 'isProductComplete', 'get').mockReturnValue(
                true
            );
            component.products = [{}];
            fixture.detectChanges();
            const manageProductsBtn1 = fixture.debugElement.query(
                By.css('#manageProductsBtn1')
            );
            expect(manageProductsBtn1).toBeFalsy();
        });
    });

    it('should get icon based on product family', () => {
        let productFamily: string;
        jest.spyOn(
            productIconService,
            'getIconByProductFamily'
        ).mockReturnValue('WHOLE_BLOOD');
        component.getIcon(productFamily);
        fixture.detectChanges();
        expect(component.getIcon(productFamily)).toBe('WHOLE_BLOOD');
    });
});
