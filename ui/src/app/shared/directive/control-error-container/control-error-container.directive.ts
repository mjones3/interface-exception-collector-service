import { Directive, forwardRef, Inject, ViewContainerRef } from '@angular/core';

@Directive({
  standalone: true,
  selector: '[rsaControlErrorContainer]'
})
export class ControlErrorContainerDirective {
  constructor(@Inject(forwardRef(() => ViewContainerRef)) public vcr: ViewContainerRef) {}
}
