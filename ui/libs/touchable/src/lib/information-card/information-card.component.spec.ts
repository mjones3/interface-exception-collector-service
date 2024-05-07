import { SimpleChange } from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { MaterialModule } from '@rsa/material';
import { createTestContext } from '@rsa/testing';
import { ThemeModule } from '@rsa/theme';
import { TreoCardModule } from '@treo';
import { addTestingIconsMock } from '../../shared/testing/mocks/data/icons.mock';
import { InformationCardComponent } from './information-card.component';

describe('InformationCardComponent', () => {
  let component: InformationCardComponent;
  let fixture: ComponentFixture<InformationCardComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [InformationCardComponent],
      imports: [MaterialModule, NoopAnimationsModule, ThemeModule, TreoCardModule,
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useClass: TranslateFakeLoader
          }
        })]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    const testContext = createTestContext<InformationCardComponent>(InformationCardComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    component.descriptions = [{
      label: 'Unit Number',
      value: ''
    }];
    addTestingIconsMock(testContext);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('create descriptions with 2 rows', () => {
    component.maxRows = 2;
    component.descriptions = [{
      label: 'Unit Number',
      value: ''
    }, {
      label: 'Blood Type',
      value: ''
    }, {
      label: 'Draw Date',
      value: ''
    }, {
      label: 'Draw Time',
      value: ''
    }];
    component.ngOnChanges({
      maxRows: new SimpleChange(4, 2, false),
      descriptions: new SimpleChange([{
        label: 'Unit Number',
        value: ''
      }], component.descriptions, false)
    });
    fixture.detectChanges();
    expect(component.descriptionsGroup.length).toEqual(2);
  });
});
