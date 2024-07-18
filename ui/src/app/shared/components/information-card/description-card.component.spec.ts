import { ComponentFixture, TestBed } from '@angular/core/testing';
import { createTestContext } from '../../../../test/test-context';
import { DescriptionCardComponent } from './description-card.component';

describe('DescriptionCardComponent', () => {
  let component: DescriptionCardComponent;
  let fixture: ComponentFixture<DescriptionCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        DescriptionCardComponent,
      ],
    }).compileComponents();
    const testContext = createTestContext<DescriptionCardComponent>(DescriptionCardComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
