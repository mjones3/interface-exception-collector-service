import { ErrorHandler, Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class DefaultErrorHandlerService implements ErrorHandler {
  handleError(error: any): void {
    // Always log errors (Integrate with 3P API like rollbar or new relic)
    console.error(error);
  }
}
