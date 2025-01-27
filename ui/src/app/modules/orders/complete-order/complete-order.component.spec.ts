import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { By } from '@angular/platform-browser';
import { provideAnimations } from '@angular/platform-browser/animations';
import { CompleteOrderComponent } from './complete-order.component';

describe('CompleteOrderComponent', () => {
    let component: CompleteOrderComponent;
    let fixture: ComponentFixture<CompleteOrderComponent>;
    const mockDialogData = {
        backOrderCreationActive: false,
    };

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [CompleteOrderComponent, ReactiveFormsModule],
            providers: [
                provideAnimations(),
                { provide: MAT_DIALOG_DATA, useValue: mockDialogData },
                {
                    provide: MatDialogRef,
                    useValue: {
                        close: jest.fn(),
                    },
                },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(CompleteOrderComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should return "false" when Cancel button is clicked', () => {
        const element = fixture.debugElement;
        const cancelButtonElement = element.query(
            By.css('#completeOrderCancelBtn')
        );
        const cancelButton =
            cancelButtonElement.nativeElement as HTMLButtonElement;

        cancelButton.click();
        expect(component.dialogRef.close).toHaveBeenCalledWith(false);
    });

    it('should return CompleteOrderCommandDTO with values when Continue button is clicked', () => {
        const element = fixture.debugElement;
        component.data.isBackOrderCreationActive = true;
        const completeOrderReasonElement = element.query(
            By.css('#completeOrderReason')
        );
        const completeOrderReasonTextarea =
            completeOrderReasonElement.nativeElement as HTMLTextAreaElement;
        completeOrderReasonTextarea.value = 'ABC123';
        completeOrderReasonTextarea.dispatchEvent(new Event('input'));

        const continueButtonElement = element.query(
            By.css('#completeOrderSubmitBtn')
        );
        const continueButton =
            continueButtonElement.nativeElement as HTMLButtonElement;

        continueButton.click();
        expect(component.dialogRef.close).toHaveBeenCalledWith({
            comments: 'ABC123',
            createBackOrder: false,
        });
    });

    it('should hide toggle button for back order field when isBackOrderCreationActive is false', () => {
        component.data = { isBackOrderCreationActive: false };
        fixture.detectChanges();
        expect(
            fixture.debugElement.nativeElement.querySelector('#isBackOrderId')
        ).toBeNull();
    });
});
