import { Component, OnDestroy, ViewEncapsulation } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { FuseLoadingBarComponent } from '@fuse/components/loading-bar';
import { ProcessHeaderService } from 'app/shared/services/process-header.service';
import { Subject, filter, take } from 'rxjs';

@Component({
    selector: 'empty-layout',
    templateUrl: './empty.component.html',
    encapsulation: ViewEncapsulation.None,
    standalone: true,
    imports: [FuseLoadingBarComponent, RouterOutlet],
})
export class EmptyLayoutComponent implements OnDestroy {
    private _unsubscribeAll: Subject<any> = new Subject<any>();


    constructor(
        protected router: Router,
        protected activeRoute: ActivatedRoute,
        protected headerService: ProcessHeaderService,
      ) {}
    
      ngOnInit(): void {
        // Initial subtitle load
        this.setSubtitle();
        this.router.events.pipe(filter(event => event instanceof NavigationEnd)).subscribe(() => {
          this.setSubtitle();
        });
      }

    // -----------------------------------------------------------------------------------------------------
    // @ Lifecycle hooks
    // -----------------------------------------------------------------------------------------------------

    /**
     * On destroy
     */
    ngOnDestroy(): void {
        // Unsubscribe from all subscriptions
        this._unsubscribeAll.next(null);
        this._unsubscribeAll.complete();
    }
    setSubtitle(): void {
        // Title from Main Route
        if (this.activeRoute.data) {
          this.activeRoute.data.pipe(take(1)).subscribe(data => {
            this.headerService.setTitle(data && data.title ? data.title : '');
          });
        }
    
        // Title/SubTitle from Child Routes
        if (this.activeRoute.firstChild) {
          this.activeRoute.firstChild.data.pipe(take(1)).subscribe(data => {
            this.headerService.setSubtitle(data && data.subTitle ? data.subTitle : '');
            this.headerService.setMainSubTitle(data && data.mainSubTitle ? data.mainSubTitle : '');
            if (data && data.title) {
              this.headerService.title.next(data.title);
            }
          });
        }
      }
}
