import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Observable, throwError } from 'rxjs';

export function errorHandler(error: any): Observable<any> {
    return throwError(error);
}

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
    standalone: true,
    imports: [RouterOutlet],
})
export class AppComponent  {

}
