import { provideHttpClient } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { provideMockStore } from '@ngrx/store/testing';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ApolloModule } from 'apollo-angular';
import { ApolloTestingModule } from 'apollo-angular/testing';
import { ToastrModule, ToastrService } from 'ngx-toastr';
import { of } from 'rxjs';
import { FillProductsComponent } from './fill-products.component';

describe('FillProductsComponent', () => {
  let component: FillProductsComponent;
  let fixture: ComponentFixture<FillProductsComponent>;
  let toasterService: ToastrService;
  let mockTranslateService: TranslateService;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        FillProductsComponent,
        NoopAnimationsModule,
        ApolloTestingModule,
        ApolloModule,
        ToastrModule.forRoot(),
        TranslateModule.forRoot()
      ],
      providers: [
        provideHttpClient(),
        {
          provide: ToastrService, useValue: toasterService
        },
        {
          provide: TranslateService, useValue: mockTranslateService
        },        
        {
          provide: ActivatedRoute,
          useValue: {
            paramMap: of({}),
            snapshot: {
              params: { id: 1 ,  productId: '1' },
            },
          },
        },
        provideMockStore(),
      ],
    })
    .compileComponents();  
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FillProductsComponent);
    router = TestBed.inject(Router);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should navigate back to shipment details page', () => {
    jest.spyOn(router, 'navigateByUrl');
    component.backToShipmentDetails();
    expect(router.navigateByUrl).toHaveBeenCalledWith('/shipment/1/shipment-details');
  });
});
