import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider } from '../../data/mock/environment-config.mock';
import { DescriptionFactoryService } from './description-factory.service';

describe('DescriptionFactoryService', () => {
  let service: DescriptionFactoryService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib')]
    });
    service = TestBed.inject(DescriptionFactoryService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
