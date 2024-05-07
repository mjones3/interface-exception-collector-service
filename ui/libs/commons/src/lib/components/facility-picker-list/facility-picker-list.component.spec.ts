import { CommonModule } from '@angular/common';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { MaterialModule } from '@rsa/material';
import { createTestContext, MatDialogRefMock } from '@rsa/testing';
import { TreoScrollbarMock, TreoScrollbarModule } from '@treo';
import { getAppInitializerMockProvider } from '../../data/mock/environment-config.mock';
import { addRsaIconsMock } from '../../data/mock/icons.mock';
import { RsaCommonsModule } from '../../rsa-commons.module';

import { FacilityPickerListComponent } from './facility-picker-list.component';

describe('FacilityPickerListComponent', () => {
  let component: FacilityPickerListComponent;
  let fixture: ComponentFixture<FacilityPickerListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        RsaCommonsModule,
        MaterialModule,
        ReactiveFormsModule,
        NoopAnimationsModule,
        CommonModule,
        RouterTestingModule,
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useClass: TranslateFakeLoader,
          },
        }),
      ],
      providers: [
        ...getAppInitializerMockProvider('commons-lib'),
        { provide: MatDialogRef, useClass: MatDialogRefMock },
        {
          provide: MAT_DIALOG_DATA,
          useValue: {
            iconName: 'Name',
            dialogTitle: 'Title',
            dialogText: 'Dialog Text',
            options: [],
          },
        },
      ],
    })
      .overrideModule(RsaCommonsModule, {
        remove: { imports: [TreoScrollbarModule] },
        add: { declarations: [TreoScrollbarMock] },
      })
      .compileComponents();
  });

  beforeEach(() => {
    const testContext = createTestContext<FacilityPickerListComponent>(FacilityPickerListComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    addRsaIconsMock(testContext);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
