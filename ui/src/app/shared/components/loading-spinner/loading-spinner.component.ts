import { AsyncPipe } from '@angular/common';
import { Component, input } from '@angular/core';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';

@Component({
  selector: 'biopro-loading-spinner',
  standalone: true,
  imports: [],
  templateUrl: './loading-spinner.component.html'
})
export class LoadingSpinnerComponent {
  textLabel = input<string>();
}
