import { HttpClientModule, HttpResponse } from '@angular/common/http';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { provideMockStore } from '@ngrx/store/testing';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import {
  getAppInitializerMockProvider,
  RsaCommonsModule,
  ShipmentInfoDto,
  ShipmentInfoItemDto,
  ShipmentService,
  toasterMockProvider,
  ValidationPipe,
} from '@rsa/commons';
import { TreoCardModule } from '@treo';
import { ButtonModule } from 'primeng/button';
import { RippleModule } from 'primeng/ripple';
import { TableModule } from 'primeng/table';
import { of } from 'rxjs';
import { ShipmentDetailsComponent } from './shipment-details.component';

const SHIPMENT_ID = 1;
describe('ShipmentDetailsComponent', () => {
  let component: ShipmentDetailsComponent;
  let fixture: ComponentFixture<ShipmentDetailsComponent>;
  let shipmentService: ShipmentService;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ShipmentDetailsComponent],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
      imports: [
        RsaCommonsModule,
        MatDividerModule,
        MatIconModule,
        ButtonModule,
        RippleModule,
        TableModule,
        TreoCardModule,
        BrowserAnimationsModule,
        RouterTestingModule,
        HttpClientModule,
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useClass: TranslateFakeLoader,
          },
        }),
      ],
      providers: [
        ...getAppInitializerMockProvider('distribution-app'),
        ...toasterMockProvider,
        ValidationPipe,
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
    expect(shipmentService.getShipmentById).toBeCalledWith(SHIPMENT_ID, true);
  });

  it('should navigate back to search order', () => {
    spyOn(router, 'navigateByUrl');
    component.backToSearch();
    expect(router.navigateByUrl).toBeCalledWith('/orders/search');
  });

  it('should navigate to fill product when click on Fill Product button', () => {
    const shipment = {
      id: '1',
    };
    spyOn(router, 'navigateByUrl');
    component.fillProducts(shipment as ShipmentInfoItemDto);
    expect(router.navigateByUrl).toBeCalledWith('shipment/1/fill-products/1');
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
    expect(shipmentService.completeShipment).toBeCalledWith({
      shipmentId: 1,
      employeeId: 'user-id-12',
    });
  });
});
