import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { RsaCommonsModule } from '@rsa/commons';
import { createTestContext, TestContext } from '@rsa/testing';
import { SanitizerPipe } from './sanitizer.pipe';

@Component({ template: `{{ 'POST_LEUKO_VOLUME' | sanitizer }}` })
class ComponentTestWrapper {}

describe('Sanitizer', () => {
  let testContext: TestContext<ComponentTestWrapper>;
  let component: ComponentTestWrapper;
  let pipe: SanitizerPipe;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ComponentTestWrapper],
      imports: [RsaCommonsModule],
    }).compileComponents();

    testContext = createTestContext<ComponentTestWrapper>(ComponentTestWrapper);
    component = testContext.component;
    pipe = new SanitizerPipe();
    testContext.fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('Should sanitize the string', () => {
    const sanitizedValue = pipe.transform('POST_LEUKO_REDUCTION', null);
    expect(sanitizedValue).toStrictEqual('Post leuko reduction');
  });

  it('Should return the value the way as it is', () => {
    const value = 'date.label';
    const sanitizedValue = pipe.transform(value, null);
    expect(sanitizedValue).toStrictEqual(value);
  });
});
