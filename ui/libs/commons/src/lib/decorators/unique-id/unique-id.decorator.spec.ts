import {Component} from '@angular/core';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import {UniqueId} from './unique-id.decorator';

// Test Wrapper Component
@Component({
  template: ``,
})
class UniqueIdDecoratorComponent {
  @UniqueId() uniqueId;
  @UniqueId('prefix-') prefixUniqueId;
}

describe('AutoUnsubscribeDecorator', () => {
  let component: UniqueIdDecoratorComponent;
  let fixture: ComponentFixture<UniqueIdDecoratorComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [UniqueIdDecoratorComponent]
    });
    fixture = TestBed.createComponent(UniqueIdDecoratorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }));

  it('uniqueId and prefixUniqueId should get unique ids', () => {
    const prefix = 'prefix-';
    expect(component.uniqueId).toBeTruthy();
    expect(component.uniqueId.length).toBeGreaterThan(0);
    expect(component.prefixUniqueId.toString().startsWith(prefix)).toEqual(true);
  });
});
