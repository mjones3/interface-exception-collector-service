import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { RsaCommonsModule } from '@rsa/commons';
import { MaterialModule } from '@rsa/material';
import { createTestContext } from '@rsa/testing';
import { InputKeyboardComponent } from '@rsa/touchable';
import { addTestingIconsMock } from '../../shared/testing/mocks/data/icons.mock';
import { ConfirmationDialogComponent } from './confirmation-dialog.component';

describe('ConfirmationDialogComponent', () => {
  let component: ConfirmationDialogComponent;
  let fixture: ComponentFixture<ConfirmationDialogComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ConfirmationDialogComponent, InputKeyboardComponent],
      imports: [MaterialModule, NoopAnimationsModule, RsaCommonsModule, TranslateModule.forRoot({
        loader: {
          provide: TranslateLoader,
          useClass: TranslateFakeLoader
        }
      })]
    });
    const testContext = createTestContext<ConfirmationDialogComponent>(ConfirmationDialogComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    addTestingIconsMock(testContext);

    component.iconName = 'Name';
    component.dialogTitle = 'Title';
    component.dialogText = 'Dialog Text';
    component.commentsModal = true;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
