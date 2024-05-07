import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {MatDialogRef, MAT_DIALOG_DATA} from '@angular/material/dialog';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {RsaCommonsModule} from '@rsa/commons';
import {MaterialModule} from '@rsa/material';
import {createTestContext, MatDialogRefMock} from '@rsa/testing';
import {InputKeyboardComponent, TouchableComponentsModule} from '@rsa/touchable';
import {TreoScrollbarMock, TreoScrollbarModule} from '@treo';
import {addTestingIconsMock} from '../../shared/testing/mocks/data/icons.mock';
import {optionsMock} from '../../shared/testing/mocks/data/shared.mock';
import {FilterableDropDownComponent} from './filterable-drop-down.component';

describe('FilterableDropDownComponent', () => {
  let component: FilterableDropDownComponent;
  let fixture: ComponentFixture<FilterableDropDownComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [FilterableDropDownComponent, InputKeyboardComponent, TreoScrollbarMock],
      imports: [
        NoopAnimationsModule,
        MaterialModule,
        ReactiveFormsModule,
        RsaCommonsModule,
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useClass: TranslateFakeLoader
          }
        })
      ],
      providers: [
        {provide: MatDialogRef, useClass: MatDialogRefMock},
        {
          provide: MAT_DIALOG_DATA, useValue: {
            iconName: 'Name',
            dialogTitle: 'Title',
            dialogText: 'Dialog Text',
            options: optionsMock
          }
        }
      ]
    }).overrideModule(TouchableComponentsModule, {
      remove: {imports: [TreoScrollbarModule]},
      add: {declarations: [TreoScrollbarMock]}
    });
    const testContext = createTestContext<FilterableDropDownComponent>(FilterableDropDownComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    component.options = optionsMock;
    component.optionsLabel = 'name';
    addTestingIconsMock(testContext);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should select value', () => {
    spyOn(component.dialogRef, 'close').and.callThrough();
    component.selectValue(optionsMock[0]);
    expect(component.dialogRef.close).toHaveBeenCalled();
  });
});
