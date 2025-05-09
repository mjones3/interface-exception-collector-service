import { Component, input, Input } from '@angular/core';
import { WidgetComponent } from '@shared';
import { LoadingSpinnerComponent } from 'app/shared/components/loading-spinner/loading-spinner.component';

@Component({
  selector: 'biopro-unsuitable-unit-report',
  standalone: true,
  imports: [WidgetComponent, LoadingSpinnerComponent],
  templateUrl: './unsuitable-unit-report.component.html'
})
export class UnsuitableUnitReportComponent {
  loading = input<boolean>(false);
  readonly loaderMessage = 'Unacceptable Products Report is in progress';

}
