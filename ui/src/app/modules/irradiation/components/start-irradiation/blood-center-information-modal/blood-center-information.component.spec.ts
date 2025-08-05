import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ImportDetailsModal } from './blood-center-information.component';

describe('ImportDetailsModal', () => {
  let component: ImportDetailsModal;
  let fixture: ComponentFixture<ImportDetailsModal>;
  let mockDialogRef: any;

  beforeEach(async () => {
    mockDialogRef = {
      close: jest.fn()
    };

    await TestBed.configureTestingModule({
      imports: [
        ImportDetailsModal,
        ReactiveFormsModule,
        MatFormFieldModule,
        MatInputModule,
        NoopAnimationsModule
      ],
      providers: [
        { provide: MatDialogRef, useValue: mockDialogRef }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ImportDetailsModal);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with required validators', () => {
    expect(component.importForm.get('bloodCenterName')?.hasError('required')).toBeTruthy();
    expect(component.importForm.get('address')?.hasError('required')).toBeTruthy();
    expect(component.importForm.get('registrationNumber')?.hasError('required')).toBeTruthy();
    expect(component.importForm.get('licenseNumber')?.hasError('required')).toBeFalsy();
  });

  it('should not submit when form is invalid', () => {
    component.submit();
    expect(mockDialogRef.close).not.toHaveBeenCalled();
  });

  it('should submit when form is valid', () => {
    const formData = {
      bloodCenterName: 'Test Center',
      address: 'Test Address',
      registrationNumber: 'REG123',
      licenseStatus: 'isLicensed',
      licenseNumber: 'LIC456'
    };

    component.importForm.patchValue(formData);
    component.submit();
      const expectedData = {
        bloodCenterName: 'Test Center',
        address: 'Test Address',
        registrationNumber: 'REG123',
        licenseNumber: 'LIC456'
      };

    expect(mockDialogRef.close).toHaveBeenCalledWith(expectedData);
  });

  it('should close dialog with null on cancel', () => {
    component.cancel();
    expect(mockDialogRef.close).toHaveBeenCalledWith(null);
  });

  it('should submit without license number when not licensed', () => {
  const formData = {
    bloodCenterName: 'Test Center',
    address: 'Test Address',
    registrationNumber: 'REG123',
    licenseStatus: 'isNotLicensed',
    licenseNumber: ''
  };

  component.importForm.patchValue(formData);
  component.submit();

  const expectedData = {
    bloodCenterName: 'Test Center',
    address: 'Test Address',
    registrationNumber: 'REG123'
  };

  expect(mockDialogRef.close).toHaveBeenCalledWith(expectedData);
});
});
