import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatIconModule } from '@angular/material/icon';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { createTestContext } from '@rsa/testing';
import { addRsaIconsMock } from '../../data/mock/icons.mock';
import { GlobalMessageComponent } from './global-message.component';

describe('GlobalMessageComponent', () => {
  let component: GlobalMessageComponent;
  let fixture: ComponentFixture<GlobalMessageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [GlobalMessageComponent],
      imports: [
        MatIconModule,
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useClass: TranslateFakeLoader,
          },
        }),
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    const testContext = createTestContext<GlobalMessageComponent>(GlobalMessageComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    addRsaIconsMock(testContext);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
