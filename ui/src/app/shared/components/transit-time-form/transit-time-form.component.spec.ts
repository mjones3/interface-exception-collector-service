import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TransitTimeFormComponent } from './transit-time-form.component';
import { ReactiveFormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { ApolloTestingModule } from 'apollo-angular/testing';
import { ToastrService } from 'ngx-toastr';

jest.mock('keycloak-js');

describe('TransitTimeFormComponent', () => {
  let component: TransitTimeFormComponent;
  let fixture: ComponentFixture<TransitTimeFormComponent>;
  let mockToastr: jest.Mocked<ToastrService>;

  beforeEach(async () => {

    mockToastr = {
      show: jest.fn(),
      success: jest.fn(),
      error: jest.fn(),
      warning: jest.fn()
    } as any;


    await TestBed.configureTestingModule({
      imports: [
        TransitTimeFormComponent,
        ReactiveFormsModule,
        NoopAnimationsModule,
        MatDatepickerModule,
        ApolloTestingModule,
        MatNativeDateModule
      ],
      providers: [
        { provide: ToastrService, useValue: mockToastr }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(TransitTimeFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with null values', () => {
    const form = component.formGroup();
    expect(form.get('startDate')?.value).toBeNull();
    expect(form.get('startTime')?.value).toBe('');
    expect(form.get('startZone')?.value).toBe('');
    expect(form.get('endDate')?.value).toBeNull();
    expect(form.get('endTime')?.value).toBe('');
    expect(form.get('endZone')?.value).toBe('');
  });

  it('should enable form and set validators when useTransitTime is true', () => {
    component.updateValidators(true);
    const form = component.formGroup();
    
    expect(form.enabled).toBeTruthy();
    expect(form.get('startDate')?.hasError('required')).toBeTruthy();
    expect(form.get('startTime')?.hasError('required')).toBeTruthy();
    expect(form.get('startZone')?.hasError('required')).toBeTruthy();
    expect(form.get('endDate')?.hasError('required')).toBeTruthy();
    expect(form.get('endTime')?.hasError('required')).toBeTruthy();
    expect(form.get('endZone')?.hasError('required')).toBeTruthy();
  });

  it('should disable form and clear validators when useTransitTime is false', () => {
    component.updateValidators(false);
    const form = component.formGroup();
    
    expect(form.disabled).toBeTruthy();
  });

  it('should set end zone value', () => {
    const testZone = 'America/New_York';
    component.setEndZone(testZone);
    
    expect(component.formGroup().get('endZone')?.value).toBe(testZone);
  });

  it('should reset form', () => {
    const form = component.formGroup();
    form.patchValue({ startTime: '10:00', endTime: '12:00' });
    
    component.reset();
    
    expect(form.get('startTime')?.value).toBeNull();
    expect(form.get('endTime')?.value).toBeNull();
  });

  it('should blur element on triggerElementBlur', () => {
    const mockElement = { blur: jest.fn() } as any;
    const mockEvent = { target: mockElement } as Event;
    
    component.triggerElementBlur(mockEvent);
    
    expect(mockElement.blur).toHaveBeenCalled();
  });

  it('should set max date to now for transit start date', () => {
    const nowDate = new Date().toISOString().split('T')[0]; 
    const startDateInput = fixture.debugElement.nativeElement.querySelector('[data-testid="transit-time-start-date"]');
    expect(startDateInput.max).toBe(nowDate);
  });

  it('should set min date to start date value for transit end date when start date is set', () => {
    const testDate = new Date('2024-01-15').toISOString().split('T')[0];;
    component.formGroup().patchValue({ startDate: testDate });
    fixture.detectChanges();
    const endDateInput = fixture.debugElement.nativeElement.querySelector('[data-testid="transit-time-end-date"]');
    expect(endDateInput.min).toBe(testDate);
  });

  it('should set min date to now for transit end date when start date is not set', () => {
    const nowDate = new Date().toISOString().split('T')[0];
    component.formGroup().patchValue({ startDate: null });
    fixture.detectChanges();
    
    const endDateInput = fixture.debugElement.nativeElement.querySelector('[data-testid="transit-time-end-date"]');
    expect(endDateInput.min).toBe(nowDate);
  });

  it('should set max date to now for transit end date', () => {
    const nowDate = new Date().toISOString().split('T')[0];
    const endDateInput = fixture.debugElement.nativeElement.querySelector('[data-testid="transit-time-end-date"]');
    expect(endDateInput.max).toBe(nowDate);
  });
});