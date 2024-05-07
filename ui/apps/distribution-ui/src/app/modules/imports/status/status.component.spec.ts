import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialogModule } from '@angular/material/dialog';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { ModalTemplateComponent } from '@rsa/commons';
import { createTestContext } from '@rsa/testing';
import { TreoCardModule } from '@treo';
import { ImportStatusModalComponent } from './status.component';
import { MatIconModule } from '@angular/material/icon';

describe('ImportStatusModalComponent', () => {
  let component: ImportStatusModalComponent;
  let fixture: ComponentFixture<ImportStatusModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ImportStatusModalComponent, ModalTemplateComponent],
      imports: [
        TreoCardModule,
        MatProgressBarModule,
        MatDialogModule,
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
    const testContext = createTestContext<ImportStatusModalComponent>(ImportStatusModalComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
