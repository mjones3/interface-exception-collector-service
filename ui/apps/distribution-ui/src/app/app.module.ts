import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterModule } from '@angular/router';
import { AuthModule, toasterDefaultConfig } from '@rsa/commons';
import { appRoutes } from '@rsa/distribution/app.routing';
import { appConfig } from '@rsa/distribution/core/config/app.config';
import { CoreModule } from '@rsa/distribution/core/core.module';
import { mockDataServices } from '@rsa/distribution/data/mock';
import { LayoutModule } from '@rsa/distribution/layout/layout.module';
import { TreoConfigModule, TreoMockApiModule, TreoModule } from '@treo';
import { ToastrModule } from 'ngx-toastr';
import { AppComponent } from './app.component';

@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    RouterModule.forRoot(appRoutes, {
      onSameUrlNavigation: 'reload',
      relativeLinkResolution: 'legacy',
    }),

    // Treo & Treo Mock API
    TreoModule,
    TreoConfigModule.forRoot(appConfig.appTreoConfig),

    // Treo Mock API
    TreoMockApiModule.forRoot(mockDataServices),
    ToastrModule.forRoot(toasterDefaultConfig),

    // Core
    CoreModule,

    // Layout
    LayoutModule,
    AuthModule,

    // 3rd party modules
    ReactiveFormsModule,
    FormsModule,
  ],
  providers: [],
  bootstrap: [AppComponent],
})
export class AppModule {}
