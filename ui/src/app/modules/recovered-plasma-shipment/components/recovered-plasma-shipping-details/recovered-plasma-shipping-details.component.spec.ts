import { CommonModule, DatePipe } from '@angular/common';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { ApolloError } from '@apollo/client';
import { Store } from '@ngrx/store';
import { ToastrImplService } from '@shared';
import { CookieService } from 'ngx-cookie-service';
import { of, throwError } from 'rxjs';
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
    let datePipe: DatePipe;

    beforeEach(async () => {
        mockRouter = {
            navigate: jest.fn(),
            navigateByUrl: jest.fn(),
            url: '/test-url',
        } as any;

        mockRecoveredPlasmaService = {
            getShipmentById: jest.fn(),
            createCarton: jest.fn(),
        } as any;

        mockToastrService = {
            error: jest.fn(),
            success: jest.fn(),
            warning: jest.fn(),
        } as any;

        mockStore = {
            select: jest.fn(),
        } as any;

        cookieService = {
            get: jest.fn(),
        } as any;

        await TestBed.configureTestingModule({
            imports: [
                RecoveredPlasmaShippingDetailsComponent,
                NoopAnimationsModule,
                CommonModule,
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
        datePipe = TestBed.inject(DatePipe);
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
});
