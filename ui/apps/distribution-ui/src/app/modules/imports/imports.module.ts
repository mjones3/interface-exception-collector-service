import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MomentDateAdapter } from '@angular/material-moment-adapter';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE } from '@angular/material/core';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { TransitTimeModule } from '@rsa/distribution/shared/components/transit-time/transit-time.module';
import { SharedModule } from '@rsa/distribution/shared/shared.module';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { ImportsRoutingModule } from './imports-routing.module';
import { ImportsComponent } from './imports.component';
import { PatientSearchComponent } from './patient-search/patient-search.component';
import { ImportStatusModalComponent } from './status/status.component';

@NgModule({
  declarations: [ImportsComponent, PatientSearchComponent, ImportStatusModalComponent],
  imports: [
    SharedModule,
    MatProgressBarModule,
    TableModule,
    ButtonModule,
    CommonModule,
    TransitTimeModule,
    ImportsRoutingModule,
  ],
  providers: [
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
export class ImportsModule {}
