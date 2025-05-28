import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { MatStepperModule } from '@angular/material/stepper';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MockStore, provideMockStore } from '@ngrx/store/testing';
import { of, throwError } from 'rxjs';
import { ToastrModule, ToastrService } from 'ngx-toastr';
import { ApolloTestingModule } from 'apollo-angular/testing';
import { DatePipe } from '@angular/common';
import { RecoveredPlasmaService } from '../../services/recovered-plasma.service';
import { ProcessHeaderService } from '@shared';
import { CookieService } from 'ngx-cookie-service';
import { ProductIconsService } from 'app/shared/services/product-icon.service';
import { ApolloError } from '@apollo/client/errors';
import { CartonDTO } from '../../models/recovered-plasma.dto';
import { MatStepper } from '@angular/material/stepper';
import { Component, Input } from '@angular/core';
import { PackCartonItemsDTO } from '../../graphql/mutation-definitions/pack-items.graphql';
import { VerifyCartonItemsDTO } from '../../graphql/mutation-definitions/verify-products.graphql';
import { ManageCartonComponent } from './manage-carton-products.component';
import { By } from '@angular/platform-browser';
import { ApolloQueryResult } from '@apollo/client';
import { UseCaseResponseDTO } from 'app/shared/models/use-case-response.dto';
import { VerifyProductsComponent } from 'app/modules/shipments/verify-products/verify-products.component';

// Mock child components
@Component({
  selector: 'biopro-add-recovered-plasma-products',
  template: '<div></div>'
})
class MockAddRecoveredPlasmaProductsComponent {
  @Input() cartonDetails: CartonDTO;
  resetProductGroup = jest.fn();
  focusOnUnitNumber = jest.fn();
  disableInputsIfMaxCartonProduct = jest.fn();
}

@Component({
  selector: 'biopro-verify-recovered-plasma-products',
  template: '<div></div>'
})
class MockVerifyRecoveredPlasmaProductsComponent {
  @Input() cartonDetails: CartonDTO;
  resetProductGroup = jest.fn();
  focusOnUnitNumber = jest.fn();
}

