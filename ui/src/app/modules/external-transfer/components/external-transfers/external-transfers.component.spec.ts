import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
    MAT_DATE_FORMATS,
    MAT_NATIVE_DATE_FORMATS,
    MatNativeDateModule,
} from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { By } from '@angular/platform-browser';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { provideMockStore } from '@ngrx/store/testing';
import { ToastrImplService } from '@shared';
import { ApolloTestingModule } from 'apollo-angular/testing';
import { ToastrModule } from 'ngx-toastr';
import { of } from 'rxjs';
import { ExternalTransferService } from '../../services/external-transfer.service';
import { ExternalTransfersComponent } from './external-transfers.component';

describe('ExternalTransfersComponent', () => {
    let component: ExternalTransfersComponent;
    let fixture: ComponentFixture<ExternalTransfersComponent>;
    let dateInput: HTMLInputElement;
    let service: ExternalTransferService;
    let toastr: ToastrImplService;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                ExternalTransfersComponent,
                ApolloTestingModule,
                MatDatepickerModule,
                MatNativeDateModule,
                NoopAnimationsModule,
                ToastrModule.forRoot(),
            ],
            providers: [
                ExternalTransferService,
                provideMockStore({}),
                {
                    provide: MAT_DATE_FORMATS,
                    useValue: MAT_NATIVE_DATE_FORMATS,
                },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(ExternalTransfersComponent);
        component = fixture.componentInstance;
        service = TestBed.inject(ExternalTransferService);
        jest.spyOn(service, 'customerInfo').mockReturnValue(of());
        toastr = TestBed.inject(ToastrImplService);
        fixture.detectChanges();
        dateInput = fixture.debugElement.query(By.css('input')).nativeElement;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should allow user to select today or past date', () => {
        const today = new Date().toISOString().split('T')[0];
        component.externalTransfer.controls['transferDate'].setValue(today);
        fixture.detectChanges();
        expect(component.externalTransfer.controls['transferDate'].value).toBe(
            today
        );
    });

    it('should not allow user to select future date', () => {
        const futureDate = new Date();
        futureDate.setDate(futureDate.getDate() + 1);
        dateInput.value = futureDate.toISOString().split('T')[0];
        dateInput.dispatchEvent(new Event('input'));
        fixture.detectChanges();
        expect(component.externalTransfer.controls['transferDate'].value).toBe(
            ''
        );
    });

    it('should disable submit button', () => {
        const submitBtn = fixture.debugElement.query(
            By.css('#submitBtnId')
        ).nativeElement;
        expect(submitBtn.disabled).toBeTruthy();
    });
});
