import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CustomButtonToggleComponent } from './custom-button-toggle.component';
import { FormControl } from '@angular/forms';
import { ButtonOption } from 'app/shared/models/custom-button-toggle.model';

describe('CustomButtonToggleComponent', () => {
  let component: CustomButtonToggleComponent;
  let fixture: ComponentFixture<CustomButtonToggleComponent>;
  let mockFormControl: FormControl;
  let mockToggleId: string;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CustomButtonToggleComponent]
    })
    .compileComponents();

    mockFormControl = new FormControl('test');
    mockToggleId = '123';
    
    fixture = TestBed.createComponent(CustomButtonToggleComponent);
    component = fixture.componentInstance;

    // Set required input properties
    fixture.componentRef.setInput('toggleId', mockToggleId);
    fixture.componentRef.setInput('control', mockFormControl);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('Input properties', () => {
    it('should have default values for optional inputs', () => {
      expect(component.showLabel()).toBeFalsy();
      expect(component.hideSingleSelectionIndicator()).toBeFalsy();
      expect(component.required()).toBeFalsy();
      expect(component.isDisabled()).toBeFalsy();
    });

    it('should use default button options when none provided', () => {
      const defaultOptions = component.options();
      expect(defaultOptions.length).toBe(2);
      expect(defaultOptions[0].value).toBe('YES');
      expect(defaultOptions[1].value).toBe('NO');
    });

    it('should accept custom button options', () => {
      const customOptions: ButtonOption[] = [
        { value: 'CUSTOM1', label: 'Custom 1', iconName: 'icon1' },
        { value: 'CUSTOM2', label: 'Custom 2', iconName: 'icon2' }
      ];
      fixture.componentRef.setInput('options', customOptions);
      fixture.detectChanges();

      expect(component.options()).toEqual(customOptions);
    });

    it('should update form control value', () => {
      mockFormControl.setValue('YES');
      expect(component.control().value).toBe('YES');
    });
  });

  describe('getIconName method', () => {
    it('should return correct icon name for option value', () => {
      const customOptions: ButtonOption[] = [
        { value: 'TEST', iconName: 'test-icon' }
      ];
      fixture.componentRef.setInput('options', customOptions);
      fixture.detectChanges();

      expect(component.getIconName('TEST')).toBe('test-icon');
    });

    it('should return empty string for non-existent option value', () => {
      expect(component.getIconName('NON_EXISTENT')).toBe('');
    });
  });

  describe('selectedOption method', () => {
    it('should emit when option is selected', () => {
      const emitSpy = jest.spyOn(component.optionSelected, 'emit');
      
      component.selectedOption();
      
      expect(emitSpy).toHaveBeenCalled();
    });
  });

  describe('Component behavior', () => {
    it('should handle disabled state', () => {
      fixture.componentRef.setInput('isDisabled', true);
      fixture.detectChanges();
      expect(component.isDisabled()).toBeTruthy();
    });

    it('should handle required state', () => {
      fixture.componentRef.setInput('required', true);
      fixture.detectChanges();
      expect(component.required()).toBeTruthy();
    });

    it('should handle label visibility', () => {
      fixture.componentRef.setInput('showLabel', true);
      fixture.componentRef.setInput('label', 'Test Label');
      fixture.detectChanges();
      
      expect(component.showLabel()).toBeTruthy();
      expect(component.label()).toBe('Test Label');
    });
  });
});
