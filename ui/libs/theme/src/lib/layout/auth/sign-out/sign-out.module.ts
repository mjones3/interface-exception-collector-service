import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { RouterModule, ROUTES } from '@angular/router';
import { TreoCardModule } from '@treo';
import { AuthSignOutComponent } from './sign-out.component';
import { authSignOutRoutes } from './sign-out.routing';

@NgModule({
  declarations: [AuthSignOutComponent],
  imports: [
    CommonModule,
    MatButtonModule,
    TreoCardModule,
    {
      ngModule: RouterModule,
      providers: [
        {
          provide: ROUTES,
          multi: true,
          useValue: authSignOutRoutes,
        },
      ],
    },
  ],
})
export class AuthSignOutModule {}
