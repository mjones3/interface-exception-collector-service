import {CommonModule} from '@angular/common';
import {Component, ViewChild} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormControl, ReactiveFormsModule} from '@angular/forms';
import {By} from '@angular/platform-browser';
import {MaskRegexDirective} from '../mask-regex/mask-regex.directive';

@Component({
  template: `
    <input type="text" id="input" #el="maskRegex" rsaMaskRegex allowedCharsRegex="[0-9]+"
           [formControl]="formControl">
  `,
})
class MaskRegexDirectiveComponent {
  @ViewChild('el') textEl;
  formControl = new FormControl('100');
}

describe('Directive: MaskRegexDirective', () => {
  let component: MaskRegexDirectiveComponent;
  let fixture: ComponentFixture<MaskRegexDirectiveComponent>;
  let inputEl: HTMLInputElement;
  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [MaskRegexDirectiveComponent, MaskRegexDirective],
      imports: [ReactiveFormsModule, CommonModule],
    });
    fixture = TestBed.createComponent(MaskRegexDirectiveComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    inputEl = fixture.debugElement.query(By.css('#input')).nativeElement;
  });

  it('Should be truthy', () => {
    expect(component).toBeTruthy();
  });

  it('Try to write alpa characters on field with [0-9] regex', () => {
    const event = new KeyboardEvent('keypress', {
      'key': 'a',
      bubbles: true,
      cancelable: true,
    });
    spyOn(event, 'preventDefault').and.callThrough();
    component.textEl.onKeyPressOrPasteOrDropEvent(event);
    fixture.detectChanges();
    expect(event.preventDefault).toHaveBeenCalled();
  });

  it('Try to paste alpa characters on field with [0-9] regex', () => {
    const event = new Event('paste', {});
    // @ts-ignore
    event.clipboardData = {
      getData(_) {
        return 'copy';
      }
    };
    spyOn(event, 'preventDefault').and.callThrough();
    component.textEl.onKeyPressOrPasteOrDropEvent(event);
    fixture.detectChanges();
    expect(event.preventDefault).toHaveBeenCalled();
  });

  it('Try to drag and drop alpa characters on field with [0-9] regex', () => {
    const event = new Event('paste', {});
    // @ts-ignore
    event.dataTransfer = {
      getData(_) {
        return 'transfer';
      }
    };
    spyOn(event, 'preventDefault').and.callThrough();
    component.textEl.onKeyPressOrPasteOrDropEvent(event);
    fixture.detectChanges();
    expect(event.preventDefault).toHaveBeenCalled();
  });
});
