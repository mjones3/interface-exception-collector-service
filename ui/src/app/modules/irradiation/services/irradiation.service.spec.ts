import { TestBed } from '@angular/core/testing';
import { IrradiationService } from './irradiation.service';
import { DynamicGraphqlPathService } from '../../../core/services/dynamic-graphql-path.service';
import { of } from 'rxjs';
import { VALIDATE_DEVICE_ON_CLOSE_BATCH } from '../graphql/query.graphql';
import { IrradiationProductDTO } from '../models/model';

describe('IrradiationService', () => {
  let service: IrradiationService;
  let mockDynamicGraphqlPathService: any;

  beforeEach(() => {
    mockDynamicGraphqlPathService = {
      executeQuery: jest.fn(),
      executeMutation: jest.fn()
    };

    TestBed.configureTestingModule({
      providers: [
        { provide: DynamicGraphqlPathService, useValue: mockDynamicGraphqlPathService }
      ]
    });
    service = TestBed.inject(IrradiationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should call validateLotNumber with correct parameters', () => {
    const lotNumber = 'LOT-001';
    const type = 'IRRADIATION_INDICATOR';

    service.validateLotNumber(lotNumber, type);

    expect(mockDynamicGraphqlPathService.executeQuery).toHaveBeenCalledWith(
      '/irradiation/graphql',
      expect.any(Object),
      { lotNumber, type }
    );
  });

  describe('validateDeviceOnCloseBatch', () => {
    it('should call executeQuery with correct parameters', () => {
      const deviceId = 'device-123';
      const location = 'location-456';

      service.validateDeviceOnCloseBatch(deviceId, location);

      expect(mockDynamicGraphqlPathService.executeQuery).toHaveBeenCalledWith(
        '/irradiation/graphql',
        VALIDATE_DEVICE_ON_CLOSE_BATCH,
        { deviceId, location }
      );
    });

    it('should return observable with IrradiationProductDTO array', () => {
      const deviceId = 'device-123';
      const location = 'location-456';
      const mockResponse = {
        data: {
          validateDeviceOnCloseBatch: [
            { unitNumber: 'UNIT-001', productCode: 'PROD-001' },
            { unitNumber: 'UNIT-002', productCode: 'PROD-002' }
          ] as IrradiationProductDTO[]
        }
      };

      mockDynamicGraphqlPathService.executeQuery.mockReturnValue(of(mockResponse));

      const result = service.validateDeviceOnCloseBatch(deviceId, location);

      result.subscribe(response => {
        expect(response.data.validateDeviceOnCloseBatch).toHaveLength(2);
        expect(response.data.validateDeviceOnCloseBatch[0].unitNumber).toBe('UNIT-001');
      });
    });

    it('should handle empty response', () => {
      const deviceId = 'device-123';
      const location = 'location-456';
      const mockResponse = {
        data: {
          validateDeviceOnCloseBatch: []
        }
      };

      mockDynamicGraphqlPathService.executeQuery.mockReturnValue(of(mockResponse));

      const result = service.validateDeviceOnCloseBatch(deviceId, location);

      result.subscribe(response => {
        expect(response.data.validateDeviceOnCloseBatch).toHaveLength(0);
      });
    });
  });
});
