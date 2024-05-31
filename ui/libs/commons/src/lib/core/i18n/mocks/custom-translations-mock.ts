import { Translation } from './translations-mock';

export interface CustomTranslation {
  [key: string]: CustomTranslation | string;
}

export const NEW_TRANSLATIONS: Translation = {
  cancel: 'Cancel',
  ok: 'Ok',
  close: 'Close',
  pick_list: 'Pick List',
  order_number: 'Order Number',
  customer_id: 'Customer ID',
  customer_name: 'Customer Name',
  product_details: 'Product Details',
  quantity: 'Quantity',
  product: 'Product',
  blood_type: 'Blood Type',
  comments: 'Comments',
  short_date_details: 'Short Date Details',
  unit_number: 'Unit Number',
  product_code: 'Product Code',
  no_suggested_short_dated_products_message: "There are no suggested short-dated products.",
}

export const CUSTOM_TRANSLATIONS: Readonly<Partial<CustomTranslation>> = Object.freeze({
  ...NEW_TRANSLATIONS
});
