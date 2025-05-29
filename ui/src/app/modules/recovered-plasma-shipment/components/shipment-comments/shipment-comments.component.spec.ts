import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { ToastrService } from 'ngx-toastr';
import { CookieService } from 'ngx-cookie-service';
import { of, throwError } from 'rxjs';
import { ApolloError, ApolloQueryResult } from '@apollo/client';
import { ShipmentCommentsComponent } from './shipment-comments.component';
import { ProcessHeaderService } from '@shared';
import { RecoveredPlasmaService } from '../../services/recovered-plasma.service';
import { ProductIconsService } from 'app/shared/services/product-icon.service';
import { RecoveredPlasmaShipmentService } from '../../services/recovered-plasma-shipment.service';
import { ERROR_MESSAGE } from 'app/core/data/common-labels';
import { ShipmentHistoryDTO } from '../../graphql/query-definitions/shipment-comments-history.graphql';
import { AuthState } from 'app/core/state/auth/auth.reducer';
import { provideMockStore } from '@ngrx/store/testing';

jest.mock('keycloak-js');

describe('ShipmentCommentsComponent', () => {
  let component: ShipmentCommentsComponent;
  let fixture: ComponentFixture<ShipmentCommentsComponent>;
  let mockActivatedRoute: any;
  let mockRouter: jest.Mocked<Router>;
  let mockToastr: jest.Mocked<ToastrService>;
  let mockRecoveredPlasmaService: jest.Mocked<RecoveredPlasmaService>;
  let mockCookieService: jest.Mocked<CookieService>;
  let mockProductIconService: jest.Mocked<ProductIconsService>;
  let mockShipmentService: jest.Mocked<RecoveredPlasmaShipmentService>;
  let mockProcessHeaderService: jest.Mocked<ProcessHeaderService>;

  const initialState: AuthState = {
    id: 'mock-user-id',
    loaded: true,
  };

  const mockShipmentId = 123;
  const mockShipmentHistoryData = [
    {
      createEmployeeId: 'EMP001',
      createDate: '2024-01-01T10:00:00Z',
      comments: 'Test comment 1'
    },
    {
      createEmployeeId: 'EMP002',
      createDate: '2024-01-02T11:00:00Z',
      comments: 'Test comment 2'
    }
  ];

  beforeEach(async () => {
    mockActivatedRoute = {
      snapshot: {
        params: { id: mockShipmentId.toString() }
      }
    } 

    mockRouter = {
      navigate: jest.fn(),
    } as Partial<Router> as jest.Mocked<Router>;

    mockToastr = {
      error: jest.fn()
    } as Partial<ToastrService> as jest.Mocked<ToastrService>

    mockRecoveredPlasmaService = {
      getShipmentHistory: jest.fn()
    } as Partial <RecoveredPlasmaService> as jest.Mocked<RecoveredPlasmaService>

    mockShipmentService = {
      getShipmentHistory: jest.fn()
    } as Partial <RecoveredPlasmaShipmentService> as jest.Mocked<RecoveredPlasmaShipmentService>

    await TestBed.configureTestingModule({
      imports: [ShipmentCommentsComponent],
      providers: [
        provideMockStore({initialState}),
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
        { provide: Router, useValue: mockRouter },
        { provide: ToastrService, useValue: mockToastr },
        { provide: RecoveredPlasmaService, useValue: mockRecoveredPlasmaService },
        { provide: CookieService, useValue: mockCookieService },
        { provide: ProductIconsService, useValue: mockProductIconService },
        { provide: RecoveredPlasmaShipmentService, useValue: mockShipmentService },
        { provide: ProcessHeaderService, useValue: mockProcessHeaderService },
      ]
    }).compileComponents();
    
    fixture = TestBed.createComponent(ShipmentCommentsComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with correct shipment ID from route params', () => {
    expect(component.shipmentId).toBe(mockShipmentId);
  });

  it('should have correct table configuration', () => {
    const tableConfig = component.shipmentInfoCommentsTableConfigComputed();
    
    expect(tableConfig.showPagination).toBe(false);
    expect(tableConfig.columns).toHaveLength(3);
    
    const [staffCol, dateCol, commentsCol] = tableConfig.columns;
    
    expect(staffCol.id).toBe('createEmployeeId');
    expect(staffCol.header).toBe('Staff');
    
    expect(dateCol.id).toBe('createDate');
    expect(dateCol.header).toBe('Date and Time');
    
    expect(commentsCol.id).toBe('comments');
    expect(commentsCol.header).toBe('Comments');
  });

  describe('fetchShipmentCommentsHistory', () => {
    it('should fetch and set shipment history data successfully', () => {
      const mockResponse = {
        data: {
          findAllShipmentHistoryByShipmentId : [
            {
              createEmployeeId: 'EMP001',
              createDate: '2024-01-01T10:00:00Z',
              comments: 'Test comment 1'
            },
            {
              createEmployeeId: 'EMP002',
              createDate: '2024-01-02T11:00:00Z',
              comments: 'Test comment 2'
            }
        ]
        }
      } as any as ApolloQueryResult<{ findAllShipmentHistoryByShipmentId: ShipmentHistoryDTO}>

      mockShipmentService.getShipmentHistory.mockReturnValue(of(mockResponse));

      component.ngOnInit();

      expect(mockShipmentService.getShipmentHistory).toHaveBeenCalledWith(mockShipmentId);
      expect(component.shipmentHistoryData()).toEqual(mockShipmentHistoryData);
    });

    it('should set empty array when no data is returned', () => {
      const mockResponse = {
        data: {
          findAllShipmentHistoryByShipmentId : []
        }
      } as any as ApolloQueryResult<{ findAllShipmentHistoryByShipmentId: ShipmentHistoryDTO}>

      mockShipmentService.getShipmentHistory.mockReturnValue(of(mockResponse));

      component.ngOnInit();

      expect(component.shipmentHistoryData()).toEqual([]);
    });

    it('should handle error when fetching shipment history fails', () => {
      const mockError = new ApolloError({ errorMessage: 'Test error' });
      mockShipmentService.getShipmentHistory.mockReturnValue(throwError(() => mockError));
      jest.spyOn(mockToastr, 'error');
      component.fetchShipmentCommentsHistory();
      expect(mockToastr.error).toHaveBeenCalledWith(ERROR_MESSAGE);
    });
  });
});
