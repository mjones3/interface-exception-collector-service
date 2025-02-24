import { Component } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { UppercaseDirective } from './uppercase.directive';

@Component({
    template: ` <input type="text" appUppercase />`,
})
class TestComponent {}

describe('UppercaseDirective', () => {
    let fixture: ComponentFixture<TestComponent>;
    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [TestComponent],
            imports: [UppercaseDirective],
        });
        fixture = TestBed.createComponent(TestComponent);
        fixture.detectChanges();
    });

    it('should create an instance', () => {
        const el = fixture.debugElement.query(By.css('input')).nativeElement;
        el.value = 'bio';
        el.dispatchEvent(new Event('input'));
        fixture.detectChanges();
        const directive = new UppercaseDirective(el);
        expect(directive).toBeTruthy();
        expect(el.value).toBe('BIO');
    });
});
