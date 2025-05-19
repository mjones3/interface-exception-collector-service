import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CloseShipmentDailogComponent } from './close-shipment-dailog.component';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { By } from '@angular/platform-browser';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ReactiveFormsModule } from '@angular/forms';
import { MatNativeDateModule } from '@angular/material/core';
import { ToastrModule } from 'ngx-toastr';

describe('CloseShipmentDailogComponent', () => {
  let component: CloseShipmentDailogComponent;
  let fixture: ComponentFixture<CloseShipmentDailogComponent>;
  let dialogRef: MatDialogRef<CloseShipmentDailogComponent>;
  
  const mockShipmentDate = new Date();
  
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        CloseShipmentDailogComponent,
        NoopAnimationsModule,
        ReactiveFormsModule,
        MatNativeDateModule,
        ToastrModule.forRoot()
      ],
      providers: [
        { 
          provide: MatDialogRef, 
          useValue: dialogRef 
        },
        { 
          provide: MAT_DIALOG_DATA, 
          useValue: { 
            shipmentDate: mockShipmentDate, 
          } 
        }
      ]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(CloseShipmentDailogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
  
  it('should initialize the form with the provided shipment date', () => {
    expect(component.shipmentDate.value).toEqual(mockShipmentDate);
  });
  
  it('should set minDate to current date', () => {
    const today = new Date();
    expect(component.minDate.getDate()).toEqual(today.getDate());
    expect(component.minDate.getMonth()).toEqual(today.getMonth());
    expect(component.minDate.getFullYear()).toEqual(today.getFullYear());
  });
  
  it('should have the form invalid when shipment date is cleared', () => {
    component.shipmentDate.setValue(null);
    expect(component.shipmentDate.valid).toBeFalsy();
  });
  
  it('should disable Continue button when form is invalid', () => {
    component.shipmentDate.setValue('3434343');
    fixture.detectChanges();
    
    const continueButton = fixture.debugElement.query(By.css('#btnContinue'));
    expect(continueButton.nativeElement.enabled).toBeFalsy();
  });
  
  it('should enable Continue button when form is valid', () => {
    component.shipmentDate.setValue(new Date().toDateString());
    fixture.detectChanges();
    const continueButton = fixture.debugElement.query(By.css('#btnContinue'));
    expect(continueButton.nativeElement.disabled).toBeFalsy();
  });
  
  
  it('should call onClickContinue when Continue button is clicked', () => {
    jest.spyOn(component, 'onClickContinue');
    component.shipmentDate.setValue(new Date().toDateString());
    fixture.detectChanges();
    const continueButton = fixture.debugElement.query(By.css('#btnContinue'));
    continueButton.triggerEventHandler('buttonClicked');
    expect(component.onClickContinue).toHaveBeenCalled();
  });
  
  it('should display error message when shipment date is required', () => {
    component.shipmentDate.setValue(null);
    component.shipmentDate.markAsTouched();
    fixture.detectChanges();
    
    const errorElement = fixture.debugElement.query(By.css('mat-error'));
    expect(errorElement.nativeElement.textContent).toContain('Shipment Date is required');
  });

  
  it('should have a confirmation message', () => {
    const messageElement = fixture.debugElement.query(By.css('.text-secondary'));
    expect(messageElement.nativeElement.textContent).toContain('The shipment will be closed');
  });
});
