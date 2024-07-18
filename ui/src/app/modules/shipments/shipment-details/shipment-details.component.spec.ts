import { HttpResponse } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { provideMockStore } from '@ngrx/store/testing';
import { of } from 'rxjs';
import { ShipmentInfoDto, ShipmentInfoItemDto } from '../models/shipment-info.dto';
import { ShipmentService } from '../services/shipment.service';
import { ShipmentDetailsComponent } from './shipment-details.component';

const SHIPMENT_ID = 1;
describe('ShipmentDetailsComponent', () => {
  let component: ShipmentDetailsComponent;
  let fixture: ComponentFixture<ShipmentDetailsComponent>;
  let shipmentService: ShipmentService;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        ShipmentDetailsComponent, 
      ],
      providers: [
        provideHttpClientTesting,
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              params: {
                id: SHIPMENT_ID,
              },
              data: {
                shipmentDetailsConfigData: [],
              },
            },
          },
        },
        provideMockStore(),
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ShipmentDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    shipmentService = TestBed.inject(ShipmentService);
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch shipment details', () => {
    spyOn(shipmentService, 'getShipmentById').and.returnValue(of({ body: { id: 1 } } as HttpResponse<ShipmentInfoDto>));
    component.fetchShipmentDetails();
    expect(shipmentService.getShipmentById).toHaveBeenCalledWith(SHIPMENT_ID, true);
  });

  it('should navigate back to search order', () => {
    spyOn(router, 'navigateByUrl');
    component.backToSearch();
    expect(router.navigateByUrl).toHaveBeenCalledWith('/orders/search');
  });

  it('should navigate to fill product when click on Fill Product button', () => {
    const shipment = {
      id: '1',
    };
    spyOn(router, 'navigateByUrl');
    component.fillProducts(shipment as ShipmentInfoItemDto);
    expect(router.navigateByUrl).toHaveBeenCalledWith('shipment/1/fill-products/1');
  });

  it('should complete on click complete shipment button', () => {
    component.loggedUserId = 'user-id-12';
    spyOn(shipmentService, 'completeShipment').and.returnValue(
      of({
        body: {
          notifications: [
            {
              statusCode: 200,
              notificationType: 'success',
              message: 'completed-shipment.success',
            },
          ],
          _links: {
            next: `/shipment/${SHIPMENT_ID}/shipment-details`,
          },
        },
      })
    );
    component.completeShipment();
    expect(shipmentService.completeShipment).toHaveBeenCalledWith({
      shipmentId: 1,
      employeeId: 'user-id-12',
    });
  });
});
