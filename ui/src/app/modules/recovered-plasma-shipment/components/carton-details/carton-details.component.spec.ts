import { CommonModule, DatePipe } from '@angular/common';
import { ComponentFixture, TestBed, fakeAsync } from '@angular/core/testing';
import { MatIconTestingModule } from '@angular/material/icon/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import {
    ActivatedRoute,
    ActivatedRouteSnapshot,
    ParamMap,
    Router,
} from '@angular/router';
import { ApolloError, ApolloQueryResult } from '@apollo/client';
import { Store } from '@ngrx/store';
import { ProcessHeaderService, ToastrImplService } from '@shared';
import { ApolloTestingModule } from 'apollo-angular/testing';
import { ScanUnitNumberProductCodeComponent } from 'app/shared/components/scan-unit-number-product-code/scan-unit-number-product-code.component';
import { ConfirmationAcknowledgmentService } from 'app/shared/services/confirmation-acknowledgment.service';
import { CookieService } from 'ngx-cookie-service';
import { of, throwError } from 'rxjs';
import { UseCaseResponseDTO } from '../../../../shared/models/use-case-response.dto';
import { ProductIconsService } from '../../../../shared/services/product-icon.service';
import { PackCartonItemsDTO } from '../../graphql/mutation-definitions/pack-items.graphql';
import { RecoveredPlasmaShipmentReportDTO } from '../../graphql/query-definitions/shipment.graphql';
import { FindShipmentRequestDTO } from '../../graphql/query-definitions/shipmentDetails.graphql';
import { CartonDTO } from '../../models/recovered-plasma.dto';
import { RecoveredPlasmaService } from '../../services/recovered-plasma.service';
import { cartonDetailsComponent } from './carton-details.component';

