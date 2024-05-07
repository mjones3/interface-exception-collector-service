import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { envConfigFactoryMock, EnvironmentConfigService, RsaCommonsModule } from '@rsa/commons';
import { MaterialModule } from '@rsa/material';
import { createTestContext } from '@rsa/testing';
import { InputKeyboardComponent } from '@rsa/touchable';
import { TreoCardModule } from '@treo';
import { addTestingIconsMock } from '../../shared/testing/mocks/data/icons.mock';
import { ScanProdCodeComponent } from './scan-prod-code.component';

describe('ScanProdCodeComponent', () => {
  let component: ScanProdCodeComponent;
  let fixture: ComponentFixture<ScanProdCodeComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ScanProdCodeComponent, InputKeyboardComponent],
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
    const testContext = createTestContext<ScanProdCodeComponent>(ScanProdCodeComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    addTestingIconsMock(testContext);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call submit product code', function () {
    const productCode = 'E1234V00';
    component.scanProductCodeForm.patchValue({ prodCode: productCode });
    spyOn(component.scannedProductCode, 'emit').and.callThrough();
    component.onFormSubmit();
    expect(component.scannedProductCode.emit).toHaveBeenCalledWith(productCode.replace('V', ''));
  });

  it('should not call submit product code empty or incorrect product code', function () {
    component.scanProductCodeForm.patchValue({ prodCode: '' });
    spyOn(component.scannedProductCode, 'emit').and.callThrough();
    component.onFormSubmit();
    expect(component.scannedProductCode.emit).not.toHaveBeenCalled();
    component.scanProductCodeForm.patchValue({ prodCode: 'E123400' });
    component.onFormSubmit();
    expect(component.scannedProductCode.emit).not.toHaveBeenCalled();
  });
});
