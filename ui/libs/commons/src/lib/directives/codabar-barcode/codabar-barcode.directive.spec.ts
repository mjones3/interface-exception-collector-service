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
      rsaCodabarBarcode
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

describe('CodabarBarcodeDirective', () => {
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

  it('It should call barcode service to get barcode parts on enter keydown event', () => {
    component.focused = true;
    const barcode = 'C12345';
    typeInElement('d0012345d', component.inputEl.nativeElement);
    fixture.detectChanges();
    dispatchKeyboardEvent(component.inputEl.nativeElement, 'keydown', null, null, 'Enter');
    fixture.detectChanges();
    expect(component.formGroup.value.control).toEqual(barcode);
  });

  it('It should call barcode service to get barcode parts on tab keydown event', () => {
    component.focused = true;
    const barcode = 'D12345';
    typeInElement('d0012345d', component.inputEl.nativeElement);
    fixture.detectChanges();
    dispatchKeyboardEvent(component.inputEl.nativeElement, 'keydown', null, null, 'Tab');
    fixture.detectChanges();
    expect(component.formGroup.value.control).not.toEqual(barcode);
  });
});
