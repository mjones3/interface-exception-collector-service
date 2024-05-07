import { TestBed, waitForAsync } from '@angular/core/testing';
import {
  TreoAnimations,
  TreoConfigService,
  TreoMediaWatcherService,
  TreoMockApiService,
  TreoMockApiUtils,
  TreoModule
} from '@treo';

describe('TreoModule', () => {
  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [TreoModule]
    }).compileComponents();
  }));

  // TODO: Add real tests here.
  //
  // NB: This particular test does not do anything useful.
  //     It does NOT check for correct instantiation of the module.
  it('should have a module definition', () => {
    expect(TreoModule).toBeDefined();
  });
});

describe('@treo/animations', () => {
  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [TreoModule]
    }).compileComponents();
  }));

  it('CONST should exist', () => {
    expect(TreoAnimations).toBeDefined();
  });
});

describe('@treo/service/config', () => {
  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [TreoModule]
    }).compileComponents();
  }));

  it('CONST should exist', () => {
    expect(TreoConfigService).toBeDefined();
  });
});

describe('@treo/service/media-watcher', () => {
  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [TreoModule]
    }).compileComponents();
  }));

  it('CONST should exist', () => {
    expect(TreoMediaWatcherService).toBeDefined();
  });
});

describe('@treo/lib/mock-api', () => {
  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [TreoModule]
    }).compileComponents();
  }));

  it('CONST should exist', () => {
    expect(TreoMockApiService).toBeDefined();
  });

  it('CONST should exist', () => {
    expect(TreoMockApiUtils).toBeDefined();
  });
});
