import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialogModule } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { TreoScrollbarModule } from '../../directives/scrollbar/scrollbar.module';
import { TreoHorizontalNavigationBasicItemComponent } from './horizontal/components/basic/basic.component';
import { TreoHorizontalNavigationBranchItemComponent } from './horizontal/components/branch/branch.component';
import { TreoHorizontalNavigationDividerItemComponent } from './horizontal/components/divider/divider.component';
import { TreoHorizontalNavigationSpacerItemComponent } from './horizontal/components/spacer/spacer.component';
import { TreoHorizontalNavigationComponent } from './horizontal/horizontal.component';
import { TreoVerticalNavigationAsideItemComponent } from './vertical/components/aside/aside.component';
import { TreoVerticalNavigationBasicItemComponent } from './vertical/components/basic/basic.component';
import { TreoVerticalNavigationCollapsableItemComponent } from './vertical/components/collapsable/collapsable.component';
import { TreoVerticalNavigationDividerItemComponent } from './vertical/components/divider/divider.component';
import { TreoVerticalNavigationGroupItemComponent } from './vertical/components/group/group.component';
import { TreoVerticalNavigationSpacerItemComponent } from './vertical/components/spacer/spacer.component';
import { TreoVerticalNavigationComponent } from './vertical/vertical.component';

@NgModule({
  declarations: [
    TreoHorizontalNavigationBasicItemComponent,
    TreoHorizontalNavigationBranchItemComponent,
    TreoHorizontalNavigationDividerItemComponent,
    TreoHorizontalNavigationSpacerItemComponent,
    TreoHorizontalNavigationComponent,
    TreoVerticalNavigationAsideItemComponent,
    TreoVerticalNavigationBasicItemComponent,
    TreoVerticalNavigationCollapsableItemComponent,
    TreoVerticalNavigationDividerItemComponent,
    TreoVerticalNavigationGroupItemComponent,
    TreoVerticalNavigationSpacerItemComponent,
    TreoVerticalNavigationComponent
  ],
  imports: [
    CommonModule,
    RouterModule,
    MatButtonModule,
    MatDividerModule,
    MatIconModule,
    MatMenuModule,
    MatTooltipModule,
    TreoScrollbarModule,
    MatCardModule,
    MatFormFieldModule,
    FormsModule,
    MatDialogModule,
    TranslateModule
  ],
  exports: [
    TreoHorizontalNavigationComponent,
    TreoVerticalNavigationComponent
  ]
})
export class TreoNavigationModule {
}
