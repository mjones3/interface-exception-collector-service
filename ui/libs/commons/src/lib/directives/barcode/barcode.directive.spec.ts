import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Component, ElementRef, ViewChild } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { EnvironmentConfigService, getAppInitializerMockProvider, RsaCommonsModule } from '@rsa/commons';
import { dispatchKeyboardEvent, typeInElement } from '@rsa/testing';

/***** Test Wrapper Component *****/
@Component({
  template: ` <div class="testContainer" [formGroup]="formGroup">
    <input
      #inputEl
      class="form-control"
      type="text"
      formControlName="control"
      [(rsaAutoFocusIf)]="focused"
      rsaBarcode
    />
  </div>`,
})
class BarcodeDirectiveWrapper {
  focused = true;
  @ViewChild('inputEl', { static: false }) inputEl: ElementRef;
  formGroup: FormGroup = new FormGroup({
    control: new FormControl(122),
  });
}

describe('BarcodeDirective', () => {
  let component: BarcodeDirectiveWrapper;
  let fixture: ComponentFixture<BarcodeDirectiveWrapper>;
  let httpMock: HttpTestingController;
  let envConfig: EnvironmentConfigService;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [BarcodeDirectiveWrapper],
        imports: [ReactiveFormsModule, HttpClientTestingModule, RsaCommonsModule],
        providers: [...getAppInitializerMockProvider('commons-lib')],
      });
      fixture = TestBed.createComponent(BarcodeDirectiveWrapper);
      component = fixture.componentInstance;
      httpMock = TestBed.inject(HttpTestingController);
      envConfig = TestBed.inject(EnvironmentConfigService);
      fixture.detectChanges();
    })
  );

  function flushBarcodeParts(barcode: string) {
    const request = httpMock.expectOne(`${envConfig.env.serverApiURL}/v1/barcodes/=Q123123123123100`);
    expect(request.request.method).toBe('GET');
    request.flush({
      unitNumber: 'W1231231231231',
      barcode,
    });
  }

  it('It should call barcode service to get barcode parts on enter keydown event', () => {
    component.focused = true;
    const barcode = 'Q1231231231231';
    typeInElement('=Q123123123123100', component.inputEl.nativeElement);
    fixture.detectChanges();
    dispatchKeyboardEvent(component.inputEl.nativeElement, 'keydown', null, null, 'Enter');
    fixture.detectChanges();
    flushBarcodeParts(barcode);
    expect(component.formGroup.value.control).toEqual(barcode);
  });

  it('It should call barcode service to get barcode parts on tab keydown event', () => {
    component.focused = true;
    const barcode = 'Q1231231231231';
    typeInElement('=Q123123123123100', component.inputEl.nativeElement);
    fixture.detectChanges();
    dispatchKeyboardEvent(component.inputEl.nativeElement, 'keydown', null, null, 'Tab');
    fixture.detectChanges();
    flushBarcodeParts(barcode);
    expect(component.formGroup.value.control).toEqual(barcode);
  });
});