describe('cartonDetailsComponent', () => {
    let component: cartonDetailsComponent;
    let fixture: ComponentFixture<cartonDetailsComponent>;
    let mockRouter: jest.Mocked<Router>;
    let mockActivatedRoute: Partial<ActivatedRoute>;
    let mockStore: jest.Mocked<Store>;
    let mockRecoveredPlasmaService: jest.Mocked<RecoveredPlasmaService>;
    let mockToastr: jest.Mocked<ToastrImplService>;
    let mockProductIconService: jest.Mocked<ProductIconsService>;
    let mockCookieService: jest.Mocked<CookieService>;
    let toastr: ToastrImplService;
    let confirmationAcknowledgmentService: ConfirmationAcknowledgmentService;

    const mockCartonData = {
        id: 1,
        shipmentId: 100,
    };

    const mockShipmentData = {
        id: 100,
    };

    const mockPackedProductsData = {
        data: {
            packCartonItem: {
                notifications: [
                    {
                        message: 'Carton Item packed successfully',
                        type: 'SUCCESS',
                        code: 7,
                        reason: null,
                        action: null,
                        details: null,
                        name: null,
                    },
                ],
                data: {
                    id: 5,
                    cartonId: 2,
                    unitNumber: 'W036898786800',
                    productCode: 'E6022V00',
                    productDescription: 'CP2D PLS MI 120H',
                    productType: 'RP_FROZEN_WITHIN_120_HOURS',
                    volume: 229,
                    weight: 150,
                    packedByEmployeeId: '4c973896-5761-41fc-8217-07c5d13a004b',
                    aboRh: 'AP',
                    status: 'PACKED',
                    expirationDate: '2024-09-03T10:15:30',
                    collectionDate: '2011-12-03T09:15:30Z',
                    createDate: '2025-04-17T16:36:30.643488157Z',
                    modificationDate: '2025-04-17T16:36:30.643488157Z',
                },
                _links: null,
            },
        },
    };

    const mockPackedProductsDataInfo = {
        data: {
            packCartonItem: {
                notifications: [
                    {
                        message:
                            'This product is discarded and cannot be shipped',
                        type: 'INFO',
                        code: 7,
                        reason: null,
                        action: 'TRIGGER_DISCARD',
                        details: null,
                        name: null,
                    },
                ],
                data: {
                    id: 5,
                    cartonId: 2,
                    unitNumber: 'W036898786800',
                    productCode: 'E6022V00',
                    productDescription: 'CP2D PLS MI 120H',
                    productType: 'RP_FROZEN_WITHIN_120_HOURS',
                    volume: 229,
                    weight: 150,
                    packedByEmployeeId: '4c973896-5761-41fc-8217-07c5d13a004b',
                    aboRh: 'AP',
                    status: 'PACKED',
                    expirationDate: '2024-09-03T10:15:30',
                    collectionDate: '2011-12-03T09:15:30Z',
                    createDate: '2025-04-17T16:36:30.643488157Z',
                    modificationDate: '2025-04-17T16:36:30.643488157Z',
                },
                _links: null,
            },
        },
    };

    const mockPackedProductsDataWarn = {
        data: {
            packCartonItem: {
                notifications: [
                    {
                        message: 'Maximum number of products exceeded',
                        type: 'WARN',
                        code: 7,
                        reason: null,
                        action: null,
                        details: null,
                        name: null,
                    },
                ],
                data: null,
                _links: null,
            },
        },
    };

    beforeEach(async () => {
        mockRouter = {
            navigateByUrl: jest.fn(),
        } as unknown as jest.Mocked<Router>;

        mockActivatedRoute = {
            snapshot: {
                params: {
                    id: '1',
                } as unknown as ParamMap,
            } as unknown as ActivatedRouteSnapshot,
        };

        mockStore = {
            dispatch: jest.fn(),
            select: jest.fn(() => of({ id: 'employeeId' })),
        } as unknown as jest.Mocked<Store>;

        mockRecoveredPlasmaService = {
            getCartonById: jest.fn().mockReturnValue(
                of({
                    data: {
                        findCartonById: {
                            data: mockCartonData,
                            notifications: [],
                        },
                    },
                })
            ),
            getShipmentById: jest.fn().mockReturnValue(
                of({
                    data: {
                        findShipmentById: {
                            data: mockShipmentData,
                            notifications: [],
                        },
                    },
                })
            ),
            addCartonProducts: jest.fn().mockReturnValue(
                of({
                    data: {
                        findShipmentById: {
                            data: mockPackedProductsData,
                            notifications: [],
                        },
                    },
                })
            ),
        } as unknown as jest.Mocked<RecoveredPlasmaService>;

        mockToastr = {
            show: jest.fn(),
            error: jest.fn(),
            success: jest.fn(),
            warning: jest.fn(),
        } as unknown as jest.Mocked<ToastrImplService>;

        mockProductIconService = {
            getIconByProductFamily: jest.fn(() => 'icon'),
        } as unknown as jest.Mocked<ProductIconsService>;
        mockCookieService = {
            get: jest.fn(() => '123456789'),
        } as unknown as jest.Mocked<CookieService>;

        await TestBed.configureTestingModule({
            imports: [
                cartonDetailsComponent,
                MatIconTestingModule,
                NoopAnimationsModule,
                CommonModule,
                ApolloTestingModule,
                ScanUnitNumberProductCodeComponent,
            ],
            providers: [
                DatePipe,
                ProcessHeaderService,
                { provide: Router, useValue: mockRouter },
                { provide: ActivatedRoute, useValue: mockActivatedRoute },
                { provide: Store, useValue: mockStore },
                {
                    provide: RecoveredPlasmaService,
                    useValue: mockRecoveredPlasmaService,
                },
                { provide: ToastrImplService, useValue: mockToastr },
                {
                    provide: ProductIconsService,
                    useValue: mockProductIconService,
                },
                { provide: CookieService, useValue: mockCookieService },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(cartonDetailsComponent);
        toastr = TestBed.inject(ToastrImplService);
        confirmationAcknowledgmentService = TestBed.inject(
            ConfirmationAcknowledgmentService
        );
        component = fixture.componentInstance;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should load carton and shipment details on init', fakeAsync(() => {
        const expectedFindShipmentRequest: FindShipmentRequestDTO = {
            locationCode: '123456789',
            employeeId: 'employeeId',
            shipmentId: 100,
        };

        const id = 100;

        component.loadRecoveredPlasmaShippingDetails(id);
        fixture.detectChanges();

        expect(mockRecoveredPlasmaService.getCartonById).toHaveBeenCalledWith(
            1
        );
        expect(mockRecoveredPlasmaService.getShipmentById).toHaveBeenCalledWith(
            expectedFindShipmentRequest
        );
        expect(component.cartonDetailsSignal()).toEqual(mockCartonData);
    }));

    it('should handle Apollo errors when loading carton details', () => {
        const apolloError = new ApolloError({
            graphQLErrors: [{ message: 'Test error' }],
        });
        mockRecoveredPlasmaService.getCartonById.mockReturnValue(
            throwError(() => apolloError)
        );

        fixture.detectChanges();
        expect(mockToastr.error).toHaveBeenCalled();
    });

    it('should navigate back to shipment details', () => {
        component.shipmentDetailsSignal.set({
            id: 100,
        } as unknown as RecoveredPlasmaShipmentReportDTO);
        component.backToShipment();
        expect(mockRouter.navigateByUrl).toHaveBeenCalledWith(
            '/recovered-plasma/100/shipment-details'
        );
    });

    it('should handle notifications from the backend', () => {
        mockRecoveredPlasmaService.getCartonById.mockReturnValue(
            of({
                data: {
                    findCartonById: {
                        data: mockCartonData,
                        notifications: [
                            { type: 'SUCCESS', message: 'Test notification' },
                        ],
                    },
                },
            } as unknown as ApolloQueryResult<{
                findCartonById: UseCaseResponseDTO<CartonDTO>;
            }>)
        );

        fixture.detectChanges();
        expect(mockToastr.show).toHaveBeenCalledWith(
            'Test notification',
            null,
            {},
            'success'
        );
    });

    it('should update carton details signal with received data', () => {
        fixture.detectChanges();
        expect(component.cartonDetailsSignal()).toEqual(mockCartonData);
    });

    it('should display toaster', () => {
        const notifications = [
            {
                message: 'Product Type does not match',
                type: 'WARN',
                code: 6,
                reason: null,
                action: null,
                details: null,
                name: null,
            },
        ];
        jest.spyOn(toastr, 'show');
        component.displayNotificationMessage(notifications);
        expect(toastr.show).toHaveBeenCalled();
    });

    it('should open openAcknowledgmentMessageDialog', () => {
        const notifications = [
            {
                message: 'This product is discarded and cannot be shipped',
                type: 'INFO',
                code: 6,
                reason: null,
                action: null,
                details: null,
                name: null,
            },
        ];
        jest.spyOn(
            confirmationAcknowledgmentService,
            'notificationConfirmation'
        );
        component.openAcknowledgmentMessageDialog(notifications[0]);
        expect(
            confirmationAcknowledgmentService.notificationConfirmation
        ).toHaveBeenCalled();
    });

    it('should add products to a carton', () => {
        const filledProduct: PackCartonItemsDTO = {
            unitNumber: 'W036898786800',
            productCode: 'E6022V00',
            cartonId: 2,
            locationCode: '123456789',
            employeeId: '4c973896-5761-41fc-8217-07c5d13a004b',
        };
        jest.spyOn(
            mockRecoveredPlasmaService,
            'addCartonProducts'
        ).mockReturnValue(of(mockPackedProductsData) as any);
        component.enterAndVerifyProduct(filledProduct);
        expect(mockRecoveredPlasmaService.addCartonProducts).toHaveBeenCalled();
    });

    it('should disable product group if maxNumberOfProducts reached', () => {
        const maxNumberOfProducts = 1;
        jest.spyOn(component, 'maxProductsComputed').mockReturnValue(
            maxNumberOfProducts
        );
        jest.spyOn(component, 'disableUnitProductGroup');
        component.packedProductsDataSignal.set([{}]);
        fixture.detectChanges();
        component.disableInputsIfMaxCartonProduct();
        expect(component.disableUnitProductGroup).toHaveBeenCalled();
    });

    it('should set data to the packedProductsDataSignal', () => {
        const filledProduct: PackCartonItemsDTO = {
            unitNumber: 'W036898786800',
            productCode: 'E6022V00',
            cartonId: 2,
            locationCode: '123456789',
            employeeId: '4c973896-5761-41fc-8217-07c5d13a004b',
        };
        jest.spyOn(
            mockRecoveredPlasmaService,
            'addCartonProducts'
        ).mockReturnValue(of(mockPackedProductsData) as any);
        component.enterAndVerifyProduct(filledProduct);
        expect(mockRecoveredPlasmaService.addCartonProducts).toHaveBeenCalled();
        expect(component.packedProductsDataSignal()).toEqual([
            mockPackedProductsData.data.packCartonItem.data,
        ]);
    });

    it('should call triggerDiscard', () => {
        const filledProduct: PackCartonItemsDTO = {
            unitNumber: 'W036898786800',
            productCode: 'E6022V00',
            cartonId: 2,
            locationCode: '123456789',
            employeeId: '4c973896-5761-41fc-8217-07c5d13a004b',
        };
        jest.spyOn(
            mockRecoveredPlasmaService,
            'addCartonProducts'
        ).mockReturnValue(of(mockPackedProductsDataInfo) as any);
        jest.spyOn(component, 'resetproductGroup').mockImplementation(() => {});
        jest.spyOn(component, 'triggerDiscard');
        component.enterAndVerifyProduct(filledProduct);
        expect(mockRecoveredPlasmaService.addCartonProducts).toHaveBeenCalled();
        expect(component.resetproductGroup).toHaveBeenCalled();
        expect(component.triggerDiscard).toHaveBeenCalled();
    });

    it('should disable product group if notification message is MAXIMUM_UNITS_BY_CARTON', () => {
        const filledProduct: PackCartonItemsDTO = {
            unitNumber: 'W036898786800',
            productCode: 'E6022V00',
            cartonId: 2,
            locationCode: '123456789',
            employeeId: '4c973896-5761-41fc-8217-07c5d13a004b',
        };
        jest.spyOn(
            mockRecoveredPlasmaService,
            'addCartonProducts'
        ).mockReturnValue(of(mockPackedProductsDataWarn) as any);
        component.enterAndVerifyProduct(filledProduct);
        jest.spyOn(component, 'resetproductGroup').mockImplementation(() => {});
        jest.spyOn(
            component,
            'disableInputsIfMaxCartonProduct'
        ).mockImplementation(() => {});
        fixture.detectChanges();
        expect(mockRecoveredPlasmaService.addCartonProducts).toHaveBeenCalled();
        component.disableInputsIfMaxCartonProduct();
        expect(component.disableInputsIfMaxCartonProduct).toHaveBeenCalled();
    });
});
