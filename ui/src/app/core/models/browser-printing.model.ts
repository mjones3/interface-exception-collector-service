export enum PageSize {
    'portrait',
    'landscape',
    'A5', // 148mm x 210mm
    'A5 portrait',
    'A5 landscape',
    'A4', // 210mm x 297mm
    'A4 portrait',
    'A4 landscape',
    'A3', // 297mm x 420mm
    'A3 portrait',
    'A3 landscape',
    'B5', // 176mm x 250mm
    'B5 portrait',
    'B5 landscape',
    'B4', // 250mm x 353mm
    'B4 portrait',
    'B4 landscape',
    'JIS-B5', // 182mm x 257mm
    'JIS-B5 portrait',
    'JIS-B5 landscape',
    'JIS-B4', // 257mm x 364mm
    'JIS-B4 portrait',
    'JIS-B4 landscape',
    'letter', // 8.5in x 11in
    'letter portrait',
    'letter landscape',
    'legal', // 8.5in x 14in
    'legal portrait',
    'legal landscape',
    'ledger', // 11in x 17in
    'ledger portrait',
    'ledger landscape',
}

export const DEFAULT_PAGE_SIZE: keyof typeof PageSize = 'A4 portrait';
export const DEFAULT_PAGE_SIZE_DIALOG_PORTRAIT_WIDTH = '210mm';
export const DEFAULT_PAGE_SIZE_DIALOG_LANDSCAPE_WIDTH = '297mm';
export const DEFAULT_PAGE_SIZE_DIALOG_HEIGHT = 'calc(100vh - 2rem)';

export interface BrowserPrintingModel {
    printTitle: string;
    useExistingCss: boolean;
    bodyClass: string;
    openNewTab: boolean;
    previewOnly: boolean;
    closeWindow: boolean;
    printDelay: number;
    pageSize: keyof typeof PageSize | string;
}
