import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CustomButtonToggleComponent } from './custom-button-toggle.component';
import { FormControl } from '@angular/forms';

describe('CustomButtonToggleComponent', () => {
  let component: CustomButtonToggleComponent;
  let fixture: ComponentFixture<CustomButtonToggleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CustomButtonToggleComponent]
    })
    .compileComponents();

    const mockFormControl = new FormControl('test') as FormControl;

    const mockToggleId = '123';
    
    fixture = TestBed.createComponent(CustomButtonToggleComponent);
    component = fixture.componentInstance;

    // Set input properties
    fixture.componentRef.setInput('toggleId', mockToggleId);
    fixture.componentRef.setInput('control', mockFormControl);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
