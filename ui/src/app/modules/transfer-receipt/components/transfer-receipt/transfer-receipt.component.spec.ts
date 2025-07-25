import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TransferReceiptComponent } from './transfer-receipt.component';
import { provideMockStore } from '@ngrx/store/testing';
import { ApolloTestingModule } from 'apollo-angular/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ReactiveFormsModule } from '@angular/forms';
import { ToastrModule, ToastrService } from 'ngx-toastr';
import { MatNativeDateModule } from '@angular/material/core';
import { CookieService } from 'ngx-cookie-service';
import { Router } from '@angular/router';
import { TransferReceiptService } from '../../services/transfer-receipt.service';
import { ReceivingService } from 'app/modules/imports/service/receiving.service';
import { TemperatureDeviceService } from 'app/shared/services/temperature-device.service';
import { of } from 'rxjs';

jest.mock('keycloak-js');

describe('TransferReceiptComponent', () => {
  let component: TransferReceiptComponent;
  let fixture: ComponentFixture<TransferReceiptComponent>;
  let mockCookieService: jest.Mocked<CookieService>;
  let mockRouter: jest.Mocked<Router>;
  let mockTransferReceiptService: jest.Mocked<TransferReceiptService>;
  let mockReceivingService: jest.Mocked<ReceivingService>;
  let mockToastrService: jest.Mocked<ToastrService>;
  let mockTemperatureDeviceService: jest.Mocked<TemperatureDeviceService>;

  const mockValidateOrderResponse = {
    data : {
      validateTransferOrderNumber : {
        _links : null,
        data : {
          productCategory : "FROZEN",
          temperatureUnit : "celsius",
          displayTransitInformation : false,
          displayTemperature : false,
          transitTimeZoneList : [ ],
          visualInspectionList : [ {
            id : 10,
            type : "VISUAL_INSPECTION_STATUS",
            optionValue : "SATISFACTORY",
            descriptionKey : "Satisfactory",
            orderNumber : 1,
            active : true
          }],
          defaultTimeZone : null,
          receivedDifferentLocation : true,
          orderNumber : 1
        },
        notifications : [ ]
      }
    }
  } as any;

  beforeEach(async () => {
    mockCookieService = { get: jest.fn().mockReturnValue('testFacility') } as any;
    mockRouter = { navigate: jest.fn() } as any;
    mockTransferReceiptService = {
      validateTransferOrderNumber: jest.fn().mockReturnValue(of(mockValidateOrderResponse))
    } as any;
    mockReceivingService = {
      getReceivingList: jest.fn().mockReturnValue(of({
        data: {
          getReceivingList: {
            data: [],
            notifications: []
          }
        }
      })),
    } as any;

    mockToastrService = {
      success: jest.fn(),
      error: jest.fn(),
      warning: jest.fn()
    } as any;
    mockTemperatureDeviceService = {} as any;

    await TestBed.configureTestingModule({
      imports: [ 
        TransferReceiptComponent,
        ApolloTestingModule,
        ReactiveFormsModule,
        BrowserAnimationsModule,
        MatNativeDateModule,
        ToastrModule.forRoot()
      ],
      providers: [
        provideMockStore({
          initialState: {
            auth: { id: 'testEmployeeId' }
          }
        }),
        { provide: CookieService, useValue: mockCookieService },
        { provide: Router, useValue: mockRouter },
        { provide: TransferReceiptService, useValue: mockTransferReceiptService },
        { provide: ReceivingService, useValue: mockReceivingService },
        { provide: ToastrService, useValue: mockToastrService },
        { provide: TemperatureDeviceService, useValue: mockTemperatureDeviceService }
      ]
    }).compileComponents();
    
    fixture = TestBed.createComponent(TransferReceiptComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with empty values', () => {
    expect(component.transferInformationForm.get('transferOrderNumber').value).toBe('');
    expect(component.transferInformationForm.get('temperatureCategory').value).toBe('');
    expect(component.transferInformationForm.get('comments').value).toBe('');
  });

  it('should get location code from cookie', () => {
    expect(component.locationCodeComputed()).toBe('testFacility');
  });

  it('should get employee ID from auth state', () => {
    expect(component.employeeIdComputed()).toBe('testEmployeeId');
  });

  it('should reset form on cancel', () => {
    component.transferInformationForm.patchValue({
      transferOrderNumber: '12345',
      comments: 'test'
    });
    const mockTransitTimeReset = jest.fn();
    const mockTemperatureReset = jest.fn();
    
    const mockTransitTimeComponent = { reset: mockTransitTimeReset };
    const mockTemperatureComponent = { reset: mockTemperatureReset };
    
    // Mock the viewChild signals to return the mock components
    Object.defineProperty(component, 'transitTimeFormComponent', {
        value: () => mockTransitTimeComponent,
        writable: true
    });
    Object.defineProperty(component, 'temperatureFormComponent', {
        value: () => mockTemperatureComponent,
        writable: true
    });
    
    component.cancel();
    expect(component.transferInformationForm.get('transferOrderNumber').value).toBeNull();
    expect(component.transferInformationForm.get('comments').value).toBeNull();
    expect(mockTransitTimeReset).toHaveBeenCalled();
    expect(mockTemperatureReset).toHaveBeenCalled();
  });

  it('should validate transfer order number', () => {
    component.transferInformationForm.patchValue({ transferOrderNumber: '12345' });
    component.onEnterTransferOrder();
    expect(mockTransferReceiptService.validateTransferOrderNumber).toHaveBeenCalledWith({
      orderNumber: 12345,
      employeeId: 'testEmployeeId',
      locationCode: 'testFacility'
    });
  });

  it('should handle successful transfer order validation', () => {    
    component.transferInformationForm.controls.transferOrderNumber.setValue('1');
    component.onEnterTransferOrder();
    jest.spyOn(mockTransferReceiptService, 'validateTransferOrderNumber').mockReturnValue(of(mockValidateOrderResponse))
    expect(mockTransferReceiptService.validateTransferOrderNumber).toHaveBeenCalled();
    expect(component.transferInformationForm.controls.temperatureCategory.value).toBe('FROZEN');
    expect(component.transferInformationForm.controls.transferOrderNumber.value).toBe(1);
  });

  it('should update transit time quarantine', () => {
    const notification = { type: 'WARN', message: 'Test warning' };
    component.updateTransitTimeQuarantine(notification);
    expect(component.transitTimeQuarantineSignal()).toEqual(notification);
  });

  it('should update temperature quarantine', () => {
    const notification = { type: 'WARN', message: 'Test warning' };
    component.updateTemperatureQuarantine(notification);
    expect(component.temperatureQuarantineSignal()).toEqual(notification);
  });

  it('should add required validator when isCommentRequired is true', () => {
    const isCommentRequired: boolean = true;
    component.updateValidtatorForComments(isCommentRequired);
    component.transferInformationForm.controls.comments.setValue('');
    expect(component.transferInformationForm.controls.comments.errors).toHaveProperty('required');
    expect(component.transferInformationForm.controls.comments.valid).toBeFalsy();
  });

  it('should not add required validator when isCommentRequired is false', () => {
    const isCommentRequired: boolean = false;
    component.updateValidtatorForComments(isCommentRequired);
    component.transferInformationForm.controls.comments.setValue('');
    expect(component.transferInformationForm.controls.comments.errors).toBeNull();
    expect(component.transferInformationForm.controls.comments.valid).toBeTruthy();
  });
});