import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatIconTestingModule } from '@angular/material/icon/testing';
import { ViewPickListComponent } from './view-pick-list.component';

describe('ViewPickListComponent', () => {
    let component: ViewPickListComponent;
    let fixture: ComponentFixture<ViewPickListComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [ViewPickListComponent, MatIconTestingModule],
            providers: [
                { provide: MatDialogRef, useValue: {} },
                { provide: MAT_DIALOG_DATA, useValue: {} },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(ViewPickListComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
