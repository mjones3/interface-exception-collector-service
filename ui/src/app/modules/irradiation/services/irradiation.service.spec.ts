import { TestBed } from '@angular/core/testing';
import { IrradiationService } from './irradiation.service';
import { DynamicGraphqlPathService } from '../../../core/services/dynamic-graphql-path.service';

describe('IrradiationService', () => {
  let service: IrradiationService;

  beforeEach(() => {
    const mockDynamicGraphqlPathService = {
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
});
