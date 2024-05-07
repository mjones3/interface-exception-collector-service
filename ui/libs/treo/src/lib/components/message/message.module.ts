import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { TreoMessageComponent } from './message.component';

@NgModule({
    declarations: [
        TreoMessageComponent
    ],
    imports     : [
        CommonModule,
        MatButtonModule,
        MatIconModule
    ],
    exports     : [
        TreoMessageComponent
    ]
})
export class TreoMessageModule
{
}
