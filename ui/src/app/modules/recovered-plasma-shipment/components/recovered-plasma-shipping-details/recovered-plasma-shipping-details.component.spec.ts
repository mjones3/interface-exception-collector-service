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
        } as any;

        mockRecoveredPlasmaService = {
            getShipmentById: jest.fn(),
        } as any;

        mockToastrService = {
            error: jest.fn(),
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

    it('should set employee id on construction', () => {
        expect(mockStore.select).toHaveBeenCalled();
        expect(component.employeeId).toBe('emp123');
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
        component.fetchRecoveredPlasmaShippingDetails();
        expect(mockToastrService.error).toHaveBeenCalled();
    });

    it('should navigate back to search page', () => {
        component.backToSearch();
        expect(mockRouter.navigate).toHaveBeenCalledWith(['/recovered-plasma']);
    });
});
