import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider } from '../../data/mock/environment-config.mock';
import { InventoryService } from './inventory.service';

describe('InventoryService', () => {
  let httpMock: HttpTestingController;
  let service: InventoryService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib')]
    });


    service = TestBed.inject(InventoryService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });


  //Create Inventory
  // it('should create an inventory', () => {
  //   service.createInventory(null).subscribe(
  //     (response) => {
  //       expect(response.body).not.toBe(null);
  //       expect(response.body).toEqual(_inventoryData);
  //     }
  //   );
  //
  //   const request = httpMock.expectOne('http://localhost/v1/inventories');
  //   expect(request.request.method).toEqual('POST');
  // });
});
