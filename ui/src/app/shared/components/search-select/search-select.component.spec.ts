import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { SearchSelectComponent } from './search-select.component';

describe('SearchSelectComponent', () => {
    let component: SearchSelectComponent;
    let fixture: ComponentFixture<SearchSelectComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                SearchSelectComponent,
                ReactiveFormsModule,
                MatFormFieldModule,
                MatSelectModule,
                NoopAnimationsModule,
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(SearchSelectComponent);
        component = fixture.componentInstance;

        // Initialize the FormGroup and pass it to the component
        component.formGroup = new FormGroup({
            controlForm: new FormControl([]), // Initialize with an empty array
        });
        component.controlName = 'controlForm';
        component.items = [
            { code: 'A12', name: 'Random' },
            { code: 'A13', name: 'Hospital' },
        ];
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
