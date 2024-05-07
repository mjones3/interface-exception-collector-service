import { Component, ElementRef, ViewChild } from '@angular/core';
import { TestBed, waitForAsync } from '@angular/core/testing';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { createTestContext, TestContext } from '@rsa/testing';
import { COMMONS_PIPES } from './index';
import { TranslateInterpolationPipe } from './translate-interpolation.pipe';

@Component({
  template: `
    <div #divElement>{{'Interpolation <%= key %> example' | translateInterpolation: interpolationObject}}</div>`
})
class ComponentTestWrapper {
  interpolationObject = {key: 'value'};
  @ViewChild('divElement', {read: ElementRef}) divElement: ElementRef<HTMLDivElement>;
}

describe('ValidationPipe', () => {
  let testContext: TestContext<ComponentTestWrapper>;
  let component: ComponentTestWrapper;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ComponentTestWrapper, ...COMMONS_PIPES],
      imports: [TranslateModule.forRoot({
        loader: {
          provide: TranslateLoader,
          useClass: TranslateFakeLoader
        }
      })],
      providers: [...COMMONS_PIPES]
    }).compileComponents();
    testContext = createTestContext<ComponentTestWrapper>(ComponentTestWrapper);
    component = testContext.component;
    testContext.fixture.detectChanges();
  }));

  it('create an instance', () => {
    const pipe = TestBed.inject(TranslateInterpolationPipe);
    expect(pipe).toBeTruthy();
    const a = component.divElement;
    expect(component.divElement.nativeElement.innerHTML).toEqual('Interpolation value example');
  });
});
