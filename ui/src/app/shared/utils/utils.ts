import { HttpHeaders } from '@angular/common/http';
import { HeaderValue } from '../types/header-value.enum';
import { Headers } from '../types/headers.enum';

/**
 * Get loader headers
 * @param selector Selector if you need to show the loader in a section of the page
 * @param ignoreLoader If this flag is set the loader is not going to be shown for that request
 * @param debounceTime Debounce time in ms, if the request last more than that time the loader is showed, otherwise is
 * not showed
 */
export const getLoaderHeaders = (
    selector,
    ignoreLoader: HeaderValue = HeaderValue.False,
    debounceTime = 0
) => {
    if (ignoreLoader === HeaderValue.True) {
        return new HttpHeaders().set(Headers.XIgnoreLoader, ignoreLoader);
    }
    return new HttpHeaders()
        .set(Headers.XLoaderSelector, btoa(selector))
        .set(Headers.XLoaderDebounceTime, debounceTime.toString());
};

export const getPaginationHeaders = (total: number, link: string) => {
    return new HttpHeaders()
        .set(Headers.Link, link)
        .set(Headers.XTotalCount, `${total}`);
};

export const getLocalTimeZone = (dateInput: Date | string): string => {
    const dateObject = dateInput || new Date(),
        dateString = dateObject + '';

    const timeZoneAbbr: RegExpMatchArray =
        dateString.match(/\(([A-Za-z\s].*)\)/) ||
        dateString.match(/([A-Z]{1,3}) \d{4}$/) ||
        dateString.match(/([A-Z]{1,3})-\d{4}$/);

    let ret = timeZoneAbbr?.join('');
    if (timeZoneAbbr) {
        ret = timeZoneAbbr[1].match(/[A-Z]/g).join('');
    }

    if (!timeZoneAbbr && /(GMT\W*\d{4})/.test(dateString)) {
        ret = RegExp.$1;
    }

    return ret;
};

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

export const isInputElement = el => /^(?:input|select|textarea)$/i.test(el.nodeName);