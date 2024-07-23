import { DOCUMENT } from '@angular/common';
import {
    Inject,
    Injectable,
    Injector,
    NgZone,
    Renderer2,
    RendererFactory2,
} from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { FuseAlertType } from '@fuse/components/alert/alert.types';
import { isFunction } from 'lodash-es';
import {
    ActiveToast,
    IndividualConfig,
    Overlay,
    TOAST_CONFIG,
    ToastToken,
    ToastrService,
} from 'ngx-toastr';
import { ActiveElementService } from './active-element.service';

@Injectable({
    providedIn: 'root',
})
export class ToastrImplService extends ToastrService {
    private renderer2: Renderer2;
    private readonly blockingBackdropClass = 'rsa-blocking-backdrop';

    constructor(
        @Inject(TOAST_CONFIG) token: ToastToken,
        overlay: Overlay,
        injector: Injector,
        sanitizer: DomSanitizer,
        ngZone: NgZone,
        private rendererFactory: RendererFactory2,
        @Inject(DOCUMENT) private document: any,
        private activeEl: ActiveElementService,
    ) {
        super(token, overlay, injector, sanitizer, ngZone);
        this.renderer2 = rendererFactory.createRenderer(document.body, null);
    }

    show(
        message?: string,
        title?: string,
        override?: Partial<IndividualConfig>,
        type?: FuseAlertType
    ): ActiveToast<any> {
        title = title ?? this.getDefaultTitle(type);

        if (type === 'error') {
            return this.error(message, title, override);
        }

        const toast = super.show(message, title, override, type);
        toast.onHidden.subscribe(() => {
            this.focusElement();
        });
        return toast;
    }

    error(
        message?: string,
        title?: string,
        override?: Partial<IndividualConfig>
    ): ActiveToast<any> {
        title = title ?? this.getDefaultTitle('warn');

        // Showing backdrop in case of error and disable timeout
        this.showLockingBackdrop();
        const toast = super.error(message, title, {
            ...override,
            disableTimeOut: true,
        });

        // Subscribe to OnHide event to remove the backdrop in case of error
        this.subscribeOnHideWithError(toast);
        return toast;
    }

    warning(
        message?: string,
        title?: string,
        override?: Partial<IndividualConfig>
    ): ActiveToast<any> {
        return this.show(message, title, override, 'warning');
    }

    success(
        message?: string,
        title?: string,
        override?: Partial<IndividualConfig>
    ): ActiveToast<any> {
        return this.show(message, title, override, 'success');
    }

    private subscribeOnHideWithError(toast: ActiveToast<any>) {
        toast.onHidden.subscribe(() => {
            this.renderer2.removeClass(
                this.document.body,
                this.blockingBackdropClass
            );
            this.focusElement();
        });
    }

    private showLockingBackdrop() {
        this.renderer2.addClass(this.document.body, this.blockingBackdropClass);
        if (document.activeElement instanceof HTMLElement) {
            document.activeElement.blur();
        }
    }

    private focusElement(): void {
        const el: HTMLElement = this.activeEl.getElement();
        if (el && 'focus' in el && isFunction(el.focus)) {
            el.focus();
        }
    }

    private getDefaultTitle(toasterType: FuseAlertType): string {
        switch (toasterType) {
            case 'success':
                return 'Success';
            case 'warn':
                return 'Error';
            case 'warning':
                return 'Warning';
            default:
                break;
        }
    }
}
