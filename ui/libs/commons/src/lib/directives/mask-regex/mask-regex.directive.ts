import {Directive, ElementRef, forwardRef, HostListener, Inject, Input, OnInit} from '@angular/core';
import {NgControl} from '@angular/forms';
import {isNil} from 'lodash';

@Directive({
  selector: '[rsaMaskRegex]',
  exportAs: 'maskRegex'
})
export class MaskRegexDirective implements OnInit {
  /**
   * Allowed Characters Regex (Not allowed complex regex the purpose of this directive is prevent the user write some
   * characters in a specific scenarios e.g. [0-9]{8,20} will not work because when you write the first char is not
   * matching the regex and not allowing write this char even when is correct)
   * Dont use ^ and $ characters to start and end regex if you want to use in paste event to get the characters allowed by
   * the regex
   * Examples of valid regex:
   *
   * @example [0-9]+ -> Only numbers
   * @example [a-zA-Z]+ -> Only letters
   * @example [0-9a-zA-Z]+ -> Only Letters and Numbers
   */
  @Input() allowedCharsRegex = '*';
  @Input() maxlength: number;
  readonly flags = 'g';
  private regExpression: RegExp;

  constructor(private controlDir: NgControl, @Inject(forwardRef(() => ElementRef)) private el: ElementRef<HTMLInputElement>) {
  }

  ngOnInit(): void {
    this.regExpression = new RegExp(this.allowedCharsRegex, this.flags);
  }

  @HostListener('keypress', ['$event'])
  @HostListener('paste', ['$event'])
  @HostListener('drop', ['$event'])
  onKeyPressOrPasteOrDropEvent(event) {
    this.validateMaskRegex(event);
  }

  /**
   * Validate a keypress or paste or with a mask regex
   * @param event Keypress, Paste or Drop event
   */
  validateMaskRegex(event) {
    if ('clipboardData' in event) {
      // Copy and Paste event
      const clipboardData = (event.clipboardData) ? event.clipboardData.getData('text/plain') : '';
      this.validateDropAndClipboardEvent(clipboardData, event);
    } else if ('dataTransfer' in event) {
      // Drag and Drop over the field
      const data = event.dataTransfer.getData('Text');
      this.validateDropAndClipboardEvent(data, event);
    } else if (event instanceof KeyboardEvent) {
      if (!this.isCtrlVKeyboardEvent(event)) {
        // Keypress event
        this.regExpression.lastIndex = 0;
        this.validateText(event.key, event);
      } else {
        event.preventDefault();
      }
    }
  }

  isCtrlVKeyboardEvent(event: KeyboardEvent) {
    return event.ctrlKey && event.code === 'KeyV';
  }

  /**
   * Validate Drag and Drop over field and clipboard events
   * @param data Date to validate
   * @param event Drop or Clipboard event
   */
  private validateDropAndClipboardEvent(data, event) {
    const cleanText = this.getCleanText(data);
    const textValue = this.getTextValue(cleanText);
    if (cleanText && textValue !== this.control.value && this.isValidText(textValue)) {
      this.control.setValue(textValue);
    }
    event.preventDefault();
  }

  get control() {
    return this.controlDir.control;
  }

  /**
   * Get text value on paste event or keypress
   */
  getTextValue(textValue) {
    const nativeEl = this.el.nativeElement;
    let finalText = this.control.value ? this.control.value.toString() : '';
    // Selection start
    const startPos = (!isNil(nativeEl.selectionStart)) ? nativeEl.selectionStart : null;
    const endPos = (!isNil(nativeEl.selectionEnd)) ? nativeEl.selectionEnd : null;
    // Change text
    if (this.maxlength && textValue.length > this.maxlength) {
      // Check text selected
      const splitLength = (!isNil(startPos) && !isNil(endPos) && (endPos - startPos >= 0)) ? (endPos - startPos)
        : this.maxlength;
      textValue = textValue.substr(0, splitLength);
    }
    if ((!isNil(startPos) && (!isNil(startPos) && !isNil(endPos) && (endPos - startPos >= 0))) ||
      finalText.length < this.maxlength) {
      const startText = finalText.substr(0, startPos);
      let endText = '';
      if (finalText.length > endPos) {
        endText = finalText.substr(-(finalText.length - endPos));
      } else {
        endText = finalText.substr(0, finalText.length - endPos);
      }
      finalText = startText + textValue + endText;
    }
    // Change final text
    if (this.maxlength && finalText.length > this.maxlength) {
      finalText = finalText.substr(0, this.maxlength);
    }
    return finalText;
  }

  private isValidText(strValidate: string) {
    return this.regExpression.test(strValidate) && this.verifyLength(this.control.value);
  }

  private validateText(strValidate: string, event) {
    if (!this.isValidText(strValidate)) {
      event.preventDefault();
    }
  }

  getCleanText(text: string) {
    if (!this.allowedCharsRegex) {
      return text;
    }
    const textMatch = text.match(new RegExp(this.allowedCharsRegex, this.flags));
    return (textMatch) ? textMatch.reduce((accumulator, value) => accumulator + value, '') : '';
  }

  /**
   * Check length text
   * @param value Text value
   */
  verifyLength(value) {
    const maxLength = this.maxlength || 0;
    const length = value ? value.toString().length : 0;
    return length < maxLength || maxLength === 0;
  }
}
