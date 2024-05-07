import {Directive, forwardRef, Inject, ViewContainerRef} from '@angular/core';

@Directive({
  selector: '[rsaControlErrorContainer]'
})
export class ControlErrorContainerDirective {
  constructor(@Inject(forwardRef(() => ViewContainerRef)) public vcr: ViewContainerRef) {}
}
