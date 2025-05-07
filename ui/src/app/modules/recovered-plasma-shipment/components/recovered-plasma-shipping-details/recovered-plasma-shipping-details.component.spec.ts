import { CommonModule, DatePipe } from '@angular/common';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatIconTestingModule } from '@angular/material/icon/testing';
import { By } from '@angular/platform-browser';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { ApolloError, ApolloQueryResult } from '@apollo/client';
import { Store } from '@ngrx/store';
import { ToastrImplService } from '@shared';
import { CookieService } from 'ngx-cookie-service';
import { of, throwError } from 'rxjs';
import { UseCaseResponseDTO } from '../../../../shared/models/use-case-response.dto';
import { CartonDTO, RecoveredPlasmaShipmentResponseDTO } from '../../models/recovered-plasma.dto';
import { RecoveredPlasmaService } from '../../services/recovered-plasma.service';
import { RecoveredPlasmaShippingDetailsComponent } from './recovered-plasma-shipping-details.component';

describe('RecoveredPlasmaShippingDetailsComponent', () => {
    let component: RecoveredPlasmaShippingDetailsComponent;
    let fixture: ComponentFixture<RecoveredPlasmaShippingDetailsComponent>;
    let mockRouter: jest.Mocked<Router>;
    let mockRecoveredPlasmaService: jest.Mocked<RecoveredPlasmaService>;
    let mockToastrService: jest.Mocked<ToastrImplService>;
    let mockStore: jest.Mocked<Store>;
    let cookieService: jest.Mocked<CookieService>;

    beforeEach(async () => {
        mockRouter = {
            navigate: jest.fn(),
            navigateByUrl: jest.fn(),
            url: '/test-url',
        } as Partial<Router> as jest.Mocked<Router>;

        mockRecoveredPlasmaService = {
            getShipmentById: jest.fn(),
            createCarton: jest.fn(),
            getCartonById: jest.fn(),
        } as Partial<RecoveredPlasmaService> as jest.Mocked<RecoveredPlasmaService>;

        mockToastrService = {
            error: jest.fn(),
            success: jest.fn(),
            warning: jest.fn(),
        } as Partial<ToastrImplService> as jest.Mocked<ToastrImplService>;

        mockStore = {
            select: jest.fn(),
        } as Partial<Store> as jest.Mocked<Store>;

        cookieService = {
            get: jest.fn(),
        } as Partial<CookieService> as jest.Mocked<CookieService>;

        await TestBed.configureTestingModule({
            imports: [
                RecoveredPlasmaShippingDetailsComponent,
                NoopAnimationsModule,
                CommonModule,
                MatIconTestingModule,
            ],
            providers: [
                DatePipe,
                {
                    provide: ActivatedRoute,
                    useValue: {
                        paramMap: of({}),
                        snapshot: {
                            params: { id: 1 },
                        },
                    },
                },
                { provide: Router, useValue: mockRouter },
                {
                    provide: RecoveredPlasmaService,
                    useValue: mockRecoveredPlasmaService,
                },
                { provide: ToastrImplService, useValue: mockToastrService },
                { provide: CookieService, useValue: cookieService },
                { provide: Store, useValue: mockStore },
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

        it('should hide "add carton button" when canAddCartons is false', () => {
            const buttonIdCssSelector = By.css('#btnAddCarton');
            const root = fixture.debugElement;
            mockRecoveredPlasmaService.getShipmentById.mockReturnValue(
                of({
                    data: {
                        findShipmentById: {
                            data: {
                                canAddCartons: false,
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

        it('should show "add carton button" when canAddCartons is true', () => {
            const buttonIdCssSelector = By.css('#btnAddCarton');
            const root = fixture.debugElement;
            mockRecoveredPlasmaService.getShipmentById.mockReturnValue(
                of({
                    data: {
                        findShipmentById: {
                            data: {
                                canAddCartons: true,
                            },
                        },
                    },
                } as unknown as ApolloQueryResult<{
                    findShipmentById: UseCaseResponseDTO<RecoveredPlasmaShipmentResponseDTO>;
                }>)
            );

            fixture.detectChanges();
            const button = root.query(buttonIdCssSelector)?.nativeElement;
            expect(button).toBeTruthy();
        });
    });

    describe('getStatusBadgeCssClass', () => {
        it('should return correct CSS class for OPEN status', () => {
            const result = component.getStatusBadgeCssClass('OPEN');
            expect(result).toBe(
                'text-sm font-bold py-1.5 px-2 badge rounded-full bg-blue-100 text-blue-700'
            );
        });

        it('should return correct CSS class for IN_PROGRESS status', () => {
            const result = component.getStatusBadgeCssClass('IN_PROGRESS');
            expect(result).toBe(
                'text-sm font-bold py-1.5 px-2 badge rounded-full bg-[#FFEDD5] text-[#C2410C]'
            );
        });

        it('should return correct CSS class for CLOSED status', () => {
            const result = component.getStatusBadgeCssClass('CLOSED');
            expect(result).toBe(
                'text-sm font-bold py-1.5 px-2 badge rounded-full bg-green-100 text-green-700'
            );
        });

        it('should return empty string for unknown status', () => {
            const result = component.getStatusBadgeCssClass('UNKNOWN' as any);
            expect(result).toBe('');
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
        mockRecoveredPlasmaService.getShipmentById.mockReturnValue(
            of({
                data: {
                    findShipmentById: {
                        data: { cartonList: [ { canPrint: true } ] } as RecoveredPlasmaShipmentResponseDTO,
                    },
                },
            } as unknown as ApolloQueryResult<{ findShipmentById: UseCaseResponseDTO<RecoveredPlasmaShipmentResponseDTO> }>)
        );

        fixture.detectChanges();
        const button = fixture.debugElement
            ?.query(By.css('button[data-testid=view-shipping-carton-packing-slip]'))
            ?.nativeElement;

        expect(button).toBeTruthy();
    });

    it('should hide carton print button when canPrint is false', () => {
        mockRecoveredPlasmaService.getShipmentById.mockReturnValue(
            of({
                data: {
                    findShipmentById: {
                        data: { cartonList: [ { canPrint: false } ] } as RecoveredPlasmaShipmentResponseDTO,
                    },
                },
            } as unknown as ApolloQueryResult<{ findShipmentById: UseCaseResponseDTO<RecoveredPlasmaShipmentResponseDTO> }>)
        );

        fixture.detectChanges();
        const button = fixture.debugElement
            ?.query(By.css('button[data-testid=view-shipping-carton-packing-slip]'))
            ?.nativeElement;

        expect(button).toBeFalsy();
    });

    it('should navigate to verify product page when click on verify product button', () => {
        const id = 1;
        jest.spyOn(mockRouter, 'navigate');
        component.verifyProducts(id);
        expect(mockRouter.navigate).toHaveBeenCalledWith([
            `recovered-plasma/${id}/verify-carton`,
        ]);
    });
});
