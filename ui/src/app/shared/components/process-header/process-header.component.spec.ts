import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FuseNavigationService } from '@fuse/components/navigation';
import { ProcessHeaderComponent } from './process-header.component';

describe('ProcessHeaderComponent', () => {
  let component: ProcessHeaderComponent;
  let fixture: ComponentFixture<ProcessHeaderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProcessHeaderComponent],
      providers: [FuseNavigationService]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(ProcessHeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
