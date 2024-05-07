import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Component, ElementRef, ViewChild } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { EnvironmentConfigService, getAppInitializerMockProvider, RsaCommonsModule } from '@rsa/commons';
import { dispatchKeyboardEvent, typeInElement } from '@rsa/testing';
import { RsaValidators } from '../../shared/forms/rsa-validators';

/***** Test Wrapper Component *****/
@Component({
  template: ` <div class="testContainer" [formGroup]="formGroup">
    <input
      #inputEl
      class="form-control"
      type="text"
      formControlName="control"
      [(rsaAutoFocusIf)]="focused"
      rsaFullProductCode
    />
  </div>`,
})
class FullProductCodeDirectiveWrapper {
  focused = true;
  @ViewChild('inputEl', { static: false }) inputEl: ElementRef;
  formGroup: FormGroup = new FormGroup({
    control: new FormControl('E764400', RsaValidators.fullProductCode),
  });
}

describe('FullProductCodeDirective', () => {
  let component: FullProductCodeDirectiveWrapper;
  let fixture: ComponentFixture<FullProductCodeDirectiveWrapper>;
  let httpMock: HttpTestingController;
  let envConfig: EnvironmentConfigService;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [FullProductCodeDirectiveWrapper],
        imports: [ReactiveFormsModule, HttpClientTestingModule, RsaCommonsModule],
        providers: [...getAppInitializerMockProvider('commons-lib')],
      });
      fixture = TestBed.createComponent(FullProductCodeDirectiveWrapper);
      component = fixture.componentInstance;
      httpMock = TestBed.inject(HttpTestingController);
      envConfig = TestBed.inject(EnvironmentConfigService);
      fixture.detectChanges();
    })
  );

  function flushFullProductCode(productCode: string, fullProdCode: string) {
    const request = httpMock.expectOne(
      `${envConfig.env.serverApiURL}/v1/barcodes/translations/translate?barcode=${productCode}`
    );
    expect(request.request.method).toBe('GET');
    request.flush({
      barcodeTranslation: { productCode: fullProdCode },
    });
  }

  it('should call barcode service to get product code translation on enter keydown event', () => {
    component.focused = true;
    const fullProdCode = 'E7644V00';
    const scannedProdCode = '=<E7644V00&>0221080359';
    typeInElement(scannedProdCode, component.inputEl.nativeElement);
    fixture.detectChanges();
    dispatchKeyboardEvent(component.inputEl.nativeElement, 'keydown', null, null, 'Enter');
    fixture.detectChanges();
    flushFullProductCode('=%3CE7644V00%26%3E0221080359', fullProdCode);
    expect(component.formGroup.value.control).toEqual(fullProdCode);
  });

  it('should call barcode service to get product code translation on tab keydown event', () => {
    component.focused = true;
    const fullProdCode = 'E7644V00';
    const scannedProdCode = '=<E7644V00&>0221080359';
    typeInElement(scannedProdCode, component.inputEl.nativeElement);
    fixture.detectChanges();
    dispatchKeyboardEvent(component.inputEl.nativeElement, 'keydown', null, null, 'Tab');
    fixture.detectChanges();
    flushFullProductCode('=%3CE7644V00%26%3E0221080359', fullProdCode);
    expect(component.formGroup.value.control).toEqual(fullProdCode);
  });

  it('should not call barcode service and should not change the input value', () => {
    component.focused = true;
    const fullProdCode = 'E7644V00';
    typeInElement(fullProdCode, component.inputEl.nativeElement);
    fixture.detectChanges();
    dispatchKeyboardEvent(component.inputEl.nativeElement, 'keydown', null, null, 'Enter');
    fixture.detectChanges();
    expect(component.formGroup.value.control).toEqual(fullProdCode);
  });

  it('should not pass full product code validation', () => {
    component.focused = true;
    dispatchKeyboardEvent(component.inputEl.nativeElement, 'keydown', null, null, 'Tab');
    fixture.detectChanges();
    expect(component.formGroup.invalid).toBeTruthy();
  });
});
