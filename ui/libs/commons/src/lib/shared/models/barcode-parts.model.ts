export interface BarcodeParts {
  barcode: string;
  unitNumber?: string;
  originalBarcode?: string;

  [key: string]: any;
}
