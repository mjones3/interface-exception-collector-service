import { Directive, ElementRef, Optional, Renderer2 } from '@angular/core';
import { ControlContainer } from '@angular/forms';
import { fromEvent } from 'rxjs';
import { shareReplay, tap } from 'rxjs/operators';

@Directive({
  selector: '[rsaFormSubmit]'
})
export class FormSubmitDirective {
  submit$ = fromEvent(this.element, 'submit')
    .pipe(
      tap(() => {
        if (this.element.classList && this.element.classList.contains('submitted') === false &&
          this.controlContainer.valid) {
          this.renderer2.addClass(this.element, 'submitted');
        }
      }),
      shareReplay(1)
    );

  constructor(private host: ElementRef<HTMLFormElement>, private renderer2: Renderer2,
              @Optional() private controlContainer: ControlContainer) { }

  get element() {
    return this.host.nativeElement;
  }
}
