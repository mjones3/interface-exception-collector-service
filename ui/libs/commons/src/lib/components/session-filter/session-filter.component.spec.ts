import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { PageHistoryService, SessionFilterComponent, SessionStorageService } from '@rsa/commons';

describe('SessionFilterComponent', () => {
  let component: SessionFilterComponent;
  let router: Router;
  let sessionStorage: SessionStorageService;
  let pageHistory: PageHistoryService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule],
      providers: [SessionStorageService, PageHistoryService],
    });

    router = TestBed.inject(Router);
    sessionStorage = TestBed.inject(SessionStorageService);
    pageHistory = TestBed.inject(PageHistoryService);
    component = new SessionFilterComponent(router, sessionStorage, pageHistory, 'test', 'test');
  });

  it('Should be created', () => {
    expect(component).toBeTruthy();
  });

  it('Should return stored value', () => {
    const mockFilter = { test: 'test' };
    jest.spyOn(sessionStorage, 'getJsonSession').mockImplementation(() => mockFilter);
    const filter = component.getFilter();
    expect(filter).toStrictEqual(mockFilter);
  });

  it('Should store a new filter', () => {
    const setJsonSessionSpy = jest.spyOn(sessionStorage, 'setJsonSession');
    const filter = { test: 'test' };
    component.setFilter(filter);
    expect(setJsonSessionSpy).toHaveBeenCalledWith('test', filter);
  });
});
