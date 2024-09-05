import { ToasterComponent } from './toaster.component';

export const toasterDefaultConfig = {
    toastComponent: ToasterComponent,
    toastClass: 'bg-transparent pointer-events-auto max-w-96',
    iconClasses: {
        error: 'error',
    },
};
