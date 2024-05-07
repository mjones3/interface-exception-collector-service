import { SimpleChange } from '@angular/core';
import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { BrowserDynamicTestingModule } from '@angular/platform-browser-dynamic/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { envConfigFactoryMock, EnvironmentConfigService, RsaCommonsModule, toasterMockProvider } from '@rsa/commons';
import { MaterialModule } from '@rsa/material';
import { createTestContext } from '@rsa/testing';
import { ConfirmationDialogComponent, InputKeyboardComponent, OptionsPickerDialogComponent } from '@rsa/touchable';
import { TreoCardModule, TreoScrollbarMock } from '@treo';
import { addTestingIconsMock } from '../../shared/testing/mocks/data/icons.mock';
import { UnitObjectForBatchScreenComponent } from './unit-object-for-batch-screen.component';

describe('UnitObjectForBatchScreenComponent', () => {
  let component: UnitObjectForBatchScreenComponent;
  let fixture: ComponentFixture<UnitObjectForBatchScreenComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [
        UnitObjectForBatchScreenComponent,
        InputKeyboardComponent,
        TreoScrollbarMock,
        ConfirmationDialogComponent,
        OptionsPickerDialogComponent,
      ],
      imports: [
        RouterTestingModule,
        NoopAnimationsModule,
        RsaCommonsModule,
        MaterialModule,
        TreoCardModule,
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useClass: TranslateFakeLoader,
          },
        }),
      ],
      providers: [
        ...toasterMockProvider,
        { provide: EnvironmentConfigService, useValue: envConfigFactoryMock('donor-app') },
      ],
    }).overrideModule(BrowserDynamicTestingModule, {
      set: {
        entryComponents: [ConfirmationDialogComponent, OptionsPickerDialogComponent],
      },
    });

    const testContext = createTestContext<UnitObjectForBatchScreenComponent>(UnitObjectForBatchScreenComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    addTestingIconsMock(testContext);
    component.addedUnits = [
      {
        unitNumber: 'W999912366534',
        productCode: 'WHOLE BLOOD',
        descriptionKey: 'WHOLE BLOOD',
        icon: 'product-whole-blood',
      },
      {
        unitNumber: 'W9999123665346',
        productCode: 'Platelet',
        descriptionKey: 'Platelet',
        icon: 'product-platelets',
      },
      {
        unitNumber: 'W999914003459',
        productCode: 'RBC APH BAG A',
        icon: 'product-rbc',
      },
    ];
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should delete unit', () => {
    const length = component.addedUnits.length;
    spyOn(component.addedUnits, 'splice').and.callThrough();
    component.deleteProduct(1);
    expect(component.addedUnits.length).toEqual(length - 1);
  });

  it('should delete all unit', done => {
    fakeAsync(() => {
      spyOn(component.toaster, 'success').and.callFake(() => {});
      component.removeAll();
      component.dialogRef.close(true);
      fixture.detectChanges();
      tick(1000);
      expect(component.addedUnits.length).toEqual(0);
      done();
    })();
  });

  it('should emit unit number scanned', () => {
    const value = 'W999912366534';
    spyOn(component.unitNumberScanned, 'emit').and.callThrough();
    component.unitNumberControl.patchValue(value);
    component.onTabOrEnterPressed();
    expect(component.unitNumberScanned.emit).toHaveBeenCalledWith(value);
  });

  it('should not emit unit number scanned', () => {
    const value = '';
    spyOn(component.unitNumberScanned, 'emit').and.callThrough();
    component.unitNumberControl.patchValue(value);
    component.onTabOrEnterPressed();
    expect(component.unitNumberScanned.emit).not.toHaveBeenCalledWith(value);
  });

  it('should emit centrifuge scanned', () => {
    const value = '23665346';
    spyOn(component.centrifugeBarcodeScanned, 'emit').and.callThrough();
    component.centrifugeBarcodeControl.patchValue(value);
    component.centrifugeScanned();
    expect(component.centrifugeBarcodeScanned.emit).toHaveBeenCalledWith(value);
  });

  it('should not emit centrifuge scanned', () => {
    const value = '';
    spyOn(component.centrifugeBarcodeScanned, 'emit').and.callThrough();
    component.centrifugeBarcodeControl.patchValue(value);
    component.centrifugeScanned();
    expect(component.centrifugeBarcodeScanned.emit).not.toHaveBeenCalledWith(value);
  });

  it('should add unit', () => {
    component.addUnits({ productCode: 'PC', unitNumber: 'W999914003459' });
    expect(component.addedUnits.length).toEqual(4);
    expect(component.unitNumberControl.value).toEqual(null);
  });

  it('should open donation modal', () => {
    const products = [
      { descriptionKey: 'P1DK', productCode: 'PC', unitNumber: 'W999914003459' },
      { descriptionKey: 'P2DK', productCode: 'PC', unitNumber: 'W999914003459' },
    ];
    spyOn(component, 'openDonationTypeModal').and.callThrough();
    component.ngOnChanges({
      productsInventory: new SimpleChange([], products, false),
    });
    expect(component.openDonationTypeModal).toHaveBeenCalled();
  });

  it('should add product to addedUnits array', () => {
    const products = [{ descriptionKey: 'P1DK', productCode: 'PC', unitNumber: 'W9999123665444' }];
    spyOn(component, 'openDonationTypeModal').and.callThrough();
    component.ngOnChanges({
      productsInventory: new SimpleChange([], products, false),
    });
    expect(component.openDonationTypeModal).not.toHaveBeenCalled();
    expect(component.addedUnits.length).toEqual(4);
  });

  it('should show an error when there is zero product', () => {
    const products = [
      {
        unitNumber: 'W999912366534',
        productCode: 'WHOLE BLOOD',
        descriptionKey: 'WHOLE BLOOD',
        icon: 'product-whole-blood',
      },
    ];
    spyOn(component.toaster, 'error').and.returnValue(1);
    component.ngOnChanges({
      productsInventory: new SimpleChange([], products, false),
    });
    expect(component.toaster.error).toHaveBeenCalled();
    expect(component.addedUnits.length).toEqual(3);
  });
});
