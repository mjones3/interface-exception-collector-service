import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { createTestContext } from '@rsa/testing';
import { ProcessHeaderComponent } from './process-header.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('ProcessHeaderComponent', () => {
  let component: ProcessHeaderComponent;
  let fixture: ComponentFixture<ProcessHeaderComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [ProcessHeaderComponent],
        imports: [HttpClientTestingModule,
          TranslateModule.forRoot({
            loader: {
              provide: TranslateLoader,
              useClass: TranslateFakeLoader,
            },
          }),
        ],
      }).compileComponents();
    })
  );

  beforeEach(() => {
    const testContext = createTestContext<ProcessHeaderComponent>(ProcessHeaderComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