describe('ManageCartonComponent', () => {
  let component: ManageCartonComponent;
  let fixture: ComponentFixture<ManageCartonComponent>;
  let router: Router;
  let service: RecoveredPlasmaService;
  let toastr: ToastrService;
  let cookieService: CookieService;
  let store: MockStore;
  let activatedRoute: ActivatedRoute;
  let headerService: ProcessHeaderService;

  beforeEach(() => {
    jest.clearAllMocks();
  });

  // Mock data
  const mockCartonData: CartonDTO = {
    id: 123,
    shipmentId: 456,
    cartonNumber: 'C123',
    status: 'OPEN',
    canClose: false,
    canVerify: true,
    packedProducts: []
  };

  const mockShipmentData = {
    id: 456,
    shipmentNumber: 'S456',
    status: 'OPEN',
    customerName: 'Test Customer',
    createdDate: new Date().toISOString()
  };

  // Mock service responses
  const mockCartonResponse = {
    data: {
      findCartonById: {
        data: mockCartonData,
        notifications: [{ type: 'SUCCESS', message: 'Carton found' }]
      }
    }
  };

  const mockShipmentResponse = {
    data: {
      findShipmentById: {
        data: mockShipmentData,
        notifications: [{ type: 'SUCCESS', message: 'Shipment found' }]
      }
    }
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        ManageCartonComponent,
        MatStepperModule,
        NoopAnimationsModule,
        ApolloTestingModule,
        ToastrModule.forRoot(),
      ],
      declarations: [
        MockAddRecoveredPlasmaProductsComponent,
        MockVerifyRecoveredPlasmaProductsComponent
      ],
      providers: [
        provideMockStore({
          initialState: {
            auth: { id: 'EMP123' }
          }
        }),
        DatePipe,
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              params: { id: '123' }
            },
            data: of({ step: 0 })
          }
        },
        {
          provide: Router,
          useValue: {
            navigateByUrl: jest.fn()
          }
        },
        {
          provide: RecoveredPlasmaService,
          useValue: {
            getCartonById: jest.fn(),
            closeCarton: jest.fn(),
            getShipmentById: jest.fn(),
            addCartonProducts: jest.fn(),
            verifyCartonProducts: jest.fn(),
            displayNotificationMessage: jest.fn(),
            handleInfoNotificationAndDiscard: jest.fn(),
            removeCartonItems: jest.fn()
          }
        },
        {
          provide: ToastrService,
          useValue: {
            error: jest.fn(),
            success: jest.fn(),
            show: jest.fn()
          } as Partial<ToastrService> as jest.Mocked<ToastrService>
        },
        {
          provide: CookieService,
          useValue: {
            get: jest.fn().mockReturnValue('LOC123')
          }
        },
        {
          provide: ProductIconsService,
          useValue: {
            getIconByProductFamily: jest.fn()
          }
        },
        {
          provide: ProcessHeaderService,
          useValue: {
            title: of('Test Title'),
            subTitle: of('Test Subtitle')
          }
        }
      ],
    })
    .compileComponents();

    fixture = TestBed.createComponent(ManageCartonComponent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    service = TestBed.inject(RecoveredPlasmaService);
    toastr = TestBed.inject(ToastrService);
    cookieService = TestBed.inject(CookieService);
    store = TestBed.inject(MockStore);
    activatedRoute = TestBed.inject(ActivatedRoute);
    headerService = TestBed.inject(ProcessHeaderService);

    // Mock the stepper
    component.stepper = {selectedIndex: 0} as MatStepper;

    // Setup spy methods
    jest.spyOn(service, 'getCartonById').mockReturnValue(of(mockCartonResponse as ApolloQueryResult<{ findCartonById: UseCaseResponseDTO<CartonDTO> }>));
    jest.spyOn(service, 'getShipmentById').mockReturnValue(of(mockShipmentResponse as any));
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('should load carton and shipment details on init', () => {
    fixture.detectChanges();
    expect(service.getCartonById).toHaveBeenCalledWith(123);
    expect(service.getShipmentById).toHaveBeenCalled();
    expect(component.cartonDetailsSignal()).toEqual(mockCartonData);
    expect(component.shipmentDetailsSignal()).toEqual(mockShipmentData);
  });

  it('should navigate back to shipment details', () => {
    component.shipmentDetailsSignal.set(mockShipmentData);
    component.navigateBackToShipmentDetails();

    expect(router.navigateByUrl).toHaveBeenCalledWith('/recovered-plasma/456/shipment-details');
  });

  it('should handle error when loading carton details', () => {
    const error = new ApolloError({ graphQLErrors: [{ message: 'Test error' }] });
    jest.spyOn(service, 'getCartonById').mockReturnValue(throwError(() => error));
    jest.spyOn(toastr, 'error');

    component.loadRecoveredPlasmaShippingCartonDetails(123).subscribe({
      error: () => {
        expect(toastr.error).toHaveBeenCalled();
      }
    });
  });

  it('should set carton details', () => {
    component.setCartonDetails(mockCartonData);
    expect(component.cartonDetailsSignal()).toEqual(mockCartonData);
  });

  it('should handle click on previous button', () => {
    const loadSpy = jest.spyOn(component, 'loadRecoveredPlasmaShippingCartonDetails');
    component.cartonDetailsSignal.set(mockCartonData);

    component.onClickPrevious({ index: 1, displayStaticMessage: true, resetMessage: 'Test message' });

    expect(loadSpy).toHaveBeenCalledWith(123);
    expect(component.stepper.selectedIndex).toBe(1);
    expect(component.messageTypeSignal()).toBe('warning');
    expect(component.messageSignal()).toBe('Test message');
  });

  it('should handle click on next button', () => {
    const loadSpy = jest.spyOn(component, 'loadRecoveredPlasmaShippingCartonDetails');
    component.cartonDetailsSignal.set(mockCartonData);

    component.onClickNext();

    expect(loadSpy).toHaveBeenCalledWith(123);
  });

  it('should get values for reset from URL', () => {
    const result = component.getValuesForReset({
      next: '/test?step=2&reset=true&resetMessage=Test%20Reset'
    });

    expect(result).toEqual({
      index: 2,
      displayStaticMessage: 'true',
      resetMessage: 'Test Reset'
    });
  });

  describe('enterAndVerifyProduct', () => {
    const mockProductItem: PackCartonItemsDTO = {
      cartonId: 123,
      unitNumber: 'UNIT123',
      productCode: 'PROD123',
      locationCode: 'LOC123',
      employeeId: 'EMP123'
    };

    const mockSuccessResponse = {
      data: {
        packCartonItem: {
          data: mockCartonData,
          notifications: [{ type: 'SUCCESS', message: 'Product added successfully' }]
        }
      }
    };

    beforeEach(() => {
      component.addProductsControl = new MockAddRecoveredPlasmaProductsComponent() as any;
      jest.spyOn(component, 'resetAddProductGroup');
      jest.spyOn(component, 'AddProductFocusOnUnitNumber');
    });

    it('should add product successfully', () => {
      jest.spyOn(service, 'addCartonProducts').mockReturnValue(of(mockSuccessResponse));

      component.enterAndVerifyProduct(mockProductItem);

      expect(service.addCartonProducts).toHaveBeenCalledWith({
        cartonId: 123,
        unitNumber: 'UNIT123',
        productCode: 'PROD123',
        locationCode: 'LOC123',
        employeeId: 'EMP123'
      });
      expect(component.cartonDetailsSignal()).toEqual(mockCartonData);
      expect(component.resetAddProductGroup).toHaveBeenCalled();
      expect(component.AddProductFocusOnUnitNumber).toHaveBeenCalled();
    });

    it('should handle info notification', () => {
      const infoResponse = {
        data: {
          packCartonItem: {
            data: mockCartonData,
            notifications: [{ type: 'INFO', message: 'Info message', action: 'TRIGGER_DISCARD' }]
          }
        }
      };

      jest.spyOn(service, 'addCartonProducts').mockReturnValue(of(infoResponse));
      jest.spyOn(service, 'handleInfoNotificationAndDiscard');

      component.enterAndVerifyProduct(mockProductItem);

      expect(component.resetAddProductGroup).toHaveBeenCalled();
      expect(service.handleInfoNotificationAndDiscard).toHaveBeenCalled();
    });

    it('should handle maximum units notification', () => {
      const maxUnitsResponse = {
        data: {
          packCartonItem: {
            data: mockCartonData,
            notifications: [{ type: 'WARNING', message: 'Max units reached', name: 'MAXIMUM_UNITS_BY_CARTON' }]
          }
        }
      };

      jest.spyOn(service, 'addCartonProducts').mockReturnValue(of(maxUnitsResponse));
      jest.spyOn(service, 'displayNotificationMessage');
      jest.spyOn(component.addProductsControl, 'disableInputsIfMaxCartonProduct');

      component.enterAndVerifyProduct(mockProductItem);

      expect(service.displayNotificationMessage).toHaveBeenCalled();
      expect(component.addProductsControl.disableInputsIfMaxCartonProduct).toHaveBeenCalled();
    });

    it('should handle error when adding product', () => {
      jest.spyOn(service, 'addCartonProducts').mockReturnValue(throwError(() => new Error('Test error')));
      jest.spyOn(toastr, 'error');
      component.enterAndVerifyProduct(mockProductItem);
      expect(component.resetAddProductGroup).toHaveBeenCalled();
      expect(toastr.error).toHaveBeenCalled();
    });
  });

  describe('verifyProducts', () => {
    const mockVerifyItem: VerifyCartonItemsDTO = {
      cartonId: 123,
      unitNumber: 'UNIT123',
      productCode: 'PROD123',
      locationCode: 'LOC123',
      employeeId: 'EMP123'
    };

    const mockSuccessResponse = {
      data: {
        verifyCarton: {
          data: mockCartonData,
          notifications: [{ type: 'SUCCESS', message: 'Product verified successfully' }],
          _links: null
        }
      }
    };

    beforeEach(() => {
      component.verifyProductsControl = new MockVerifyRecoveredPlasmaProductsComponent() as any;
      jest.spyOn(component.verifyProductsControl, 'resetProductGroup');
      jest.spyOn(component.verifyProductsControl, 'focusOnUnitNumber');
    });

    it('should verify product successfully', () => {
      jest.spyOn(service, 'verifyCartonProducts').mockReturnValue(of(mockSuccessResponse));
      component.verifyProducts(mockVerifyItem);
      expect(service.verifyCartonProducts).toHaveBeenCalledWith({
        cartonId: 123,
        unitNumber: 'UNIT123',
        productCode: 'PROD123',
        locationCode: 'LOC123',
        employeeId: 'EMP123'
      });
      expect(component.cartonDetailsSignal()).toEqual(mockCartonData);
      expect(component.verifyProductsControl.resetProductGroup).toHaveBeenCalled();
      expect(component.verifyProductsControl.focusOnUnitNumber).toHaveBeenCalled();
    });

    it('should disable close carton option', () => {
      jest.spyOn(service, 'verifyCartonProducts').mockReturnValue(of(mockSuccessResponse));
      component.setCartonDetails(mockCartonData);
      const buttonIdCssSelector = By.css('#closeCartonBtn button');
            const root = fixture.debugElement;
            fixture.detectChanges();
            const button = root.query(buttonIdCssSelector)?.nativeElement;
            expect(button.disabled).toBeTruthy();
    });

    it('should handle info notification during verification', () => {
      const infoResponse = {
        data: {
          verifyCarton: {
            data: mockCartonData,
            notifications: [{ type: 'INFO', message: 'Info message', action: 'TRIGGER_DISCARD' }],
            _links: null
          }
        }
      };

      jest.spyOn(service, 'verifyCartonProducts').mockReturnValue(of(infoResponse));
      jest.spyOn(service, 'handleInfoNotificationAndDiscard');
      component.verifyProducts(mockVerifyItem);
      expect(component.verifyProductsControl.resetProductGroup).toHaveBeenCalled();
      expect(service.handleInfoNotificationAndDiscard).toHaveBeenCalled();
    });

    it('should handle navigation links in response', () => {
      const responseWithLinks = {
        data: {
          verifyCarton: {
            data: mockCartonData,
            notifications: [{ type: 'WARNING', message: 'Warning message' }],
            _links: { next: '/test?step=1&reset=true&resetMessage=Test' }
          }
        }
      };

      jest.spyOn(service, 'verifyCartonProducts').mockReturnValue(of(responseWithLinks));
      jest.spyOn(component, 'onClickPrevious');
      component.verifyProducts(mockVerifyItem);
      expect(component.onClickPrevious).toHaveBeenCalledWith({
        index: 1,
        displayStaticMessage: 'true',
        resetMessage: 'Test'
      });
    });

    it('should handle error when verifying product', () => {
      jest.spyOn(service, 'verifyCartonProducts').mockReturnValue(throwError(() => new Error('Test error')));
      jest.spyOn(toastr, 'error');
      component.verifyProducts(mockVerifyItem);
      expect(component.verifyProductsControl.resetProductGroup).toHaveBeenCalled();
      expect(toastr.error).toHaveBeenCalled();
    });
  });

  describe('request', () => {
    it('should get carton product request', () => {
      const item: PackCartonItemsDTO = {
        unitNumber: 'UNIT123',
        productCode: 'PROD123',
        cartonId: 0,
        locationCode: '',
        employeeId: ''
      };

      const result = component['getCartonProductRequest'](item);

      expect(result).toEqual({
        cartonId: 123,
        unitNumber: 'UNIT123',
        productCode: 'PROD123',
        locationCode: 'LOC123',
        employeeId: 'EMP123'
      });
    });

    it('should get verify product request', () => {
      const item: VerifyCartonItemsDTO = {
        unitNumber: 'UNIT123',
        productCode: 'PROD123',
        cartonId: 0,
        locationCode: '',
        employeeId: ''
      };

      const result = component['getVerifyProductRequest'](item);

      expect(result).toEqual({
        cartonId: 123,
        unitNumber: 'UNIT123',
        productCode: 'PROD123',
        locationCode: 'LOC123',
        employeeId: 'EMP123'
      });
    });

    it('should get callbacks for add products', () => {
      component.addProductsControl = new MockAddRecoveredPlasmaProductsComponent() as any;
      const callbacks = component.getCallBacks();
      expect(callbacks).toHaveProperty('resetFn');
      expect(callbacks).toHaveProperty('focusFn');
    });

    it('should get callbacks for verify products', () => {
      component.verifyProductsControl = new MockVerifyRecoveredPlasmaProductsComponent() as any;
      const callbacks = component.getVerifyProductsCallBacks();
      expect(callbacks).toHaveProperty('resetFn');
      expect(callbacks).toHaveProperty('focusFn');
    });
  });

describe('close carton', () => {
const mockResponseCloseCarton = {
  data: {
    closeCarton: {
      data: {},
    notifications: {
      type: 'SUCCESS',
      message: 'Carton closed successfully'
    },
    _links: {
      next: '/recovered-plasma/1/shipment-details?print=true'
  }
    }
  }
}

  it('should call closeCarton service and navigate on success', () => {
    jest.spyOn(service, 'closeCarton').mockReturnValue(of(mockResponseCloseCarton) as any);
    component.closeCarton();
    expect(service.closeCarton).toHaveBeenCalled();
  });

  it('should not navigate if no next url returned', () => {
    const mockResponse = {
      data: {
        closeCarton: {
          notifications: [{type: 'SUCCESS', message: 'Success'}],
          _links: {
            next: null
          }
        }
      }
    };

    jest.spyOn(service, 'closeCarton').mockReturnValue(of(mockResponse) as any);
    component.closeCarton();
    expect(router.navigateByUrl).not.toHaveBeenCalled();
  });
})

it('should remove selected products when user choose remove option', () => {
  const mockCartonDetails =  {
        id : 1,
        cartonNumber : "BPMMH11",
        shipmentId : 1,
        cartonSequence : 1,
        packedProducts : [ {
          id : 11,
          cartonId : '1',
          unitNumber : "W036898786801",
          productCode : "E2534V00",
          productDescription : "CPD PLS MI 24H",
          verifiedProducts : [ 
            {
              id : 11,
              cartonId : '1',
              unitNumber : "W036898786801",
              productCode : "E2534V00",
              productDescription : "CPD PLS MI 24H"
            }
          ]
        } ],
  };


  const removeItemsResponse =  {
    removeCartonItems : {
      notifications : [ {
        message : "Products successfully removed",
        type : "SUCCESS",
        code : 25
      } ],
      data : {
        id : 1,
        packedProducts : [],
        verifiedProducts : []
      }
    }
  } as any;

  const removeReq = {
    cartonId: 123,
    employeeId: 'EMP123',
    cartonItemIds: [11],
  }

  component.setCartonDetails(mockCartonDetails);
  const spyService = jest.spyOn(service, 'removeCartonItems').mockReturnValue(of(removeItemsResponse));

  component.removeCartonProducts([11]);
  fixture.detectChanges();
  expect(spyService).toHaveBeenCalled();
  expect(toastr.show).toHaveBeenCalled();
  expect(component.cartonDetailsSignal().packedProducts).toEqual(removeItemsResponse.removeCartonItems.data.packedProducts);
  expect(spyService).toHaveBeenCalledWith(removeReq);
  expect(removeItemsResponse.removeCartonItems.data.verifiedProducts).toStrictEqual([]);
  });
});
