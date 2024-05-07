import { HttpHeaders } from '@angular/common/http';
import { defaults } from 'lodash-es';
import { Observable } from 'rxjs';
import { HeaderValue } from '../types/header-value.enum';
import { Headers } from '../types/headers.enum';

/**
 * Uses canvas.measureText to compute and return the width of the given text of given font in pixels.
 *
 * @param {String} text The text to be rendered.
 * @param {String} font The css font descriptor that text is to be rendered with (e.g. "bold 14px verdana").
 *
 * @see http://stackoverflow.com/questions/118241/calculate-text-width-with-javascript/21015393#21015393
 */
export const getTextWidth = (text, font) => {
  // if given, use cached canvas for better performance else, create new canvas
  const canvas = document.createElement('canvas');
  const context = canvas.getContext('2d');
  context.font = font;
  const metrics = context.measureText(text);
  return metrics.width;
};

/**
 * Generate a function from string
 * @param str String to convert to a function
 */
export const functionFromString = (str: string) => new Function(`return ${str}`)();

export const commonRegex = {
  unitNumber: '^W[0-9]{12}$',
  unitNumberWithZerosTail: '^=W[0-9]{12}00$',
  extractUnitNumber: '(^[=])(W[\\d]{12})(00)',
  bagLetter: '[A-Z]{1}',
  productCode: '^E\\w{6}$',
  fullProductCode: '^E\\w{7}$',
  scannedProductCode: '\\=\\<\\w{8}',
  extractProductCode: '(^=<)(E[\\d]{4})([^\\d])([\\d]{2})\\S*',
  extractFullProductCode: '(^)(E[\\d]{4})([^\\d])([\\d]{2})\\S*',
  codabarUnitNumber: '^d[0-9]{7}d$',
  codabarProductCode: '^a0[0-9]{5}3b$',
  aboRh: '^([A-Z]|=%[a-zA-Z]{1}[0-9]{3}|=%[a-zA-Z]{2}[0-9]{2}|=%[0-9]{4})',
};

export function isUnitNumberValid(unitNumber: string): boolean {
  return unitNumber ? unitNumber.match(commonRegex.unitNumber) !== null : false;
}

/**
 * Extract unit number and remove possible unused characters provided from scanner
 * @param barcode
 */
export const extractUnitNumber = (barcode: string): string => {
  if (new RegExp(commonRegex.unitNumber, 'g').test(barcode)) {
    return barcode;
  }
  const unitNumberWithZerosTailTest = new RegExp(commonRegex.unitNumberWithZerosTail).test(barcode);
  if (!unitNumberWithZerosTailTest) {
    return;
  }
  return barcode.replace(new RegExp(commonRegex.extractUnitNumber), (match, g1, g2) => g2);
};

export const addQueryParamsToUrl = (url: string, params: object): string => {
  const urlObject = new URL(url);
  const searchParams = urlObject.searchParams;

  Object.keys(params).forEach(key => {
    searchParams.set(key, params[key]);
  });

  // change the search property of the main url
  urlObject.search = searchParams.toString();
  return urlObject.toString();
};

/**
 * Get loader headers
 * @param selector Selector if you need to show the loader in a section of the page
 * @param ignoreLoader If this flag is set the loader is not going to be shown for that request
 * @param debounceTime Debounce time in ms, if the request last more than that time the loader is showed, otherwise is
 * not showed
 */
export const getLoaderHeaders = (selector, ignoreLoader: HeaderValue = HeaderValue.False, debounceTime = 0) => {
  if (ignoreLoader === HeaderValue.True) {
    return new HttpHeaders().set(Headers.XIgnoreLoader, ignoreLoader);
  }
  return new HttpHeaders()
    .set(Headers.XLoaderSelector, btoa(selector))
    .set(Headers.XLoaderDebounceTime, debounceTime.toString());
};

export const getPaginationHeaders = (total: number, link: string) => {
  return new HttpHeaders().set(Headers.Link, link).set(Headers.XTotalCount, `${total}`);
};

