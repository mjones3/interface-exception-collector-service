import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { TreoHighlightComponent } from './highlight.component';

@NgModule({
    declarations   : [
        TreoHighlightComponent
    ],
    imports        : [
        CommonModule
    ],
    exports        : [
        TreoHighlightComponent
    ],
    entryComponents: [
        TreoHighlightComponent
    ]
})
export class TreoHighlightModule
{
}
