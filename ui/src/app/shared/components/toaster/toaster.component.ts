import { Component, Input, ViewEncapsulation } from '@angular/core';
import { FuseAlertComponent } from '@fuse/components/alert';
import { TranslateModule } from '@ngx-translate/core';
import { Toast, ToastPackage, ToastrService } from 'ngx-toastr';

@Component({
    selector: 'rsa-toaster',
    templateUrl: './toaster.component.html',
    encapsulation: ViewEncapsulation.None,
    standalone: true,
    imports: [FuseAlertComponent, TranslateModule],
})
export class ToasterComponent extends Toast {
    @Input() appearance: 'border' | 'fill' | 'outline' = 'border';

    constructor(toastrService: ToastrService, toastPackage: ToastPackage) {
        super(toastrService, toastPackage);
    }
}
