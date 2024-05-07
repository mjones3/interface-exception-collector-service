import { NgModule } from '@angular/core';
import { MaterialModule } from '@rsa/material';
import { Error403Component } from './error-403/error-403.component';
import { Error404Component } from './error-404/error-404.component';
import { Error500Component } from './error-500/error-500.component';
import { ErrorsRoutingModule } from './errors-routing.module';

@NgModule({
  declarations: [Error403Component, Error404Component, Error500Component],
  imports: [MaterialModule, ErrorsRoutingModule],
  exports: [Error403Component, Error404Component, Error500Component],
})
export class ErrorsModule {}
