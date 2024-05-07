import {Component} from '@angular/core';
import { TestBed, waitForAsync } from '@angular/core/testing';
import {TranslateFakeLoader, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {RsaCommonsModule, ValidationType} from '@rsa/commons';
import {createTestContext, TestContext} from '@rsa/testing';
import {ValidationPipe} from './validation.pipe';

@Component({
  template: `{{'discard-reason.label' | validation: validationType.REQUIRED}}`
})
class ComponentTestWrapper {
  validationType = ValidationType;
}

describe('ValidationPipe', () => {
  let testContext: TestContext<ComponentTestWrapper>;
  let component: ComponentTestWrapper;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ComponentTestWrapper],
      imports: [RsaCommonsModule, TranslateModule.forRoot({
        loader: {
          provide: TranslateLoader,
          useClass: TranslateFakeLoader
        }
      })]
    }).compileComponents();
    testContext = createTestContext<ComponentTestWrapper>(ComponentTestWrapper);
    component = testContext.component;
    testContext.fixture.detectChanges();
  }));

  it('create an instance', () => {
    expect(component).toBeTruthy();
  });
});
