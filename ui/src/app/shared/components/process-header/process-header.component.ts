import { CommonModule } from '@angular/common';
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
import { FuseNavigationService, FuseVerticalNavigationComponent } from '@fuse/components/navigation';
import { Subscription } from 'rxjs';

@Component({
  selector: 'rsa-process-header',
  standalone: true,
  exportAs: 'processHeader',
  templateUrl: './process-header.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None,
  imports: [CommonModule]
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

  constructor(private _fuseNavigationService: FuseNavigationService, private cdr: ChangeDetectorRef) {}


  ngOnChanges(changes: SimpleChanges) {
    if (changes.subTitle) {
      this.subTitle = changes.subTitle.currentValue;
    }
  }

  ngOnInit() {
    const leftNavigationComponent = this._fuseNavigationService.getComponent<FuseVerticalNavigationComponent>('mainNavigation');
    this.leftNavigationComponentOpened = leftNavigationComponent?.opened;

    this.leftNavigationSubscription = leftNavigationComponent?.openedChanged.subscribe(response => {
      this.leftNavigationComponentOpened = response;
      this.cdr.detectChanges();
    });
  }

  ngOnDestroy() {
    if (this.leftNavigationSubscription) {
      this.leftNavigationSubscription.unsubscribe();
    }
  }
}
