import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatSelectModule } from '@angular/material/select';
import { TranslateModule } from '@ngx-translate/core';
import { MaterialModule } from '@rsa/material';
import { TreoCardModule, TreoMessageModule, TreoScrollbarModule } from '@treo';
import { ControlErrorComponent } from './components/control-error/control-error.component';
import { GlobalMessageComponentModule } from './components/global-message/global-message.component';
import { COMMONS_COMPONENTS } from './components/index';
import { COMMONS_DIRECTIVES } from './directives/index';
import { COMMONS_PIPES } from './pipes/index';

@NgModule({
  imports: [
    CommonModule,
    MatFormFieldModule,
    TreoScrollbarModule,
    TreoMessageModule,
    TreoCardModule,
    MatSelectModule,
    TranslateModule,
    MatIconModule,
    ReactiveFormsModule,
    MatListModule,
    MatInputModule,
    MatDialogModule,
    MatButtonModule,
    MaterialModule,
    MatMenuModule,
    GlobalMessageComponentModule,
  ],
  declarations: [...COMMONS_DIRECTIVES, ...COMMONS_COMPONENTS, ...COMMONS_PIPES],
  exports: [CommonModule, ...COMMONS_DIRECTIVES, ...COMMONS_COMPONENTS, ...COMMONS_PIPES, GlobalMessageComponentModule],
  entryComponents: [ControlErrorComponent],
  providers: [...COMMONS_PIPES],
})
export class RsaCommonsModule {}
