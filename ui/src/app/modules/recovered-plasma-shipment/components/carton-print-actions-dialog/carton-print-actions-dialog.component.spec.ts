import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { By } from '@angular/platform-browser';
import { CartonPrintActionsDialogComponent } from './carton-print-actions-dialog.component';
import { ActionButtonComponent } from '../../../../shared/components/buttons/action-button.component';
import { CartonDTO } from '../../models/recovered-plasma.dto';

describe('CartonPrintActionsDialogComponent', () => {
  let component: CartonPrintActionsDialogComponent;
  let fixture: ComponentFixture<CartonPrintActionsDialogComponent>;

  const mockCartonData: CartonDTO = {
    cartonNumber: 'CARTON12345',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        CartonPrintActionsDialogComponent,
        MatDialogModule
      ],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: mockCartonData }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CartonPrintActionsDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display the carton number from injected data', () => {
    const cartonNumberElement = fixture.debugElement.query(By.css('[data-testid="carton-number"]'));
    expect(cartonNumberElement.nativeElement.textContent.trim()).toBe(mockCartonData.cartonNumber);
  });

  it('should have correct action constants', () => {
    expect(CartonPrintActionsDialogComponent.PRINT_ACTION_PRINT_CARTON_LABEL).toBe('print-carton-label');
    expect(CartonPrintActionsDialogComponent.PRINT_ACTION_CARTON_PACKING_SLIP).toBe('print-carton-packing-slip');
  });

  it('should have getter methods returning correct action values', () => {
    expect(component.actionPrintCartonLabel).toBe(CartonPrintActionsDialogComponent.PRINT_ACTION_PRINT_CARTON_LABEL);
    expect(component.actionPrintCartonPackingSlip).toBe(CartonPrintActionsDialogComponent.PRINT_ACTION_CARTON_PACKING_SLIP);
  });

  it('should have two action buttons with correct IDs and labels', () => {
    const actionButtons = fixture.debugElement.queryAll(By.directive(ActionButtonComponent));
    expect(actionButtons.length).toBe(2);

    const [labelButton, slipButton] = actionButtons;

    expect(labelButton.attributes['btnId']).toBe('cartonPrintActionsCartonLabelBtn');
    expect(labelButton.attributes['label']).toBe('Carton Label');
    expect(labelButton.attributes['color']).toBe('secondary');

    expect(slipButton.attributes['btnId']).toBe('cartonPrintActionsCartonPackingSlipBtn');
    expect(slipButton.attributes['label']).toBe('Carton Packing Slip');
    expect(slipButton.attributes['color']).toBe('secondary');
  });

  it('should have a close button', () => {
    const closeButton = fixture.debugElement.query(By.css('#cartonPrintActionsCancel'));
    expect(closeButton).toBeTruthy();
    expect(closeButton.attributes['mat-dialog-close']).toBeDefined();
  });

  it('should pass correct action values to mat-dialog-close', () => {
    const labelAction = fixture.debugElement.queryAll(By.css('#cartonPrintActionsCartonLabel'))?.[0];
    expect(labelAction.attributes['ng-reflect-dialog-result']).toBe(component.actionPrintCartonLabel);

    const packingSlipAction = fixture.debugElement.queryAll(By.css('#cartonPrintActionsCartonPackingSlip'))?.[0];
    expect(packingSlipAction.attributes['ng-reflect-dialog-result']).toBe(component.actionPrintCartonPackingSlip);
  });

});
