import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { UnsuitableUnitReportComponent } from './unsuitable-unit-report.component';
import { WidgetComponent } from '@shared';
import { LoadingSpinnerComponent } from 'app/shared/components/loading-spinner/loading-spinner.component';
import { Component } from '@angular/core';

// Mock the WidgetComponent
@Component({
  selector: 'rsa-common-widget',
  template: '<ng-content></ng-content><ng-content></ng-content>',
  standalone: true
})
class MockWidgetComponent {
  templateRef: any;
  title: string;
}

// Mock the LoadingSpinnerComponent
@Component({
  selector: 'biopro-loading-spinner',
  template: '<div class="mock-spinner">{{textLabel}}</div>',
  standalone: true
})
class MockLoadingSpinnerComponent {
  textLabel: string;
}

describe('UnsuitableUnitReportComponent', () => {
  let component: UnsuitableUnitReportComponent;
  let fixture: ComponentFixture<UnsuitableUnitReportComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UnsuitableUnitReportComponent, MockWidgetComponent, MockLoadingSpinnerComponent]
    })
    .overrideComponent(UnsuitableUnitReportComponent, {
      remove: { imports: [WidgetComponent, LoadingSpinnerComponent] },
      add: { imports: [MockWidgetComponent, MockLoadingSpinnerComponent] }
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(UnsuitableUnitReportComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have loading input with default value of false', () => {
    expect(component.loading()).toBe(false);
  });

  it('should have the correct loader message', () => {
    expect(component.loaderMessage).toBe('Unacceptable Products Report is in progress');
  });

  it('should not show loading spinner when loading is false', () => {
    fixture.detectChanges();
    const spinnerElement = fixture.debugElement.query(By.css('biopro-loading-spinner'));
    expect(spinnerElement).toBeFalsy();
  });

  it('should use the correct widget title', () => {
    const widgetElement = fixture.debugElement.query(By.css('rsa-common-widget'));
    expect(widgetElement.properties['title']).toBe('Unsuitable Unit Report');
  });

  it('should pass a template reference to the widget component', () => {
    const widgetElement = fixture.debugElement.query(By.css('rsa-common-widget'));
    expect(widgetElement.properties['templateRef']).toBeDefined();
  });
});
