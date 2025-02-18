import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
    MAT_DIALOG_DATA,
    MatDialog,
    MatDialogRef,
} from '@angular/material/dialog';
import { By } from '@angular/platform-browser';
import { AcknowledgeDetailDTO } from 'app/shared/models';
import { AcknowledgeConfirmationComponent } from './acknowledge-confirmation.component';

describe('AcknowledgeConfirmationComponent', () => {
    let component: AcknowledgeConfirmationComponent;
    let fixture: ComponentFixture<AcknowledgeConfirmationComponent>;
    let matDialog: MatDialog;
    const mockDialogData: AcknowledgeDetailDTO = {
        title: 'Acknowledgment Message',
        description: '',
        subtitle: '',
        details: [],
    };

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [AcknowledgeConfirmationComponent],
            providers: [
                { provide: MatDialogRef, useValue: {} },
                { provide: MAT_DIALOG_DATA, useValue: mockDialogData },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(AcknowledgeConfirmationComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        matDialog = TestBed.inject(MatDialog);
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should display acknowledgeSubtitle and acknowledgeDetails if details is available', () => {
        component.data.details = ['reason'];
        fixture.detectChanges();
        const subTitleDiv = fixture.debugElement.query(
            By.css('#acknowledgeSubtitle')
        );
        expect(subTitleDiv).toBeTruthy();
        const detailDiv = fixture.debugElement.query(
            By.css('#acknowledgeDetails')
        );
        expect(detailDiv).toBeTruthy();
    });

    it('should not acknowledgeSubtitle and display acknowledgeDetails if details is empty', () => {
        component.data.details = [];
        fixture.detectChanges();
        const subTitleDiv = fixture.debugElement.query(
            By.css('#acknowledgeSubtitle')
        );
        expect(subTitleDiv).toBeFalsy();
        const detailDiv = fixture.debugElement.query(
            By.css('#acknowledgeDetails')
        );
        expect(detailDiv).toBeFalsy();
    });

    it('should not acknowledgeSubtitle and display acknowledgeDetails if details is null', () => {
        component.data.details = null;
        fixture.detectChanges();
        const subTitleDiv = fixture.debugElement.query(
            By.css('#acknowledgeSubtitle')
        );
        expect(subTitleDiv).toBeFalsy();
        const detailDiv = fixture.debugElement.query(
            By.css('#acknowledgeDetails')
        );
        expect(detailDiv).toBeFalsy();
    });
});
