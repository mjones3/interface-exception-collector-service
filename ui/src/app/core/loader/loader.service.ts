import { DOCUMENT } from '@angular/common';
import { Inject, Injectable, InjectionToken } from '@angular/core';
import { Loader } from 'app/shared/models/loader.model';
import { BehaviorSubject, Subscription, timer } from 'rxjs';
import { skip, skipWhile } from 'rxjs/operators';

export interface LoaderData {
    fullScreenClass: string;
    loaderClasses: string[];
    debounceTime?: number;
}

export function loaderDataFn() {
    return {
        fullScreenClass: 'rsa-full-screen',
        loaderClasses: ['rsa-loading', 'rsa-locked'],
        debounceTime: 150,
    };
}

export const LOADER_DATA = new InjectionToken<LoaderData>('Loader Data', {
    providedIn: 'root',
    factory: loaderDataFn,
});

/**
 * App Loader Service | Show and hide loader indicator adding loader classes to body (default) or specific element
 * on the page
 * Scenarios:
 * Default     | Global loader add the loader class to body when an http request is dispatched
 * Skip Loader | Don't want to show loader indicator, add http header from the enum Headers.XIgnoreLoader or
 *               get it call to getLoaderHeaders util method and pass it to the request
 *               `const skipLoaderHeader: HttpHeaders = getLoaderHeaders(null, HeaderValue.False);`
 * Selector    | Show the loader over an element on the page, add http header from the enum Headers.XLoaderSelector
 *               or get it call to getLoaderHeaders util method and pass it to the request
 *               `const selectorLoaderHeader: HttpHeaders = getLoaderHeaders('selector');`
 * Manually    | Inject LoaderService into your controller or service and call show and hide method when you initiate a
 *               request and get the response from backend respectively with skip loader
 *               Example:
 *               ```ts
 *               @Component()
 *               export class FeatureComponent {
 *                  readonly selector = 'div.component';
 *                  readonly debounceTimeMs = 150;
 *                  constructor(private loader: LoaderService, private service: DataService) {}
 *                  getData() {
 *                    // Show the loader on selector element and with a debounce time
 *                    this.loader.show(selector, debounceTimeMs);
 *                    // Add http header from the enum Headers.XIgnoreLoader or get it call to getLoaderHeaders
 *                    // util method and pass it to the request
 *                    this.service.getData().subscribe(() => this.loader.hide(selector));
 *                  }
 *               }```
 */
@Injectable({
    providedIn: 'root',
})
export class LoaderService {
    readonly defaultHtmlElement: HTMLElement = this.document.body;
    showedLoader = false;
    initLoaderTime = 0;
    totalLoaderTime = 0;
    private loaderSubject = new BehaviorSubject<Loader>({ show: false });
    loaderState$ = this.loaderSubject.asObservable();
    private subscriptions: Subscription[] = [];

    constructor(
        @Inject(DOCUMENT) private document: any,
        @Inject(LOADER_DATA) private loaderData: LoaderData
    ) {
        const loaderSubscription = this.loaderState$
            .pipe(skip(1))
            .subscribe((loader: Loader) => {
                const classes = !loader.element
                    ? [
                          ...this.loaderData.loaderClasses,
                          this.loaderData.fullScreenClass,
                      ]
                    : this.loaderData.loaderClasses;
                if (loader.show) {
                    this.showLoaderIndicator(loader, classes);
                } else {
                    this.hideLoaderIndicator(loader, classes);
                }
            });
        this.subscriptions.push(loaderSubscription);
    }

    /**
     * Show the loader indicator
     * @param selector Selector if you need to show the loader in a section of the page
     * @param debounceTime Debounce time in ms, if the request last more than that time the loader is showed, otherwise is
     * not showed
     */
    show(selector?: string, debounceTime = 0): void {
        this.loaderSubject.next(
            this.getLoaderState(true, selector, debounceTime)
        );
    }

    /**
     * Get loader state
     * @param show if the the loading indicator need to be shown
     * @param selector Selector if you need to show the loader in a section of the page
     * @param debounceTime Debounce time in ms, if the request last more than that time the loader is showed, otherwise is
     * not showed
     */
    getLoaderState(
        show: boolean,
        selector?: string,
        debounceTime?: number
    ): Loader {
        const loader: Loader = { show };
        if (selector) {
            loader.element = this.getNativeElement(selector);
        }
        if (debounceTime) {
            loader.debounceTime = debounceTime;
        }
        return loader;
    }

    /**
     * Get element to attach the loader classes
     * @param selector Selector if you need to show the loader in a section of the page
     */
    getNativeElement(selector?: string): HTMLElement {
        let nativeElement = this.defaultHtmlElement;
        if (selector) {
            nativeElement = this.defaultHtmlElement.querySelector(selector);
        }
        return nativeElement;
    }

    /**
     * Get the element where the loader classes are attached
     * @param loader Loader state
     */
    getLoaderElement(loader?: Loader): HTMLElement {
        return (
            loader.element ||
            this.defaultHtmlElement.querySelector(
                `.${this.loaderData.loaderClasses.join('.')}`
            )
        );
    }

    /**
     * Hide the loader indicator
     * @param selector Selector if you need to show the loader in a section of the page
     */
    hide(selector?: string): void {
        const loader = this.getLoaderState(false, selector, 0);
        this.loaderSubject.next(loader);
    }

    /**
     * Dispose subscriptions
     */
    dispose(): void {
        this.subscriptions.forEach((subscription) =>
            subscription.unsubscribe()
        );
    }

    /**
     * Hide loader indicator
     * @param loader Loader state
     * @param classes Loader classes
     */
    private hideLoaderIndicator(loader: Loader, classes: string[]): void {
        const element =
            this.getLoaderElement(loader) || this.defaultHtmlElement;
        this.totalLoaderTime = Date.now().valueOf() - this.initLoaderTime;
        const addedDebounceTime =
            this.totalLoaderTime < this.loaderData.debounceTime &&
            this.showedLoader
                ? this.loaderData.debounceTime
                : 0;
        timer(addedDebounceTime).subscribe(() =>
            element.classList.remove(...classes)
        );
        this.showedLoader = false;
    }

    /**
     * Show loader indicator
     * @param loader Loader state
     * @param classes Loader classes
     */
    private showLoaderIndicator(loader: Loader, classes: string[]): void {
        this.showedLoader = true;
        this.initLoaderTime = Date.now().valueOf();
        const element = loader.element || this.defaultHtmlElement;
        timer(loader.debounceTime || 0)
            .pipe(
                skipWhile(() => !this.showedLoader && loader.debounceTime > 0)
            )
            .subscribe(() => element.classList.add(...classes));
    }
}
