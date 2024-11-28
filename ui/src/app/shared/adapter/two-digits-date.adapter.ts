import { NativeDateAdapter } from '@angular/material/core';

export class TwoDigitsDateAdapter extends NativeDateAdapter {
    override parse(value: any): Date | null {
        if (!value) return null;
        const parts = value.split('/');
        if (parts.length === 3) {
            const month = +parts[0] - 1;
            const day = +parts[1];
            const year = +parts[2];
            return new Date(year, month, day);
        }
        return null;
    }

    override format(date: Date, _displayFormat): string {
        if (!date) return '';
        const month = (date.getMonth() + 1).toString().padStart(2, '0');
        const day = date.getDate().toString().padStart(2, '0');
        const year = date.getFullYear();
        return `${month}/${day}/${year}`;
    }
}
