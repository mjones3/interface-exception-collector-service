import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { RouterModule } from '@angular/router';
import { SharedModule } from '@rsa/distribution/shared/shared.module';
import { LandingHomeComponent } from './home.component';
import { landingHomeRoutes } from './home.routing';

@NgModule({
  declarations: [LandingHomeComponent],
  imports: [RouterModule.forChild(landingHomeRoutes), MatButtonModule, SharedModule],
})
export class LandingHomeModule {}
