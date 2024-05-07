import { HttpClient, HttpHandler } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider } from '../../data/mock/environment-config.mock';
import { LeftNavMenuService } from './left-nav-menu.service';

describe('LeftNavMenuService', () => {
  let service: LeftNavMenuService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [LeftNavMenuService, HttpClient, HttpHandler, ...getAppInitializerMockProvider('commons-lib')],
    });
    service = TestBed.inject(LeftNavMenuService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
