import { HttpClientModule } from '@angular/common/http';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';
import { ActivatedRoute, Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { provideMockStore } from '@ngrx/store/testing';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import {
  getAppInitializerMockProvider,
  RsaCommonsModule,
  ShipmentService,
  toasterMockProvider,
  ValidationPipe,
  VerifyFilledProductDto,
} from '@rsa/commons';
import { of } from 'rxjs';
import { EnterUnitNumberProductCodeComponent } from '../enter-unit-number-product-code/enter-unit-number-product-code.component';
import { FillProductsComponent } from './fill-products.component';

describe('FillProductsComponent', () => {
  let component: FillProductsComponent;
  let fixture: ComponentFixture<FillProductsComponent>;
  let router: Router;
  let shipmentService: ShipmentService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [FillProductsComponent, EnterUnitNumberProductCodeComponent],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
      imports: [
        RsaCommonsModule,
        HttpClientModule,
        RouterTestingModule,
        FormsModule,
        ReactiveFormsModule,
        BrowserModule,
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useClass: TranslateFakeLoader,
          },
        }),
      ],
      providers: [
        ...toasterMockProvider,
        ...getAppInitializerMockProvider('distribution-app'),
        ValidationPipe,
        {
          provide: ActivatedRoute,
          useValue: {
            paramMap: of({}),
            snapshot: {
              params: { id: '1', productId: '1' },
            },
          },
        },
        provideMockStore(),
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FillProductsComponent);
    component = fixture.componentInstance;
    shipmentService = TestBed.inject(ShipmentService);
    fixture.detectChanges();
    router = TestBed.inject(Router);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should add filled products', () => {
    const filledProduct: VerifyFilledProductDto = {
      unitNumber: 'W036898786769',
      productCode: 'E9747D0E',
      visualInspection: 'SATISFACTORY',
    };
    component.filledProductsData = [];
    spyOn(shipmentService, 'verifyShipmentProduct').and.returnValue(
      of({
        data: {},
      })
    );
    component.unitNumberProductCodeSelected(filledProduct);
    expect(shipmentService.verifyShipmentProduct).toBeCalled();
  });

  it('should fetch shipment details', () => {
    spyOn(shipmentService, 'getShipmentById').and.returnValue(
      of({
        data: {},
      })
    );
    component.fetchShipmentDetails();
    expect(shipmentService.getShipmentById).toBeCalledWith('1', true);
  });

  it('should navigate back to shipment details page', () => {
    spyOn(router, 'navigateByUrl');
    component.backToShipmentDetails();
    expect(router.navigateByUrl).toBeCalledWith('/shipment/1/shipment-details');
  });
});
