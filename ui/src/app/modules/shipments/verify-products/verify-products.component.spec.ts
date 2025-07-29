import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { MatDialog } from '@angular/material/dialog';
import { MatIconTestingModule } from '@angular/material/icon/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { provideMockStore } from '@ngrx/store/testing';
import { TranslateModule } from '@ngx-translate/core';
import { NotificationDto } from '@shared';
import { ApolloModule } from 'apollo-angular';
import { ApolloTestingModule } from 'apollo-angular/testing';
import { ToastrModule, ToastrService } from 'ngx-toastr';
import { of } from 'rxjs';
import { FuseConfirmationService } from '../../../../@fuse/services/confirmation';
import { ShipmentService } from '../services/shipment.service';
import { VerifyProductsComponent } from './verify-products.component';
import { ProductResponseDTO } from '../graphql/query-defintions/get-unlabeled-products.graphql';
import {
    SelectProductPickerModalComponent
} from '../shared/select-product-picker-modal/select-product-picker-modal.component';
import { NotificationCriteriaService } from '../../../shared/services/notification-criteria.service';
import { ConfirmationAcknowledgmentService } from '../../../shared/services/confirmation-acknowledgment.service';
import { VerifyProductResponseDTO } from '../graphql/verify-products/query-definitions/verify-products.graphql';
import { ShipmentDetailResponseDTO, VerifyFilledProductDto } from '../models/shipment-info.dto';
import { DatePipe } from '@angular/common';

const SHIPMENT_ID = 1;
const MOCK_USER_ID = 'mock-user-id';

