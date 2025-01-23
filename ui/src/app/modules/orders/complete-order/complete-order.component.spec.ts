import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { CompleteOrderComponent } from './complete-order.component';
import { MatDialogRef } from '@angular/material/dialog';
import { ReactiveFormsModule } from '@angular/forms';
import { provideAnimations } from '@angular/platform-browser/animations';

describe('CompleteOrderComponent', () => {
    let component: CompleteOrderComponent;
    let fixture: ComponentFixture<CompleteOrderComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [CompleteOrderComponent, ReactiveFormsModule],
            providers: [
                provideAnimations(),
                {
                    provide: MatDialogRef,
                    useValue: {
                        close: jest.fn()
                    }
                }
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(CompleteOrderComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should return CompleteOrderCommandDTO with values when Continue button is clicked', () => {
        const element = fixture.debugElement;

        const completeOrderReasonElement = element.query(By.css('#completeOrderReason'));
        const completeOrderReasonTextarea = completeOrderReasonElement.nativeElement as HTMLTextAreaElement;
        completeOrderReasonTextarea.value = 'ABC123';
        completeOrderReasonTextarea.dispatchEvent(new Event('input'));

        const continueButtonElement = element.query(By.css('#completeOrderSubmitBtn'));
        const continueButton = continueButtonElement.nativeElement as HTMLButtonElement;

        continueButton.click();
        expect(component.dialogRef.close).toHaveBeenCalledWith({
            comments: 'ABC123'
        });
    });

});
