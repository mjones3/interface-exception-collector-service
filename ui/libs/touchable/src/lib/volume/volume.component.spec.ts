import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { EnvironmentConfigService, RsaCommonsModule } from '@rsa/commons';
import { MaterialModule } from '@rsa/material';
import { createTestContext } from '@rsa/testing';
import { InputKeyboardComponent, VolumeComponent } from '@rsa/touchable';
import { TreoCardModule } from '@treo';
import { addTestingIconsMock } from '../../shared/testing/mocks/data/icons.mock';
import { envConfigFactoryMock } from '../../shared/testing/mocks/env-config.mock';

describe('VolumeComponent', () => {
  let component: VolumeComponent;
  let fixture: ComponentFixture<VolumeComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [VolumeComponent, InputKeyboardComponent],
      imports: [
        NoopAnimationsModule,
        ReactiveFormsModule,
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
      providers: [
        {
          provide: EnvironmentConfigService,
          useFactory: envConfigFactoryMock,
        },
      ],
    });
    const testContext = createTestContext<VolumeComponent>(VolumeComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    addTestingIconsMock(testContext);
    fixture.detectChanges();
  });

  it('should create', () => {
    pending('Force skip');
    expect(component).toBeTruthy();
  });

  it('On submit volume empty value', () => {
    pending('Force skip');
    spyOn(component.calculateVolumeFormSubmitted, 'emit');
    component.form.patchValue({ volume: '' });
    component.calculateVolume();
    expect(component.calculateVolumeFormSubmitted.emit).not.toHaveBeenCalled();
  });

  it('On submit volume incorrect value', () => {
    pending('Force skip');
    spyOn(component.calculateVolumeFormSubmitted, 'emit');
    component.form.patchValue({ volume: 'volume' });
    component.calculateVolume();
    expect(component.calculateVolumeFormSubmitted.emit).not.toHaveBeenCalled();
  });

  it('On submit weight correct value', done => {
    pending('Force skip');
    spyOn(component.calculateVolumeFormSubmitted, 'emit').and.callThrough();
    component.calculateVolumeFormSubmitted.subscribe(volume => {
      expect(volume.weight).toEqual(100);
      done();
    });
    component.form.patchValue({ weight: 100 });
    component.calculateVolume();
    expect(component.calculateVolumeFormSubmitted.emit).toHaveBeenCalled();
  });
});
