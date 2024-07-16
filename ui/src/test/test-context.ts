import { DebugElement, Type } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';

export class TestContext<T> {
  constructor(public fixture: ComponentFixture<T>) {}

  get component(): T {
    return this.fixture.componentInstance;
  }

  get el(): HTMLElement {
    return this.fixture.debugElement.nativeElement;
  }

  get dl(): DebugElement {
    return this.fixture.debugElement;
  }

  get instance(): T {
    return this.fixture.componentInstance;
  }

  getElByCss<E>(selector: string): E {
    return this.fixture.debugElement.query(By.css(selector)).nativeElement as E;
  }

  getElByDirective<E, T1>(type: Type<T1>): E {
    return this.fixture.debugElement.query(By.directive(type)).nativeElement as E;
  }

  detectChanges(checkNoChanges?: boolean): void {
    this.fixture.detectChanges(checkNoChanges);
  }

  resolve<T1>(component: Type<T1>): T1 {
    return this.fixture.debugElement.injector.get(component) as T1;
  }
}

export const createTestContext = <T>(component: Type<T>) => {
  return new TestContext<T>(TestBed.createComponent<T>(component));
};
