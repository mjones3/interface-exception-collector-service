import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { UserComponent } from './user.component';

@NgModule({
  declarations: [UserComponent],
  imports: [MatButtonModule, MatDividerModule, MatIconModule, MatMenuModule, CommonModule],
  exports: [UserComponent],
})
export class UserModule {}
