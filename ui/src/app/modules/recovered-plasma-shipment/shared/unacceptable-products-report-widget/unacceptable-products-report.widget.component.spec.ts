import { ComponentFixture, TestBed } from '@angular/core/testing';
import { UnacceptableProductsReportWidgetComponent } from './unacceptable-products-report-widget.component';
import { DatePipe } from '@angular/common';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { RecoveredPlasmaService } from '../../services/recovered-plasma.service';
import {
    ViewUnacceptableProductsComponent
} from '../../components/view-unacceptable-products/view-unacceptable-products.component';
import { of, throwError } from 'rxjs';
import { ApolloError, ApolloQueryResult } from '@apollo/client';
import { RecoveredPlasmaShipmentResponseDTO } from '../../models/recovered-plasma.dto';
import { UnacceptableUnitReportOutput } from '../../graphql/query-definitions/print-unacceptable-units-report.graphql';
import { UseCaseResponseDTO } from '../../../../shared/models/use-case-response.dto';
import { MatIconTestingModule } from '@angular/material/icon/testing';
import { ToastrService } from 'ngx-toastr';

describe('UnacceptableProductsReportWidgetComponent', () => {
  let component: UnacceptableProductsReportWidgetComponent;
  let fixture: ComponentFixture<UnacceptableProductsReportWidgetComponent>;
  let mockRecoveredPlasmaService: jest.Mocked<RecoveredPlasmaService>;
  let mockToastr: jest.Mocked<ToastrService>;
  let mockMatDialog: jest.Mocked<MatDialog>;
  let mockDatePipe: jest.Mocked<DatePipe>;
  let mockDialogRef: Partial<MatDialogRef<ViewUnacceptableProductsComponent, UnacceptableUnitReportOutput>>;

  const mockShipment: RecoveredPlasmaShipmentResponseDTO = {
    id: 123,
    shipmentNumber: 'SHIP-123',
    lastUnsuitableReportRunDate: '2025-05-13T10:30:00Z',
    unsuitableUnitReportDocumentStatus: 'COMPLETED_FAILED'
  } as RecoveredPlasmaShipmentResponseDTO;

  const mockEmployeeId = 'EMP-123';
  const mockLocationCode = 'LOC-456';

  beforeEach(async () => {
    mockRecoveredPlasmaService = {
      printUnacceptableUnitsReport: jest.fn()
    } as unknown as jest.Mocked<RecoveredPlasmaService>;

    mockToastr = {
      show: jest.fn(),
      error: jest.fn(),
    } as unknown as jest.Mocked<ToastrService>;

    mockDialogRef = {
      afterOpened: jest.fn().mockReturnValue(of({}))
    };

    mockMatDialog = {
      open: jest.fn().mockReturnValue(mockDialogRef)
    } as unknown as jest.Mocked<MatDialog>;

    mockDatePipe = {
      transform: jest.fn().mockReturnValue('05/13/2025 10:30')
    } as unknown as jest.Mocked<DatePipe>;

    await TestBed.configureTestingModule({
      imports: [
          UnacceptableProductsReportWidgetComponent,
          MatIconTestingModule,
      ],
      providers: [
        { provide: RecoveredPlasmaService, useValue: mockRecoveredPlasmaService },
        { provide: ToastrService, useValue: mockToastr },
        { provide: MatDialog, useValue: mockMatDialog },
        { provide: DatePipe, useValue: mockDatePipe }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(UnacceptableProductsReportWidgetComponent);
    component = fixture.componentInstance;

    // Set input properties
    fixture.componentRef.setInput('shipment', mockShipment);
    fixture.componentRef.setInput('shipment', mockShipment);
    fixture.componentRef.setInput('employeeId', mockEmployeeId);
    fixture.componentRef.setInput('locationCode', mockLocationCode);
    fixture.componentRef.setInput('loading', false);

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('unsuitableReportInfo', () => {
    it('should return empty array when lastUnsuitableReportRunDate is not available', () => {
      const shipmentWithoutDate = { ...mockShipment, lastUnsuitableReportRunDate: null };
        fixture.componentRef.setInput('shipment', shipmentWithoutDate);

      expect(component.unsuitableReportInfo()).toEqual([]);
    });

    it('should return formatted date when lastUnsuitableReportRunDate is available', () => {
      const result = component.unsuitableReportInfo();

      expect(result).toEqual([
        {
          label: 'Last Run',
          value: '05/13/2025 10:30'
        }
      ]);
      expect(mockDatePipe.transform).toHaveBeenCalledWith(mockShipment.lastUnsuitableReportRunDate, 'MM/dd/yyyy HH:mm');
    });
  });

  it('should call printUnacceptableUnitsReport with correct parameters', () => {
    const mockResponse = {
      data: {
        printUnacceptableUnitsReport: {
          notifications: [{ type: 'SUCCESS' }],
          data: { shipmentNumber: 'SHIP-123' }
        }
      }
    } as ApolloQueryResult<{ printUnacceptableUnitsReport: UseCaseResponseDTO<UnacceptableUnitReportOutput> }>;

    mockRecoveredPlasmaService.printUnacceptableUnitsReport.mockReturnValue(of(mockResponse));

    component.viewReport();

    expect(mockRecoveredPlasmaService.printUnacceptableUnitsReport).toHaveBeenCalledWith({
      shipmentId: mockShipment.id,
      employeeId: mockEmployeeId,
      locationCode: mockLocationCode
    });
  });

  it('should open dialog when response is successful', () => {
    const mockReportData = { shipmentNumber: 'SHIP-123' };
    const mockResponse = {
      data: {
        printUnacceptableUnitsReport: {
          notifications: [{ type: 'SUCCESS' }],
          data: mockReportData
        }
      }
    } as ApolloQueryResult<{ printUnacceptableUnitsReport: UseCaseResponseDTO<UnacceptableUnitReportOutput> }>;

    mockRecoveredPlasmaService.printUnacceptableUnitsReport.mockReturnValue(of(mockResponse));

    component.viewReport();

    expect(mockMatDialog.open).toHaveBeenCalledWith(ViewUnacceptableProductsComponent,
      {
        id: 'viewUnacceptableProductsDialog',
        width: expect.any(String),
        height: expect.any(String),
        data: mockReportData
      }
    );
  });

  it('should handle notifications when response is not successful', () => {
    const mockResponse = {
      data: {
        printUnacceptableUnitsReport: {
          notifications: [{ type: 'ERROR', message: 'Error message' }],
          data: null
        }
      }
    } as ApolloQueryResult<{ printUnacceptableUnitsReport: UseCaseResponseDTO<UnacceptableUnitReportOutput> }>;

    mockRecoveredPlasmaService.printUnacceptableUnitsReport
      .mockReturnValue(of(mockResponse));

    component.viewReport();

    expect(mockMatDialog.open).not.toHaveBeenCalled();
    expect(mockToastr.show).toHaveBeenCalledWith('Error message', null, {}, 'error')
  });

  it('should handle Apollo errors', () => {
    const mockError = new ApolloError({
      graphQLErrors: [{ message: 'GraphQL Error' }]
    });

    mockRecoveredPlasmaService.printUnacceptableUnitsReport
      .mockReturnValue(throwError(() => mockError));

    component.viewReport();

    expect(mockToastr.error).toHaveBeenCalledWith('GraphQL Error')
  });
});
