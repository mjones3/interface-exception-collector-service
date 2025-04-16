import { CommonModule, DatePipe } from '@angular/common';
import { ComponentFixture, fakeAsync, TestBed } from '@angular/core/testing';
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
import { CookieService } from 'ngx-cookie-service';
import { of, throwError } from 'rxjs';
import { UseCaseResponseDTO } from '../../../../shared/models/use-case-response.dto';
import { ProductIconsService } from '../../../../shared/services/product-icon.service';
import { RecoveredPlasmaShipmentReportDTO } from '../../graphql/query-definitions/shipment.graphql';
import { FindShipmentRequestDTO } from '../../graphql/query-definitions/shipmentDetails.graphql';
import { CartonDTO } from '../../models/recovered-plasma.dto';
import { RecoveredPlasmaService } from '../../services/recovered-plasma.service';
import { AddCartonProductsComponent } from './add-carton-products.component';

describe('AddCartonProductsComponent', () => {
    let component: AddCartonProductsComponent;
    let fixture: ComponentFixture<AddCartonProductsComponent>;
    let mockRouter: jest.Mocked<Router>;
    let mockActivatedRoute: Partial<ActivatedRoute>;
    let mockStore: jest.Mocked<Store>;
    let mockRecoveredPlasmaService: jest.Mocked<RecoveredPlasmaService>;
    let mockToastr: jest.Mocked<ToastrImplService>;
    let mockProductIconService: jest.Mocked<ProductIconsService>;
    let mockCookieService: jest.Mocked<CookieService>;

    const mockCartonData = {
        id: 1,
        shipmentId: 100,
    };

    const mockShipmentData = {
        id: 100,
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
                AddCartonProductsComponent,
                MatIconTestingModule,
                NoopAnimationsModule,
                CommonModule,
                ApolloTestingModule,
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

        fixture = TestBed.createComponent(AddCartonProductsComponent);
        component = fixture.componentInstance;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should load carton and shipment details on init', fakeAsync(() => {
        fixture.detectChanges();

        expect(mockRecoveredPlasmaService.getCartonById).toHaveBeenCalledWith(
            1
        );
        expect(mockRecoveredPlasmaService.getShipmentById).toHaveBeenCalledWith(
            {
                locationCode: '123456789',
                employeeId: 'employeeId',
                shipmentId: 100,
            } as FindShipmentRequestDTO
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
});