/**
 * Interpolation Messages Utils
 */
const interpolationRegex = /<%=[^<]+%>/g;
export const interpolate = (template, variables, fallback = '') => {
  return template.replace(interpolationRegex, (match: string) => {
    const path = match.slice(3, -2).trim();
    return getObjPath(path, variables, fallback);
  });
};

//Get the specified property or nested property of an object
function getObjPath(path, obj, fallback = '') {
  return path.split('.').reduce((res, key) => res[key] || fallback, obj);
}

/**
 * Test is form action element
 * @param {Object} el
 * @return {Boolean} true if a form action element
 */
export const isInputElement = el => /^(?:input|select|textarea)$/i.test(el.nodeName);

export const getElementOffset = (el: HTMLElement) => {
  const rect = el.getBoundingClientRect();
  return {
    left: rect.left + window.scrollX,
    top: rect.top + window.scrollY,
    bottom: rect.bottom + window.scrollY,
    right: rect.right + window.scrollX,
  };
};

export const getUrlFromFile = (file: File): Observable<string> => {
  return new Observable(observer => {
    const reader = new FileReader();
    reader.onload = () => {
      observer.next(reader.result as string);
      observer.complete();
    };
    reader.readAsDataURL(file);
  });
};

export const transformToTree = arr => {
  const nodes = {};
  return arr.filter(obj => {
    const id = obj['id'],
      parentId = obj['parentId'];

    nodes[id] = defaults(obj, nodes[id], { children: [] });
    if (parentId) {
      (nodes[parentId] = nodes[parentId] || { children: [] })['children'].push(obj);
    }
    return !parentId;
  });
};

export const getDocumentExtensionIcon = (extension: string): string => {
  switch (extension) {
    case 'pdf':
      return 'rsa:icon-pdf';
    case 'xlsx':
    case 'xls':
      return 'rsa:icon-xls';
    case 'docx':
    case 'doc':
      return 'rsa:icon-word';
    case 'csv':
      return 'rsa:icon-csv';
    default:
      return '';
  }
};

/**
 * Extract product code and remove possible unused characters provided from scanner
 * @param prodCode
 */
export const extractProductCode = (prodCode: string): string => {
  //Product Code
  if (isProductCode(prodCode)) {
    return prodCode;
  }

  //Product Code with Collection Type
  if (isFullProductCode(prodCode)) {
    return prodCode.slice(0, prodCode.length - 3) + prodCode.slice(prodCode.length - 2);
  }

  //Scanned Product Code
  if (isScannedProductCode(prodCode)) {
    return prodCode.replace(new RegExp(commonRegex.extractProductCode), (match, g1, g2, g3, g4) => g2 + g4);
  }

  return;
};

/**
 * Create product code with Letter in the 6 position.
 * @param prodCode
 */
export const addLetterToProductCode = (prodCode: string, letter: string): string => {
  //Product Code
  if (isProductCode(prodCode)) {
    return prodCode.slice(0, prodCode.length - 2) + letter + prodCode.slice(prodCode.length - 2);
  }

  //Product Code with Collection Type
  if (isFullProductCode(prodCode)) {
    return prodCode;
  }

  //Scanned Product Code
  if (isScannedProductCode(prodCode)) {
    const prod = prodCode.replace(new RegExp(commonRegex.extractProductCode), (match, g1, g2, g3, g4) => g2 + g4);
    return prod.slice(0, prod.length - 2) + letter + prod.slice(prod.length - 2);
  }

  return;
};

export const isProductCode = (prodCode: string): boolean => new RegExp(commonRegex.productCode, 'g').test(prodCode);
export const isFullProductCode = (prodCode: string): boolean => new RegExp(commonRegex.fullProductCode).test(prodCode);
export const isScannedProductCode = (prodCode: string): boolean =>
  new RegExp(commonRegex.scannedProductCode).test(prodCode);
