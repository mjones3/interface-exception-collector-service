import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

export abstract class PrintableReportComponent {

    protected constructor(protected domSanitizer: DomSanitizer) {}

    public getBase64DataImage(payload: string): SafeResourceUrl {
        this.validateBase64(payload);
        this.validateImageHeader(payload);
        return this.domSanitizer.bypassSecurityTrustResourceUrl(
            `data:image/*;base64,${payload}`
        );
    }

    private validateBase64(payload: string): void {
        if (!/^[A-Za-z0-9+/=]+$/.test(payload)) {
            throw new Error('Sanitization error: invalid Base64 payload');
        }
    }

    private validateImageHeader(payload: string): void {
        const decodedData = atob(payload);
        const isImage =
            decodedData.startsWith('\xFF\xD8') || // JPEG
            decodedData.startsWith('\x89\x50\x4E\x47') || // PNG
            decodedData.startsWith('GIF'); // GIF

        if (!isImage) {
            throw new Error('Sanitization error: invalid image header');
        }
    }

    public get navigatorLanguage() {
        return navigator.languages?.[0] ?? navigator.language;
    }

    public get localTimezone() {
        const dateParts = new Date()
            .toLocaleTimeString(this.navigatorLanguage, {
                timeZoneName: 'short',
            })
            .split(' ');

        return dateParts?.length > 0 ? dateParts[dateParts.length - 1] : '';
    }
}
