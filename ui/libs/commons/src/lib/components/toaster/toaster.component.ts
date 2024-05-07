import { Component, Input, ViewEncapsulation } from '@angular/core';
import { TreoMessageAppearance } from '@treo';
import { Toast, ToastPackage, ToastrService } from 'ngx-toastr';

@Component({
  selector: 'rsa-toaster',
  templateUrl: './toaster.component.html',
  styleUrls: ['./toaster.component.scss'],
  encapsulation: ViewEncapsulation.None,
})
export class ToasterComponent extends Toast {
  @Input() appearance: TreoMessageAppearance = 'border';

  constructor(toastrService: ToastrService, toastPackage: ToastPackage) {
    super(toastrService, toastPackage);
  }
}
