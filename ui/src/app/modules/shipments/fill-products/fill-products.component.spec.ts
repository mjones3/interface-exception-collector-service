import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatIconTestingModule } from '@angular/material/icon/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { FuseConfirmationService } from '@fuse/services/confirmation';
import { provideMockStore } from '@ngrx/store/testing';
import { TranslateModule } from '@ngx-translate/core';
import { ToastrImplService } from '@shared';
import {
    ApolloTestingController,
    ApolloTestingModule,
} from 'apollo-angular/testing';
import { ToastrModule } from 'ngx-toastr';
import { of } from 'rxjs';
import { VerifyFilledProductDto } from '../models/shipment-info.dto';
import { ShipmentService } from '../services/shipment.service';
import { FillProductsComponent } from './fill-products.component';

describe('FillProductsComponent', () => {
    let component: FillProductsComponent;
    let fixture: ComponentFixture<FillProductsComponent>;
    let router: Router;
    let service: ShipmentService;
    let toaster: ToastrImplService;
    let confirmationService: FuseConfirmationService;
    let controller: ApolloTestingController;

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
        toaster = TestBed.inject(ToastrImplService);
        service = TestBed.inject(ShipmentService);
        confirmationService = TestBed.inject(FuseConfirmationService);
        controller = TestBed.inject(ApolloTestingController);

        jest.spyOn(service, 'getShipmentById').mockReturnValue(of());
        jest.spyOn(service, 'verifyShipmentProduct').mockReturnValue(of());
        jest.spyOn(router, 'navigateByUrl');
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should fetch shipment details', () => {
        component.fetchShipmentDetails();
        expect(service.getShipmentById).toHaveBeenCalledWith(1);
    });

    it('should add filled products', () => {
        const filledProduct: VerifyFilledProductDto = {
            unitNumber: 'W036898786769',
            productCode: 'E9747D0E',
            visualInspection: 'SATISFACTORY',
        };
        component.filledProductsData = [];
        component.unitNumberProductCodeSelected(filledProduct);
        expect(service.verifyShipmentProduct).toHaveBeenCalled();
    });

    it('should navigate back to shipment details page', () => {
        component.backToShipmentDetails();
        expect(router.navigateByUrl).toHaveBeenCalledWith(
            '/shipment/1/shipment-details'
        );
    });
    it('should call displayMessageFromNotificationDto', () => {
        const item: VerifyFilledProductDto = {
            unitNumber: '',
            productCode: '',
            visualInspection: '',
        };

        jest.spyOn(service, 'verifyShipmentProduct').mockReturnValue(
            of({
                data: {
                    packItem: {
                        ruleCode: '400 BAD_REQUEST',
                        notifications: [
                            {
                                statusCode: 400,
                                notificationType: 'WARN',
                                name: 'PRODUCT_ALREADY_USED_ERROR',
                                action: null,
                                reason: null,
                                code: null,
                                message: 'Product already used',
                            },
                        ],
                        _links: null,
                        results: {
                            inventory: [[]],
                        },
                    },
                },
            })
        );

        jest.spyOn(component, 'displayMessageFromNotificationDto');
        component.unitNumberProductCodeSelected(item);
        expect(service.verifyShipmentProduct).toHaveBeenCalled();
        expect(component.displayMessageFromNotificationDto).toHaveBeenCalled();
    });

    it('should display toaster', () => {
        const notification = [
            {
                statusCode: 400,
                notificationType: 'WARN',
                name: 'PRODUCT_ALREADY_USED_ERROR',
                action: null,
                reason: null,
                code: null,
                message: 'Product already used',
            },
        ];

        jest.spyOn(toaster, 'show');
        component.displayMessageFromNotificationDto(notification);
        expect(toaster.show).toHaveBeenCalled();
    });

    it('should open openAcknowledgmentMessageDialog', () => {
        const notification = [
            {
                statusCode: 1,
                notificationType: '',
                message: '',
                code: 1,
            },
        ];
        jest.spyOn(confirmationService, 'open');
        component.openAcknowledgmentMessageDialog(notification);
        expect(confirmationService.open).toHaveBeenCalled();
    });

    it('should increment selected units card length', () => {
        const products = {
            unitNumber: 'W036898786811',
            productCode: 'E4701V00',
            visualInspection: 'SATISFACTORY',
        };
        component.selectedProducts = [];
        component.toggleProduct(products);
        fixture.detectChanges();
        expect(component.selectedProducts.length).toBe(1);
    });

    it('should decrement selected units card length from selectedProducts', () => {
        const products = {
            unitNumber: 'W036898786811',
            productCode: 'E4701V00',
            visualInspection: 'SATISFACTORY',
        };
        component.selectedProducts = [];
        component.toggleProduct(products);
        fixture.detectChanges();
        expect(component.selectedProducts.length).toBe(1);
        component.toggleProduct(products);
        fixture.detectChanges();
        expect(component.selectedProducts.length).toBe(0);
    });
});
