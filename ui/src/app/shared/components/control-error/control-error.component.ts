import { Component, Input } from '@angular/core';
import { MatError } from '@angular/material/form-field';

@Component({
  standalone: true,
  selector: 'rsa-control-error',
  templateUrl: './control-error.component.html',
  styleUrls: ['./control-error.component.scss'],
  imports: [MatError]
})
export class ControlErrorComponent {
  @Input() text;
  @Input() hide = true;
}
