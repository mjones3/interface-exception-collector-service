import { Injectable } from '@angular/core';
import { AsyncValidatorFn, ValidationErrors } from '@angular/forms';
import { Observable, of } from 'rxjs';
import { fromPromise } from 'rxjs/internal-compatibility';
import { catchError, switchMap } from 'rxjs/operators';
import { BarcodeService } from '../services';
import { StorageService } from '../services/storage.service';

/**
 * @description
 * Async Validator that requires the a valid barcode value.
 *
 * @usageNotes
 *
 * ```typescript
 * // AsyncBarcodeValidator have to be injected in your component
 * const control = new FormControl('Q21312313213', [], barcodeValidator.validate());
 * console.log(control.errors); // {invalidBarcode: true}
 * ```
 * @returns An error map with the `invalidBarcode` property
 * if the validation check fails, otherwise `null`.
 *
 */

@Injectable({
  providedIn: 'root',
})
export class AsyncBarcodeValidator {
  constructor(private storageService: StorageService, private barcodeService: BarcodeService) {}

  barcodeValidation(initialValue: string = '', deviceTypeId: number[], processName: string): AsyncValidatorFn {
    return (control): Promise<ValidationErrors | null> | Observable<ValidationErrors | null> => {
      if (!control.value) {
        return of(null);
      } else if (control.value === initialValue) {
        return of(null);
      } else {
        // Validate and sanitize and get barcode parts
        return this.barcodeService.getBarcodeParts(control.value).pipe(
          switchMap(barcodeParts => {
            // Validate barcode exists in rule engine
            const source$ = fromPromise(
              this.storageService.validateBarcode(barcodeParts.barcode, deviceTypeId, processName)
            );
            return source$.pipe(
              switchMap(value => {
                if (barcodeParts.barcode !== control.value && value) {
                  control.setValue(barcodeParts.barcode, { emitEvent: false });
                } else if (!value) {
                  control.reset();
                }
                return of(value ? null : ({ invalidBarcode: true } as ValidationErrors));
              })
            );
          }),
          catchError(() => of({ invalidBarcode: true }))
        );
      }
    };
  }
}
