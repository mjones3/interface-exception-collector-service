import { isPlatformBrowser } from '@angular/common';
import {
    AfterViewInit,
    Directive,
    ElementRef,
    EventEmitter,
    forwardRef,
    Inject,
    Input,
    OnChanges,
    Output,
    PLATFORM_ID,
    SimpleChanges,
} from '@angular/core';

@Directive({
    selector: '[bioproAutoFocusIf]',
    standalone: true,
    host: {
        '(blur)': 'onBlur($event)',
    },
})
export class AutoFocusIfDirective implements AfterViewInit, OnChanges {
    @Input() bioproAutoFocusIf;
    @Output() bioproAutoFocusIfChange = new EventEmitter<boolean>();

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

    onBlur() {
        this.bioproAutoFocusIfChange.emit(false);
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes && changes.bioproAutoFocusIf) {
            this.focusElement(changes.bioproAutoFocusIf.currentValue);
        }
    }

    ngAfterViewInit(): void {
        if (!isPlatformBrowser(this.platformId)) {
            return;
        }
        setTimeout(() => this.focusElement(this.bioproAutoFocusIf));
    }
}
