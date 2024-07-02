import { AsyncPipe, CommonModule, DatePipe, NgIf } from '@angular/common';
import {
    AfterViewInit,
    Component,
    ElementRef,
    NgZone,
    OnDestroy,
    OnInit,
    Renderer2,
    ViewChild,
    ViewEncapsulation
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { FuseFullscreenComponent } from '@fuse/components/fullscreen';
import { FuseLoadingBarComponent } from '@fuse/components/loading-bar';
import {
    FuseNavigationItem,
    FuseNavigationService,
    FuseVerticalNavigationComponent,
} from '@fuse/components/navigation';
import { FuseMediaWatcherService } from '@fuse/services/media-watcher';
import { FilterableDropDownComponent } from 'app/shared/components/filterable-drop-down/filterable-drop-down.component';
import { ProcessProductVersionModel } from 'app/shared/models/process-product-version.model';
import { FacilityService, MenuService } from 'app/shared/services';
import { EnvironmentConfigService } from 'app/shared/services/environment-config.service';
import { getLocalTimeZone } from 'app/shared/utils/utils';
import { Subject, takeUntil } from 'rxjs';

@Component({
    selector: 'classic-layout',
    templateUrl: './classic.component.html',
    encapsulation: ViewEncapsulation.None,
    styleUrls: ['./classic.component.scss'],
    standalone: true,
    imports: [
        FuseLoadingBarComponent,
        FuseVerticalNavigationComponent,
        FuseFullscreenComponent,
        RouterOutlet,
        RouterLink,
        MatButtonModule,
        MatIconModule,
        MatTooltipModule,
        AsyncPipe,
        DatePipe,
        NgIf,
        CommonModule
    ],
})
export class ClassicLayoutComponent
    implements OnInit, AfterViewInit, OnDestroy
{
    readonly sidebarWidth = 280;
    readonly calcContentWidth = `calc(100% - ${this.sidebarWidth}px)`;    
    @ViewChild('footerDateTime', { static: false })
    footerDateTime: ElementRef<HTMLDivElement>;

    moduleTitle: string;
    defaultLogo: string;
    releaseNumber: string;
    investigationalDevice: string;
    processProductVersion: ProcessProductVersionModel;
    fixedHeader: boolean;
    fixedFooter: boolean;
    timeInterval: any;
    isScreenSmall: boolean;
    navigation: FuseNavigationItem[];

    private _unsubscribeAll: Subject<any> = new Subject<any>();

    /**
     * Constructor
     */
    constructor(
        public config: EnvironmentConfigService,
        public _facilityService: FacilityService,
        public _menuService: MenuService,
        private _router: Router,
        private _fuseMediaWatcherService: FuseMediaWatcherService,
        private _fuseNavigationService: FuseNavigationService,
        private _datePipe: DatePipe,
        private _renderer: Renderer2,
        private _zone: NgZone
    ) {
        const properties = this.config.env.properties || [];
        this.moduleTitle = 'ARC-One';
        this.defaultLogo =
            properties['arc-default-logo'] || 'images/logo/arc.png';
        this.releaseNumber = properties['release_number'];
        this.investigationalDevice = properties['INVESTIGATIONAL_DEVICE'];
        this.processProductVersion = this.config.env?.productVersion;

        // Set the defaults
        this.fixedHeader = false;
        this.fixedFooter = false;
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Accessors
    // -----------------------------------------------------------------------------------------------------

    /**
     * Getter for current year
     */
    get currentYear(): number {
        return new Date().getFullYear();
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Lifecycle hooks
    // -----------------------------------------------------------------------------------------------------

    /**
     * On init
     */
    ngOnInit(): void {
        // Subscribe to navigation data
        this._menuService.menus$
            .pipe(takeUntil(this._unsubscribeAll))
            .subscribe((menus) => (this.navigation = menus));

        // Subscribe to media changes
        this._fuseMediaWatcherService.onMediaChange$
            .pipe(takeUntil(this._unsubscribeAll))
            .subscribe(({ matchingAliases }) => {
                // Check if the screen is small
                this.isScreenSmall = !matchingAliases.includes('md');
            });
    }

    /**
     * After View Init
     */
    ngAfterViewInit(): void {
        this.getCurrentDate();
    }

    /**
     * On destroy
     */
    ngOnDestroy(): void {
        // Unsubscribe from all subscriptions
        this._unsubscribeAll.next(null);
        this._unsubscribeAll.complete();
    }

    // -----------------------------------------------------------------------------------------------------
    // @ Public methods
    // -----------------------------------------------------------------------------------------------------

    /**
     * Toggle navigation
     *
     * @param name
     */
    toggleNavigation(name: string): void {
        // Get the navigation
        const navigation =
            this._fuseNavigationService.getComponent<FuseVerticalNavigationComponent>(
                name
            );

        if (navigation) {
            // Toggle the opened status
            navigation.toggle();
        }
    }

    isHomePage() {
        return this._router.url === '/' || this._router.url === '/home';
    }

    /**
     * Get current time
     */
    getCurrentDate(): void {
        this._zone.runOutsideAngular(() => {
            this.timeInterval = setInterval(() => {
                const time = new Date(); //set time variable with current date
                const location = getLocalTimeZone(time);
                this._renderer.setProperty(
                    this.footerDateTime.nativeElement,
                    'innerHTML',
                    `${this._datePipe.transform(time, 'medium')} ${location}`
                );
            }, 1000); // set it every one seconds
        });
    }

    changeFacility() {
        if (this.isHomePage()) {
            this._facilityService
                .getFacilityDialog(
                    FilterableDropDownComponent,
                    true
                )
                .subscribe();
        }
    }

    getChangeFacilityTooltip() {
        return this.isHomePage() ? 'Click to change facility' : '';
    }

    /**
     * Navigate to the Version Details
     */
    goToBuildDetails() {
        window.open(this.processProductVersion.releaseNotes, '_blank');
    }
}
