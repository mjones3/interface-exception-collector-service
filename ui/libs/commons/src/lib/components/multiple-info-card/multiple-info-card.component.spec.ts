import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { MaterialModule } from '@rsa/material';
import { createTestContext } from '@rsa/testing';
import { TreoCardModule } from '@treo';
import { MultipleInfoCardComponent } from './multiple-info-card.component';
import { addRsaIconsMock } from '../../data/mock/icons.mock';

describe('MultipleInfoCardComponent', () => {
  let component: MultipleInfoCardComponent;
  let fixture: ComponentFixture<MultipleInfoCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [MultipleInfoCardComponent],
      imports: [
        MatDialogModule,
        MatIconModule,
        TreoCardModule,
        MaterialModule,
        BrowserAnimationsModule,
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
    const testContext = createTestContext<MultipleInfoCardComponent>(MultipleInfoCardComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    addRsaIconsMock(testContext);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
