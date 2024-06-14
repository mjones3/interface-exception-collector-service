import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewPackingListComponent } from './view-packing-list.component';

describe('ViewPackingListComponent', () => {
  let component: ViewPackingListComponent;
  let fixture: ComponentFixture<ViewPackingListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewPackingListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewPackingListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
