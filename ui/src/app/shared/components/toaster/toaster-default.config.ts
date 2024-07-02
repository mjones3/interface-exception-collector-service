import { ToasterComponent } from './toaster.component';

export const toasterDefaultConfig = {
    timeOut: 10000,
    positionClass: 'toast-top-right',
    preventDuplicates: false,
    preventOpenDuplicates: true,
    toastComponent: ToasterComponent,
    tapToDismiss: true,
    toastClass: 'rsa-toast',
    iconClasses: {
        error: 'error',
        info: 'info',
        success: 'success',
        warning: 'warning',
    },
};
