import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatIconTestingModule } from '@angular/material/icon/testing';
import { By } from '@angular/platform-browser';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { provideMockStore } from '@ngrx/store/testing';
import { TranslateModule } from '@ngx-translate/core';
import { ToastrImplService } from '@shared';
import {
    ApolloTestingController,
    ApolloTestingModule,
} from 'apollo-angular/testing';
import { ConfirmationAcknowledgmentService } from 'app/shared/services/confirmation-acknowledgment.service';
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
    let confirmationAcknowledgmentService: ConfirmationAcknowledgmentService;
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
        confirmationAcknowledgmentService = TestBed.inject(
            ConfirmationAcknowledgmentService
        );
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
        const notification = {
            statusCode: 1,
            notificationType: '',
            message: '',
            details: ['expired', 'broken bag'],
            code: 1,
        };
        jest.spyOn(
            confirmationAcknowledgmentService,
            'notificationConfirmation'
        );
        component.openAcknowledgmentMessageDialog(notification);
        expect(
            confirmationAcknowledgmentService.notificationConfirmation
        ).toHaveBeenCalled();
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

    it('should disable select all and remove buttons when filled product is empty', () => {
        component.filledProductsData = [];
        const selectAllBtn = fixture.debugElement.query(
            By.css('#select-all-btn')
        ).nativeElement;
        const removeBtn = fixture.debugElement.query(
            By.css('#remove-btn')
        ).nativeElement;

        expect(selectAllBtn.disabled).toBeTruthy();
        expect(removeBtn.disabled).toBeTruthy();
    });

    it('should enable selecte all button when alteast one product is added on the list', () => {
        component.filledProductsData = [{}];
        fixture.detectChanges();
        const selectAllBtn = fixture.debugElement.query(
            By.css('#select-all-btn')
        ).nativeElement;
        expect(selectAllBtn.disabled).toBeFalsy();
    });

    it('should enable remove button when at least one product is selected', () => {
        component.selectedProducts = [
            {
                unitNumber: 'W12121212121',
                productCode: 'E121212V44',
            },
        ];
        fixture.detectChanges();
        const selectAllBtn = fixture.debugElement.query(
            By.css('#remove-btn')
        ).nativeElement;
        expect(selectAllBtn.disabled).toBeFalsy();
    });

    it('should disable submit button when at least one product is selected', () => {
        component.selectedProducts = [
            { unitNumber: 'W12121212132', productCode: 'E121212V0' },
        ];
        fixture.detectChanges();
        const submitBtn = fixture.debugElement.query(
            By.css('#backActionBtn')
        ).nativeElement;
        expect(submitBtn.disabled).toBeTruthy();
    });

    it('should remove selected products when user choose remove option', () => {
        component.filledProductsData = [
            { unitNumber: 'w1233333333333', productCode: 'E23231111' },
            { unitNumber: 'w1212121455212', productCode: 'E232454532V0' },
        ];
        jest.spyOn(toaster, 'show');
        const enableFillUnitNumberAndProductCodeSpy = jest.spyOn(
            component,
            'enableFillUnitNumberAndProductCode'
        );
        const unpackItemsSpy = jest
            .spyOn(service, 'unpackedItem')
            .mockReturnValue(
                of({
                    data: {
                        unpackItems: {
                            ruleCode: '200 OK',
                            _links: null,
                            results: {
                                results: [
                                    {
                                        packedItems: [
                                            {
                                                unitNumber: 'w1233333333333',
                                                productCode: 'E23231111',
                                            },
                                        ],
                                    },
                                ],
                            },
                            notifications: [
                                {
                                    statusCode: 200,
                                    notificationType: 'SUCCESS',
                                    name: 'REMOVED SUCCESSFULLY',
                                    action: null,
                                    reason: null,
                                    code: null,
                                    message: 'REMOVED SUCCESSFULLY',
                                },
                            ],
                        },
                    },
                })
            );
        component.selectedProducts = [
            {
                unitNumber: 'w1212121455212',
                productCode: 'E232454532V0',
            },
        ];
        component.removeSelectedProducts();
        fixture.detectChanges();
        expect(unpackItemsSpy).toHaveBeenCalled();
        expect(component.filledProductsData.length).toBe(1);
        expect(component.selectedProducts.length).toBe(0);
        expect(enableFillUnitNumberAndProductCodeSpy).toHaveBeenCalled();
    });
});
