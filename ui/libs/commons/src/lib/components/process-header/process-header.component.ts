import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  Input,
  OnChanges,
  OnDestroy,
  OnInit,
  SimpleChanges,
  TemplateRef,
  ViewEncapsulation,
} from '@angular/core';
import { TreoNavigationService } from '@treo';
import { Subscription } from 'rxjs';

@Component({
  selector: 'rsa-process-header',
  exportAs: 'processHeader',
  templateUrl: './process-header.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None,
})
export class ProcessHeaderComponent implements OnInit, OnChanges, OnDestroy {
  _titleTpl: TemplateRef<any>;
  _title: string | null;
  leftNavigationComponentOpened: boolean;
  leftNavigationSubscription: Subscription;
  readonly sidebarWidth = 280;
  readonly calcContentWidth = `calc(100% - ${this.sidebarWidth}px)`;

  @Input() subTitle;
  @Input() buttons: TemplateRef<any>;
  @Input() mainSubTitle;
  @Input() stickyHeader: false;

  @Input() set title(title: string | TemplateRef<any>) {
    if (title instanceof TemplateRef) {
      this._title = null;
      this._titleTpl = title;
    } else {
      this._title = title;
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.subTitle) {
      this.subTitle = changes.subTitle.currentValue;
    }
  }

  ngOnInit() {
    const leftNavigationComponent = this._treoNavigationService.getComponent('mainNavigation');
    this.leftNavigationComponentOpened = leftNavigationComponent?.opened;

    this.leftNavigationSubscription = leftNavigationComponent?.openedChanged.subscribe(response => {
      this.leftNavigationComponentOpened = response;
      this.cdr.detectChanges();
    });
  }

  constructor(private _treoNavigationService: TreoNavigationService, private cdr: ChangeDetectorRef) {}

  ngOnDestroy() {
    if (this.leftNavigationSubscription) {
      this.leftNavigationSubscription.unsubscribe();
    }
  }
}
