import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { ProgressBarComponent } from './progress-bar.component';

describe('ProgressBarComponent', () => {
    let component: ProgressBarComponent;
    let fixture: ComponentFixture<ProgressBarComponent>;
    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [ProgressBarComponent],
        }).compileComponents();

        fixture = TestBed.createComponent(ProgressBarComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should display progress bar buffer as complete when value is 100', () => {
        component.value = 100;
        fixture.detectChanges();
        const progressBar = fixture.debugElement.query(
            By.css('mat-progress-bar')
        );
        expect(component.value).toBe(100);
        expect(progressBar.classes['inProgress']).toBeFalsy();
        expect(progressBar.classes['complete']).toBeTruthy();
    });

    it('should display progress bar buffer as inProgress when value is greater than 0 and less than 100', () => {
        component.value = 60;
        fixture.detectChanges();
        const progressBar = fixture.debugElement.query(
            By.css('mat-progress-bar')
        );
        expect(component.value).toBe(60);
        expect(progressBar.classes['inProgress']).toBeTruthy();
        expect(progressBar.classes['complete']).toBeFalsy();
    });
});
