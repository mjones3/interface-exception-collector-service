import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { TransferReceiptDto } from '../models/transfer-receipt.dto';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class TransferReceiptService {
  transferReceiptsEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.transferReceiptsEndpoint = config.env.serverApiURL + '/v1/transfer-receipts';
  }

  createTransferReceipt(transferReceipt: Partial<TransferReceiptDto>): Observable<HttpResponse<TransferReceiptDto>> {
    return this.httpClient.post<TransferReceiptDto>(this.transferReceiptsEndpoint, transferReceipt, {
      observe: 'response',
    });
  }

  public getTransferReceiptsByCriteria(criteria?: {}): Observable<HttpResponse<TransferReceiptDto[]>> {
    return this.httpClient
      .get<TransferReceiptDto[]>(`${this.transferReceiptsEndpoint}`, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
