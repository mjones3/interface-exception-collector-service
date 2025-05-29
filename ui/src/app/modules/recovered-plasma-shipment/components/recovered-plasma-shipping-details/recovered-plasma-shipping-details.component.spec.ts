import { CommonModule, DatePipe } from '@angular/common';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatIconTestingModule } from '@angular/material/icon/testing';
import { By } from '@angular/platform-browser';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { ApolloError, ApolloQueryResult } from '@apollo/client';
import { Store } from '@ngrx/store';
import { CookieService } from 'ngx-cookie-service';
import { of, throwError } from 'rxjs';
import { UseCaseResponseDTO } from '../../../../shared/models/use-case-response.dto';
import { CartonDTO, RecoveredPlasmaShipmentResponseDTO } from '../../models/recovered-plasma.dto';
import { RecoveredPlasmaService } from '../../services/recovered-plasma.service';
import { RecoveredPlasmaShippingDetailsComponent } from './recovered-plasma-shipping-details.component';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { BrowserPrintingService } from '../../../../core/services/browser-printing/browser-printing.service';
import {
    ViewShippingCartonPackingSlipComponent
} from '../view-shipping-carton-packing-slip/view-shipping-carton-packing-slip.component';
import { CartonPackingSlipDTO } from '../../graphql/query-definitions/generate-carton-packing-slip.graphql';
import { ToastrModule, ToastrService } from 'ngx-toastr';
import { FuseConfirmationService } from '@fuse/services/confirmation';
import { ShipmentResponseDTO } from 'app/modules/shipments/models/shipment-info.dto';
import { LabelPrinterResponseDTO, PrintLabelService } from '../../../../shared/services/print-label.service';
import { ApolloTestingModule } from 'apollo-angular/testing';

