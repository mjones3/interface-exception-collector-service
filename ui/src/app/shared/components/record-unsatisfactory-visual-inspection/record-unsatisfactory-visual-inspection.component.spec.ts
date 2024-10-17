import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RecordUnsatisfactoryVisualInspectionComponent } from './record-unsatisfactory-visual-inspection.component';

describe('RecordUnsatisfactoryVisualInspectionComponent', () => {
    let component: RecordUnsatisfactoryVisualInspectionComponent;
    let fixture: ComponentFixture<RecordUnsatisfactoryVisualInspectionComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                NoopAnimationsModule,
                RecordUnsatisfactoryVisualInspectionComponent,
            ],
            providers: [
                { provide: MatDialogRef, useValue: {} },
                { provide: MAT_DIALOG_DATA, useValue: {} },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(
            RecordUnsatisfactoryVisualInspectionComponent
        );
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
