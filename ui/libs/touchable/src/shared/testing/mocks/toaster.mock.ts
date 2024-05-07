import { IndividualConfig, OverlayRef, ToastPackage, ToastRef, ToastrService, TOAST_CONFIG } from 'ngx-toastr';

export const toasterProvidersMock = [
  ToastrService,
  {
    provide: TOAST_CONFIG,
    useValue: {
      default: {},
      config: {},
    },
  },
  {
    provide: ToastPackage,
    useFactory: () => {
      return new ToastPackage(
        1,
        {
          easeTime: 1000,
        } as IndividualConfig,
        'Test',
        'Test',
        'info',
        new ToastRef({} as OverlayRef)
      );
    },
  },
];
