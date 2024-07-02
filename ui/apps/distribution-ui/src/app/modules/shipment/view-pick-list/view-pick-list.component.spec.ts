import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HttpClientModule } from '@angular/common/http';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { TableModule } from 'primeng/table';
import { ViewPickListComponent } from './view-pick-list.component';

describe('ViewPickListComponent', () => {
  let component: ViewPickListComponent;
  let fixture: ComponentFixture<ViewPickListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ViewPickListComponent],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
      imports: [
        TableModule,
        RouterTestingModule,
        HttpClientModule,
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
    fixture = TestBed.createComponent(ViewPickListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
