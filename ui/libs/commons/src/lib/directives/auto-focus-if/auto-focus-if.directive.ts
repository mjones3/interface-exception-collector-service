import { isPlatformBrowser } from '@angular/common';
import {
  AfterViewInit,
  Directive,
  ElementRef,
  EventEmitter,
  forwardRef,
  HostListener,
  Inject,
  Input,
  OnChanges,
  Output,
  PLATFORM_ID,
  Renderer2,
  SimpleChanges,
} from '@angular/core';

@Directive({
  selector: '[rsaAutoFocusIf]',
})
export class AutoFocusIfDirective implements AfterViewInit, OnChanges {
  @Input() rsaAutoFocusIf;
  @Output() rsaAutoFocusIfChange = new EventEmitter<boolean>();

  constructor(
    @Inject(forwardRef(() => ElementRef)) private el: ElementRef,
    @Inject(PLATFORM_ID) private platformId: string
  ) {}

  focusElement(value) {
    const autoFocusIfEval = Boolean(value);
    if ((value && autoFocusIfEval) || value === undefined) {
      const el = this.el.nativeElement;
      if (el.selectionStart || el.selectionStart === '0') {
        el.selectionStart = el.value.length;
        el.selectionEnd = el.value.length;
      }
      el.focus();
    }
  }

  @HostListener('blur')
  onBlur() {
    this.rsaAutoFocusIfChange.emit(false);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes && changes.rsaAutoFocusIf) {
      this.focusElement(changes.rsaAutoFocusIf.currentValue);
    }
  }

  ngAfterViewInit(): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }
    setTimeout(() => this.focusElement(this.rsaAutoFocusIf));
  }
}
