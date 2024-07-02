import { CommonModule } from '@angular/common';
import {
    AfterViewInit,
    Component,
    EventEmitter,
    Inject,
    Input,
    OnInit,
    Output,
    ViewChild,
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatSelectModule } from '@angular/material/select';
import { FuseScrollbarDirective } from '@fuse/directives/scrollbar';
import { TranslocoDirective, TranslocoService } from '@ngneat/transloco';
import { AutoUnsubscribe } from 'app/shared/decorators/auto-unsubscribe/auto-unsubscribe.decorator';
import { Pageable, pageableDefault } from 'app/shared/models';
import { Paginator } from 'app/shared/utils/paginator';
import { Subscription } from 'rxjs';
import { InputKeyboardComponent } from '../input-keyboard/input-keyboard.component';

export const FILTERABLE_DROPDOWN_LOADER_CONTAINER = 'flex flex-1 py-4 relative';

@Component({
    selector: 'rsa-filterable-drop-down',
    templateUrl: './filterable-drop-down.component.html',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatSelectModule,
        MatListModule,
        TranslocoDirective,
        FuseScrollbarDirective,
        MatIconModule,
        MatDialogModule,
        MatFormFieldModule,
        InputKeyboardComponent,
        MatInputModule
    ],
})
@AutoUnsubscribe()
export class FilterableDropDownComponent implements OnInit, AfterViewInit {
    filteredList: any[] = [];
    filterForm: FormGroup;
    @Input() dialogTitle: string;
    @Input() inputPlaceholder: '';
    @Input() iconName: string;
    @Input() filterableContainerClasses = FILTERABLE_DROPDOWN_LOADER_CONTAINER;
    @Input() pageable: Pageable;
    @Input() paginator: Paginator;
    @Input() closable = true;
    @Input() showKeyboard = false;

    // Pass any object type to be filtered and the object property to display
    @Input() options: any;
    @Input() optionsLabel: string;
    @Output() pageableChange = new EventEmitter<Pageable>();
    @ViewChild('fuseScrollbar', { static: false })
    fuseScrollbar: FuseScrollbarDirective;
    private pageChangeSub: Subscription;
    private valueChangesSub: Subscription;

    constructor(
        @Inject(MAT_DIALOG_DATA) private data: any,
        public dialogRef: MatDialogRef<FilterableDropDownComponent>,
        private formBuilder: FormBuilder,
        private translocoService: TranslocoService
    ) {
        if (data) {
            this.options = data.options ? data.options : [];
            this.optionsLabel = data.optionsLabel;
            this.dialogTitle = data.dialogTitle
                ? data.dialogTitle
                : 'Title goes here';
            this.inputPlaceholder = data.inputPlaceholder
                ? data.inputPlaceholder
                : '';
            this.iconName = data.iconName ? data.iconName : 'hi_outline:x';
            this.pageable = data.pageable || pageableDefault;
            this.paginator = new Paginator(this.pageable);
            this.closable = data.hasOwnProperty('closable')
                ? data.closable
                : this.closable;
        }
    }

    ngOnInit(): void {
        this.filteredList = this.options;
        this.filterForm = this.formBuilder.group({
            filterCriteria: [''],
        });
        this.valueChangesSub = this.filterForm.valueChanges.subscribe(() =>
            this.filterList()
        );
        this.pageChangeSub = this.paginator.pageChange.subscribe(
            this.pageableChange
        );
    }

    ngAfterViewInit(): void {
        if (this.fuseScrollbar.ps) {
            this.fuseScrollbar.ps.element.addEventListener(
                'ps-y-reach-end',
                () => {
                    this.paginator.next();
                }
            );
        }
    }

    filterList(): void {
        console.log('this.filterForm ===', this.filterForm.value)
        this.filteredList = this.options.filter((item) => {
            const itemToFilter = this.optionsLabel
                ? item[this.optionsLabel]
                : item;
            return this.translocoService
                .translate(itemToFilter)
                .toLowerCase()
                .includes(
                    this.filterForm.controls[
                        'filterCriteria'
                    ].value.toLowerCase()
                );
        });
        this.fuseScrollbar.scrollToTop(10);
    }

    selectValue(optionSelected): void {
        this.dialogRef.close(optionSelected);
    }

    updateOptionList(options: any[]): void {
        this.filteredList = [...options];
        this.fuseScrollbar.update();
        this.fuseScrollbar.scrollToBottom(10);
    }
}
