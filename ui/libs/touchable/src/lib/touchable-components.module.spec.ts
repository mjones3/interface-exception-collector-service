import { TestBed, waitForAsync } from '@angular/core/testing';
import { TouchableComponentsModule } from './touchable-components.module';

describe('TouchableComponentsModule', () => {
  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [TouchableComponentsModule],
    }).compileComponents();
  }));

  // TODO: Add real tests here.
  //
  // NB: This particular test does not do anything useful.
  //     It does NOT check for correct instantiation of the module.
  it('should have a module definition', () => {
    expect(TouchableComponentsModule).toBeDefined();
  });
});
