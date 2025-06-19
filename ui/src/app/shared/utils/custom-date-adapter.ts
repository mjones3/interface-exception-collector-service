import { LuxonDateAdapter } from '@angular/material-luxon-adapter';
import { DateTime } from 'luxon';

export class CustomLuxonAdapterModule extends LuxonDateAdapter {
    parse(value: any, parseFormat: string | string[]) {
        if (typeof value == 'string' && value.length > 0) {
            const formats = Array.isArray(parseFormat)
                ? parseFormat
                : [parseFormat];
            if (!parseFormat.length) {
                throw Error('Formats array must not be empty.');
            }
            for (const format of formats) {
                const fromFormat = DateTime.fromFormat(value, format);
                if (this.isValid(fromFormat)) {
                    return fromFormat;
                }
            }
            return this.invalid();
        } else if (typeof value === 'number') {
            return DateTime.fromMillis(value);
        } else if (value instanceof Date) {
            return DateTime.fromJSDate(value);
        } else if (value instanceof DateTime) {
            return DateTime.fromMillis(value.toMillis());
        }
        return null;
    }
}
