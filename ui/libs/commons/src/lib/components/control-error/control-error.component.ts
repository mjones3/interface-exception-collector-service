import { Component, Input } from '@angular/core';

@Component({
  selector: 'rsa-control-error',
  templateUrl: './control-error.component.html',
  styleUrls: ['./control-error.component.scss'],
})
export class ControlErrorComponent {
  @Input() text;
  @Input() hide = true;
}