describe('RecoveredPlasmaShippingDetailsComponent', () => {
    let component: RecoveredPlasmaShippingDetailsComponent;
    let fixture: ComponentFixture<RecoveredPlasmaShippingDetailsComponent>;
    let mockRouter: jest.Mocked<Router>;
    let mockRecoveredPlasmaService: jest.Mocked<RecoveredPlasmaService>;
    let mockToastrService: jest.Mocked<ToastrService>;
    let mockStore: jest.Mocked<Store>;
    let cookieService: jest.Mocked<CookieService>;
    let mockMatDialog: jest.Mocked<MatDialog>;
    let mockPrintLabelService: jest.Mocked<PrintLabelService>;
    let mockBrowserPrintingService: jest.Mocked<BrowserPrintingService>;
    let mockDialogRef: jest.Mocked<MatDialogRef<ViewShippingCartonPackingSlipComponent, CartonPackingSlipDTO | string>>;
    let mockConfirmationDialog: jest.Mocked<MatDialog>;
    let mockActivatedRoute: any;

    beforeEach(async () => {
        mockRouter = {
            navigate: jest.fn(),
            navigateByUrl: jest.fn(),
            url: '/test-url',
        } as Partial<Router> as jest.Mocked<Router>;

        mockDialogRef = {
            afterOpened: jest.fn().mockReturnValue(of({})),
            afterClosed: jest.fn().mockReturnValue(of()),
            close: jest.fn(),
        } as Partial<MatDialogRef<ViewShippingCartonPackingSlipComponent, CartonPackingSlipDTO>> as jest.Mocked<MatDialogRef<ViewShippingCartonPackingSlipComponent, CartonPackingSlipDTO>>;

        mockMatDialog = {
            open: jest.fn().mockReturnValue(mockDialogRef),
        } as Partial<MatDialog> as jest.Mocked<MatDialog>;

        mockConfirmationDialog = {
            open: jest.fn().mockReturnValue({
                afterClosed: () => of('confirmed')
            }),
        } as Partial<MatDialog> as jest.Mocked<MatDialog>;

        mockPrintLabelService = {
            installed: jest.fn().mockReturnValue(of(true)),
            print: jest.fn().mockReturnValue(of<LabelPrinterResponseDTO>({
                type: 'success',
                message: 'Printed successfully',
                status: 200,
            })),
        } as Partial<PrintLabelService> as jest.Mocked<PrintLabelService>;

        mockBrowserPrintingService = {
            print: jest.fn(),
        } as Partial<BrowserPrintingService> as jest.Mocked<BrowserPrintingService>;

        mockRecoveredPlasmaService = {
            getShipmentById: jest.fn(),
            createCarton: jest.fn(),
            getCartonById: jest.fn(),
            closeShipment: jest.fn(),
            repackCarton: jest.fn(),
            generateCartonPackingSlip: jest.fn(),
            removeLastCarton: jest.fn(),
        } as Partial<RecoveredPlasmaService> as jest.Mocked<RecoveredPlasmaService>;

        mockToastrService = {
            error: jest.fn(),
            success: jest.fn(),
            warning: jest.fn(),
            show: jest.fn(),
        } as Partial<ToastrService> as jest.Mocked<ToastrService>;

        mockStore = {
            select: jest.fn(),
        } as Partial<Store> as jest.Mocked<Store>;

        cookieService = {
            get: jest.fn(),
        } as Partial<CookieService> as jest.Mocked<CookieService>;

        mockActivatedRoute = {
            paramMap: of({}),
            snapshot: {
                params: { id: 1 },
                queryParams: {},
            },
        };

        const matDailogMockData = {
                width: '24rem',
                disableClose: true,
                data: {
                    shipmentDate: '2024-12-24',
                    continueFn: ()=> {}
            }
        }

        await TestBed.configureTestingModule({
            imports: [
                RecoveredPlasmaShippingDetailsComponent,
                NoopAnimationsModule,
                ApolloTestingModule,
                CommonModule,
                MatIconTestingModule,
                ToastrModule.forRoot()
            ],
            providers: [
                DatePipe,
                { provide: ActivatedRoute, useValue: mockActivatedRoute },
                { provide: Router, useValue: mockRouter },
                { provide: RecoveredPlasmaService, useValue: mockRecoveredPlasmaService },
                { provide: ToastrService, useValue: mockToastrService },
                { provide: CookieService, useValue: cookieService },
                { provide: Store, useValue: mockStore },
                { provide: MatDialog, useValue: mockMatDialog },
                { provide: FuseConfirmationService, useValue: mockConfirmationDialog },
                { provide: PrintLabelService, useValue: mockPrintLabelService },
                { provide: BrowserPrintingService, useValue: mockBrowserPrintingService },
                {provide: MAT_DIALOG_DATA, useValue: matDailogMockData}
            ],
        }).compileComponents();

        mockStore.select.mockReturnValue(of({ id: 'emp123' }));
        jest.spyOn(cookieService, 'get').mockReturnValue('123456789');
        fixture = TestBed.createComponent(
            RecoveredPlasmaShippingDetailsComponent
        );
        cookieService = TestBed.inject(
            CookieService
        ) as jest.Mocked<CookieService>;
        component = fixture.componentInstance;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should get shipment id from route params', () => {
        expect(component.shipmentId).toBe(1);
    });

    it('should handle error when fetching shipment details fails', () => {
        const mockError = new ApolloError({
            errorMessage: 'Network error',
        });

        mockRecoveredPlasmaService.getShipmentById.mockReturnValue(
            throwError(() => mockError)
        );
        component.loadRecoveredPlasmaShippingDetails().subscribe();
        expect(mockToastrService.error).toHaveBeenCalled();
    });

    it('should navigate back to search page', () => {
        component.backToSearch();
        expect(mockRouter.navigate).toHaveBeenCalledWith(['/recovered-plasma']);
    });

    describe('addCarton', () => {
        it('should create carton and navigate to next URL on success', () => {
            const mockResponse = {
                data: {
                    createCarton: {
                        _links: {
                            next: '/next-url',
                        },
                        notifications: [],
                    },
                },
            };

            mockRecoveredPlasmaService.createCarton.mockReturnValue(
                of(mockResponse)
            );

            component.addCarton();

            expect(
                mockRecoveredPlasmaService.createCarton
            ).toHaveBeenCalledWith({
                shipmentId: 1,
                employeeId: 'emp123',
            });
            expect(mockRouter.navigateByUrl).toHaveBeenCalledWith('/next-url');
        });

        it('should handle error when creating carton fails', () => {
            const mockError = new ApolloError({
                errorMessage: 'Network error',
            });

            mockRecoveredPlasmaService.createCarton.mockReturnValue(
                throwError(() => mockError)
            );

            component.addCarton();

            expect(mockToastrService.error).toHaveBeenCalled();
        });

        it('should not navigate if next URL is not provided', () => {
            const mockResponse = {
                data: {
                    createCarton: {
                        _links: {},
                        notifications: [],
                    },
                },
            };

            mockRecoveredPlasmaService.createCarton.mockReturnValue(
                of(mockResponse)
            );

            component.addCarton();

            expect(mockRouter.navigateByUrl).not.toHaveBeenCalled();
        });



        it('should show "add carton button" when canAddCartons is true', () => {
            const buttonIdCssSelector = By.css('#btnAddCarton');
            const root = fixture.debugElement;
            jest.spyOn(component, 'shipmentDetailsSignal').mockReturnValue({ canAddCartons: true })
            fixture.detectChanges();
            const button = root.query(buttonIdCssSelector)?.nativeElement;
            expect(button).toBeTruthy();
        });
    });

    it('should handle error when loadCartonPackedProduct fails', () => {
        const mockError = new ApolloError({
            errorMessage: 'Network error',
        });
        const carton = {
            id: 1,
            cartonNumber: '123',
            shipmentId: 123,
            cartonSequence: null,
            createEmployeeId: '',
            closeEmployeeId: '',
            createDate: '',
            modificationDate: '',
            closeDate: '',
            status: '',
            totalProducts: 0,
            totalWeight: 0,
            totalVolume: 0,
            maxNumberOfProducts: 0,
            minNumberOfProducts: 0,
            packedProducts: [],
        };

        mockRecoveredPlasmaService.getCartonById.mockReturnValue(
            throwError(() => mockError)
        );

        component.loadCartonPackedProduct(carton);

        expect(mockToastrService.error).toHaveBeenCalled();
    });

    it('should not call service if carton id is undefined', () => {
        const carton: CartonDTO = { id: undefined };
        component.loadCartonPackedProduct(carton);
        jest.spyOn(mockRecoveredPlasmaService, 'getCartonById');
        expect(mockRecoveredPlasmaService.getCartonById).not.toHaveBeenCalled();
    });

    it('should not call service if carton is null', () => {
        component.loadCartonPackedProduct(null);
        jest.spyOn(mockRecoveredPlasmaService, 'getCartonById');
        expect(mockRecoveredPlasmaService.getCartonById).not.toHaveBeenCalled();
    });

    it('should load packed products successfully and update signal', () => {
        const carton: CartonDTO = { id: 2 };
        const mockResponse = {
            data: {
                findCartonById: {
                    _links: null,
                    data: {
                        id: 2,
                        cartonNumber: 'BPMMH12',
                        shipmentId: 2,
                        cartonSequence: 123,
                        createEmployeeId:
                            '4c973896-5761-41fc-8217-07c5d13a004b',
                        closeEmployeeId: null,
                        createDate: '2025-04-17T13:03:54.560015Z',
                        modificationDate: '2025-04-17T13:03:54.560015Z',
                        closeDate: null,
                        status: 'OPEN',
                        totalProducts: 1,
                        totalWeight: 0,
                        totalVolume: 0,
                        packedProducts: [
                            {
                                id: 5,
                                cartonId: 2,
                                unitNumber: 'W036898786800',
                                productCode: 'E6022V00',
                                productDescription: 'CP2D PLS MI 120H',
                                productType: 'RP_FROZEN_WITHIN_120_HOURS',
                                volume: 229,
                                weight: 150,
                                packedByEmployeeId:
                                    '4c973896-5761-41fc-8217-07c5d13a004b',
                                aboRh: 'AP',
                                status: 'PACKED',
                                expirationDate: '2024-09-03T10:15:30',
                                collectionDate: '2011-12-03T09:15:30Z',
                                createDate: '2025-04-17T16:36:30.643488Z',
                                modificationDate: '2025-04-17T16:36:30.643488Z',
                            },
                        ],
                        maxNumberOfProducts: 2,
                        minNumberOfProducts: 20,
                    },
                    notifications: [],
                },
            },
        } as any as ApolloQueryResult<{
            findCartonById: UseCaseResponseDTO<CartonDTO>;
        }>;
        mockRecoveredPlasmaService.getCartonById.mockReturnValue(
            of(mockResponse)
        );
        component.loadCartonPackedProduct(carton);
        jest.spyOn(mockRecoveredPlasmaService, 'getCartonById');

        expect(mockRecoveredPlasmaService.getCartonById).toHaveBeenCalledWith(
            2
        );
        expect(component.expandedRowDataSignal()).toEqual(
            mockResponse.data.findCartonById.data.packedProducts
        );
    });

    it('should not update signal if packed products data is null', () => {
        const carton: CartonDTO = { id: 123 };
        const mockResponse = {
            packedProducts: [],
        } as any as ApolloQueryResult<{
            findCartonById: UseCaseResponseDTO<CartonDTO>;
        }>;

        mockRecoveredPlasmaService.getCartonById.mockReturnValue(
            of(mockResponse)
        );
        component.loadCartonPackedProduct(carton);
        expect(mockRecoveredPlasmaService.getCartonById).toHaveBeenCalledWith(
            123
        );
        expect(component.expandedRowDataSignal()).toEqual([]);
    });

    it('should hide "edit" when carton status is closed', () => {
        const buttonIdCssSelector = By.css('#editBtn');
        const root = fixture.debugElement;
        mockRecoveredPlasmaService.getShipmentById.mockReturnValue(
            of({
                data: {
                    findShipmentById: {
                        data: {
                            status: 'CLOSED',
                        },
                    },
                },
            } as unknown as ApolloQueryResult<{
                findShipmentById: UseCaseResponseDTO<RecoveredPlasmaShipmentResponseDTO>;
            }>)
        );

        fixture.detectChanges();
        const button = root.query(buttonIdCssSelector)?.nativeElement;
        expect(button).toBeFalsy();
    });

    it('should navigate  to carton details page when click on edit', () => {
        const cartonId = 1;
        jest.spyOn(mockRouter, 'navigate');
        component.editCarton(cartonId);
        expect(mockRouter.navigate).toHaveBeenCalledWith([
            `recovered-plasma/${cartonId}/carton-details`,
        ]);
    });

    it('should show carton print button when canPrint is true', () => {
        jest.spyOn(component, 'cartonsComputed').mockReturnValue([ { canPrint: true } ]);
        fixture.detectChanges();
        const button = fixture.debugElement
            ?.query(By.css('button[data-testid=view-shipping-carton-packing-slip]'))
            ?.nativeElement;

        expect(button).toBeTruthy();
    });

    it('should hide carton print button when canPrint is false', () => {
        jest.spyOn(component, 'cartonsComputed').mockReturnValue([ { canPrint: false } ]);
        fixture.detectChanges();
        const button = fixture.debugElement
            ?.query(By.css('button[data-testid=view-shipping-carton-packing-slip]'))
            ?.nativeElement;

        expect(button).toBeFalsy();
    });

    it('should show shipment close button when canClose is true', () => {
        jest.spyOn(component, 'shipmentDetailsSignal').mockReturnValue({ canClose: true });
        fixture.detectChanges();
        const button = fixture.debugElement
            ?.query(By.css('button#closeShipmentBtnId'))
            ?.nativeElement;

        expect(button).toBeTruthy();
    });

    it('should hide shipment close button when canClose is false', () => {
        jest.spyOn(component, 'shipmentDetailsSignal').mockReturnValue({ canClose: false });
        fixture.detectChanges();
        const button = fixture.debugElement
            ?.query(By.css('button#closeShipmentBtnId'))
            ?.nativeElement;

        expect(button).toBeFalsy();
    });

    it('should show reports button when shipment status is "CLOSED"', () => {
        jest.spyOn(component, 'shipmentDetailsSignal').mockReturnValue({ status: 'CLOSED' });
        fixture.detectChanges();
        const button = fixture.debugElement
            ?.query(By.css('button#reportsDialogBtnId'))
            ?.nativeElement;

        expect(button).toBeTruthy();
    });

    it('should hide reports button when shipment status is not "CLOSED"', () => {
        jest.spyOn(component, 'shipmentDetailsSignal').mockReturnValue({ status: 'OPEN' });
        fixture.detectChanges();
        const button = fixture.debugElement
            ?.query(By.css('button#reportsDialogBtnId'))
            ?.nativeElement;

        expect(button).toBeFalsy();
    });

    it('should not call closeShipment service when result is falsy', () => {
        component.handleCloseShipmentContinue(null);
        expect(mockRecoveredPlasmaService.closeShipment).not.toHaveBeenCalled();
    });

    it('should call closeShipment service with formatted date when result is truthy', () => {
        const mockDate = new Date(2024, 11, 26);
        mockRecoveredPlasmaService.closeShipment.mockReturnValue(
            of({
                data: {
                    closeShipment: {
                        data: { id: 1 },
                        notifications: [{ type: 'SUCCESS', message: 'Shipment closed successfully' }]
                    }
                }
            })
        );

        component.handleCloseShipmentContinue(mockDate);

        expect(mockRecoveredPlasmaService.closeShipment).toHaveBeenCalledWith({
            locationCode: '123456789',
            shipmentId: 1,
            shipDate: '2024-12-26',
            employeeId: 'emp123',
        });
    });

    it('should handle error when closeShipment fails', () => {
        const mockDate = new Date(2026, 11, 26);
        const mockError = new ApolloError({
            errorMessage: 'Network error',
        });

        mockRecoveredPlasmaService.closeShipment.mockReturnValue(
            throwError(() => mockError)
        );

        expect(() => {
            component.handleCloseShipmentContinue(mockDate);
        }).not.toThrow();
    });

    it('should fetch shipment data when closeShipment succeeds', () => {
        const mockDate = new Date(2026, 11, 26);
        const mockResponse = {
            data: {
                closeShipment: {
                    data: { id: 1 },
                    notifications: [{ type: 'SUCCESS', message: 'Shipment closed successfully' }]
                }
            }
        };

        mockRecoveredPlasmaService.closeShipment.mockReturnValue(of(mockResponse));

        const fetchShipmentDataSpy = jest.spyOn(component, 'fetchShipmentData');

        component.handleCloseShipmentContinue(mockDate);

        expect(fetchShipmentDataSpy).toHaveBeenCalledWith(1);
    });


    describe('fetchShipmentData', () => {
        jest.useFakeTimers();
        it('should call loadRecoveredPlasmaShippingDetails with the provided id', () => {
            jest.clearAllTimers();

            const shipmentId = 123;
            const loadSpy = jest.spyOn(component, 'loadRecoveredPlasmaShippingDetails').mockReturnValue(of({}));

            component.fetchShipmentData(shipmentId);
            jest.advanceTimersByTime(500);

            expect(loadSpy).toHaveBeenCalledWith(shipmentId);
        });

        it('should print both carton packing slip and label when shouldPrintCartonPackingSlipAndLabel is true', () => {
            jest.clearAllTimers();

            const shipmentId = 123;
            const cartonId = 456;
            mockActivatedRoute.snapshot.queryParams = { print: 'true', closeCartonId: cartonId.toString() };
            const loadSpy = jest.spyOn(component, 'loadRecoveredPlasmaShippingDetails').mockReturnValue(of({}));
            const printLabelSpy = jest.spyOn(component, 'printCartonLabel').mockImplementation();
            const printPackingSlipSpy = jest.spyOn(component, 'printCartonPackingSlip').mockImplementation();

            component.fetchShipmentData(shipmentId);
            jest.advanceTimersByTime(500);

            expect(loadSpy).toHaveBeenCalledWith(shipmentId);
            expect(printLabelSpy).toHaveBeenCalledWith(cartonId);
            expect(printPackingSlipSpy).toHaveBeenCalledWith(cartonId);
        });
    });

    describe('ngOnInit', () => {
        jest.useFakeTimers();
        it('should call fetchShipmentData with routeIdComputed value', () => {
            jest.clearAllTimers();
            const fetchSpy = jest.spyOn(component, 'fetchShipmentData').mockImplementation();
            component.ngOnInit();
            jest.advanceTimersByTime(500);
            expect(fetchSpy).toHaveBeenCalledWith(component.routeIdComputed());
        });
    });

    it('should open CloseShipmentDailogComponent', () => {
        const mockDate = '2026-12-26';
        const mockShipmentData = {
            id: 456,
            shipmentNumber: 'S456',
            status: 'OPEN',
            customerName: 'Test Customer',
            shipmentDate: mockDate
          };
        component.shipmentDetailsSignal.set(mockShipmentData);
        component.onClickCloseShipment();
        jest.spyOn(mockMatDialog, 'open');
        expect(mockMatDialog.open).toHaveBeenCalled();
    });


    describe('repackCarton', () => {
        const cartonId = 1;
        const req = 'demo';
        const repackRequestMock = {
            locationCode: '123456789',
            cartonId: cartonId,
            comments: req,
            employeeId: 'emp123',
        }

        const repackMockData = {
            data : {
              repackCarton : {
                notifications : [ {
                  message : "Products successfully removed",
                  type : "SUCCESS",
                  code : 18,
                } ],
                data : {
                  id : 1,
                  cartonNumber : "BPMMH11",
                  shipmentId : 1,
                  cartonSequence : 1,
                  packedProducts : [ ],
                  maxNumberOfProducts : 20,
                  minNumberOfProducts: 1,
                  canVerify : false,
                  canClose : false,
                  canPrint : false,
                  verifiedProducts : [ ],
                  failedCartonItem : null
                },
                _links : {
                  next : "/test-url"
                }
              }
            }
          }

        beforeEach(() => {
            // Reset mocks before each test
            mockMatDialog.open.mockClear();
            mockRecoveredPlasmaService.repackCarton.mockClear();
            mockRouter.navigateByUrl.mockClear();
            mockDialogRef.afterClosed.mockClear();
        });

        it('should open RepackCartonDialogComponent', () => {
            component.repackCarton(cartonId);
            expect(mockMatDialog.open).toHaveBeenCalled();
        });

        it('should make API call with correct parameters when dialog is closed with comments', () => {
            // Setup the dialog to return comments when closed
            mockDialogRef.afterClosed.mockReturnValue(of(req));
            component.repackCarton(cartonId);
            mockRecoveredPlasmaService.repackCarton.mockReturnValue(of(repackMockData));
            expect(mockMatDialog.open).toHaveBeenCalled();
            expect(mockRecoveredPlasmaService.repackCarton).toHaveBeenCalledWith(repackRequestMock);
        });

        it('should navigate to next URL when repackCarton API call succeeds', () => {
            mockDialogRef.afterClosed.mockReturnValue(of(req));

            mockRecoveredPlasmaService.repackCarton.mockReturnValue(of(repackMockData));
            component.repackCarton(cartonId);
            expect(mockRouter.navigateByUrl).toHaveBeenCalledWith('/test-url');
        });

        it('should not navigate when repackCarton API call succeeds but no next URL is provided', () => {
            mockDialogRef.afterClosed.mockReturnValue(of(req));
            const responseWithoutNextUrl = {
                data: {
                    repackCarton: {
                        notifications: [{
                            message: "Products successfully removed",
                            type: "SUCCESS",
                            code: 18
                        }],
                        data: {
                            id: 1,
                            cartonNumber: "BPMMH11"
                        },
                        _links: {}
                    }
                }
            };

            mockRecoveredPlasmaService.repackCarton.mockReturnValue(of(responseWithoutNextUrl));
            component.repackCarton(cartonId);
            expect(mockRouter.navigateByUrl).not.toHaveBeenCalled();
        });

        it('should not make API call when dialog is closed without comments (undefined)', () => {
            // Setup the dialog to return undefined when closed (cancel button)
            mockDialogRef.afterClosed.mockReturnValue(of(undefined));

            component.repackCarton(cartonId);
            expect(mockMatDialog.open).toHaveBeenCalled();
            expect(mockRecoveredPlasmaService.repackCarton).not.toHaveBeenCalled();
        });

        it('should handle error when repackCarton API call fails', () => {
            mockDialogRef.afterClosed.mockReturnValue(of(req));
            const mockError = new ApolloError({
                errorMessage: 'Network error',
            });
            mockRecoveredPlasmaService.repackCarton.mockReturnValue(throwError(() => mockError));

            component.repackCarton(cartonId);
            expect(mockToastrService.error).toHaveBeenCalled();
        });

        it('should display notifications from API response', () => {
            mockDialogRef.afterClosed.mockReturnValue(of(req));
            mockRecoveredPlasmaService.repackCarton.mockReturnValue(of(repackMockData));

            component.repackCarton(cartonId);
            expect(mockToastrService.show).toHaveBeenCalled();
        });

        it('should hide "remove carton button" when canAddCartons is false', () => {
            jest.spyOn(component, 'cartonsComputed').mockReturnValue([{canRemove: false, id: 3}])
            const buttonIdCssSelector = By.css('#removeCarton3');
            const root = fixture.debugElement;
            fixture.detectChanges();
            const button = root.query(buttonIdCssSelector)?.nativeElement;
            expect(button).not.toBeTruthy();
        });
    });

    describe('removeCarton', () => {
        const mockCartonId = 123;
        const mockEmployeeId = 'emp123';

        beforeEach(() => {
            jest.clearAllMocks();
        });

        it('should open confirmation dialog with correct configuration', () => {
            const expectedConfig = {
                title: 'Remove Confirmation',
                message: 'Carton will be removed. <b>Are you sure you want to continue?</b>',
                dismissible: false,
                icon: {
                    show: false,
                },
                actions: {
                    confirm: {
                        label: 'Continue',
                        class: 'bg-red-700 text-white',
                    },
                    cancel: {
                        class: 'mat-secondary',
                    },
                },
            };

            component.removeCarton(mockCartonId);

            expect(mockConfirmationDialog.open).toHaveBeenCalledWith(expectedConfig);
        });

        it('should remove carton and update shipment data when confirmed', () => {
            const mockShipmentData = {
                id: 1,
                cartonList: [
                    { id: 123, cartonNumber: 'C123' },
                    { id: 456, cartonNumber: 'C456' }
                ]
            };

            const mockResponse = {
                data: {
                    removeCarton: {
                        notifications: [{
                            type: 'SUCCESS',
                            message: 'Carton removed successfully'
                        }]
                    }
                }
            } as ApolloQueryResult<{ removeCarton: UseCaseResponseDTO<ShipmentResponseDTO> }>;

            const mockDialogRef = {
                afterClosed: () => of('confirmed')
            };

            mockConfirmationDialog.open.mockReturnValue(mockDialogRef as any);
            mockRecoveredPlasmaService.removeLastCarton.mockReturnValue(of(mockResponse));

            component.shipmentDetailsSignal.set(mockShipmentData);
            component.removeCarton(mockCartonId);

            expect(mockRecoveredPlasmaService.removeLastCarton).toHaveBeenCalledWith({
                cartonId: mockCartonId,
                employeeId: mockEmployeeId
            });
            expect(mockToastrService.show).toHaveBeenCalledWith(
                'Carton removed successfully',
                null,
                {},
                'success'
            );
            expect(component.shipmentDetailsSignal()).toEqual(mockResponse.data.removeCarton.data);
        });

        it('should not remove carton when dialog is cancelled', () => {
            const mockDialogRef = {
                afterClosed: () => of('cancelled')
            };

            mockConfirmationDialog.open.mockReturnValue(mockDialogRef as any);

            component.removeCarton(mockCartonId);

            expect(mockConfirmationDialog.open).toHaveBeenCalled();
            expect(mockRecoveredPlasmaService.removeLastCarton).not.toHaveBeenCalled();
        });

        it('should handle Apollo error when removing carton', () => {
            const mockDialogRef = {
                afterClosed: () => of('confirmed')
            };

            const mockError = new ApolloError({
                errorMessage: 'Network error',
                networkError: new Error('Network error')
            });

            mockConfirmationDialog.open.mockReturnValue(mockDialogRef as any);
            mockRecoveredPlasmaService.removeLastCarton.mockReturnValue(throwError(() => mockError));

            component.removeCarton(mockCartonId);

            expect(mockToastrService.error).toHaveBeenCalledWith(
                'Network error'
            );
        });

        it('should not update shipment data when response has no data', () => {
            const mockShipmentData = {
                id: 1,
                cartonList: [
                    { id: 123, cartonNumber: 'C123' }
                ]
            };

            const mockResponse = {
                data: {
                    removeCarton: {
                        notifications: [{
                            type: 'ERROR',
                            message: 'Failed to remove carton'
                        }],
                        data: null
                    }
                }
            } as ApolloQueryResult<{ removeCarton: UseCaseResponseDTO<ShipmentResponseDTO> }>;

            const mockDialogRef = {
                afterClosed: () => of('confirmed')
            };

            mockConfirmationDialog.open.mockReturnValue(mockDialogRef as any);
            mockRecoveredPlasmaService.removeLastCarton.mockReturnValue(of(mockResponse));

            component.shipmentDetailsSignal.set(mockShipmentData);
            component.removeCarton(mockCartonId);

            expect(component.shipmentDetailsSignal()).toEqual(mockShipmentData);
            expect(mockToastrService.show).toHaveBeenCalledWith(
                'Failed to remove carton',
                null,
                {},
                'error'
            );
        });
      })
})
