import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RepackCartonDialogComponent } from './repack-carton-dialog.component';
import { MatDialogRef } from '@angular/material/dialog';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { By } from '@angular/platform-browser';

describe('RepackCartonDialogComponent', () => {
  let component: RepackCartonDialogComponent;
  let fixture: ComponentFixture<RepackCartonDialogComponent>;
  let dialogRefMock: jest.Mocked<MatDialogRef<RepackCartonDialogComponent>>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        RepackCartonDialogComponent,
        NoopAnimationsModule
      ],
      providers: [
        { provide: MatDialogRef, useValue: dialogRefMock }
      ]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(RepackCartonDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

    it('should initialize with empty reason comments and required validator', () => {
      expect(component.reasonComments.value).toBe('');
      expect(component.reasonComments.valid).toBeFalsy();
      expect(component.reasonComments.hasError('required')).toBeTruthy();
    });

    it('should set max length for comments to 250', () => {
      expect(component.commentsMaxLength).toBe(250);
    });

 
    it('should disable Continue button when form is invalid', () => {
      component.reasonComments.setValue(null);
      fixture.detectChanges();
      const continueButton = fixture.debugElement.query(By.css('#btnContinue'));
      expect(continueButton.nativeElement.enabled).toBeFalsy();
    });

    it('should enable Continue button when form is valid', () => {
      component.reasonComments.setValue('Valid comment');
      fixture.detectChanges();
      const continueButton = fixture.debugElement.query(By.css('#btnContinue'));

      expect(continueButton.nativeElement.disabled).toBeFalsy();
    });

    it('should call onClickContinue when Continue button is clicked', () => {
      jest.spyOn(component, 'onClickContinue');
      component.reasonComments.setValue('testComment');
      const continueButton = fixture.debugElement.query(By.css('#btnContinue'));
      continueButton.triggerEventHandler('buttonClicked');
      expect(component.onClickContinue).toHaveBeenCalled();
    });


    it('should display character count in the hint', () => {
      const testComment = 'Test comment';
      component.reasonComments.setValue(testComment);
      fixture.detectChanges();
      
      const hintElement = fixture.debugElement.query(By.css('mat-hint'));
      expect(hintElement.nativeElement.textContent.trim()).toContain(`${testComment.length} / ${component.commentsMaxLength}`);
    });

    it('should display 0 in character count when comments are empty', () => {
      component.reasonComments.setValue('');
      fixture.detectChanges();
      
      const hintElement = fixture.debugElement.query(By.css('mat-hint'));

      expect(hintElement.nativeElement.textContent.trim()).toContain(`0 / ${component.commentsMaxLength}`);
    });

  describe('Form validation', () => {
    it('should mark form control as invalid when empty', () => {
      component.reasonComments.setValue('');
      expect(component.reasonComments.valid).toBeFalsy();
    });

    it('should mark form control as valid when filled', () => {
      component.reasonComments.setValue('Valid comment');
      expect(component.reasonComments.valid).toBeTruthy();
    });
  });
});
