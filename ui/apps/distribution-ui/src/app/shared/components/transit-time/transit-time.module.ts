import { DecimalPipe } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MomentDateAdapter } from '@angular/material-moment-adapter';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE } from '@angular/material/core';
import { TranslateModule } from '@ngx-translate/core';
import { RsaCommonsModule } from '@rsa/commons';
import { MaterialModule } from '@rsa/material';
import { TransitTimeComponent } from './transit-time.component';

@NgModule({
  declarations: [TransitTimeComponent],
  imports: [RsaCommonsModule, ReactiveFormsModule, MaterialModule, TranslateModule],
  exports: [TransitTimeComponent],
  providers: [
    DecimalPipe,
    { provide: DateAdapter, useClass: MomentDateAdapter, deps: [MAT_DATE_LOCALE] },
    {
      provide: MAT_DATE_FORMATS,
      useValue: {
        parse: {
          dateInput: 'L',
        },
        display: {
          dateInput: 'L',
          monthYearLabel: 'MMM YYYY',
        },
      },
    },
  ],
})
export class TransitTimeModule {}
