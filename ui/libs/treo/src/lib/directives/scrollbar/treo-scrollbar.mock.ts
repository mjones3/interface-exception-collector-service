import { Directive, Input } from '@angular/core';

@Directive({
  selector: '[treoScrollbar]',
  exportAs: 'treoScrollbar'
})
export class TreoScrollbarMock {
  @Input() treoScrollbarOptions(value: any) {}
}
