import { UpperCasePipe } from '@angular/common';
import { Component, Input, ViewEncapsulation } from '@angular/core';
import {
    FuseAlertComponent,
} from '@fuse/components/alert';
import { FuseAlertAppearance } from '@fuse/components/alert/alert.types';
import { Toast, ToastPackage, ToastrService } from 'ngx-toastr';

@Component({
    selector: 'rsa-toaster',
    templateUrl: './toaster.component.html',
    encapsulation: ViewEncapsulation.None,
    standalone: true,
    imports: [FuseAlertComponent, UpperCasePipe],
})
export class ToasterComponent extends Toast {
    @Input() appearance: FuseAlertAppearance = 'border';

    constructor(toastrService: ToastrService, toastPackage: ToastPackage) {
        super(toastrService, toastPackage);
    }
}