describe('VerifyProductsComponent', () => {
    let component: VerifyProductsComponent;
    let fixture: ComponentFixture<VerifyProductsComponent>;
    let shipmentService: ShipmentService;
    let fuseConfirmationService: FuseConfirmationService;
    let toastr: ToastrService;
    let router: Router;
    let matDialog: MatDialog;
    let notificationCriteriaService: NotificationCriteriaService;
    let confirmationAcknowledgmentService: ConfirmationAcknowledgmentService;

    const initialState = {
        auth: {
            id: MOCK_USER_ID,
            loaded: true,
        }
    };

    const mockShipmentData: ShipmentDetailResponseDTO = {
        id: SHIPMENT_ID,
        orderNumber: 12345,
        status: 'OPEN',
        createDate: '2024-01-01',
        customerAddressPostalCode: '12345',
        customerAddressCountryCode: 'US',
        shippingMethod: 'FEDEX',
        labelStatus: 'LABELED'
    };

    const mockVerificationData: VerifyProductResponseDTO = {
        shipmentId: SHIPMENT_ID,
        packedItems: [
            { id: 1, unitNumber: 'UN001', productCode: 'PC001' },
            { id: 2, unitNumber: 'UN002', productCode: 'PC002' }
        ],
        verifiedItems: [
            { id: 1, unitNumber: 'UN001', productCode: 'PC001' }
        ]
    };

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                VerifyProductsComponent,
                ApolloTestingModule,
                NoopAnimationsModule,
                MatIconTestingModule,
                ApolloModule,
                TranslateModule.forRoot(),
                ToastrModule.forRoot(),
            ],
            providers: [
                provideHttpClientTesting(),
                provideMockStore({initialState}),
                {
                    provide: ActivatedRoute,
                    useValue: {
                        snapshot: {
                            params: { id: SHIPMENT_ID },
                        },
                    },
                },
                {
                    provide: Router,
                    useValue: {
                        navigateByUrl: jest.fn().mockResolvedValue(true),
                        url: '/current-route'
                    },
                },
                {
                    provide: MatDialog,
                    useValue: {
                        open: jest.fn()
                    }
                },
                DatePipe,
                ShipmentService,
                NotificationCriteriaService,
                ConfirmationAcknowledgmentService,
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(VerifyProductsComponent);
        component = fixture.componentInstance;

        shipmentService = TestBed.inject(ShipmentService);
        fuseConfirmationService = TestBed.inject(FuseConfirmationService);
        toastr = TestBed.inject(ToastrService);
        router = TestBed.inject(Router);
        matDialog = TestBed.inject(MatDialog);
        notificationCriteriaService = TestBed.inject(NotificationCriteriaService);
        confirmationAcknowledgmentService = TestBed.inject(ConfirmationAcknowledgmentService);

        // Setup default mocks
        jest.spyOn(shipmentService, 'getShipmentById').mockReturnValue(of({
            data: { getShipmentDetailsById: mockShipmentData }
        } as any));
        jest.spyOn(shipmentService, 'getShipmentVerificationDetailsById').mockReturnValue(of({
            data: { getShipmentVerificationDetailsById: mockVerificationData }
        } as any));
        jest.spyOn(shipmentService, 'getNotificationDetailsByShipmentId').mockReturnValue(of({
            data: { getNotificationDetailsByShipmentId: { toBeRemovedItems: [] } }
        } as any));

        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    describe('Component Initialization', () => {
        it('should initialize computed properties correctly', () => {
            expect(component['verifyProductsNotificationsRouteComputed']()).toBe(
                `/shipment/${SHIPMENT_ID}/verify-products/notifications`
            );
            expect(component['verifiedItemsPercentage']()).toBe(0.5); // 1/2 = 50%
        });

        it('should subscribe to trigger fetch data on init', () => {
            const subscribeTriggerFetchDataSpy = jest.spyOn(component, 'subscribeTriggerFetchData');
            component.ngOnInit();
            expect(subscribeTriggerFetchDataSpy).toHaveBeenCalled();
        });
    });

    describe('onValidScan', () => {
        beforeEach(() => {
            // Initialize the scanUnitNumberProductCode mock before each test
            component['scanUnitNumberProductCode'] = {
                focusOnUnitNumber: jest.fn(),
                resetUnitProductGroup: jest.fn(),
                disableUnitProductGroup: jest.fn()
            } as any;
        });

        it('should call openSelectProductDialog for unlabeled shipments', () => {
            const openSelectProductDialogSpy = jest.spyOn(component, 'openSelectProductDialog').mockImplementation(() => {});
            component['shipmentSignal'].set({ ...mockShipmentData, labelStatus: 'UNLABELED' });

            const item: VerifyFilledProductDto = { unitNumber: 'UN001', productCode: 'PC001' };
            component.onValidScan(item);

            expect(openSelectProductDialogSpy).toHaveBeenCalledWith(item);
        });

        it('should call verifyItem for labeled shipments with valid data', () => {
            const verifyItemSpy = jest.spyOn(component, 'verifyItem').mockImplementation(() => {});
            component['shipmentSignal'].set({ ...mockShipmentData, labelStatus: 'LABELED' });

            const item: VerifyFilledProductDto = { unitNumber: 'UN001', productCode: 'PC001' };
            component.onValidScan(item);

            expect(verifyItemSpy).toHaveBeenCalledWith(item);
        });

        it('should show error for invalid scan data', () => {
            const toastrErrorSpy = jest.spyOn(toastr, 'error');
            component['shipmentSignal'].set({ ...mockShipmentData, labelStatus: 'LABELED' });

            const item: VerifyFilledProductDto = { unitNumber: '', productCode: '' };
            component.onValidScan(item);

            expect(toastrErrorSpy).toHaveBeenCalledWith('Something Went Wrong');
        });
    });

    describe('verifyItem', () => {
        it('should verify item successfully', fakeAsync(() => {
            const mockResponse = {
                data: {
                    verifyItem: {
                        ruleCode: '200 OK',
                        notifications: [],
                        results: { results: [mockVerificationData] }
                    }
                }
            };

            jest.spyOn(shipmentService, 'verifyItem').mockReturnValue(of(mockResponse as any));
            const focusOnUnitNumberSpy = jest.fn();
            const resetUnitProductGroupSpy = jest.fn();
            component['scanUnitNumberProductCode'] = {
                focusOnUnitNumber: focusOnUnitNumberSpy,
                resetUnitProductGroup: resetUnitProductGroupSpy
            } as any;

            const item: VerifyFilledProductDto = { unitNumber: 'UN001', productCode: 'PC001' };
            component.verifyItem(item);
            tick();

            expect(shipmentService.verifyItem).toHaveBeenCalledWith({
                shipmentId: SHIPMENT_ID,
                unitNumber: 'UN001',
                productCode: 'PC001',
                employeeId: MOCK_USER_ID
            });
            expect(resetUnitProductGroupSpy).toHaveBeenCalled();
            expect(focusOnUnitNumberSpy).toHaveBeenCalled();
        }));

        it('should handle verification with notifications', fakeAsync(() => {
            const mockNotifications: NotificationDto[] = [
                { notificationType: 'INFO', message: 'Test notification' } as unknown as NotificationDto,
            ];
            const mockResponse = {
                data: {
                    verifyItem: {
                        ruleCode: '400 BAD_REQUEST',
                        notifications: mockNotifications,
                        results: { results: [] }
                    }
                }
            };

            jest.spyOn(shipmentService, 'verifyItem').mockReturnValue(of(mockResponse as any));
            component['scanUnitNumberProductCode'] = {
                focusOnUnitNumber: jest.fn(),
                resetUnitProductGroup: jest.fn()
            } as any;

            const item: VerifyFilledProductDto = { unitNumber: 'UN001', productCode: 'PC001' };
            component.verifyItem(item);
            tick();

            expect(shipmentService.verifyItem).toHaveBeenCalled();
        }));
    });

    describe('openSelectProductDialog', () => {
        const mockItem: VerifyFilledProductDto = { unitNumber: 'UN001', productCode: '' };

        it('should handle single product auto-selection', fakeAsync(() => {
            const mockProduct: ProductResponseDTO = {
                inventoryId: '1',
                unitNumber: 'UN001',
                productCode: 'PC001',
                aboRh: 'O+',
                productDescription: 'Test Product',
                productFamily: 'RBC',
                status: 'AVAILABLE',
                isLabeled: false,
                isLicensed: true
            };

            const mockResponse = {
                data: {
                    getUnlabeledPackedItems: {
                        ruleCode: '200 OK',
                        notifications: [],
                        results: { results: [[mockProduct]] }
                    }
                }
            };

            const mockVerifyResponse = {
                data: {
                    verifyItem: {
                        ruleCode: '200 OK',
                        notifications: [],
                        results: { results: [] }
                    }
                }
            };

            jest.spyOn(shipmentService, 'getUnlabeledPackedItems').mockReturnValue(of(mockResponse as any));
            jest.spyOn(shipmentService, 'verifyItem').mockReturnValue(of(mockVerifyResponse as any));
            const verifyItemSpy = jest.spyOn(component, 'verifyItem');
            component['scanUnitNumberProductCode'] = {
                resetUnitProductGroup: jest.fn(),
                focusOnUnitNumber: jest.fn()
            } as any;

            component.openSelectProductDialog(mockItem);
            tick();

            expect(verifyItemSpy).toHaveBeenCalledWith({
                ...mockItem,
                productCode: mockProduct.productCode
            });
        }));

        it('should open product picker dialog for multiple products', fakeAsync(() => {
            const mockProducts: ProductResponseDTO[] = [
                {
                    inventoryId: '1',
                    unitNumber: 'UN001',
                    productCode: 'PC001',
                    aboRh: 'O+',
                    productDescription: 'Test Product 1',
                    productFamily: 'RBC',
                    status: 'AVAILABLE',
                    isLabeled: false,
                    isLicensed: true
                },
                {
                    inventoryId: '2',
                    unitNumber: 'UN001',
                    productCode: 'PC002',
                    aboRh: 'O+',
                    productDescription: 'Test Product 2',
                    productFamily: 'PLT',
                    status: 'AVAILABLE',
                    isLabeled: false,
                    isLicensed: true
                }
            ];

            const mockResponse = {
                data: {
                    getUnlabeledPackedItems: {
                        ruleCode: '200 OK',
                        notifications: [],
                        results: { results: [mockProducts] }
                    }
                }
            };

            const mockVerifyResponse = {
                data: {
                    verifyItem: {
                        ruleCode: '200 OK',
                        notifications: [],
                        results: { results: [] }
                    }
                }
            };

            const mockDialogRef = {
                afterClosed: jest.fn().mockReturnValue(of(mockProducts[0]))
            };

            jest.spyOn(shipmentService, 'getUnlabeledPackedItems').mockReturnValue(of(mockResponse as any));
            jest.spyOn(shipmentService, 'verifyItem').mockReturnValue(of(mockVerifyResponse as any));
            jest.spyOn(matDialog, 'open').mockReturnValue(mockDialogRef as any);
            const verifyItemSpy = jest.spyOn(component, 'verifyItem');

            component.openSelectProductDialog(mockItem);
            tick();

            expect(matDialog.open).toHaveBeenCalledWith(
                SelectProductPickerModalComponent,
                {
                    data: mockProducts,
                    disableClose: true,
                }
            );
            expect(verifyItemSpy).toHaveBeenCalledWith({
                ...mockItem,
                productCode: mockProducts[0].productCode
            });
        }));

        it('should handle no products found', fakeAsync(() => {
            const mockResponse = {
                data: {
                    getUnlabeledPackedItems: {
                        ruleCode: '404 NOT_FOUND',
                        notifications: [],
                        results: { results: [[]] }
                    }
                }
            };

            jest.spyOn(shipmentService, 'getUnlabeledPackedItems').mockReturnValue(of(mockResponse as any));
            component['scanUnitNumberProductCode'] = {
                resetUnitProductGroup: jest.fn()
            } as any;

            component.openSelectProductDialog(mockItem);
            tick();

            expect(component['scanUnitNumberProductCode'].resetUnitProductGroup).toHaveBeenCalled();
        }));

        it('should handle dialog cancellation', fakeAsync(() => {
            const mockProducts: ProductResponseDTO[] = [
                {
                    inventoryId: '1',
                    unitNumber: 'UN001',
                    productCode: 'PC001',
                    aboRh: 'O+',
                    productDescription: 'Test Product',
                    productFamily: 'RBC',
                    status: 'AVAILABLE',
                    isLabeled: false,
                    isLicensed: true
                }
            ];

            const mockResponse = {
                data: {
                    getUnlabeledPackedItems: {
                        ruleCode: '200 OK',
                        notifications: [],
                        results: { results: [mockProducts] }
                    }
                }
            };

            const mockDialogRef = {
                afterClosed: jest.fn().mockReturnValue(of(null))
            };

            jest.spyOn(shipmentService, 'getUnlabeledPackedItems').mockReturnValue(of(mockResponse as any));
            jest.spyOn(matDialog, 'open').mockReturnValue(mockDialogRef as any);
            jest.spyOn(shipmentService, 'verifyItem').mockReturnValue(of({
                data: {
                    verifyItem: {
                        ruleCode: '200 OK',
                        notifications: [],
                        results: { results: [] }
                    }
                }
            } as any));

            component['scanUnitNumberProductCode'] = {
                resetUnitProductGroup: jest.fn(),
                focusOnUnitNumber: jest.fn()
            } as any;

            component.openSelectProductDialog(mockItem);
            tick();

            expect(component['scanUnitNumberProductCode'].resetUnitProductGroup).toHaveBeenCalled();
            expect(component['scanUnitNumberProductCode'].focusOnUnitNumber).toHaveBeenCalled();
        }));

    });

    describe('disableInputsIfAllPackItemsVerified', () => {
        it('should disable inputs when all items are verified', () => {
            const disableUnitProductGroupSpy = jest.fn();
            component['scanUnitNumberProductCode'] = {
                disableUnitProductGroup: disableUnitProductGroupSpy
            } as any;

            // Set up scenario where all items are verified
            component['verificationSignal'].set({
                packedItems: [{ id: 1 }, { id: 2 }],
                verifiedItems: [{ id: 1 }, { id: 2 }]
            } as any);

            component.disableInputsIfAllPackItemsVerified();

            expect(disableUnitProductGroupSpy).toHaveBeenCalled();
        });

        it('should not disable inputs when not all items are verified', () => {
            const disableUnitProductGroupSpy = jest.fn();
            component['scanUnitNumberProductCode'] = {
                disableUnitProductGroup: disableUnitProductGroupSpy
            } as any;

            // Set up scenario where not all items are verified
            component['verificationSignal'].set({
                packedItems: [{ id: 1 }, { id: 2 }],
                verifiedItems: [{ id: 1 }]
            } as any);

            component.disableInputsIfAllPackItemsVerified();

            expect(disableUnitProductGroupSpy).not.toHaveBeenCalled();
        });
    });

    describe('completeShipment', () => {
        it('should complete shipment with confirmation dialog', fakeAsync(() => {
            const mockNotifications: NotificationDto[] = [
                { notificationType: 'CONFIRMATION', message: 'Confirm completion' } as unknown as NotificationDto,
            ];
            const mockResponse = {
                data: {
                    completeShipment: {
                        notifications: mockNotifications,
                        _links: { next: '/next-page' }
                    }
                }
            };

            jest.spyOn(shipmentService, 'completeShipment').mockReturnValue(of(mockResponse as any));
            const mockDialogRef = { afterClosed: jest.fn().mockReturnValue(of(true)) };
            jest.spyOn(matDialog, 'open').mockReturnValue(mockDialogRef as any);
            component['scanUnitNumberProductCode'] = {
                disableUnitProductGroup: jest.fn()
            } as any;

            component.completeShipment();
            tick();

            expect(matDialog.open).toHaveBeenCalled();
            expect(component['scanUnitNumberProductCode'].disableUnitProductGroup).toHaveBeenCalled();
        }));

        it('should complete shipment without confirmation', fakeAsync(() => {
            const mockResponse = {
                data: {
                    completeShipment: {
                        notifications: [],
                        _links: { next: '/next-page' }
                    }
                }
            };

            jest.spyOn(shipmentService, 'completeShipment').mockReturnValue(of(mockResponse as any));
            const handleNavigationSpy = jest.spyOn(component, 'handleNavigation');

            component.completeShipment();
            tick();

            expect(handleNavigationSpy).toHaveBeenCalledWith('/next-page');
        }));
    });

    describe('getProductStatus', () => {
        it('should return productStatus when available and not AVAILABLE', () => {
            const product = { productStatus: 'EXPIRED', ineligibleStatus: 'QUARANTINED' } as any;
            const result = component.getProductStatus(product);
            expect(result).toBe('EXPIRED');
        });

        it('should return ineligibleStatus when productStatus is AVAILABLE', () => {
            const product = { productStatus: 'AVAILABLE', ineligibleStatus: 'QUARANTINED' } as any;
            const result = component.getProductStatus(product);
            expect(result).toBe('QUARANTINED');
        });

        it('should return ineligibleStatus when productStatus is undefined', () => {
            const product = { ineligibleStatus: 'QUARANTINED' } as any;
            const result = component.getProductStatus(product);
            expect(result).toBe('QUARANTINED');
        });
    });

    describe('displayMessageFromNotificationDto', () => {
        it('should display notifications with correct toast settings', () => {
            const mockNotifications: NotificationDto[] = [
                { notificationType: 'SUCCESS', message: 'Success message' } as unknown as NotificationDto,
                { notificationType: 'SYSTEM', message: 'System message' } as unknown as NotificationDto,
            ];

            const toastrShowSpy = jest.spyOn(toastr, 'show').mockReturnValue({
                onTap: of(null)
            } as any);
            component['scanUnitNumberProductCode'] = {
                focusOnUnitNumber: jest.fn()
            } as any;

            component.displayMessageFromNotificationDto(mockNotifications);

            expect(toastrShowSpy).toHaveBeenCalledTimes(2);
        });
    });

    describe('cancelButtonHandler', () => {
        it('should cancel second verification directly when no confirmation needed', fakeAsync(() => {
            const cancelSecondVerificationSpy = jest
                .spyOn(shipmentService, 'cancelSecondVerification')
                .mockReturnValue(of({
                    data: {
                        cancelSecondVerification: {
                            ruleCode: '200 OK',
                            notifications: [{ notificationType: 'SUCCESS' }],
                            _links: { next: `/shipment/${SHIPMENT_ID}/shipment-details` }
                        }
                    }
                } as any));

            const routeSpy = jest.spyOn(router, 'navigateByUrl');

            component.cancelButtonHandler();
            tick();

            expect(cancelSecondVerificationSpy).toHaveBeenCalled();
            expect(routeSpy).toHaveBeenCalled();
        }));

        it('should open confirmation dialog when confirmation notification exists', fakeAsync(() => {
            const cancelSecondVerificationSpy = jest
                .spyOn(shipmentService, 'cancelSecondVerification')
                .mockReturnValue(of({
                    data: {
                        cancelSecondVerification: {
                            notifications: [{ notificationType: 'CONFIRMATION', message: 'Confirm cancellation' }]
                        }
                    }
                } as any));

            const fuseConfirmationOpenSpy = jest.spyOn(fuseConfirmationService, 'open')
                .mockReturnValue({ afterClosed: jest.fn().mockReturnValue(of('confirmed')) } as any);
            const confirmCancelSpy = jest.spyOn(component, 'confirmCancel')
                .mockReturnValue(of({
                    data: {
                        confirmCancelSecondVerification: {
                            ruleCode: '200 OK',
                            notifications: [],
                            _links: { next: '/next-page' }
                        }
                    }
                }) as unknown as any);
            component['scanUnitNumberProductCode'] = {
                disableUnitProductGroup: jest.fn(),
                resetUnitProductGroup: jest.fn()
            } as any;

            component.cancelButtonHandler();
            tick();

            expect(cancelSecondVerificationSpy).toHaveBeenCalled();
            expect(fuseConfirmationOpenSpy).toHaveBeenCalled();
            expect(confirmCancelSpy).toHaveBeenCalled();
        }));
    });

    describe('confirmCancel', () => {
        it('should confirm cancel second verification', fakeAsync(() => {
            const confirmCancelSpy = jest
                .spyOn(shipmentService, 'confirmCancelSecondVerification')
                .mockReturnValue(of({
                    data: {
                        confirmCancelSecondVerification: {
                            ruleCode: '200 OK',
                            notifications: [],
                            _links: { next: '/next-page' }
                        }
                    }
                } as any));

            const handleNavigationSpy = jest.spyOn(component, 'handleNavigation');

            component.confirmCancel();
            tick();

            expect(confirmCancelSpy).toHaveBeenCalledWith({
                shipmentId: SHIPMENT_ID,
                employeeId: MOCK_USER_ID
            });
            expect(handleNavigationSpy).toHaveBeenCalledWith('/next-page');
        }));
    });

    describe('handleNavigation', () => {
        it('should navigate to specified URL', async () => {
            const result = await component.handleNavigation('/test-url');
            expect(router.navigateByUrl).toHaveBeenCalledWith('/test-url');
            expect(result).toBe(true);
        });
    });
});
