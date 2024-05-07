import { of } from 'rxjs';

export const INVENTORY_ID_REF = 2000001234;
export const INVENTORY_DESCRIPTION_KEY_REF = 'INVENTORY_DESCRIPTION_KEY';
export const UNIT_NUMBER_REF = 'W077723427073';
export const UNIT_NUMBER_SCANNED_REF = '=W07772342707300';

export const PRODUCT_CODE_REF = 'E002300';
export const ISBT_PRODUCT_CODE_REF = 'E0023V00';
export const PRODUCT_CODE_SCANNED_REF = '=<E0023V00';

export const FAILED_RULE_RESPONSE = {
  status: 200,
  body: {
    ruleCode: 'BAD_REQUEST',
    results: {},
    notifications: [],
  },
};
export const SUCCESS_RULE_115_MULTIPLE_PRODUCTS_RESPONSE = {
  status: 200,
  body: {
    ruleCode: 'OK',
    results: {
      inventories: [
        [
          { id: INVENTORY_ID_REF, descriptionKey: INVENTORY_DESCRIPTION_KEY_REF, productCode: ISBT_PRODUCT_CODE_REF },
          { id: 12345, descriptionKey: INVENTORY_DESCRIPTION_KEY_REF, productCode: 'E0043V00' },
          { id: 12346, descriptionKey: INVENTORY_DESCRIPTION_KEY_REF, productCode: 'E0049V00' },
        ],
      ],
    },
    notifications: [],
  },
};

export const SUCCESS_RULE_115_RESPONSE = {
  status: 200,
  body: {
    ruleCode: 'OK',
    results: {
      inventories: [
        [{ id: INVENTORY_ID_REF, descriptionKey: INVENTORY_DESCRIPTION_KEY_REF, productCode: ISBT_PRODUCT_CODE_REF }],
      ],
    },
    notifications: [],
  },
};

export const FAILED_RULE_115_RESPONSE = {
  status: 200,
  body: {
    ruleCode: 'BAD_REQUEST',
  },
};

export const SUCCESS_RULE_117_RESPONSE = {
  status: 200,
  body: {
    ruleCode: 'OK',
    results: {
      markForQuarantine: [false],
      inventory: [
        {
          id: INVENTORY_ID_REF,
          descriptionKey: INVENTORY_DESCRIPTION_KEY_REF,
          isQuarantine: true,
        },
      ],
    },
    notifications: [
      {
        statusCode: '200',
        notificationType: 'success',
        message: 'rul-0117-added-succesfull.label',
        notificationEventOnDismiss: 'null',
      },
    ],
  },
};

export const FAILED_RULE_117_RESPONSE = {
  status: 200,
  body: {
    ruleCode: 'OK',
    results: {
      additionalInfo: [null],
      notificationArgs: [null],
    },
    notifications: [
      {
        statusCode: '400',
        notificationType: 'error',
        message: 'inventory-not-found.label',
        notificationEventOnDismiss: 'null',
      },
    ],
    _links: {
      next: '/',
    },
  },
};

export const SUCCESS_UNIT_NUMBER_BARCODE_RESPONSE = {
  status: 200,
  body: {
    barcodeTranslation: {
      unitNumber: UNIT_NUMBER_REF,
    },
  },
};

export const SUCCESS_PRODUCT_CODE_BARCODE_RESPONSE = {
  status: 200,
  body: {
    barcodeTranslation: {
      productCode: ISBT_PRODUCT_CODE_REF,
    },
  },
};

export const FAILED_BARCODE_RESPONSE = {
  status: 404,
  body: {
    type: 'https://arc-one.com/problem/problem-with-message',
    title: 'Not Found',
    status: 404,
    detail: '404 NOT_FOUND "barcode-pattern-not-found.label"',
    path: '/v1/barcodes/translations/translate',
    message: 'error.http.404',
  },
};

export const BARCODE_VALIDATOR = request => {
  if (request === UNIT_NUMBER_SCANNED_REF) return of(SUCCESS_UNIT_NUMBER_BARCODE_RESPONSE);
  if (request === PRODUCT_CODE_SCANNED_REF) return of(SUCCESS_PRODUCT_CODE_BARCODE_RESPONSE);
  return of(FAILED_BARCODE_RESPONSE);
};
