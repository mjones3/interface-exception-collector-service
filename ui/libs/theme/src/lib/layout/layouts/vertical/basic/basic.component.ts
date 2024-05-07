import { DatePipe } from '@angular/common';
import {
  Component,
  ElementRef,
  forwardRef,
  HostBinding,
  Inject,
  Input,
  NgZone,
  OnDestroy,
  OnInit,
  Renderer2,
  ViewChild,
  ViewEncapsulation,
} from '@angular/core';
import { ActivatedRoute, Data, Router } from '@angular/router';
import {
  DISTRIBUTION_LOCATION_TYPE_IDS,
  DONOR_LOCATION_TYPE_IDS,
  EnvironmentConfigService,
  FacilityPickerListComponent,
  FacilityService,
  getLocalTimeZone,
  ProcessProductVersionDto,
  SPECIALTY_LAB_LOCATION_TYPE_IDS,
} from '@rsa/commons';
import { TreoMediaWatcherService, TreoNavigationItem, TreoNavigationService } from '@treo';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

@Component({
  selector: 'rsa-theme-basic-layout',
  templateUrl: './basic.component.html',
  styleUrls: ['./basic.component.scss'],
  encapsulation: ViewEncapsulation.None,
  providers: [DatePipe],
})
export class BasicLayoutComponent implements OnInit, OnDestroy {
  @Input() moduleTitle: string;
  data: any;
  isScreenSmall: boolean;
  releaseNumber: string;
  processProductVersion: ProcessProductVersionDto;
  investigationalDevice: string;
  menu: TreoNavigationItem[];

  @HostBinding('class.fixed-header')
  fixedHeader: boolean;

  @HostBinding('class.fixed-footer')
  fixedFooter: boolean;

  // Private
  private _unsubscribeAll: Subject<any>;

  defaultLogo: any;
  timeInterval;
  readonly sidebarWidth = 280;
  readonly calcContentWidth = `calc(100% - ${this.sidebarWidth}px)`;

  @ViewChild('footerDateTime', { static: false }) footerDateTime: ElementRef<HTMLDivElement>;

  // -----------------------------------------------------------------------------------------------------
  // @ Accessors
  // -----------------------------------------------------------------------------------------------------

  /**
   * Getter for current year
   */
  get currentYear(): number {
    return new Date().getFullYear();
  }

  constructor(
    private _activatedRoute: ActivatedRoute,
    private _router: Router,
    private _treoMediaWatcherService: TreoMediaWatcherService,
    private _treoNavigationService: TreoNavigationService,
    @Inject(forwardRef(() => EnvironmentConfigService)) public config: EnvironmentConfigService,
    @Inject(forwardRef(() => FacilityService)) public facilityService: FacilityService,
    private zone: NgZone,
    private renderer: Renderer2,
    private datePipe: DatePipe
  ) {
    // Set the private defaults
    this._unsubscribeAll = new Subject();
    // Set the defaults
    this.fixedHeader = false;
    this.fixedFooter = false;
  }

  // -----------------------------------------------------------------------------------------------------
  // @ Lifecycle hooks
  // -----------------------------------------------------------------------------------------------------

  /**
   * On init
   */
  ngOnInit(): void {
    // Subscribe to the resolved route data
    this._activatedRoute.data.subscribe((data: Data) => (this.data = data.initialData));

    // Subscribe to media changes
    this._treoMediaWatcherService.onMediaChange$
      .pipe(takeUntil(this._unsubscribeAll))
      .subscribe(({ matchingAliases }) => {
        // Check if the breakpoint is 'lt-md'
        this.isScreenSmall = matchingAliases.includes('lt-md');
      });

    this.initLayout();
    this.getCurrentDate();
  }

  initLayout(): void {
    this.defaultLogo = this.config.env.properties['arc-default-logo'];
    this.releaseNumber = this.config.env.properties['release_number'];
    this.investigationalDevice = this.config.env.properties['INVESTIGATIONAL_DEVICE'];
    this.processProductVersion = this.config.env.productVersion;
  }

  /**
   * On destroy
   */
  ngOnDestroy(): void {
    // Unsubscribe from all subscriptions
    this._unsubscribeAll.next();
    this._unsubscribeAll.complete();
    clearInterval(this.timeInterval);
  }

  /**
   * Navigate to the Version Details
   */
  goToBuildDetails() {
    window.open(this.processProductVersion.releaseNotes, '_blank');
  }

  /**
   * Toggle navigation
   *
   * @param key
   */
  toggleNavigation(key): void {
    // Get the navigation
    const navigation = this._treoNavigationService.getComponent(key);

    if (navigation) {
      // Toggle the opened status
      navigation.toggle();
    }
  }

  /**
   * Get current time
   */
  getCurrentDate() {
    this.zone.runOutsideAngular(() => {
      this.timeInterval = setInterval(() => {
        const time = new Date(); //set time variable with current date
        const location = getLocalTimeZone(new Date());
        this.renderer.setProperty(
          this.footerDateTime.nativeElement,
          'innerHTML',
          `${this.datePipe.transform(time, 'medium')} ${location}`
        );
      }, 1000); // set it every one seconds
    });
  }

  changeFacility() {
    if (this.isHomePage()) {
      this.facilityService.getFacilityDialog(FacilityPickerListComponent, this.getLocationTypes(), true).subscribe();
    }
  }

  getChangeFacilityTooltip() {
    return this.isHomePage() ? 'Click to change facility' : '';
  }

  isHomePage() {
    return this._router.url === '/' || this._router.url === '/home';
  }

  getLocationTypes(): number[] {
    switch (this.moduleTitle) {
      case 'distribution.label':
        return DISTRIBUTION_LOCATION_TYPE_IDS;
      case 'specialty-lab.label':
        return SPECIALTY_LAB_LOCATION_TYPE_IDS;
      case 'donor-management.label':
        return DONOR_LOCATION_TYPE_IDS;
      default:
        return [];
    }
  }
}
