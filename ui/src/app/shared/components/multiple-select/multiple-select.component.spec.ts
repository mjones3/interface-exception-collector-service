import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { MultipleSelectComponent } from './multiple-select.component';

describe('MultipleSelectComponent', () => {
    let component: MultipleSelectComponent;
    let fixture: ComponentFixture<MultipleSelectComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                ReactiveFormsModule,
                MatFormFieldModule,
                MatSelectModule,
                NoopAnimationsModule,
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(MultipleSelectComponent);
        component = fixture.componentInstance;

        // Initialize the FormGroup and pass it to the component
        component.formGroup = new FormGroup({
            itemSelection: new FormControl([]), // Initialize with an empty array for multi-select
        });
        component.formControlName = 'itemSelection';
        component.items = [
            { optionKey: 'pending', optionDescription: 'Pending' },
            { optionKey: 'shipped', optionDescription: 'Shipped' },
            { optionKey: 'delivered', optionDescription: 'Delivered' },
        ];

        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
