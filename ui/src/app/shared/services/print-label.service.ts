import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable, of } from 'rxjs';
import { EnvironmentConfigService } from '@shared';
import { catchError } from 'rxjs/operators';

export enum LabelPrinterType {
    DEFAULT_PRINTER,
    NETWORK_PRINTER,
    LOCAL_PRINTER_BY_NAME,
}

export interface LabelPrinterDTO {
    labelPrinterType?: keyof typeof LabelPrinterType;
    name?: string;
    ip?: string;
    port?: number;
    labelToPrint: string;
}

export interface LabelPrinterResponseDTO {
    type: string;
    message: string;
    status: number;
}

@Injectable({
    providedIn: 'root',
})
export class PrintLabelService {

    private static readonly INSTALLED_RESPONSE = "client agent is installed and working";

    private readonly baseUrl: string;
    private readonly httpClient: HttpClient = inject(HttpClient);

    constructor(config: EnvironmentConfigService) {
        this.baseUrl = config.env.agentApiURL;
    }

    /**
     * Returns an observable with a status boolean flag indicating
     * if the agent is properly installed and working.
     */
    public installed(): Observable<boolean> {
        return this.httpClient
            .get(`${this.baseUrl}/agent/installed`, { responseType: 'text' })
            .pipe(
                catchError(() => of('not installed')),
                map((response: string) => PrintLabelService.INSTALLED_RESPONSE === response)
            );
    }

    /**
     * Returns an observable response with the print request result.
     *
     * @param payload
     */
    public print(payload: LabelPrinterDTO): Observable<LabelPrinterResponseDTO> {
        return this.httpClient
            .post<LabelPrinterResponseDTO>(`${this.baseUrl}/agent/print`, payload);
    }

}
