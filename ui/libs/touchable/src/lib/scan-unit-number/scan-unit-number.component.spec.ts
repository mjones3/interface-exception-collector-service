import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { envConfigFactoryMock, EnvironmentConfigService, RsaCommonsModule } from '@rsa/commons';
import { MaterialModule } from '@rsa/material';
import { createTestContext } from '@rsa/testing';
import { InputKeyboardComponent } from '@rsa/touchable';
import { TreoCardModule } from '@treo';
import { addTestingIconsMock } from '../../shared/testing/mocks/data/icons.mock';
import { ScanUnitNumberComponent } from './scan-unit-number.component';

describe('ScanUnitNumberComponent', () => {
  let component: ScanUnitNumberComponent;
  let fixture: ComponentFixture<ScanUnitNumberComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ScanUnitNumberComponent, InputKeyboardComponent],
      imports: [
        NoopAnimationsModule,
        MaterialModule,
        TreoCardModule,
        RsaCommonsModule,
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useClass: TranslateFakeLoader,
          },
        }),
      ],
      providers: [{ provide: EnvironmentConfigService, useValue: envConfigFactoryMock('donor-app') }],
    });
    const testContext = createTestContext<ScanUnitNumberComponent>(ScanUnitNumberComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    addTestingIconsMock(testContext);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call submit unit number', function () {
    const unitNumber = 'W999914003459';
    component.scanUnitNumberForm.patchValue({ scanUnitNumber: `=${unitNumber}00` });
    spyOn(component.scannedUnit, 'emit').and.callThrough();
    component.onFormSubmit();
    expect(component.scannedUnit.emit).toHaveBeenCalledWith(unitNumber);
  });

  it('should not call submit unit number empty or incorrect unit number', function () {
    component.scanUnitNumberForm.patchValue({ scanUnitNumber: '' });
    spyOn(component.scannedUnit, 'emit').and.callThrough();
    component.onFormSubmit();
    expect(component.scannedUnit.emit).not.toHaveBeenCalled();
    component.scanUnitNumberForm.patchValue({ scanUnitNumber: 'W999914' });
    component.onFormSubmit();
    expect(component.scannedUnit.emit).not.toHaveBeenCalled();
  });
});
