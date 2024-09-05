import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatIconTestingModule } from '@angular/material/icon/testing';
import { SkipInventoryUnavailableDialogComponent } from './skip-inventory-unavailable-dialog.component';

describe('SkipInventoryUnavailableDialogComponent', () => {
    let component: SkipInventoryUnavailableDialogComponent;
    let fixture: ComponentFixture<SkipInventoryUnavailableDialogComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                SkipInventoryUnavailableDialogComponent,
                MatIconTestingModule,
            ],
            providers: [
                { provide: MatDialogRef, useValue: {} },
                { provide: MAT_DIALOG_DATA, useValue: {} },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(
            SkipInventoryUnavailableDialogComponent
        );
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
