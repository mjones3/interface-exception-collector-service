import { ComponentFixture, TestBed } from '@angular/core/testing';
import { createTestContext } from '@testing';
import { RecordVisualInspectionModalComponent } from './record-visual-inspection-modal.component';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatIconTestingModule } from '@angular/material/icon/testing';
import { IrradiationService } from '../../services/irradiation.service';

describe('RecordVisualInspectionModalComponent', () => {
    let component: RecordVisualInspectionModalComponent;
    let fixture: ComponentFixture<RecordVisualInspectionModalComponent>;
    let dialog: jest.Mocked<MatDialogRef<RecordVisualInspectionModalComponent>>;
    let irradiationService: jest.Mocked<IrradiationService>;

    beforeEach(async () => {
        dialog = { close: jest.fn() } as any;
        irradiationService = {} as any;

        await TestBed.configureTestingModule({
            imports: [
                RecordVisualInspectionModalComponent,
                MatIconTestingModule
            ],
            providers: [
                {
                    provide: MAT_DIALOG_DATA,
                    useValue: {}
                },
                {
                    provide: MatDialogRef,
                    useValue: dialog
                },
                {
                    provide: IrradiationService,
                    useValue: irradiationService
                }
            ]
        }).compileComponents();

        const testContext =
            createTestContext<RecordVisualInspectionModalComponent>(
                RecordVisualInspectionModalComponent
            );
        fixture = testContext.fixture;
        component = testContext.component;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should close dialog when submitting', () => {
        const closeSpy = jest.spyOn(dialog, 'close');
        component.form.patchValue({
            visualInspection: true,
            comments: 'Test comment'
        });

        component.submit();
        expect(closeSpy).toHaveBeenCalled();
    });
});
