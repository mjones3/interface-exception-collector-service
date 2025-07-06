import { ComponentFixture, TestBed } from '@angular/core/testing';

import { provideHttpClient } from '@angular/common/http';
import { Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { By } from '@angular/platform-browser';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ApolloQueryResult } from '@apollo/client/core';
import {
    MatDialogRefMock,
    createTestContext,
    toasterMockProvider,
} from '@testing';
import { ApolloTestingModule } from 'apollo-angular/testing';
import { of } from 'rxjs';
import {
    GraphQLGetReasons,
    OTHER_REASON_KEY,
    ReasonDTO,
    VISUAL_INSPECTION,
} from '../../models/check-in.models';
import { CheckInService } from '../../services/check-in.service';
import { RecordVisualInspectionModalComponent } from './record-visual-inspection-modal.component';

describe('RecordVisualInspectionModalComponent', () => {
    let component: RecordVisualInspectionModalComponent;
    let fixture: ComponentFixture<RecordVisualInspectionModalComponent>;
    let service: CheckInService;
    let matDialogRef: MatDialogRef<
        RecordVisualInspectionModalComponent,
        { successful: boolean; comment: string; reasons: ReasonDTO[] }
    >;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                RecordVisualInspectionModalComponent,
                ApolloTestingModule.withClients(['checkin']),
                NoopAnimationsModule,
            ],
            providers: [
                ...toasterMockProvider,
                { provide: MatDialogRef, useClass: MatDialogRefMock },
                provideHttpClient(),
            ],
        }).compileComponents();

        const testContext =
            createTestContext<RecordVisualInspectionModalComponent>(
                RecordVisualInspectionModalComponent
            );
        fixture = testContext.fixture;
        component = testContext.component;
        service = TestBed.inject(CheckInService);
        matDialogRef = TestBed.inject(MatDialogRef);
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('Should get reasons when initializing', () => {
        const reasons: ReasonDTO[] = [
            {
                type: 'type',
                consequenceType: 'consequenceType',
                priority: 1,
                reasonKey: 'reason1',
            },
            {
                type: 'type2',
                consequenceType: 'consequenceType2',
                priority: 2,
                reasonKey: 'reason2',
            },
        ];

        jest.spyOn(service, 'getReasons').mockReturnValue(
            of({
                data: {
                    getReasons: reasons,
                },
            } as ApolloQueryResult<GraphQLGetReasons>)
        );
        component.ngOnInit();
        component.form.patchValue({ visualInspection: false });
        fixture.detectChanges();
        expect(service.getReasons).toHaveBeenCalledWith(VISUAL_INSPECTION);
        const actionButtons = fixture.debugElement.queryAll(
            By.css(`mat-grid-list biopro-action-button`)
        );
        expect(actionButtons).toHaveLength(reasons.length);
        reasons.forEach((reason) => {
            const reasonBtn = fixture.debugElement.query(
                By.css(`biopro-action-button#${reason.reasonKey} button`)
            ).nativeElement;
            expect(reasonBtn).not.toBeNull();
            expect(reasonBtn.id).toEqual(`${reason.reasonKey}actionBtn`);
        });
    });

    it('Should set visual inspection true and clear reasons control validators', () => {
        component.visualInpsection = true;
        expect(component.reasons.validator).toBe(null);
    });

    it('Should set visual inspection false and update reasons control validators', () => {
        component.visualInpsection = false;
        expect(component.reasons.validator).toBe(Validators.required);
    });

    it('Should return true if Other is selected', () => {
        component.reasons.setValue([{ id: 1, reasonKey: OTHER_REASON_KEY }]);
        expect(component.commentRequired).toBeTruthy();
    });

    it('Should select/unselect the reason', () => {
        const reason: ReasonDTO = {
            reasonKey: OTHER_REASON_KEY,
            priority: 2,
            type: '',
            consequenceType: 'QUARANTINE',
        };
        component.toggleButton(reason);
        expect(component.reasons.value.includes(reason)).toBeTruthy();

        component.toggleButton(reason);
        expect(component.reasons.value.includes(reason)).toBeFalsy();
    });

    it('Should submit with the values in the form', () => {
        jest.spyOn(matDialogRef, 'close');

        component.form.setValue({
            visualInspection: false,
            comments: null,
            reasons: [],
        });

        component.submit();

        expect(matDialogRef.close).toHaveBeenCalledWith({
            successful: false,
            comment: null,
            reasons: [],
        });
    });
});
