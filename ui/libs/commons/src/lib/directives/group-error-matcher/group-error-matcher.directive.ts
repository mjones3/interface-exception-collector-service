import { Directive } from '@angular/core';
import { ErrorStateMatcher } from '@angular/material/core';
import { DefaultErrorStateMatcher } from '../../shared/forms/default.error-matcher';

/**
 * rsaGroupErrorMatcher directive was created to set ErrorStateMatcher provider.
 *
 * Used when a form is inside a MatStep(Has their own ErrorStateMatcher and is colliding with Form Item ErrorStateMatcher)
 *
 * e.g.
 *
 *  <!--STEPPER-->
    <mat-horizontal-stepper linear>
     <!-- STEP 1 -->
     <mat-step [stepControl]="group" [label]="'sample.label' | translate">
       <form [formGroup]="group" rsaGroupErrorMatcher>
         <div class="flex flex-col space-y-2">
           <mat-form-field appearance="fill" class="block" rsaControlErrorContainer>
             <input matInput required formControlName="temperature" validateOn="blur"/>
             <span matSuffix>{{ 'celsius.label' | translate }}</span>
           </mat-form-field>
         </div>

         <div class="flex flex-row-reverse mt-2">
           <button id="step1NextBtn" type="button" matStepperNext mat-stroked-button>
           {{ 'next.label' | translate }}
           </button>
         </div>
       </form>
     </mat-step>
   </mat-horizontal-stepper>
 */

@Directive({
  selector: '[rsaGroupErrorMatcher]',
  providers: [{ provide: ErrorStateMatcher, useClass: DefaultErrorStateMatcher }],
})
export class GroupErrorMatcherDirective {}
