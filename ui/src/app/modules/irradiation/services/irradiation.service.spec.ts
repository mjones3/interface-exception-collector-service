import { TestBed } from '@angular/core/testing';
import { IrradiationService } from './irradiation.service';
import { DynamicGraphqlPathService } from '../../../core/services/dynamic-graphql-path.service';

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
});
