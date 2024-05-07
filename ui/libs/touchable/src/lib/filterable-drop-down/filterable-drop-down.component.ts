import {
  AfterViewInit,
  Component,
  EventEmitter,
  Inject,
  Input,
  OnDestroy,
  OnInit,
  Output,
  ViewChild,
} from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { TranslateService } from '@ngx-translate/core';
import { AutoUnsubscribe, Pageable, pageableDefault, Paginator } from '@rsa/commons';
import { TreoScrollbarDirective } from '@treo';
import { Subscription } from 'rxjs';

export const FILTERABLE_DROPDOWN_LOADER_CONTAINER = 'flex flex-1 py-4 relative';

@Component({
  selector: 'rsa-filterable-drop-down',
  exportAs: 'rsaFilterableDropdown',
  templateUrl: './filterable-drop-down.component.html',
  styleUrls: ['./filterable-drop-down.component.scss'],
})
@AutoUnsubscribe()
export class FilterableDropDownComponent implements OnInit, AfterViewInit, OnDestroy {
  filteredList: any[] = [];
  filterForm: FormGroup;

  private pageChangeSub: Subscription;
  private valueChangesSub: Subscription;

  @Input() dialogTitle: string;
  @Input() inputPlaceholder: string;
  @Input() iconName: string;
  @Input() filterableContainerClasses = FILTERABLE_DROPDOWN_LOADER_CONTAINER;
  @Input() pageable: Pageable;
  @Input() paginator: Paginator;
  @Input() closable = true;

  // Pass any object type to be filtered and the object property to display
  @Input() options: any;
  @Input() optionsLabel: string;

  @Output() pageableChange = new EventEmitter<Pageable>();

  @ViewChild('treoScrollbar', { static: false }) treoScrollbar: TreoScrollbarDirective;

  constructor(
    @Inject(MAT_DIALOG_DATA) private data: any,
    public dialogRef: MatDialogRef<FilterableDropDownComponent>,
    private formBuilder: FormBuilder,
    private translateService: TranslateService
  ) {
    if (data) {
      this.options = data.options ? data.options : [];
      this.optionsLabel = data.optionsLabel;
      this.dialogTitle = data.dialogTitle ? data.dialogTitle : 'Title goes here';
      this.inputPlaceholder = data.inputPlaceholder ? data.inputPlaceholder : null;
      this.iconName = data.iconName ? data.iconName : 'search';
      this.pageable = data.pageable || pageableDefault;
      this.paginator = new Paginator(this.pageable);
      this.closable = data.hasOwnProperty('closable') ? data.closable : this.closable;
    }
  }

  updateOptionList(options: any[]): void {
    this.filteredList = [...options];
    this.treoScrollbar.update();
    this.treoScrollbar.scrollToBottom(10);
  }

  ngOnInit(): void {
    this.filteredList = this.options;
    this.filterForm = this.formBuilder.group({
      filterCriteria: [''],
    });
    this.valueChangesSub = this.filterForm.valueChanges.subscribe(() => this.filterList());
    this.pageChangeSub = this.paginator.pageChange.subscribe(this.pageableChange);
  }

  filterList(): void {
    this.filteredList = this.options.filter(item => {
      const itemToFilter = this.optionsLabel ? item[this.optionsLabel] : item;
      return this.translateService
        .instant(itemToFilter)
        .toLowerCase()
        .includes(this.filterForm.controls['filterCriteria'].value.toLowerCase());
    });
    this.treoScrollbar.scrollToTop(10);
  }

  selectValue(optionSelected): void {
    this.dialogRef.close(optionSelected);
  }

  ngAfterViewInit(): void {
    if (this.treoScrollbar.ps) {
      this.treoScrollbar.ps.element.addEventListener('ps-y-reach-end', () => {
        this.paginator.next();
      });
    }
  }

  ngOnDestroy(): void {
    if (this.treoScrollbar.ps && this.treoScrollbar.ps.element) {
      this.treoScrollbar.ps.element.removeEventListener('ps-y-reach-end');
    }
  }
}
